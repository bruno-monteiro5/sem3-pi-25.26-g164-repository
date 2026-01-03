#include <stdio.h>
#include <string.h>
#include "structures.h"
#include "manager.h"
#include "ui.h"
#include "board.h"

int main() {
    // Welcome ASCII art
    clear_screen();
    printf("\n");
    printf("####STATION MANAGEMENT SYSTEM####\n");
    printf(" ||\n");
    printf(" ||\n");
    printf(" ( ) || ( ) )\n");
    printf(" ) ( || ) ( (\n");
    printf(" ( ) (||) ( ) )\n");
    printf(" ________________________________\n");
    printf(" | ________ |_____\n");
    printf(" | |_|_|_____|]\n");
    printf(" |_|_|_|_____|_______________|___________|]\n");
    printf(" |________________________________________\\\n");
    printf(" \\________________________________________\\\n");
    printf(" (OOOOOO) (OOOOOO) (OOOOOO)\n");
    printf(" (OOOOOOOO) (OOOOOOOO) (OOOOOOOO)\n");
    printf(" (OOOOOO) (OOOOOO) (OOOOOO)\n\n");
    
    printf("Loading system...\n");
    
    // Initialize Manager
    Manager* mgr = init_manager("config.txt");
    if (!mgr) {
        printf("Failed to initialize manager!\n");
        return 1;
    }
    
    // Login
    char username[32], password[32];
    User* user = NULL;
    
    printf("\n╔═══════════════════════════════════════╗\n");
    printf("║            USER LOGIN                 ║\n");
    printf("╚═══════════════════════════════════════╝\n");
    
    do {
        printf("Username: ");
        scanf("%s", username);
        
        printf("Password: ");
        scanf("%s", password);
        
        user = login_user(mgr, username, password);
        
        if (!user) {
            printf("\n❌ Invalid credentials! Please try again.\n\n");
        }
    } while (!user);
    
    printf("\n✅ Login successful! Welcome, %s\n", user->name);
    printf("\nPress Enter to continue...");
    getchar();
    getchar();
    
    // Login log
    char log_msg[256];
    sprintf(log_msg, "User logged in: %s", user->username);
    log_action(mgr, log_msg);
    
    // Main UI loop
    ui_main_loop(mgr);
    
    // Cleanup
    printf("\n\nLogging out...\n");
    log_action(mgr, "User logged out");
    
    free_manager(mgr);
    
    printf("Thank you for using Station Management System!\n");
    printf("Goodbye! 👋\n\n");
    
    return 0;
}