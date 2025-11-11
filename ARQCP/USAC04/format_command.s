.section .text

.global format_command

format_command:
	li t0, 'A'
	li t1, 'Z'
	li t5, 0      
	li t2, 32       

loop:
	lb t6, 0(a0)
	beqz t6, ending  
	li t3, ' '
	beq t6, t3, loop_skip_space   
	j loop_non_zero

loop_skip_space:
	addi a0, a0, 1
	j loop

loop_non_zero:
	li t3, ' '
	beq t6, t3, validate_command 
	beqz t6, validate_command 
	
	li t3, 'a'
	li t4, 'z'
	blt t6, t3, store_upper       
	bgt t6, t4, store_upper    
	
	sub t6, t6, t2          
	
store_upper:
	sb t6, 0(a2)
	addi a2, a2, 1
	addi a0, a0, 1
	lb t6, 0(a0)
	j loop_non_zero

validate_command:

	mv t3, a2
	addi t3, t3, -2  
	lb t4, 0(t3)   
	lb t6, 1(t3) 
	
	li t0, 'R'
	bne t4, t0, check_ye
	li t0, 'E'
	beq t6, t0, valid_re
	li t0, 'B'
	beq t6, t0, valid_rb
	j check_3char

check_ye:
	li t0, 'Y'
	bne t4, t0, check_ge
	li t0, 'E'
	beq t6, t0, valid_2char
	j check_3char

check_ge:
	li t0, 'G'
	bne t4, t0, check_3char
	li t0, 'E'
	beq t6, t0, valid_2char
	j check_3char

valid_re:
valid_rb:
valid_2char:
	li t0, 100
	bge a1, t0, invalid
	
	li t4, ','
	sb t4, 0(a2)
	addi a2, a2, 1
	
	li t0, 10
	div t3, a1, t0 
	rem t4, a1, t0
	
	li t0, '0'
	add t3, t3, t0
	add t4, t4, t0
	
	sb t3, 0(a2)
	sb t4, 1(a2)
	addi a2, a2, 2
	
	sb zero, 0(a2)
	
	li t5, 1 
	j ending

check_3char:

	addi t3, a2, -3
	lb t4, 0(t3)       # G
	lb t6, 1(t3)       # T
	lb t0, 2(t3)       # H
	
	li t1, 'G'
	bne t4, t1, invalid
	li t1, 'T'
	bne t6, t1, invalid
	li t1, 'H'
	bne t0, t1, invalid
	
	sb zero, 0(a2)
	li t5, 1    
	j ending

invalid:
	addi a2, a2, -10   
	sb zero, 0(a2)
	li t5, 0

ending:
	mv a0, t5
	ret
