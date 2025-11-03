    .text
    .globl decrypt_data
# int decrypt_data(char* in, int key, char* out)
decrypt_data:
    # a0 = in, a1 = key, a2 = out

    mv   t1, a0              # pin
    mv   t2, a2              # pout
    li   tA, 'A'             # tA = 'A'
    li   t26, 26             # t26 = 26

LOOP:
    lbu  t0, 0(t1)           # c = *pin
    beqz t0, DONE            # fim da string?

    addi t6, t0, -'A'        # x = c - 'A'  (0..25)
    sub  t6, t6, a1          # x -= key     (pode ficar < 0)
    bge  t6, zero, STORE
    addi t6, t6, 26          # se negativo, +26

STORE:
    addi t6, t6, 'A'         # c' = x + 'A'
    sb   t6, 0(t2)           # *pout = c'

    addi t1, t1, 1           # avança ponteiros
    addi t2, t2, 1
    j    LOOP

DONE:
    sb   zero, 0(t2)         # termina out
    li   a0, 1               # return 1
    ret
