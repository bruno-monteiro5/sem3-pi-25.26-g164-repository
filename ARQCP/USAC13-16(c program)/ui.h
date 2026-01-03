#ifndef UI_H
#define UI_H

#include "structures.h"
#include "manager.h"
#include "sensors.h"

// Show main menu
void show_main_menu();

// Show Arduino menu
void show_arduino_menu();

// Show track management menu
void show_track_menu();

// Control LEDs via Arduino
int handle_led_control(int serial_port, Manager* mgr);

// Read sensors via Arduino
int handle_sensor_read(int serial_port, Manager* mgr);

// Complete Arduino menu
void arduino_menu(Manager* mgr);

// Track management menu
void track_management_menu(Manager* mgr);

// Main UI loop
void ui_main_loop(Manager* mgr);

#endif