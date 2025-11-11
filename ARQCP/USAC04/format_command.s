.section .text

.global format_command

format_command:
	li t0, 'A'
	li t1, 'Z'
	li t2, 32          # Para converter minúscula->maiúscula
	li t4, 0           # Return value

loop:
	lb t6, 0(a0)
	beqz t6, invalid       # Se fim da string, inválido
	li t3, ' '
	beq t6, t3, skip_space # Se é espaço, pula
	j check_commands       # Se é letra, processa

skip_space:
	addi a0, a0, 1
	j loop

check_commands:
	lb t3, 2(a0)       # Carregar 3º caractere
	beqz t3, check_command_2_letter
	li t6, ' '
	beq t3, t6, check_command_2_letter  # Se é espaço, comando de 2 letras
	j check_command_3_letter            # Caso contrário, 3 letras

check_command_2_letter:
	lb t5, 0(a0)       # Carregar 1ª letra
	bgt t5, t1, increase_first_letter
	lb t5, 1(a0)       # Carregar 2ª letra
	bgt t5, t1, increase_second_letter

	lb t5, 0(a0)
	li t3, 'R'
	beq t5, t3, check_rb
	li t3, 'G'
	beq t5, t3, check_ge
	li t3, 'Y'
	beq t5, t3, check_ye
	j invalid

increase_first_letter:
	lb t5, 0(a0)
	sub t5, t5, t2
	sb t5, 0(a0)
	j check_command_2_letter

increase_second_letter:
	lb t5, 1(a0)
	sub t5, t5, t2
	sb t5, 1(a0)
	j check_command_2_letter

check_rb:
	lb t5, 1(a0)
	li t3, 'B'
	bne t5, t3, check_rg
	j validate_rb

check_rg:
	lb t5, 1(a0)
	li t3, 'G'
	beq t5, t3, validate_rg
	j invalid

check_ge:
	lb t5, 1(a0)
	li t3, 'E'
	beq t5, t3, validate_ge
	j invalid

check_ye:
	lb t5, 1(a0)
	li t3, 'E'
	beq t5, t3, validate_ye
	j invalid

validate_rb:
validate_rg:
validate_ge:
validate_ye:
	lb t5, 0(a0)
	sb t5, 0(a2)
	addi a2, a2, 1
	lb t5, 1(a0)
	sb t5, 0(a2)
	addi a2, a2, 1
	li t3, 100
	blt a1, t3, represent
	j invalid


check_command_3_letter:
	lb t5, 0(a0)
	bgt t5, t1, increase_first_letter_3
	lb t5, 1(a0)
	bgt t5, t1, increase_second_letter_3
	lb t5, 2(a0)
	bgt t5, t1, increase_third_letter

	lb t5, 0(a0)
	li t3, 'G'
	beq t5, t3, check_t
	j invalid

check_t:
	lb t5, 1(a0)
	li t3, 'T'
	beq t5, t3, check_h
	j invalid

check_h:
	lb t5, 2(a0)
	li t3, 'H'
	beq t5, t3, validate_gth
	j invalid

increase_first_letter_3:
	lb t5, 0(a0)
	sub t5, t5, t2
	sb t5, 0(a0)
	j check_command_3_letter

increase_second_letter_3:
	lb t5, 1(a0)
	sub t5, t5, t2
	sb t5, 1(a0)
	j check_command_3_letter

increase_third_letter:
	lb t5, 2(a0)
	sub t5, t5, t2
	sb t5, 2(a0)
	j check_command_3_letter


validate_gth:
	lb t5, 0(a0)
	sb t5, 0(a2)
	addi a2, a2, 1
	lb t5, 1(a0)
	sb t5, 0(a2)
	addi a2, a2, 1
	lb t5, 2(a0)
	sb t5, 0(a2)
	addi a2, a2, 1
	sb zero, 0(a2)     # Null terminator
	li t4, 1
	j return

represent:
	li t3, ','
	sb t3, 0(a2)
	addi a2, a2, 1
	li t3, 10
	div t5, a1, t3     # Dezena
	li t3, '0'
	add t5, t5, t3     # Converter para ASCII
	sb t5, 0(a2)
	addi a2, a2, 1
	li t3, 10
	rem t5, a1, t3     # Unidade
	li t3, '0'
	add t5, t5, t3     # Converter para ASCII
	sb t5, 0(a2)
	addi a2, a2, 1
	sb zero, 0(a2)     # Null terminator
	li t4, 1
	j return

invalid:
	sb zero, 0(a2)     # Limpar output
	li t4, 0

return:
	mv a0, t4
	ret