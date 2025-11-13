/*
 * int enqueue_value(int* buffer, int length,
 *                   int* nelem, int* tail, int* head,
 *                   int value);
 *
 * a0 = buffer
 * a1 = length
 * a2 = nelem
 * a3 = tail
 * a4 = head
 * a5 = value
 */

    .section .text
    .global enqueue_value

enqueue_value:
    # Carregar nelem, head e tail
    lw      t0, 0(a2)      # t0 = *nelem
    lw      t1, 0(a4)      # t1 = *head
    lw      t2, 0(a3)      # t2 = *tail

    # Verificar se buffer está cheio: *nelem == length ?
    beq     t0, a1, buffer_full

    # --- Caso NÃO esteja cheio ---
    addi    t0, t0, 1      # nelem++
    sw      t0, 0(a2)      # *nelem = nelem
    j       write_value

buffer_full:
    # Buffer cheio: avançar tail (remover o mais antigo)
    addi    t2, t2, 1      # tail++
    bne     t2, a1, tail_nowrap
    li      t2, 0          # se tail == length -> tail = 0
tail_nowrap:
    sw      t2, 0(a3)      # *tail = tail

write_value:
    # Escrever value em buffer[head]
    slli    t3, t1, 2      # t3 = head * 4 (cada int = 4 bytes)
    add     t3, a0, t3     # t3 = &buffer[head]
    sw      a5, 0(t3)      # buffer[head] = value

    # Avançar head
    addi    t1, t1, 1      # head++
    bne     t1, a1, head_nowrap
    li      t1, 0          # se head == length -> head = 0
head_nowrap:
    sw      t1, 0(a4)      # *head = head

    # Retorno: 1 se buffer cheio depois da inserção, 0 caso contrário
    beq     t0, a1, ret_one
    li      a0, 0
    ret

ret_one:
    li      a0, 1
    ret
