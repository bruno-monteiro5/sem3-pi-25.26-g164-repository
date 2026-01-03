#include <stdio.h>
#include <string.h>
#include <fcntl.h>
#include <unistd.h>
#include <termios.h>
#include "ui.h"
#include "board.h"
#include "format_command.h"

// Main menu
void show_main_menu() {
    printf("\n╔═══════════════════════════════════════╗\n");
    printf("║          MAIN MENU                    ║\n");
    printf("╠═══════════════════════════════════════╣\n");
    printf("║ 1) Communicate with Arduino           ║\n");
    printf("║ 2) Manage Tracks                      ║\n");
    printf("║ 3) Export User Logs                   ║\n");
    printf("║ 4) Exit                               ║\n");
    printf("╚═══════════════════════════════════════╝\n");
    printf("Choice: ");
}

// Arduino menu
void show_arduino_menu() {
    printf("\n╔═══════════════════════════════════════╗\n");
    printf("║       ARDUINO CONTROL                 ║\n");
    printf("╠═══════════════════════════════════════╣\n");
    printf("║ 1) Control LEDs                       ║\n");
    printf("║ 2) Read Sensors                       ║\n");
    printf("║ 3) Back                               ║\n");
    printf("╚═══════════════════════════════════════╝\n");
    printf("Choice: ");
}

// Track management menu
void show_track_menu() {
    printf("\n╔═══════════════════════════════════════╗\n");
    printf("║       TRACK MANAGEMENT                ║\n");
    printf("╠═══════════════════════════════════════╣\n");
    printf("║ 1) Assign Train to Track              ║\n");
    printf("║ 2) Free Track                         ║\n");
    printf("║ 3) Set Track Non-Operational          ║\n");
    printf("║ 4) Departure Order                    ║\n");
    printf("║ 5) Back                               ║\n");
    printf("╚═══════════════════════════════════════╝\n");
    printf("Choice: ");
}

// LED control
int handle_led_control(int serial_port, Manager* mgr) {
    char cmd[32], out[16];
    int track;
    
    printf("\nLED Commands: RE (Red), YE (Yellow), GE (Green), RB (Red Blink)\n");
    printf("Command (or 'cancel' to go back): ");
    scanf("%s", cmd);
    
    if (strcmp(cmd, "cancel") == 0) return 0;
    
    printf("Track number: ");
    if (scanf("%d", &track) != 1) {
        display_error("Invalid track number");
        return 0;
    }
    
    int result = format_command(cmd, track, out);
    while (result != 1) {
        display_error("Invalid command! Try again.");
        printf("Command (or 'cancel' to go back): ");
        scanf("%s", cmd);
        
        if (strcmp(cmd, "cancel") == 0) return 0;
        
        printf("Track number: ");
        if (scanf("%d", &track) != 1) {
            display_error("Invalid track number");
            continue;
        }
        
        result = format_command(cmd, track, out);
    }
    
    // Send command to Arduino
    write(serial_port, out, sizeof(out));
    
    // Read response
    char resp[256];
    int bytes_read = read(serial_port, resp, sizeof(resp));
    if (bytes_read > 0) {
        printf("Arduino response: %.*s\n", bytes_read, resp);
    }
    
    char log_msg[256];
    sprintf(log_msg, "LED control: %s on track %d", cmd, track);
    log_action(mgr, log_msg);
    
    display_success("LED command sent successfully");
    return 1;
}

// Sensor reading
int handle_sensor_read(int serial_port, Manager* mgr) {
    char cmd[] = "GTH";
    char out[16];
    
    int result = format_command(cmd, 0, out);
    if (result != 1) {
        display_error("Failed to format sensor command");
        return 0;
    }
    
    // Send command to Arduino
    write(serial_port, out, sizeof(out));
    
    // Arduino now sends all sensor data in ONE line
    char response[1024] = {0};
    
    // Wait for Arduino to process and send
    usleep(300000); // 300ms wait for sensor reading
    
    // Read the single line response
    int bytes_read = read(serial_port, response, sizeof(response) - 1);
    
    if (bytes_read > 0) {
        response[bytes_read] = '\0';
        printf("\n=== RAW SENSOR DATA ===\n%s\n======================\n", response);
        
        // Process the sensor data (now in single line with # separators)
        int readings = process_arduino_sensor_response(mgr, response);
        
        if (readings > 0) {
            log_action(mgr, "Sensor data read and processed");
            display_success("Sensor data updated");
            
            // Show current readings
            int temp, hum;
            get_sensor_data(mgr, &temp, &hum);
            printf("Current readings: Temperature=%d°C, Humidity=%d%%\n", temp, hum);
        } else {
            display_error("No valid sensor readings found");
            return 0;
        }
    } else {
        display_error("No response from sensors");
        return 0;
    }
    
    return 1;
}

