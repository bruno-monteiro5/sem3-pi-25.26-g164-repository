#ifndef STRUCTURES_H
#define STRUCTURES_H

#include <time.h>

// User roles (Sprint 3)
typedef enum {
    ROLE_ADMIN,
    ROLE_OPERATOR
} UserRole;

// User structure
typedef struct {
    char name[64];
    char username[32];
    char password[64];  // Encrypted
    int cipher_key;
    UserRole role;
} User;

// Train structure
typedef struct {
    int id;
} Train;

// Possible track states
typedef enum {
    TRACK_FREE,
    TRACK_RESERVED,
    TRACK_OCCUPIED,
    TRACK_NON_OPERATIONAL
} TrackState;

// Track structure
typedef struct {
    int id;
    TrackState state;
    Train* train;  // NULL if no train
} Track;

// Sensor data structure
typedef struct {
    int temp_buffer_length;
    int temp_window_length;
    int* temp_buffer;
    int temp_nelem;
    int temp_tail;
    int temp_head;
    
    int hum_buffer_length;
    int hum_window_length;
    int* hum_buffer;
    int hum_nelem;
    int hum_tail;
    int hum_head;
    
    int current_temp;
    int current_hum;
} SensorData;

// Log entry structure
typedef struct {
    int id;
    char username[32];
    char action[256];
    time_t timestamp;
} LogEntry;

// Main Manager structure
typedef struct {
    User* users;
    int num_users;
    
    Track* tracks;
    int num_tracks;
    
    Train* trains;
    int num_trains;
    
    LogEntry* logs;
    int num_logs;
    int log_capacity;
    
    SensorData sensor_data;
    
    User* current_user;  // Authenticated user
    int serial_port;     // Arduino serial port file descriptor
} Manager;

#endif