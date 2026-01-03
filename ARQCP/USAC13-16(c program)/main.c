#include <stdio.h>
#include <string.h>
#include "structures.h"
#include "manager.h"
#include "ui.h"
#include "board.h"

int main() {
    // Welcome screen with ASCII art train station
    clear_screen();
    printf("\n");
    printf("   ####  WELCOME TO THE  ####\n");
    printf("   ####   C-TATION!!!    ####\n");
    printf("        (Get it? C + Station)\n\n");
    printf("            ||\n");
    printf("            ||\n");
    printf("         ( ) || ( ) )\n");
    printf("         ) ( || ) ( (\n");
    printf("         ( ) (||) ( ) )\n");
    printf("  ________________________________\n");
    printf("  | ________ |_____\n");
    printf("  | |_|_|_____|]\n");
    printf("  |_|_|_|_____|_______________|___________|]\n");
    printf("  |________________________________________\\\n");
    printf("  \\________________________________________\\\n");
    printf("   (OOOOOO)   (OOOOOO)   (OOOOOO)\n");
    printf("  (OOOOOOOO) (OOOOOOOO) (OOOOOOOO)\n");
    printf("   (OOOOOO)   (OOOOOO)   (OOOOOO)\n\n");
    
    printf("Loading system components...\n");
    sleep(1);
    
    // Initialize the manager with configuration file
    Manager* mgr = init_manager("config.txt");
    if (!mgr) {
        printf("FATAL ERROR: Could not initialize system!\n");
        return 1;
    }
    
    // User authentication process
    char username[32], password[32];
    User* user = NULL;
    
    printf("\n+========================================+\n");
    printf("|          USER AUTHENTICATION           |\n");
    printf("+========================================+\n");
    
    do {
        printf("Username: ");
        scanf("%s", username);
        
        printf("Password: ");
        scanf("%s", password);
        
        user = login_user(mgr, username, password);
        
        if (!user) {
            printf("\n[ACCESS DENIED] Invalid credentials. Try again.\n\n");
        }
    } while (!user);
    
    printf("\n[ACCESS GRANTED] Welcome, %s!\n", user->name);
    printf("\nPress Enter to continue...");
    getchar();
    getchar();
    
    // Log the successful login
    char log_msg[256];
    sprintf(log_msg, "User logged in: %s", user->username);
    log_action(mgr, log_msg);
    
    // Start the main user interface loop
    ui_main_loop(mgr);
    
    // Cleanup and exit
    printf("\n\nLogging out user %s...\n", user->name);
    log_action(mgr, "User logged out");
    
    free_manager(mgr);
    
    printf("\n");
    printf("   Thanks for using C-TATION!\n");
    printf("   ===========================\n");
    printf("      Have a safe journey!\n");
    printf("              ||  \n");
    printf("           ___||___\n");
    printf("          |_______|  \n\n");
    
    return 0;
}