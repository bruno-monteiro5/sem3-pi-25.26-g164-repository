.section .text

.global format_command

format_command:
	# a0 = input string (op)
	# a1 = number (n)
	# a2 = output string (cmd)

skip_leading_spaces:
	lb t0, 0(a0)
	beqz t0, invalid       # Empty string = invalid
	li t1, ' '
	bne t0, t1, check_command_start
	addi a0, a0, 1         # Skip space
	j skip_leading_spaces

check_command_start:
	# Check if we have at least 2 characters for a command
	lb t0, 0(a0)           # First letter
	beqz t0, invalid
	lb t1, 1(a0)           # Second letter
	beqz t1, invalid
	lb t2, 2(a0)           # Third letter (might be space or letter)
	
	# Convert first letter to uppercase (in register only)
	li t3, 'a'
	blt t0, t3, first_upper
	li t3, 'z'
	bgt t0, t3, invalid    # Not a letter
	addi t0, t0, -32       # Convert to uppercase

first_upper:
	# Check if first letter is uppercase now
	li t3, 'A'
	blt t0, t3, invalid
	li t3, 'Z'
	bgt t0, t3, invalid

	# Convert second letter to uppercase (in register only)
	li t3, 'a'
	blt t1, t3, second_upper
	li t3, 'z'
	bgt t1, t3, invalid    # Not a letter
	addi t1, t1, -32       # Convert to uppercase

second_upper:
	# Check if second letter is uppercase now
	li t3, 'A'
	blt t1, t3, invalid
	li t3, 'Z'
	bgt t1, t3, invalid

	# Now check if it's a 3-letter command
	beqz t2, check_2letter  # Null terminator = 2-letter command
	li t3, ' '
	beq t2, t3, check_2letter  # Space = 2-letter command
	
	# It's a 3-letter command - convert third letter
	li t3, 'a'
	blt t2, t3, third_upper
	li t3, 'z'
	bgt t2, t3, invalid    # Not a letter
	addi t2, t2, -32       # Convert to uppercase

third_upper:
	# Check if third letter is uppercase now
	li t3, 'A'
	blt t2, t3, invalid
	li t3, 'Z'
	bgt t2, t3, invalid
	
	# Check if it's GTH
	li t3, 'G'
	bne t0, t3, invalid
	li t3, 'T'
	bne t1, t3, invalid
	li t3, 'H'
	bne t2, t3, invalid
	
	# Validate rest of string is only spaces
	addi a0, a0, 3
	j validate_trailing_gth

check_2letter:
	# Check valid 2-letter commands: RB, RG, RE, GE, YE
	li t3, 'R'
	beq t0, t3, check_r_commands
	li t3, 'G'
	beq t0, t3, check_g_commands
	li t3, 'Y'
	beq t0, t3, check_y_commands
	j invalid

check_r_commands:
	li t3, 'B'
	beq t1, t3, valid_2letter
	li t3, 'G'
	beq t1, t3, valid_2letter
	li t3, 'E'
	beq t1, t3, valid_2letter
	j invalid

check_g_commands:
	li t3, 'E'
	beq t1, t3, valid_2letter
	j invalid

check_y_commands:
	li t3, 'E'
	beq t1, t3, valid_2letter
	j invalid

valid_2letter:
	# Validate number range (0-99)
	li t3, 0
	blt a1, t3, invalid
	li t3, 100
	bge a1, t3, invalid
	
	# Write command letters to output
	sb t0, 0(a2)
	addi a2, a2, 1
	sb t1, 0(a2)
	addi a2, a2, 1
	
	# Validate rest of string is only spaces
	addi a0, a0, 2
	j validate_trailing_2letter

validate_trailing_2letter:
	lb t0, 0(a0)
	beqz t0, format_number    # End of string, proceed
	li t1, ' '
	bne t0, t1, invalid       # Not a space = invalid
	addi a0, a0, 1
	j validate_trailing_2letter

validate_trailing_gth:
	lb t0, 0(a0)
	beqz t0, write_gth        # End of string, valid
	li t1, ' '
	bne t0, t1, invalid       # Not a space = invalid
	addi a0, a0, 1
	j validate_trailing_gth

write_gth:
	# Write GTH to output
	li t0, 'G'
	sb t0, 0(a2)
	addi a2, a2, 1
	li t0, 'T'
	sb t0, 0(a2)
	addi a2, a2, 1
	li t0, 'H'
	sb t0, 0(a2)
	addi a2, a2, 1
	sb zero, 0(a2)     # Null terminator
	li a0, 1           # Success
	ret

format_number:
	# Add comma
	li t0, ','
	sb t0, 0(a2)
	addi a2, a2, 1
	
	# Format number with leading zero if needed
	li t0, 10
	bge a1, t0, two_digits
	
	# Single digit: add leading zero
	li t0, '0'
	sb t0, 0(a2)
	addi a2, a2, 1
	add t0, a1, t0     # Convert digit to ASCII
	sb t0, 0(a2)
	addi a2, a2, 1
	j finish_number

two_digits:
	# Two digits
	li t0, 10
	div t1, a1, t0     # Tens digit
	rem t2, a1, t0     # Ones digit
	
	li t0, '0'
	add t1, t1, t0     # Convert tens to ASCII
	sb t1, 0(a2)
	addi a2, a2, 1
	
	add t2, t2, t0     # Convert ones to ASCII
	sb t2, 0(a2)
	addi a2, a2, 1

finish_number:
	sb zero, 0(a2)     # Null terminator
	li a0, 1           # Success
	ret

invalid:
	sb zero, 0(a2)     # Empty output string
	li a0, 0           # Failure
	ret