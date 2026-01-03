#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <time.h>
#include <unistd.h>
#include "manager.h"
#include "encrypt_data.h"
#include "decrypt_data.h"
#include "format_command.h"

// Initialize Manager from configuration file
Manager* init_manager(char* config_file) {
    Manager* mgr = (Manager*)malloc(sizeof(Manager));
    if (!mgr) return NULL;
    
    memset(mgr, 0, sizeof(Manager));
    
    FILE* fp = fopen(config_file, "r");
    if (!fp) {
        printf("Warning: Config file not found. Using defaults.\n");
        // Create default configuration
        mgr->num_users = 1;
        mgr->users = (User*)malloc(sizeof(User));
        strcpy(mgr->users[0].name, "Administrator");
        strcpy(mgr->users[0].username, "admin");
        strcpy(mgr->users[0].password, "VGYYCUXJKX");
        mgr->users[0].cipher_key = 6;
        
        mgr->num_tracks = 2;
        mgr->tracks = (Track*)malloc(sizeof(Track) * 2);
        for (int i = 0; i < 2; i++) {
            mgr->tracks[i].id = i + 1;
            mgr->tracks[i].state = TRACK_FREE;
            mgr->tracks[i].train = NULL;
        }
        
        // Sensor data defaults
        mgr->sensor_data.temp_buffer_length = 10;
        mgr->sensor_data.temp_window_length = 5;
        mgr->sensor_data.temp_buffer = (int*)calloc(10, sizeof(int));
        mgr->sensor_data.hum_buffer_length = 10;
        mgr->sensor_data.hum_window_length = 5;
        mgr->sensor_data.hum_buffer = (int*)calloc(10, sizeof(int));
        
        mgr->log_capacity = 100;
        mgr->logs = (LogEntry*)malloc(sizeof(LogEntry) * 100);
        
        mgr->serial_port = -1;
        
        return mgr;
    }
    
    // Read configuration file
    char line[256];
    while (fgets(line, sizeof(line), fp)) {
        if (strncmp(line, "USER:", 5) == 0) {
            mgr->num_users++;
            mgr->users = (User*)realloc(mgr->users, sizeof(User) * mgr->num_users);
            User* u = &mgr->users[mgr->num_users - 1];
            sscanf(line, "USER:%[^,],%[^,],%[^,],%d", u->name, u->username, u->password, &u->cipher_key);
        }
        else if (strncmp(line, "TRACK:", 6) == 0) {
            mgr->num_tracks++;
            mgr->tracks = (Track*)realloc(mgr->tracks, sizeof(Track) * mgr->num_tracks);
            Track* t = &mgr->tracks[mgr->num_tracks - 1];
            sscanf(line, "TRACK:%d", &t->id);
            t->state = TRACK_FREE;
            t->train = NULL;
        }
        else if (strncmp(line, "TEMP_BUFFER:", 12) == 0) {
            sscanf(line, "TEMP_BUFFER:%d", &mgr->sensor_data.temp_buffer_length);
            mgr->sensor_data.temp_buffer = (int*)calloc(mgr->sensor_data.temp_buffer_length, sizeof(int));
        }
        else if (strncmp(line, "TEMP_WINDOW:", 12) == 0) {
            sscanf(line, "TEMP_WINDOW:%d", &mgr->sensor_data.temp_window_length);
        }
        else if (strncmp(line, "HUM_BUFFER:", 11) == 0) {
            sscanf(line, "HUM_BUFFER:%d", &mgr->sensor_data.hum_buffer_length);
            mgr->sensor_data.hum_buffer = (int*)calloc(mgr->sensor_data.hum_buffer_length, sizeof(int));
        }
        else if (strncmp(line, "HUM_WINDOW:", 11) == 0) {
            sscanf(line, "HUM_WINDOW:%d", &mgr->sensor_data.hum_window_length);
        }
    }
    
    fclose(fp);
    
    mgr->log_capacity = 100;
    mgr->logs = (LogEntry*)malloc(sizeof(LogEntry) * 100);
    mgr->serial_port = -1;
    
    return mgr;
}

// Free Manager memory
void free_manager(Manager* mgr) {
    if (!mgr) return;
    
    if (mgr->users) free(mgr->users);
    if (mgr->tracks) free(mgr->tracks);
    if (mgr->trains) free(mgr->trains);
    if (mgr->logs) free(mgr->logs);
    if (mgr->sensor_data.temp_buffer) free(mgr->sensor_data.temp_buffer);
    if (mgr->sensor_data.hum_buffer) free(mgr->sensor_data.hum_buffer);
    
    free(mgr);
}

// User login
User* login_user(Manager* mgr, char* username, char* password) {
    char encrypted[64];
    
    for (int i = 0; i < mgr->num_users; i++) {
        if (strcmp(mgr->users[i].username, username) == 0) {
            if (encrypt_data(password, mgr->users[i].cipher_key, encrypted) == 1) {
                if (strcmp(encrypted, mgr->users[i].password) == 0) {
                    mgr->current_user = &mgr->users[i];
                    return &mgr->users[i];
                }
            }
        }
    }
    
    return NULL;
}

