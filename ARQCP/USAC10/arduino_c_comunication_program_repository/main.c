#include <stdio.h>
#include <string.h>
#include "encrypt_data.h"
#include <fcntl.h>
#include <unistd.h>
#include <termios.h>
#include <errno.h>
#include <stdlib.h>
#include "format_command.h"
int main() {
    char uss_corr[] = "admin";
    char pass_corr[] = "VGYYCUXJKX";
    int key = 6;
    char uss[16], pass[32], passe[32];
    char opt, opt2;
    printf("Please insert your username: ");
    scanf("%s", uss);
    while (strcmp(uss, uss_corr) != 0) {
        printf("Invalid username, please insert it again!: ");
        scanf("%s", uss);
    }
    printf("Please insert your password: ");
    scanf("%s", pass);
    int m = encrypt_data(pass, key, passe);
    while (m != 1 || strcmp(passe, pass_corr) != 0) {
        printf("Invalid password, please insert it again!: ");
        scanf("%s", pass);
        m = encrypt_data(pass, key, passe);
    }
	printf("####MANAGER MENU####\n"); 
	printf(" ||\n"); 
	printf(" ||\n"); 
	printf(" ( ) || ( ) )\n"); 
	printf(" ) ( || ) ( (\n"); 
	printf(" ( ) (||) ( ) )\n"); 
	printf(" ________________________________\n"); 
	printf(" | ________ |_____\n"); 
	printf(" | | | |_____|]\n"); 
	printf(" |_______|________|_______________|_____|]\n"); 
	printf(" |________________________________________\\\n"); 
	printf(" \\________________________________________\\\n"); 
	printf(" (OOOOOO) (OOOOOO) (OOOOOO)\n"); 
	printf(" (OOOOOOOO) (OOOOOOOO) (OOOOOOOO)\n"); 
	printf(" (OOOOOO) (OOOOOO) (OOOOOO)\n"); 
	printf("####OPTIONS####\n");
	do{	
    	printf("1) Communicate with Arduino\n2) Exit\n");
    	printf("Choice: ");
    	scanf(" %c", &opt);
    	if (opt == '1') {
        	int sp = open("/dev/ttyS0", O_RDWR);
        	if (sp < 0) return 1;
		struct termios tty;
		memset(&tty, 0, sizeof tty);
		if (tcgetattr(sp, &tty) != 0) return 1;
		cfsetispeed(&tty, B9600);
		cfsetospeed(&tty, B9600);
		
        	do {
            		printf("1) LED\n2) Sensor\n3) Exit\n");
            		scanf(" %c", &opt2);
            		if (opt2 == '1') {
                		char cmd[8], out[16];
                		int track;
                		printf("Command (RE YE GE RB or cancel): ");
                		scanf("%s", cmd);
                		if (!strcmp(cmd, "cancel")) break;
                		printf("Track: ");
                		scanf("%d", &track);
				int r = format_command(cmd, track, out);
				
                		while(r!=1){
                			printf("Invalid command, type it again!\n");
                			printf("Command (RE YE GE RB or cancel): ");
                                        scanf("%s", cmd);
                                        if (!strcmp(cmd, "cancel")) break;
                                        printf("Track: ");
                			scanf("%d", &track);
                			r = format_command(cmd, track, out);
                		}
                		write(sp, out, sizeof(out));
                		char resp[256];
                		int re = read(sp, &resp, sizeof(resp));
                		if(re>0) printf("Recieved message: %.*s\n", re, resp);
            		}else if(opt2 == '2'){
            		        char cmd[8] = "", out[16];
            		        
            		        
							while(strcmp(cmd, "cancel")!=0){
								printf("Insert the sensor command(or cancel to return to the former area): ");
								scanf("%s", cmd);
					                                
								int r = format_command(cmd, 0, out);
								while(r!=1 && strcmp(out, "GTH") != 0){
									printf("Invalid command, please insert it again: ");
									scanf("%s", cmd);
									r = format_command(cmd, 0, out);
								}
								write(sp, out, sizeof(out));
								char resp[256];
								int re = read(sp, &resp, sizeof(resp));
								if(re>0) printf("Recieved message: %.*s\n", re, resp);
							}
            		}
        	} while (opt2 != '3');
			close(sp);
		}
    } while (opt != '2');
    return 0;
}
