#include <stdio.h>
#include <string.h>
#include <fcntl.h>
#include <unistd.h>
#include <termios.h>
#include "ui.h"
#include "board.h"
#include "format_command.h"

// Display the main menu options
void show_main_menu() {
    printf("\n+========================================+\n");
    printf("|             MAIN MENU                  |\n");
    printf("+========================================+\n");
    printf("| 1) Communicate with Arduino            |\n");
    printf("| 2) Manage Tracks                       |\n");
    printf("| 3) Export User Logs                    |\n");
    printf("| 4) Exit                                |\n");
    printf("+========================================+\n");
    printf("Choice: ");
}

// Display Arduino control menu
void show_arduino_menu() {
    printf("\n+========================================+\n");
    printf("|          ARDUINO CONTROL               |\n");
    printf("+========================================+\n");
    printf("| 1) Control LEDs                        |\n");
    printf("| 2) Read Sensors                        |\n");
    printf("| 3) Back                                |\n");
    printf("+========================================+\n");
    printf("Choice: ");
}

// Display track management menu
void show_track_menu() {
    printf("\n+========================================+\n");
    printf("|         TRACK MANAGEMENT               |\n");
    printf("+========================================+\n");
    printf("| 1) Assign Train to Track               |\n");
    printf("| 2) Free Track                          |\n");
    printf("| 3) Set Track Non-Operational           |\n");
    printf("| 4) Departure Order                     |\n");
    printf("| 5) Back                                |\n");
    printf("+========================================+\n");
    printf("Choice: ");
}

// Open the serial port for Arduino communication
// Returns file descriptor or -1 on error
int open_serial_port(Manager* mgr) {
    if (mgr->serial_port >= 0) {
        return mgr->serial_port; // Already open, reuse it
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
    
    // Set baud rate to 9600
    cfsetispeed(&tty, B9600);
    cfsetospeed(&tty, B9600);
    tcsetattr(sp, TCSANOW, &tty);
    
    mgr->serial_port = sp;
    return sp;
}

// Handle manual LED control from user input
int handle_led_control(int serial_port, Manager* mgr) {
    char cmd[32], out[16];
    int track;
    
    printf("\nAvailable LED Commands:\n");
    printf("  RE = Red LED\n");
    printf("  YE = Yellow LED\n");
    printf("  GE = Green LED\n");
    printf("  RB = Red LED Blink\n\n");
    printf("Command (or 'cancel' to go back): ");
    scanf("%s", cmd);
    
    if (strcmp(cmd, "cancel") == 0) return 0;
    
    printf("Track number: ");
    if (scanf("%d", &track) != 1) {
        display_error("Invalid track number");
        return 0;
    }
    
    // Validate and format the command
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
    
    // Send the formatted command to Arduino
    write(serial_port, out, sizeof(out));
    
    // Wait for and display Arduino's response
    char resp[256];
    int bytes_read = read(serial_port, resp, sizeof(resp));
    if (bytes_read > 0) {
        printf("Arduino response: %.*s\n", bytes_read, resp);
    }
    
    // Log this action
    char log_msg[256];
    sprintf(log_msg, "Manual LED control: %s on track %d", cmd, track);
    log_action(mgr, log_msg);
    
    display_success("LED command sent successfully");
    return 1;
}

// Request and process sensor data from Arduino
int handle_sensor_read(int serial_port, Manager* mgr) {
    char cmd[] = "GTH";
    char out[16];
    
    // Format the sensor read command
    int result = format_command(cmd, 0, out);
    if (result != 1) {
        display_error("Failed to format sensor command");
        return 0;
    }
    
    // Send command to Arduino
    write(serial_port, out, sizeof(out));
    
    // Arduino sends all sensor data in one line, wait for it
    char response[1024] = {0};
    
    usleep(300000); // Wait 300ms for Arduino to read sensors
    
    // Read the response
    int bytes_read = read(serial_port, response, sizeof(response) - 1);
    
    if (bytes_read > 0) {
        response[bytes_read] = '\0';
        printf("\n=== RAW SENSOR DATA ===\n%s\n======================\n", response);
        
        // Process and update sensor data in manager
        int readings = process_arduino_sensor_response(mgr, response);
        
        if (readings > 0) {
            log_action(mgr, "Sensor data read and processed");
            display_success("Sensor data updated");
            
            // Display the current sensor readings
            int temp, hum;
            get_sensor_data(mgr, &temp, &hum);
            printf("Current readings: Temperature=%d C, Humidity=%d%%\n", temp, hum);
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

// Main Arduino communication menu
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
    
    // Keep serial port open for track management to use
}

// Track management operations menu
void track_management_menu(Manager* mgr) {
    // Make sure serial port is open for LED commands
    if (mgr->serial_port < 0) {
        printf("Opening serial port for LED control...\n");
        if (open_serial_port(mgr) < 0) {
            printf("Warning: Serial port unavailable. LED commands will not work.\n");
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
            // Assign a train to an available track
            int train_id, track_id;
            printf("Enter train ID: ");
            if (scanf("%d", &train_id) == 1) {
                if (assign_train_to_track(mgr, train_id, &track_id)) {
                    show_train_animation();
                    display_success("Train assigned successfully");
                    printf("Track %d LED is now RED (train occupying)\n", track_id);
                } else {
                    display_emergency_stop();
                }
            }
        } else if (choice == '2') {
            // Free up an occupied track
            int track_id;
            printf("Enter track ID: ");
            if (scanf("%d", &track_id) == 1) {
                if (free_track(mgr, track_id)) {
                    display_success("Track freed successfully");
                    printf("Track %d LED is now GREEN (available)\n", track_id);
                } else {
                    display_error("Failed to free track");
                }
            }
        } else if (choice == '3') {
            // Mark track as under maintenance
            int track_id;
            printf("Enter track ID: ");
            if (scanf("%d", &track_id) == 1) {
                if (set_track_non_operational(mgr, track_id)) {
                    display_success("Track set as non-operational");
                    printf("Track %d LED is now YELLOW (maintenance)\n", track_id);
                } else {
                    display_error("Failed to set track status");
                }
            }
        } else if (choice == '4') {
            // Give departure order to a train
            int track_id;
            printf("Enter track ID: ");
            if (scanf("%d", &track_id) == 1) {
                if (departure_order(mgr, track_id)) {
                    show_train_animation();
                    display_success("Departure order given");
                    printf("Track %d LED blinked RED then turned GREEN\n", track_id);
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

// Main UI loop - runs until user exits
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
            // Export logs for current user
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