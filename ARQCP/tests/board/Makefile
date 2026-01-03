CC=riscv32-linux-gcc
CFLAGS=-Wall -Wextra -g
ARCHFLAGS=-march=rv32imd -mabi=ilp32d
LIBRV32=${HOME}/bin/bootlin/riscv32-buildroot-linux-gnu/sysroot

UNITY_DIR=../unity
SRC_DIR=../..

UNITY_SRC=$(UNITY_DIR)/unity.c
BOARD_SRC=$(SRC_DIR)/board.c
MANAGER_SRC=$(SRC_DIR)/manager.c
SENSORS_SRC=$(SRC_DIR)/sensors.c

ASM_OBJS=encrypt_data.o decrypt_data.o format_command.o \
         extract_data.o enqueue_value.o dequeue_value.o \
         move_n_to_array.o sort_array.o median.o

all: test_board.elf

encrypt_data.o: $(SRC_DIR)/encrypt_data.s
	$(CC) $(CFLAGS) $(ARCHFLAGS) -c $(SRC_DIR)/encrypt_data.s

decrypt_data.o: $(SRC_DIR)/decrypt_data.s
	$(CC) $(CFLAGS) $(ARCHFLAGS) -c $(SRC_DIR)/decrypt_data.s

format_command.o: $(SRC_DIR)/format_command.s
	$(CC) $(CFLAGS) $(ARCHFLAGS) -c $(SRC_DIR)/format_command.s

extract_data.o: $(SRC_DIR)/extract_data.s
	$(CC) $(CFLAGS) $(ARCHFLAGS) -c $(SRC_DIR)/extract_data.s

enqueue_value.o: $(SRC_DIR)/enqueue_value.s
	$(CC) $(CFLAGS) $(ARCHFLAGS) -c $(SRC_DIR)/enqueue_value.s

dequeue_value.o: $(SRC_DIR)/dequeue_value.s
	$(CC) $(CFLAGS) $(ARCHFLAGS) -c $(SRC_DIR)/dequeue_value.s

move_n_to_array.o: $(SRC_DIR)/move_n_to_array.s
	$(CC) $(CFLAGS) $(ARCHFLAGS) -c $(SRC_DIR)/move_n_to_array.s

sort_array.o: $(SRC_DIR)/sort_array.s
	$(CC) $(CFLAGS) $(ARCHFLAGS) -c $(SRC_DIR)/sort_array.s

median.o: $(SRC_DIR)/median.s
	$(CC) $(CFLAGS) $(ARCHFLAGS) -c $(SRC_DIR)/median.s

# C sources
board.o: $(BOARD_SRC)
	$(CC) $(CFLAGS) $(ARCHFLAGS) -c $(BOARD_SRC)

manager.o: $(MANAGER_SRC)
	$(CC) $(CFLAGS) $(ARCHFLAGS) -c $(MANAGER_SRC)

sensors.o: $(SENSORS_SRC)
	$(CC) $(CFLAGS) $(ARCHFLAGS) -c $(SENSORS_SRC)

unity.o: $(UNITY_SRC)
	$(CC) $(CFLAGS) $(ARCHFLAGS) -c $(UNITY_SRC)

# Link
test_board.elf: test_board.c board.o manager.o sensors.o unity.o $(ASM_OBJS)
	$(CC) $(CFLAGS) $(ARCHFLAGS) -I$(UNITY_DIR) test_board.c board.o manager.o sensors.o unity.o $(ASM_OBJS) -o test_board.elf

run: test_board.elf
	qemu-riscv32 -L $(LIBRV32) ./test_board.elf

clean:
	rm -f *.o *.elf

.PHONY: all run clean