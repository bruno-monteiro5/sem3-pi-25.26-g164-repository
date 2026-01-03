#include <stdio.h>
#include <string.h>
#include <ctype.h>
#include "sensors.h"
#include "extract_data.h"
#include "queue_functions.h"
#include "sort_array.h"

// Parse Arduino sensor line format: sensor_id:X#type:Y#value:Z#unit:W#time:T
// Now handles single-line format with multiple sensors separated by #
int parse_arduino_sensor_line(char* line, int* sensor_id, char* type, float* value, char* unit) {
    // Check if line contains sensor data
    if (strstr(line, "sensor_id:") == NULL) {
        return 0; // Not a sensor data line
    }
    
    // Parse sensor_id
    char* p = strstr(line, "sensor_id:");
    if (p) {
        sscanf(p, "sensor_id:%d", sensor_id);
    } else return 0;
    
    // Parse type
    p = strstr(line, "type:");
    if (p) {
        sscanf(p, "type:%[^#]", type);
    } else return 0;
    
    // Parse value
    p = strstr(line, "value:");
    if (p) {
        sscanf(p, "value:%f", value);
    } else return 0;
    
    // Parse unit
    p = strstr(line, "unit:");
    if (p) {
        sscanf(p, "unit:%[^#]", unit);
    } else return 0;
    
    return 1;
}

// Process multiple sensor readings from Arduino (single line with # separators)
int process_arduino_sensor_response(Manager* mgr, char* full_response) {
    int readings_processed = 0;
    float temp_sum = 0, hum_sum = 0;
    int temp_count = 0, hum_count = 0;
    
    // Split by # to get individual sensor readings
    char* token = strtok(full_response, "#");
    
    while (token != NULL) {
        int sensor_id;
        char type[50];
        float value;
        char unit[50];
        
        if (parse_arduino_sensor_line(token, &sensor_id, type, &value, unit)) {
            printf("Parsed: ID=%d, Type=%s, Value=%.2f, Unit=%s\n", 
                   sensor_id, type, value, unit);
            
            // Categorize by type
            if (strstr(type, "temperature") != NULL) {
                temp_sum += value;
                temp_count++;
                
                // Add to temperature buffer
                int temp_int = (int)(value * 10);
                enqueue_value(mgr->sensor_data.temp_buffer,
                             mgr->sensor_data.temp_buffer_length,
                             &mgr->sensor_data.temp_nelem,
                             &mgr->sensor_data.temp_tail,
                             &mgr->sensor_data.temp_head,
                             temp_int);
                
                readings_processed++;
            }
            else if (strstr(type, "humidity") != NULL) {
                hum_sum += value;
                hum_count++;
                
                // Add to humidity buffer
                int hum_int = (int)(value * 10);
                enqueue_value(mgr->sensor_data.hum_buffer,
                             mgr->sensor_data.hum_buffer_length,
                             &mgr->sensor_data.hum_nelem,
                             &mgr->sensor_data.hum_tail,
                             &mgr->sensor_data.hum_head,
                             hum_int);
                
                readings_processed++;
            }
        }
        
        token = strtok(NULL, "#");
    }
    
    // Calculate median for temperature if we have enough data
    if (mgr->sensor_data.temp_nelem >= mgr->sensor_data.temp_window_length) {
        int* temp_array = (int*)malloc(sizeof(int) * mgr->sensor_data.temp_window_length);
        
        move_n_to_array(mgr->sensor_data.temp_buffer,
                       mgr->sensor_data.temp_buffer_length,
                       &mgr->sensor_data.temp_nelem,
                       &mgr->sensor_data.temp_tail,
                       &mgr->sensor_data.temp_head,
                       mgr->sensor_data.temp_window_length,
                       temp_array);
        
        for (int i = 0; i < mgr->sensor_data.temp_window_length; i++) {
            enqueue_value(mgr->sensor_data.temp_buffer,
                         mgr->sensor_data.temp_buffer_length,
                         &mgr->sensor_data.temp_nelem,
                         &mgr->sensor_data.temp_tail,
                         &mgr->sensor_data.temp_head,
                         temp_array[i]);
        }
        
        int temp_median;
        if (median(temp_array, mgr->sensor_data.temp_window_length, &temp_median) == 1) {
            mgr->sensor_data.current_temp = temp_median / 10;
        }
        
        free(temp_array);
    } else if (temp_count > 0) {
        mgr->sensor_data.current_temp = (int)(temp_sum / temp_count);
    }
    
    // Calculate median for humidity if we have enough data
    if (mgr->sensor_data.hum_nelem >= mgr->sensor_data.hum_window_length) {
        int* hum_array = (int*)malloc(sizeof(int) * mgr->sensor_data.hum_window_length);
        
        move_n_to_array(mgr->sensor_data.hum_buffer,
                       mgr->sensor_data.hum_buffer_length,
                       &mgr->sensor_data.hum_nelem,
                       &mgr->sensor_data.hum_tail,
                       &mgr->sensor_data.hum_head,
                       mgr->sensor_data.hum_window_length,
                       hum_array);
        
        for (int i = 0; i < mgr->sensor_data.hum_window_length; i++) {
            enqueue_value(mgr->sensor_data.hum_buffer,
                         mgr->sensor_data.hum_buffer_length,
                         &mgr->sensor_data.hum_nelem,
                         &mgr->sensor_data.hum_tail,
                         &mgr->sensor_data.hum_head,
                         hum_array[i]);
        }
        
        int hum_median;
        if (median(hum_array, mgr->sensor_data.hum_window_length, &hum_median) == 1) {
            mgr->sensor_data.current_hum = hum_median / 10;
        }
        
        free(hum_array);
    } else if (hum_count > 0) {
        mgr->sensor_data.current_hum = (int)(hum_sum / hum_count);
    }
    
    if (readings_processed > 0) {
        printf("\nSensor update: Temp=%d°C, Humidity=%d%%\n", 
               mgr->sensor_data.current_temp, mgr->sensor_data.current_hum);
    }
    
    return readings_processed;
}

// Process sensor data (old format compatibility)
int process_sensor_data(Manager* mgr, char* sensor_response) {
    return process_arduino_sensor_response(mgr, sensor_response);
}

// Get current sensor data
void get_sensor_data(Manager* mgr, int* temperature, int* humidity) {
    *temperature = mgr->sensor_data.current_temp;
    *humidity = mgr->sensor_data.current_hum;
}
