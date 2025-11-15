    .section .text
    .global median

median:
    # Validate length
    blt     a1, x0, len_le_zero    # length < 0
    beq     a1, x0, len_le_zero    # length == 0

    ret

len_le_zero:
    beq     a2, x0, len_ret0
    sw      x0, 0(a2)
len_ret0:
    li      a0, 0
    ret
