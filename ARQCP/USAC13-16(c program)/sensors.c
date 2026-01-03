#include <stdio.h>
#include <string.h>
#include "sensors.h"
#include "extract_data.h"
#include "queue_functions.h"
#include "sorting.h"

// Process sensor data and calculate median using Moving Median
int process_sensor_data(Manager* mgr, const char* sensor_response) {
    char unit[32];
    int value;
    char temp_token[] = "TEMP";
    char hum_token[] = "HUM";
    
    // Extract temperature
    if (extract_data((char*)sensor_response, temp_token, unit, &value) == 1) {
        // Add to temperature circular buffer
        enqueue_value(mgr->sensor_data.temp_buffer, 
                     mgr->sensor_data.temp_buffer_length,
                     &mgr->sensor_data.temp_nelem,
                     &mgr->sensor_data.temp_tail,
                     &mgr->sensor_data.temp_head,
                     value);
        
        // Calculate median if we have enough data
        if (mgr->sensor_data.temp_nelem >= mgr->sensor_data.temp_window_length) {
            int* temp_array = (int*)malloc(sizeof(int) * mgr->sensor_data.temp_window_length);
            
            // Move last N values to temporary array
            move_n_to_array(mgr->sensor_data.temp_buffer,
                           mgr->sensor_data.temp_buffer_length,
                           &mgr->sensor_data.temp_nelem,
                           &mgr->sensor_data.temp_tail,
                           &mgr->sensor_data.temp_head,
                           mgr->sensor_data.temp_window_length,
                           temp_array);
            
            // Re-add values to buffer (don't want to lose them)
            for (int i = 0; i < mgr->sensor_data.temp_window_length; i++) {
                enqueue_value(mgr->sensor_data.temp_buffer,
                             mgr->sensor_data.temp_buffer_length,
                             &mgr->sensor_data.temp_nelem,
                             &mgr->sensor_data.temp_tail,
                             &mgr->sensor_data.temp_head,
                             temp_array[i]);
            }
            
            // Calculate median
            int temp_median;
            if (median(temp_array, mgr->sensor_data.temp_window_length, &temp_median) == 1) {
                mgr->sensor_data.current_temp = temp_median;
            }
            
            free(temp_array);
        }
    }
    
    // Extract humidity
    if (extract_data((char*)sensor_response, hum_token, unit, &value) == 1) {
        // Add to humidity circular buffer
        enqueue_value(mgr->sensor_data.hum_buffer,
                     mgr->sensor_data.hum_buffer_length,
                     &mgr->sensor_data.hum_nelem,
                     &mgr->sensor_data.hum_tail,
                     &mgr->sensor_data.hum_head,
                     value);
        
        // Calculate median if we have enough data
        if (mgr->sensor_data.hum_nelem >= mgr->sensor_data.hum_window_length) {
            int* hum_array = (int*)malloc(sizeof(int) * mgr->sensor_data.hum_window_length);
            
            // Move last N values to temporary array
            move_n_to_array(mgr->sensor_data.hum_buffer,
                           mgr->sensor_data.hum_buffer_length,
                           &mgr->sensor_data.hum_nelem,
                           &mgr->sensor_data.hum_tail,
                           &mgr->sensor_data.hum_head,
                           mgr->sensor_data.hum_window_length,
                           hum_array);
            
            // Re-add values to buffer
            for (int i = 0; i < mgr->sensor_data.hum_window_length; i++) {
                enqueue_value(mgr->sensor_data.hum_buffer,
                             mgr->sensor_data.hum_buffer_length,
                             &mgr->sensor_data.hum_nelem,
                             &mgr->sensor_data.hum_tail,
                             &mgr->sensor_data.hum_head,
                             hum_array[i]);
            }
            
            // Calculate median
            int hum_median;
            if (median(hum_array, mgr->sensor_data.hum_window_length, &hum_median) == 1) {
                mgr->sensor_data.current_hum = hum_median;
            }
            
            free(hum_array);
        }
    }
    
    return 1;
}

// Get current sensor data
void get_sensor_data(Manager* mgr, int* temperature, int* humidity) {
    *temperature = mgr->sensor_data.current_temp;
    *humidity = mgr->sensor_data.current_hum;
}