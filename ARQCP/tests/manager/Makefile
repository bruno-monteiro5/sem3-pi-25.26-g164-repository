CC=riscv32-linux-gcc
CFLAGS=-Wall -Wextra -g
ARCHFLAGS=-march=rv32imd -mabi=ilp32d
LIBRV32=${HOME}/bin/bootlin/riscv32-buildroot-linux-gnu/sysroot

UNITY_DIR=../unity
SRC_DIR=../..

UNITY_SRC=$(UNITY_DIR)/unity.c

MANAGER_SRC=$(SRC_DIR)/manager.c

ASM_OBJS=encrypt_data.o decrypt_data.o format_command.o

all: test_manager.elf

encrypt_data.o: $(SRC_DIR)/encrypt_data.s
	$(CC) $(CFLAGS) $(ARCHFLAGS) -c $(SRC_DIR)/encrypt_data.s -o encrypt_data.o

decrypt_data.o: $(SRC_DIR)/decrypt_data.s
	$(CC) $(CFLAGS) $(ARCHFLAGS) -c $(SRC_DIR)/decrypt_data.s -o decrypt_data.o

format_command.o: $(SRC_DIR)/format_command.s
	$(CC) $(CFLAGS) $(ARCHFLAGS) -c $(SRC_DIR)/format_command.s -o format_command.o

manager.o: $(MANAGER_SRC)
	$(CC) $(CFLAGS) $(ARCHFLAGS) -c $(MANAGER_SRC) -o manager.o

unity.o: $(UNITY_SRC)
	$(CC) $(CFLAGS) $(ARCHFLAGS) -c $(UNITY_SRC) -o unity.o

test_manager.elf: test_manager.c manager.o unity.o $(ASM_OBJS)
	$(CC) $(CFLAGS) $(ARCHFLAGS) -I$(UNITY_DIR) test_manager.c manager.o unity.o $(ASM_OBJS) -o test_manager.elf

run: test_manager.elf
	qemu-riscv32 -L $(LIBRV32) ./test_manager.elf

clean:
	rm -f *.o *.elf

.PHONY: all run clean