// Open serial port (shared function)
int open_serial_port(Manager* mgr) {
    if (mgr->serial_port >= 0) {
        return mgr->serial_port; // Already open
    }
    
    int sp = open("/dev/ttyS0", O_RDWR);
    if (sp < 0) {
        display_error("Failed to open serial port /dev/ttyS0");
        return -1;
    }
    
    struct termios tty;
    memset(&tty, 0, sizeof(tty));
    if (tcgetattr(sp, &tty) != 0) {
        display_error("Failed to configure serial port");
        close(sp);
        return -1;
    }
    
    cfsetispeed(&tty, B9600);
    cfsetospeed(&tty, B9600);
    tcsetattr(sp, TCSANOW, &tty);
    
    mgr->serial_port = sp;
    return sp;
}

// Complete Arduino menu
void arduino_menu(Manager* mgr) {
    int serial_port = open_serial_port(mgr);
    if (serial_port < 0) {
        return;
    }
    
    char choice;
    do {
        display_board(mgr);
        show_arduino_menu();
        scanf(" %c", &choice);
        
        if (choice == '1') {
            handle_led_control(serial_port, mgr);
        } else if (choice == '2') {
            handle_sensor_read(serial_port, mgr);
        }
        
        if (choice != '3') {
            printf("\nPress Enter to continue...");
            getchar();
            getchar();
        }
        
    } while (choice != '3');
    
    // Don't close serial port - keep it open for track management
    // mgr->serial_port stays open
}

// Track management menu
void track_management_menu(Manager* mgr) {
    // Ensure serial port is open for LED commands
    if (mgr->serial_port < 0) {
        printf("Opening serial port for LED control...\n");
        if (open_serial_port(mgr) < 0) {
            printf("Warning: Serial port not available. LED commands will not work.\n");
            printf("Press Enter to continue anyway...");
            getchar();
        }
    }
    
    char choice;
    
    do {
        display_board(mgr);
        show_track_menu();
        scanf(" %c", &choice);
        
        if (choice == '1') {
            // Assign train
            int train_id, track_id;
            printf("Enter train ID: ");
            if (scanf("%d", &train_id) == 1) {
                if (assign_train_to_track(mgr, train_id, &track_id)) {
                    show_train_animation();
                    display_success("Train assigned successfully");
                    printf("Track %d LED set to RED (RE,%02d)\n", track_id, track_id);
                } else {
                    display_emergency_stop();
                }
            }
        } else if (choice == '2') {
            // Free track
            int track_id;
            printf("Enter track ID: ");
            if (scanf("%d", &track_id) == 1) {
                if (free_track(mgr, track_id)) {
                    display_success("Track freed successfully");
                    printf("Track %d LED set to GREEN (GE,%02d)\n", track_id, track_id);
                } else {
                    display_error("Failed to free track");
                }
            }
        } else if (choice == '3') {
            // Set as non-operational
            int track_id;
            printf("Enter track ID: ");
            if (scanf("%d", &track_id) == 1) {
                if (set_track_non_operational(mgr, track_id)) {
                    display_success("Track set as non-operational");
                    printf("Track %d LED set to YELLOW (YE,%02d)\n", track_id, track_id);
                } else {
                    display_error("Failed to set track status");
                }
            }
        } else if (choice == '4') {
            // Departure order
            int track_id;
            printf("Enter track ID: ");
            if (scanf("%d", &track_id) == 1) {
                if (departure_order(mgr, track_id)) {
                    show_train_animation();
                    display_success("Departure order given");
                    printf("Track %d LED blinking RED then GREEN (RB,%02d -> GE,%02d)\n", 
                           track_id, track_id, track_id);
                } else {
                    display_error("Failed to process departure");
                }
            }
        }
        
        if (choice != '5') {
            printf("\nPress Enter to continue...");
            getchar();
            getchar();
        }
        
    } while (choice != '5');
}

// Main UI loop
void ui_main_loop(Manager* mgr) {
    char choice;
    
    do {
        display_board(mgr);
        show_main_menu();
        scanf(" %c", &choice);
        
        if (choice == '1') {
            arduino_menu(mgr);
        } else if (choice == '2') {
            track_management_menu(mgr);
        } else if (choice == '3') {
            char filename[256];
            printf("Enter filename for log export: ");
            scanf("%s", filename);
            
            if (export_user_logs(mgr, mgr->current_user->username, filename)) {
                display_success("Logs exported successfully");
            } else {
                display_error("Failed to export logs");
            }
            
            printf("\nPress Enter to continue...");
            getchar();
            getchar();
        }
        
    } while (choice != '4');
}
