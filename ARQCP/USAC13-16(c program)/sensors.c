#include <stdio.h>
#include <string.h>
#include <ctype.h>
#include "sensors.h"
#include "extract_data.h"
#include "queue_functions.h"
#include "sort_array.h"

// Parse a single sensor reading from Arduino format
// Format: sensor_id:X#type:Y#value:Z#unit:W#time:T
int parse_arduino_sensor_line(char* line, int* sensor_id, char* type, float* value, char* unit) {
    // Make sure this line contains sensor data
    if (strstr(line, "sensor_id:") == NULL) {
        return 0;
    }
    
    // Extract sensor ID number
    char* p = strstr(line, "sensor_id:");
    if (p && sscanf(p, "sensor_id:%d", sensor_id) == 1) {
        // Success
    } else {
        return 0;
    }
    
    // Extract sensor type (temperature, humidity, etc)
    p = strstr(line, "type:");
    if (p && sscanf(p, "type:%[^#]", type) == 1) {
        // Success
    } else {
        return 0;
    }
    
    // Extract the measured value
    p = strstr(line, "value:");
    if (p && sscanf(p, "value:%f", value) == 1) {
        // Success
    } else {
        return 0;
    }
    
    // Extract the unit of measurement
    p = strstr(line, "unit:");
    if (p && sscanf(p, "unit:%[^#]", unit) == 1) {
        // Success
    } else {
        return 0;
    }
    
    return 1;
}

// Process all sensor readings from Arduino response
// Arduino sends multiple sensors in one line separated by sensor_id: markers
int process_arduino_sensor_response(Manager* mgr, char* full_response) {
    int readings_processed = 0;
    float temp_sum = 0, hum_sum = 0;
    int temp_count = 0, hum_count = 0;
    
    printf("DEBUG: Processing sensor response (length: %lu bytes)\n", strlen(full_response));
    
    // Manually parse by finding each "sensor_id:" pattern
    // This works better than strtok because fields contain # characters
    char* current = full_response;
    
    while (*current != '\0') {
        // Find the start of next sensor data block
        char* sensor_start = strstr(current, "sensor_id:");
        if (!sensor_start) break;
        
        // Find where this sensor block ends (next sensor or end of string)
        char* next_sensor = strstr(sensor_start + 10, "sensor_id:");
        
        // Extract just this sensor's complete data block
        char sensor_block[256];
        int block_len;
        
        if (next_sensor) {
            block_len = next_sensor - sensor_start;
        } else {
            block_len = strlen(sensor_start);
        }
        
        if (block_len > 255) block_len = 255;
        strncpy(sensor_block, sensor_start, block_len);
        sensor_block[block_len] = '\0';
        
        printf("DEBUG: Sensor block: [%s]\n", sensor_block);
        
        // Parse this complete sensor block
        int sensor_id;
        char type[50];
        float value;
        char unit[50];
        
        if (parse_arduino_sensor_line(sensor_block, &sensor_id, type, &value, unit)) {
            printf("Parsed sensor: ID=%d, Type=%s, Value=%.2f, Unit=%s\n", 
                   sensor_id, type, value, unit);
            
            // Add to appropriate buffer based on sensor type
            if (strstr(type, "temperature") != NULL) {
                temp_sum += value;
                temp_count++;
                
                // Store in temperature circular buffer
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
                
                // Store in humidity circular buffer
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
        
        // Move to the next sensor block
        if (next_sensor) {
            current = next_sensor;
        } else {
            break;
        }
    }
    
    // Update current values based on what we got
    if (temp_count > 0) {
        mgr->sensor_data.current_temp = (int)(temp_sum / temp_count);
        printf("Temperature average: %.2f -> %d°C\n", temp_sum / temp_count, mgr->sensor_data.current_temp);
    }
    
    if (hum_count > 0) {
        mgr->sensor_data.current_hum = (int)(hum_sum / hum_count);
        printf("Humidity average: %.2f -> %d%%\n", hum_sum / hum_count, mgr->sensor_data.current_hum);
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
            printf("Temperature median applied: %d°C\n", mgr->sensor_data.current_temp);
        }
        
        free(temp_array);
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
            printf("Humidity median applied: %d%%\n", mgr->sensor_data.current_hum);
        }
        
        free(hum_array);
    }
    
    if (readings_processed > 0) {
        printf("\n✅ Sensor update complete: Temp=%d°C, Humidity=%d%%\n", 
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
