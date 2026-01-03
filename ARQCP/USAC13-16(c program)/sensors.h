#ifndef SENSORS_H
#define SENSORS_H

#include "structures.h"
#include <stdlib.h>

// Process data received from Arduino sensor
int process_sensor_data(Manager* mgr, const char* sensor_response);

// Get current sensor data (temperature and humidity)
void get_sensor_data(Manager* mgr, int* temperature, int* humidity);

#endif