.section .text

.global encrypt_data

encrypt_data:
    li t0, 0
    ble a1, t0, fail
    li t0, 26
    bgt a1, t0, fail

loop:

    lbu t0, 0(a0)
    beqz t0, done

    # Validate if the letter is uppercase
    li t2, 'A'
    blt t0, t2, fail    # If < 'A', skip to failure procedure
    li t2, 'Z'
    bgt t0, t2, fail    # If > 'Z', skip to failure procedure

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
