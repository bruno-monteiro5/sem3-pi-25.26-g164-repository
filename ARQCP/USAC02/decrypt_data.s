    .text
    .globl decrypt_data
# int decrypt_data(char* in, int key, char* out)
decrypt_data:
    # a0 = in, a1 = key, a2 = out

    # validações
    beqz a0, ERROR_EMPTY         # in == NULL  -> erro (out = "")
    beqz a2, ERROR_NULL          # out == NULL -> erro (não dá para escrever "")

    li   t0, 1
    blt  a1, t0, ERROR_SET       # key < 1  -> erro
    li   t0, 26
    bgt  a1, t0, ERROR_SET       # key > 26 -> erro

    # cursores e constantes
    mv   t1, a0                  # t1 = pin
    mv   t2, a2                  # t2 = pout
    li   t3, 'A'                 # 65
    li   t4, 'Z'                 # 90
    li   t5, 26

LOOP:
    lbu  t0, 0(t1)               # t0 = *pin
    beqz t0, DONE_OK             # fim string -> sucesso

    # garantir A..Z
    blt  t0, t3, ERROR_SET
    bgt  t0, t4, ERROR_SET

    # x = (t0 - 'A') - key
    addi t6, t0, -'A'            # 0..25
    sub  t6, t6, a1              # pode ficar negativo
    bge  t6, zero, NO_ADD26      # se >=0, ok
    addi t6, t6, 26              # se <0, +26

NO_ADD26:
    addi t6, t6, 'A'             # volta a ASCII
    sb   t6, 0(t2)

    addi t1, t1, 1               # avança ponteiros
    addi t2, t2, 1
    j    LOOP

DONE_OK:
    sb   zero, 0(t2)             # termina out
    li   a0, 1
    ret

ERROR_SET:
    sb   zero, 0(a2)             # out = ""
    li   a0, 0
    ret

ERROR_NULL:
    li   a0, 0                   # não dá para escrever "", só falhar
    ret

ERROR_EMPTY:
    sb   zero, 0(a2)
    li   a0, 0
    ret