// Log action
void log_action(Manager* mgr, char* action) {
    if (!mgr->current_user) return;
    
    if (mgr->num_logs >= mgr->log_capacity) {
        mgr->log_capacity *= 2;
        mgr->logs = (LogEntry*)realloc(mgr->logs, sizeof(LogEntry) * mgr->log_capacity);
    }
    
    LogEntry* log = &mgr->logs[mgr->num_logs];
    log->id = mgr->num_logs + 1;
    strcpy(log->username, mgr->current_user->username);
    strncpy(log->action, action, 255);
    log->action[255] = '\0';
    log->timestamp = time(NULL);
    
    mgr->num_logs++;
}

// Export user logs to file
int export_user_logs(Manager* mgr, char* username, char* filename) {
    FILE* fp = fopen(filename, "w");
    if (!fp) return 0;
    
    fprintf(fp, "Log Actions for User: %s\n", username);
    fprintf(fp, "=====================================\n\n");
    
    for (int i = 0; i < mgr->num_logs; i++) {
        if (strcmp(mgr->logs[i].username, username) == 0) {
            char time_str[64];
            struct tm* tm_info = localtime(&mgr->logs[i].timestamp);
            strftime(time_str, 64, "%Y-%m-%d %H:%M:%S", tm_info);
            
            fprintf(fp, "[%s] Log #%d: %s\n", time_str, mgr->logs[i].id, mgr->logs[i].action);
        }
    }
    
    fclose(fp);
    return 1;
}

// Send LED command to Arduino via serial port
int send_led_command(Manager* mgr, int track_id, char* led_cmd) {
    if (mgr->serial_port < 0) {
        printf("Warning: Serial port not open, cannot send LED command\n");
        return 0;
    }
    
    char out[16];
    int r = format_command(led_cmd, track_id, out);
    
    if (r != 1) {
        printf("Error: Failed to format LED command\n");
        return 0;
    }
    
    // Send the LED command - Arduino handles turning off old LED
    write(mgr->serial_port, out, sizeof(out));
    usleep(100000); // 100ms delay for Arduino to process
    
    char resp[256];
    int re = read(mgr->serial_port, resp, sizeof(resp));
    if (re > 0) {
        printf("Received message: %.*s\n", re, resp);
    }
    
    return 1;
}

// Assign train to track
int assign_train_to_track(Manager* mgr, int train_id, int* assigned_track_id) {
    for (int i = 0; i < mgr->num_tracks; i++) {
        if (mgr->tracks[i].state == TRACK_FREE) {
            mgr->num_trains++;
            mgr->trains = (Train*)realloc(mgr->trains, sizeof(Train) * mgr->num_trains);
            mgr->trains[mgr->num_trains - 1].id = train_id;
            
            mgr->tracks[i].train = &mgr->trains[mgr->num_trains - 1];
            mgr->tracks[i].state = TRACK_OCCUPIED;
            *assigned_track_id = mgr->tracks[i].id;
            
            char log_msg[256];
            sprintf(log_msg, "Assigned train %d to track %d", train_id, mgr->tracks[i].id);
            log_action(mgr, log_msg);
            
            send_led_command(mgr, mgr->tracks[i].id, "RE");
            
            return 1;
        }
    }
    
    log_action(mgr, "EMERGENCY STOP - No available tracks");
    return 0;
}

// Free track
int free_track(Manager* mgr, int track_id) {
    for (int i = 0; i < mgr->num_tracks; i++) {
        if (mgr->tracks[i].id == track_id) {
            mgr->tracks[i].train = NULL;
            mgr->tracks[i].state = TRACK_FREE;
            
            char log_msg[256];
            sprintf(log_msg, "Freed track %d", track_id);
            log_action(mgr, log_msg);
            
            send_led_command(mgr, track_id, "GE");
            
            return 1;
        }
    }
    return 0;
}

// Set track as non-operational
int set_track_non_operational(Manager* mgr, int track_id) {
    for (int i = 0; i < mgr->num_tracks; i++) {
        if (mgr->tracks[i].id == track_id) {
            mgr->tracks[i].state = TRACK_NON_OPERATIONAL;
            
            char log_msg[256];
            sprintf(log_msg, "Set track %d as non-operational", track_id);
            log_action(mgr, log_msg);
            
            send_led_command(mgr, track_id, "YE");
            
            return 1;
        }
    }
    return 0;
}

// Give departure order
int departure_order(Manager* mgr, int track_id) {
    for (int i = 0; i < mgr->num_tracks; i++) {
        if (mgr->tracks[i].id == track_id && mgr->tracks[i].state == TRACK_OCCUPIED) {
            char log_msg[256];
            sprintf(log_msg, "Departure order for train on track %d", track_id);
            log_action(mgr, log_msg);
            
            send_led_command(mgr, track_id, "RB");
            
            sleep(2);
            
            return free_track(mgr, track_id);
        }
    }
    return 0;
}