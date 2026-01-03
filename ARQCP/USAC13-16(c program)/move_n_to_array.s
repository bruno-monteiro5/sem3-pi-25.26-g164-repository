/*
 * int move_n_to_array(int* buffer, int length,
 *                     int* nelem, int* tail, int* head,
 *                     int n, int* array);
 *
 * a0 = buffer
 * a1 = length
 * a2 = nelem
 * a3 = tail
 * a4 = head   (não usado aqui)
 * a5 = n
 * a6 = array
 */

    .section .text
    .globl move_n_to_array

move_n_to_array:
    # Carregar *nelem e *tail
    lw      t0, 0(a2)             # t0 = *nelem
    lw      t1, 0(a3)             # t1 = *tail

    # Guardar n e ponteiro para array em temporários
    mv      t5, a5                # t5 = n
    mv      t6, a6                # t6 = array

    # Verificar se há elementos suficientes: *nelem < n ?
    blt     t0, t5, .fail         # se *nelem < n -> não há n elem. → retorna 0

    # i = 0 (contador de quantos já movemos)
    li      t2, 0                 # t2 = i = 0

.loop:
    # Se i == n, já movemos todos → sair do ciclo
    beq     t2, t5, .done         # if (i == n) goto done

    # --- Ler elemento mais antigo: buffer[tail] ---

    # endereço de buffer[tail]
    slli    t3, t1, 2             # t3 = tail * 4
    add     t3, a0, t3            # t3 = &buffer[tail]

    lw      t4, 0(t3)             # t4 = buffer[tail]

    # --- Escrever em array[i] ---

    slli    t3, t2, 2             # t3 = i * 4
    add     t3, t6, t3            # t3 = &array[i]
    sw      t4, 0(t3)             # array[i] = buffer[tail]

    # Avançar tail: tail = (tail + 1) % length
    addi    t1, t1, 1             # tail++
    bne     t1, a1, .no_wrap_tail # se tail != length, não faz wrap
    li      t1, 0                 # senão, tail = 0
.no_wrap_tail:

    # i++
    addi    t2, t2, 1             # i++
    j       .loop                 # repetir

.done:
    # Atualizar *nelem: *nelem -= n
    sub     t0, t0, t5            # t0 = *nelem - n
    sw      t0, 0(a2)             # *nelem = t0

    # Atualizar *tail
    sw      t1, 0(a3)             # *tail = tail

    # Sucesso → retorna 1
    li      a0, 1                 # a0 = 1 (sucesso)
    ret

.fail:
    # Não há elementos suficientes para mover
    li      a0, 0                 # a0 = 0 (falha)
    ret
