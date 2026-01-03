#include <stdio.h>
#include <unistd.h>
#include "board.h"

// Clear the terminal screen
void clear_screen() {
    printf("\033[2J\033[H");
}

// Display the main system header with ASCII art
void show_header() {
    printf("   ____   _____    _    _____ ___ ___  _   _ \n");
    printf("  / ___| |_   _|  / \\  |_   _|_ _/ _ \\| \\ | |\n");
    printf(" | |       | |   / _ \\   | |  | | | | |  \\| |\n");
    printf(" | |___    | |  / ___ \\  | |  | | |_| | |\\  |\n");
    printf("  \\____|   |_| /_/   \\_\\ |_| |___\\___/|_| \\_|\n");
    printf("                                              \n");
    printf("        -= Railway Management System =-       \n");
    printf("======================================================\n");
}

// Show the current status of all tracks
void display_tracks(Manager* mgr) {
    printf("\n+------------------------------------------------------+\n");
    printf("|                   TRACK STATUS                       |\n");
    printf("+------------------------------------------------------+\n");
    
    for (int i = 0; i < mgr->num_tracks; i++) {
        Track* t = &mgr->tracks[i];
        printf("| Track %d: ", t->id);
        
        switch (t->state) {
            case TRACK_FREE:
                printf("[FREE]        ( Ready for trains )         |\n");
                break;
            case TRACK_OCCUPIED:
                printf("[OCCUPIED]    < Train #%d in station >     |\n", 
                       t->train ? t->train->id : 0);
                break;
            case TRACK_NON_OPERATIONAL:
                printf("[MAINTENANCE] ! Under repair !             |\n");
                break;
        }
    }
    
    printf("+------------------------------------------------------+\n");
}

// Display current sensor readings
void display_sensor_data(Manager* mgr) {
    int temp, hum;
    get_sensor_data(mgr, &temp, &hum);
    
    printf("\n+------------------------------------------------------+\n");
    printf("|                 SENSOR READINGS                      |\n");
    printf("+------------------------------------------------------+\n");
    printf("|  Temperature: %d degrees Celsius                     |\n", temp);
    printf("|  Humidity:    %d percent                             |\n", hum);
    printf("+------------------------------------------------------+\n");
}

// Show animated ASCII train moving across the screen
void show_train_animation() {
    const char* frames[] = {
        "                 ____                    \n"
        "              __|____|__                 \n"
        "              ( O   O )                  \n"
        "             ============                \n",
        
        "                      ____               \n"
        "                   __|____|__            \n"
        "                   ( O   O )             \n"
        "                  ============           \n",
        
        "                           ____          \n"
        "                        __|____|__       \n"
        "                        ( O   O )        \n"
        "                       ============      \n"
    };
    
    for (int i = 0; i < 3; i++) {
        clear_screen();
        printf("\n\n    TRAIN DEPARTING...\n\n");
        printf("%s\n", frames[i]);
        usleep(400000); // Wait 400ms between frames
    }
}

// Display the complete information board
void display_board(Manager* mgr) {
    clear_screen();
    show_header();
    display_tracks(mgr);
    display_sensor_data(mgr);
    
    printf("\n");
    printf("======================================================\n");
    printf(" Logged in as: %s\n", mgr->current_user ? mgr->current_user->name : "Unknown");
    printf("======================================================\n");
}

// Show emergency alert message
void display_emergency_stop() {
    printf("\n");
    printf("+======================================================+\n");
    printf("|  !!!  !!!  !!!   EMERGENCY STOP   !!!  !!!  !!!    |\n");
    printf("|                                                      |\n");
    printf("|       No available tracks! All trains halted!       |\n");
    printf("+======================================================+\n");
}

// Display success message with decorative border
void display_success(char* message) {
    printf("\n[SUCCESS] %s\n", message);
}

// Display error message with warning indicator
void display_error(char* message) {
    printf("\n[ERROR] %s\n", message);
}