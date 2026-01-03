#ifndef SENSORS_H
#define SENSORS_H

#include "structures.h"
#include <stdlib.h>

// Parse Arduino sensor line format
int parse_arduino_sensor_line(char* line, int* sensor_id, char* type, float* value, char* unit);

// Process multiple sensor readings from Arduino
int process_arduino_sensor_response(Manager* mgr, char* full_response);

// Process data received from Arduino sensor (compatibility)
int process_sensor_data(Manager* mgr, char* sensor_response);

// Get current sensor data (temperature and humidity)
void get_sensor_data(Manager* mgr, int* temperature, int* humidity);

#endif