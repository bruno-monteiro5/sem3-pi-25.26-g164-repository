#ifndef MANAGER_H
#define MANAGER_H

#include "structures.h"
#include "format_command.h"
#include <unistd.h>

Manager* init_manager(char* config_file);
void free_manager(Manager* mgr);
User* login_user(Manager* mgr, char* username, char* password);
void log_action(Manager* mgr, char* action);
int export_user_logs(Manager* mgr, char* username, char* filename);
int send_led_command(Manager* mgr, int track_id, char* led_cmd);
int assign_train_to_track(Manager* mgr, int train_id, int* assigned_track_id);
int confirm_train_arrival(Manager* mgr, int track_id);
int free_track(Manager* mgr, int track_id);
int set_track_non_operational(Manager* mgr, int track_id);
int repair_track(Manager* mgr, int track_id);
int departure_order(Manager* mgr, int track_id);

#endif