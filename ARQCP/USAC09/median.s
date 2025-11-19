.section .text
 .global median

median:
 # if (length <= 0) return 0;  (do not touch *me)
 blt     a1, x0, len_le_zero   # length < 0  -> error
 beq     a1, x0, len_le_zero   # length == 0 -> error

 mv      t5, a1                # t5 = length
 li      t0, 0                 # t0 = i (outer loop index)

# Bubble sort in ascending order
outer_loop:
 addi    t3, t5, -1            # t3 = length - 1
 bge     t0, t3, outer_done    # if (i >= length-1) exit outer loop

 li      t1, 0                 # t1 = j (inner loop index)

inner_loop:
 addi    t3, t5, -1            # t3 = length - 1
 sub     t3, t3, t0            # t3 = length - 1 - i
 bge     t1, t3, inner_done    # if (j >= length-1-i) exit inner loop

 slli    t2, t1, 2             # t2 = j * 4
 add     t4, a0, t2            # t4 = &vec[j]
 lw      t6, 0(t4)             # t6 = vec[j]

 addi    t2, t2, 4             # t2 = (j+1) * 4
 add     t3, a0, t2            # t3 = &vec[j+1]
 lw      t2, 0(t3)             # t2 = vec[j+1]

 blt     t2, t6, do_swap       # if vec[j+1] < vec[j] -> swap
 j       no_swap

do_swap:
 sw      t2, 0(t4)             # vec[j]   = vec[j+1]
 sw      t6, 0(t3)             # vec[j+1] = old vec[j]
no_swap:
 addi    t1, t1, 1             # j++
 j       inner_loop

inner_done:
 addi    t0, t0, 1             # i++
 j       outer_loop

# Compute median after sorting
outer_done:
 # mid_hi = length / 2
 srli    t0, t5, 1             # t0 = mid_hi = length >> 1

 # check parity: if (length & 1) != 0 -> odd
 andi    t1, t5, 1
 bne     t1, x0, odd_case      # if odd, jump to odd_case

# even_case: median = (vec[mid_hi-1] + vec[mid_hi]) / 2
even_case:
 addi    t1, t0, -1            # t1 = mid_lo = mid_hi - 1

 # load vec[mid_lo] into t3
 slli    t2, t1, 2             # t2 = mid_lo * 4
 add     t2, a0, t2            # &vec[mid_lo]
 lw      t3, 0(t2)             # t3 = vec[mid_lo]

 # load vec[mid_hi] into t4
 slli    t2, t0, 2             # t2 = mid_hi * 4
 add     t2, a0, t2            # &vec[mid_hi]
 lw      t4, 0(t2)             # t4 = vec[mid_hi]

 add     t1, t3, t4            # t1 = vec[mid_lo] + vec[mid_hi]
 srai    t1, t1, 1             # t1 = (sum) / 2 (arithmetic, keeps sign)

 j       store_and_return

# odd_case: median = vec[mid_hi]
odd_case:
 slli    t2, t0, 2             # t2 = mid_hi * 4
 add     t2, a0, t2            # &vec[mid_hi]
 lw      t1, 0(t2)             # t1 = vec[mid_hi]

store_and_return:
 sw      t1, 0(a2)             # *me = median
 li      a0, 1                 # return 1 (success)
 ret

# length <= 0: return 0 and do NOT modify *me
len_le_zero:
 li      a0, 0                 # return 0
 ret
