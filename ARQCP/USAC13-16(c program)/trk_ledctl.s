.section .rodata
cmd_GE: .asciz "GE"
cmd_YE: .asciz "YE"
cmd_RE: .asciz "RE"
cmd_RB: .asciz "RB"

    .section .text
    .global trk_led_update
    .type trk_led_update, @function

trk_led_update:
    beqz a0, error
    beqz a1, error

    mv t3, a1        # guardar out

    lw t0, 0(a0)     # t0 = id
    lw t1, 4(a0)     # t1 = state

    li t2, 0
    beq t1, t2, state_free

    li t2, 1
    beq t1, t2, state_reserved

    li t2, 2
    beq t1, t2, state_occupied

    la a0, cmd_RB
    j call_format

state_free:
    la a0, cmd_GE
    j call_format

state_reserved:
    la a0, cmd_YE
    j call_format

state_occupied:
    la a0, cmd_RE

call_format:
    mv a1, t0        # id
    mv a2, t3        # out
    jal format_command
    ret

error:
    li a0, 0
    ret
