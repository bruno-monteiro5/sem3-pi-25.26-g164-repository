#ifndef MANAGER_H
#define MANAGER_H

#include "structures.h"
#include "format_command.h"
#include <unistd.h>

// Initialize Manager from configuration file
Manager* init_manager(char* config_file);

// Free Manager memory
void free_manager(Manager* mgr);

// User login
User* login_user(Manager* mgr, char* username, char* password);

// Log action
void log_action(Manager* mgr, char* action);

// Export user logs to file
int export_user_logs(Manager* mgr, char* username, char* filename);

// Send LED command to Arduino via serial
int send_led_command(Manager* mgr, int track_id, char* led_cmd);

// Assign train to track (returns 1 on success, 0 if no tracks available)
int assign_train_to_track(Manager* mgr, int train_id, int* assigned_track_id);

// Confirm train arrival on a reserved track (RESERVED -> OCCUPIED)
int confirm_train_arrival(Manager* mgr, int track_id);

// Free track
int free_track(Manager* mgr, int track_id);

// Set track as non-operational
int set_track_non_operational(Manager* mgr, int track_id);

// Give departure order
int departure_order(Manager* mgr, int track_id);

#endif