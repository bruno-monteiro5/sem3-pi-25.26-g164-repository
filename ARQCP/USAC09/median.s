    .section .text
    .global median

median:
    # if (length <= 0) return 0;
    blt     a1, x0, len_le_zero   # length < 0  -> erro
    beq     a1, x0, len_le_zero   # length == 0 -> erro

    mv      t5, a1                # t5 = length
    li      t0, 0                 # t0 = i

outer_loop:
    addi    t3, t5, -1            # t3 = length - 1
    bge     t0, t3, outer_done    # if (i >= length-1) sai

    # j = 0
    li      t1, 0                 # t1 = j

inner_loop:
    addi    t3, t5, -1            # t3 = length - 1
    sub     t3, t3, t0            # t3 = length - 1 - i
    bge     t1, t3, inner_done    # if (j >= length-1-i) sai

    slli    t2, t1, 2             # t2 = j * 4
    add     t4, a0, t2            # t4 = &vec[j]
    lw      t6, 0(t4)             # t6 = vec[j]

    addi    t2, t2, 4             # t2 = (j+1)*4
    add     t3, a0, t2            # t3 = &vec[j+1]
    lw      t2, 0(t3)             # t2 = vec[j+1]

    blt     t2, t6, do_swap       # se vec[j+1] < vec[j] -> troca
    j       no_swap

do_swap:
    sw      t2, 0(t4)             # vec[j]   = vec[j+1]
    sw      t6, 0(t3)             # vec[j+1] = vec[j]
no_swap:
    addi    t1, t1, 1             # j++
    j       inner_loop

inner_done:
    addi    t0, t0, 1             # i++
    j       outer_loop

outer_done:
    srli    t0, t5, 1             # t0 = length >> 1
    slli    t0, t0, 2             # t0 = mid * 4
    add     t0, a0, t0            # &vec[mid]
    lw      t1, 0(t0)             # t1 = vec[mid]


    sw      t1, 0(a2)

    li      a0, 1                 # return 1
    ret

len_le_zero:
    beq     a2, x0, len_ret0
    sw      x0, 0(a2)
len_ret0:
    li      a0, 0                 # return 0
    ret

