#include <stdio.h>
#include <unistd.h>
#include "board.h"

// Clear screen
void clear_screen() {
    printf("\033[2J\033[H");
}

// Show system header
void show_header() {
    printf("╔═══════════════════════════════════════════════════════════════╗\n");
    printf("║            🚂 STATION MANAGEMENT SYSTEM 🚂                    ║\n");
    printf("╚═══════════════════════════════════════════════════════════════╝\n");
}

// Display track status
void display_tracks(Manager* mgr) {
    printf("\n┌─────────────────────────────────────────────────────────────┐\n");
    printf("│                      TRACK STATUS                           │\n");
    printf("├─────────────────────────────────────────────────────────────┤\n");
    
    for (int i = 0; i < mgr->num_tracks; i++) {
        Track* t = &mgr->tracks[i];
        printf("│ Track %d:  ", t->id);
        
        switch (t->state) {
            case TRACK_FREE:
                printf("🟢 FREE                                         │\n");
                break;
            case TRACK_OCCUPIED:
                printf("🔴 OCCUPIED (Train #%d)                        │\n", 
                       t->train ? t->train->id : 0);
                break;
            case TRACK_NON_OPERATIONAL:
                printf("⚠️  NON-OPERATIONAL                              │\n");
                break;
        }
    }
    
    printf("└─────────────────────────────────────────────────────────────┘\n");
}

// Display sensor data
void display_sensor_data(Manager* mgr) {
    int temp, hum;
    get_sensor_data(mgr, &temp, &hum);
    
    printf("\n┌─────────────────────────────────────────────────────────────┐\n");
    printf("│                    SENSOR READINGS                          │\n");
    printf("├─────────────────────────────────────────────────────────────┤\n");
    printf("│ 🌡️  Temperature: %d°C                                         │\n", temp);
    printf("│ 💧 Humidity: %d%%                                             │\n", hum);
    printf("└─────────────────────────────────────────────────────────────┘\n");
}

// Show animated train in ASCII art
void show_train_animation() {
    const char* frames[] = {
        "     ____\n"
        "  __|____|__\n"
        "  ( O   O )\n"
        " ============",
        
        "      ____\n"
        "   __|____|__\n"
        "   ( O   O )\n"
        "  ============",
        
        "       ____\n"
        "    __|____|__\n"
        "    ( O   O )\n"
        "   ============"
    };
    
    for (int i = 0; i < 3; i++) {
        clear_screen();
        printf("\n\n%s\n\n", frames[i % 3]);
        usleep(200000);
    }
}

// Display complete panel (synoptic board)
void display_board(Manager* mgr) {
    clear_screen();
    show_header();
    display_tracks(mgr);
    display_sensor_data(mgr);
    
    printf("\n");
    printf("═══════════════════════════════════════════════════════════════\n");
    printf(" User: %s\n", mgr->current_user ? mgr->current_user->name : "Unknown");
    printf("═══════════════════════════════════════════════════════════════\n");
}

// Display emergency message
void display_emergency_stop() {
    printf("\n");
    printf("╔═══════════════════════════════════════════════════════════════╗\n");
    printf("║  ⚠️  ⚠️  ⚠️    EMERGENCY STOP    ⚠️  ⚠️  ⚠️                         ║\n");
    printf("║                                                               ║\n");
    printf("║       No available tracks! All trains must stop!              ║\n");
    printf("╚═══════════════════════════════════════════════════════════════╝\n");
}

// Display success message
void display_success(char* message) {
    printf("\n✅ %s\n", message);
}

// Display error message
void display_error(char* message) {
    printf("\n❌ Error: %s\n", message);
}
