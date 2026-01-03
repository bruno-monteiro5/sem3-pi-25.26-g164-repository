#ifndef BOARD_H
#define BOARD_H

#include "structures.h"
#include "sensors.h"

// Clear screen
void clear_screen();

// Show system header
void show_header();

// Display track status
void display_tracks(Manager* mgr);

// Display sensor data
void display_sensor_data(Manager* mgr);

// Show train animation
void show_train_animation();

// Display complete panel (synoptic board)
void display_board(Manager* mgr);

// Display emergency message
void display_emergency_stop();

// Display success message
void display_success(const char* message);

// Display error message
void display_error(const char* message);

#endif