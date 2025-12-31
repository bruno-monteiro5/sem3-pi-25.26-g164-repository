#include <stdio.h>
#include <string.h>
#include "encrypt_data.h"
#include <fcntl.h>
#include <unistd.h>
#include <termios.h>
#include <errno.h>
#include "format_command.h"

int main(){ 
	char pass_corr[] = "VGYYCUXJKX"; 
	char uss_corr[] = "admin";
	int key = 6; 
	printf("Insert username\n"); 
	printf("Username:"); 
	char uss[16]; 
	char pass[32]; 
	scanf("%s",uss);
	while(strcmp(uss, uss_corr)!= 0){
		printf("The username entered does not exist\n");
		printf("Please enter your username again: ");
		scanf("%s", uss);
	} 
	printf("Insert password\n"); 
	printf("Password:"); 
	scanf("%s", pass); 
	char passe[] = ""; 
	int m = encrypt_data((unsigned char *)&pass, key,(unsigned char *)&passe); 
	while(m!=1 && strcmp(passe, pass_corr) != 0){ 
		printf("Your password is written in a invalid manner and/or is incorrect, please try again, and remember:\n"); 
		printf("####1: Only use members of the english alphabet when writing your password attempt. No special characters and/or numbers####\n"); 
		printf("####2: All letters of the password attempt should be capital\n"); 
		printf("Enter your password again please: "); 
		scanf("%s", pass);
		m = encrypt_data((unsigned char *)&pass, key,(unsigned char *)&passe); 
	}
	printf("####MANAGER MENU####\n");
	printf("                   ||\n");
	printf("                   ||\n");
	printf("            (  )   ||   (   )  )\n");
	printf("             ) (   ||   )  (  (\n");
	printf("             ( )  (||) (    ) )\n");
	printf("              ________________________________\n");
	printf("             |        ________                |_____\n");
	printf("             |       |        |               |_____|]\n");
	printf("             |_______|________|_______________|_____|]\n");
	printf("             |________________________________________\\\n");
	printf("              \\________________________________________\\\n");
	printf("                 (OOOOOO)   (OOOOOO)   (OOOOOO)\n");
	printf("                (OOOOOOOO) (OOOOOOOO) (OOOOOOOO)\n");
	printf("                 (OOOOOO)   (OOOOOO)   (OOOOOO)\n");
	printf("####OPTIONS####\n");
	printf("1): Comunicate with the arduino\n");
	char opt;
	printf("Please enter your option: ");
	scanf("%s", opt);
	while(opt<1 && opt>1){
		printf("Invalid option, please enter another: ");
		scanf("%c", opt);
	}
	
}

int open_serial(char *device) {
    int fd = open(device, O_RDWR | O_NOCTTY | O_SYNC);
    if (fd < 0) {
        perror("open");
        return -1;
    }

    struct termios tty;
    if (tcgetattr(fd, &tty) != 0) {
        perror("tcgetattr");
        close(fd);
        return -1;
    }

    cfsetospeed(&tty, B9600);
    cfsetispeed(&tty, B9600);

    tty.c_cflag = (tty.c_cflag & ~CSIZE) | CS8;
    tty.c_iflag = 0;
    tty.c_oflag = 0;
    tty.c_lflag = 0;

    tty.c_cc[VMIN]  = 1;
    tty.c_cc[VTIME] = 5;

    tcsetattr(fd, TCSANOW, &tty);
    return fd;
}

void send_cmd(int fd, const char *cmd) {
    write(fd, cmd, strlen(cmd));
}

void read_response(int fd) {
    char buf[256];
    int n = read(fd, buf, sizeof(buf) - 1);
    if (n > 0) {
        buf[n] = '\0';
        printf("Arduino: %s\n", buf);
    }
}





