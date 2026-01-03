CC=riscv32-linux-gcc
CFLAGS=-Wall -Wextra -fanalyzer -g 
ARCHFLAGS=-march=rv32imd -mabi=ilp32d
LIBRV32=${HOME}/bin/bootlin/riscv32-buildroot-linux-gnu/sysroot

station_mgmt.elf: main.o manager.o sensors.o board.o ui.o encrypt_data.o decrypt_data.o format_command.o enqueue_value.o dequeue_value.o move_n_to_array.o sort_array.o median.o extract_data.o
	$(CC) $(CFLAGS) $(ARCHFLAGS) main.o manager.o sensors.o board.o ui.o encrypt_data.o decrypt_data.o format_command.o enqueue_value.o dequeue_value.o move_n_to_array.o sort_array.o median.o extract_data.o -o station_mgmt.elf

main.o: main.c structures.h manager.h ui.h board.h
	$(CC) $(CFLAGS) $(ARCHFLAGS) -c main.c 

manager.o: manager.c manager.h structures.h encrypt_data.h decrypt_data.h
	$(CC) $(CFLAGS) $(ARCHFLAGS) -c manager.c

sensors.o: sensors.c sensors.h structures.h extract_data.h queue_functions.h sort_array.h
	$(CC) $(CFLAGS) $(ARCHFLAGS) -c sensors.c

board.o: board.c board.h structures.h sensors.h
	$(CC) $(CFLAGS) $(ARCHFLAGS) -c board.c

ui.o: ui.c ui.h structures.h manager.h sensors.h board.h format_command.h
	$(CC) $(CFLAGS) $(ARCHFLAGS) -c ui.c

encrypt_data.o: encrypt_data.s encrypt_data.h
	$(CC) $(CFLAGS) $(ARCHFLAGS) -c encrypt_data.s 

decrypt_data.o: decrypt_data.s decrypt_data.h
	$(CC) $(CFLAGS) $(ARCHFLAGS) -c decrypt_data.s

format_command.o: format_command.s format_command.h
	$(CC) $(CFLAGS) $(ARCHFLAGS) -c format_command.s

enqueue_value.o: enqueue_value.s queue_functions.h
	$(CC) $(CFLAGS) $(ARCHFLAGS) -c enqueue_value.s

dequeue_value.o: dequeue_value.s queue_functions.h
	$(CC) $(CFLAGS) $(ARCHFLAGS) -c dequeue_value.s

move_n_to_array.o: move_n_to_array.s queue_functions.h
	$(CC) $(CFLAGS) $(ARCHFLAGS) -c move_n_to_array.s

sort_array.o: sort_array.s sort_array.h
	$(CC) $(CFLAGS) $(ARCHFLAGS) -c sort_array.s

median.o: median.s sort_array.h
	$(CC) $(CFLAGS) $(ARCHFLAGS) -c median.s

extract_data.o: extract_data.s extract_data.h
	$(CC) $(CFLAGS) $(ARCHFLAGS) -c extract_data.s

clean:
	rm -f *.o *.elf .gdbinit 

run: station_mgmt.elf
	qemu-riscv32 -L $(LIBRV32) ./station_mgmt.elf

debug: station_mgmt.elf .gdbinit 
	@echo "On other terminal window do:"
	@echo "riscv32-linux-gdb --tui ./station_mgmt.elf"
	@echo "or just paste" 
	@echo -n "cd $(CURDIR) ; riscv32-linux-gdb --tui ./station_mgmt.elf" \
		| xclip -selection clipboard 
	qemu-riscv32 -L $(LIBRV32) -g 1234 station_mgmt.elf -S

define GDBINIT 
target extended-remote localhost:1234
set sysroot ~/bin/bootlin/riscv32-buildroot-linux-gnu/sysroot
break main 
continue 
endef 
export GDBINIT 
.gdbinit: 
	@echo "$$GDBINIT" >.gdbinit
