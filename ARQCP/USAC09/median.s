.section .text
.global median
.extern sort_array

median:
	# if (length <= 0) return 0;
	blt     a1, x0, len_le_zero   # length < 0  -> error
	beq     a1, x0, len_le_zero   # length == 0 -> error

	addi sp, sp, -16
	sw ra, 12(sp)
	sw s1, 8(sp)
	sw s2, 4(sp)
	sw s3, 0(sp)

	mv s1, a0             # s1 = vec
	mv s2, a1             # s2 = length
	mv s3, a2             # s3 = ponteir result

	# Preparar argumentos para sort_array
	mv a0, s1             # a0 = vec
	mv a1, s2             # a1 = length
	li a2, 1              # a2 = 1 (ascending order)

	jal sort_array

	# mid_hi = length / 2
	li t1, 2
	div t0, s2, t1

	# check parity: if (length & 1) != 0 -> odd
	andi t1, s2, 1
	bne t1, x0, odd_case

	# even_case: median = (vec[mid_hi-1] + vec[mid_hi]) / 2
even_case:
	addi t1, t0, -1       # t1 = mid_lo = mid_hi - 1

	# load vec[mid_lo] into t3
	slli t2, t1, 2        # t2 = mid_lo * 4
	add t2, s1, t2        # &vec[mid_lo]
	lw t3, 0(t2)          # t3 = vec[mid_lo]

	# load vec[mid_hi] into t4
	slli t2, t0, 2        # t2 = mid_hi * 4
	add t2, s1, t2        # &vec[mid_hi]
	lw t4, 0(t2)          # t4 = vec[mid_hi]

	add t1, t3, t4        # t1 = vec[mid_lo] + vec[mid_hi]
	srai t1, t1, 1        # t1 = (sum) / 2

	j store_and_return

# odd_case: median = vec[mid_hi]
odd_case:
	slli t2, t0, 2        # t2 = mid_hi * 4
	add t2, s1, t2        # &vec[mid_hi]
	lw t1, 0(t2)          # t1 = vec[mid_hi]

store_and_return:
	sw t1, 0(s3)

	lw ra, 12(sp)
	lw s1, 8(sp)
	lw s2, 4(sp)
	lw s3, 0(sp)
	addi sp, sp, 16

	li a0, 1              # return 1 (success)
	ret

# length <= 0: return 0 and do NOT modify *me
len_le_zero:
	li a0, 0              # return 0
	ret
