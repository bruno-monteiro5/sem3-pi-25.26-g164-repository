/*
 * int dequeue_value(int* buffer, int length,
 *                   int* nelem, int* tail, int* head, int* value);
 *
 * a0 = buffer
 * a1 = length
 * a2 = nelem
 * a3 = tail
 * a4 = head
 * a5 = value
 */

    .text
    .globl dequeue_value

dequeue_value:

    # Carregar *nelem para saber se há elementos no buffer
    lw      t0, 0(a2)             # t0 = *nelem
    beqz    t0, .fail             # Se *nelem == 0 → buffer vazio → retorna 0

    # Carregar índice tail (posição do elemento mais antigo)
    lw      t1, 0(a3)             # t1 = *tail

    # Calcular endereço de buffer[tail]
    slli    t2, t1, 2             # t2 = tail * 4 (cada int = 4 bytes)
    add     t3, a0, t2            # t3 = &buffer[tail]

    # Ler o elemento antigo
    lw      t4, 0(t3)             # t4 = buffer[tail]

    # Guardar elemento removido no local apontado por value
    sw      t4, 0(a5)             # *value = buffer[tail]

    # Avançar tail: tail = (tail + 1) % length
    addi    t1, t1, 1             # tail++
    bne     t1, a1, .no_wrap      # Se tail != length → não fazer wrap
    li      t1, 0                 # Caso contrário → tail = 0 (wrap)

.no_wrap:
    sw      t1, 0(a3)             # *tail = novo tail

    # Decrementar número de elementos
    addi    t0, t0, -1            # --(*nelem)
    sw      t0, 0(a2)             # *nelem = atualizado

    # Sucesso → retorna 1
    li      a0, 1                 # a0 = 1 (sucesso)
    ret

.fail:
    # Buffer vazio → não remove nada
    li      a0, 0                 # a0 = 0 (falha)
    ret
