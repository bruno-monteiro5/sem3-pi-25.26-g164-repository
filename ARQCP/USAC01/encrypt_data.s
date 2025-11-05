    .section .text
    .global encrypt_data
encrypt_data:
    li t0, 1
    blt a1, t0, fail
    li t0, 26
    bgt a1, t0, fail

loop:
    lbu t0, 0(a0)
    beqz t0, done

    add t1, t0, a1
    li t2, 'Z'
    ble t1, t2, store
    addi t1, t1, -26

store:
    sb t1, 0(a2)
    addi a0, a0, 1
    addi a2, a2, 1
    j loop

done:
    sb x0, 0(a2)
    li a0, 1
    ret

fail:
    sb x0, 0(a2)
    li a0, 0
    ret
