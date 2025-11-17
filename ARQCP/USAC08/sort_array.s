/*
 * int sort_array(int* vec, int length, char order);
 *
 * a0 = vec
 * a1 = length
 * a2 = order  (1 ascendente, 0 descendente - ainda só usamos asc)
 *
 * Retorno:
 *   1 → se conseguir ordenar;
 *   0 → se length <= 0.
 */

    .section .text
    .globl sort_array

sort_array:
    # Se length <= 0 → falha
    blez    a1, fail_sort         # if (length <= 0) goto fail_sort

    # Guardar argumentos em temporários
    mv      t0, a0                # t0 = vec
    mv      t1, a1                # t1 = length
    # order (a2) ainda não é usado nesta versão

    # length - 1 (usado várias vezes no algoritmo)
    addi    t4, t1, -1            # t4 = length - 1

    # i = 0 (índice do ciclo exterior do bubble sort)
    li      t3, 0                 # t3 = i = 0

outer_loop:
    # Condição de paragem do ciclo exterior: i >= length - 1 ?
    bge     t3, t4, done_sort     # if (i >= length-1) → done

    # j = 0 (índice do ciclo interior)
    li      t5, 0                 # t5 = j = 0

inner_loop:
    # limite = (length-1) - i
    sub     a3, t4, t3            # a3 = limite = (length - 1) - i
    # Se j >= limite → terminamos ciclo interior
    bge     t5, a3, next_i        # if (j >= limite) → próximo i

    # -------------------------------------------------
    # Carregar vec[j] em a4 e vec[j+1] em a5
    # -------------------------------------------------
    slli    a3, t5, 2             # a3 = j * 4 (offset em bytes)
    add     a3, t0, a3            # a3 = &vec[j]
    lw      a4, 0(a3)             # a4 = vec[j]

    addi    a3, a3, 4             # a3 = &vec[j+1]
    lw      a5, 0(a3)             # a5 = vec[j+1]

    # -------------------------------------------------
    # Ordem ascendente:
    # se vec[j] > vec[j+1] → trocar
    # condição equivalente: vec[j+1] < vec[j]
    # -------------------------------------------------
    blt     a5, a4, do_swap       # if (a5 < a4) swap

no_swap:
    addi    t5, t5, 1             # j++
    j       inner_loop

do_swap:
    # Neste ponto:
    #  a3 = &vec[j+1]
    #  a4 = vec[j]
    #  a5 = vec[j+1]
    #
    # Queremos:
    #  vec[j]   = a5
    #  vec[j+1] = a4

    sw      a4, 0(a3)             # vec[j+1] = valor original de vec[j]
    addi    a3, a3, -4            # a3 = &vec[j]
    sw      a5, 0(a3)             # vec[j] = valor original de vec[j+1]

    addi    t5, t5, 1             # j++
    j       inner_loop

next_i:
    # Avançar i e repetir bubble sort para próxima passagem
    addi    t3, t3, 1             # i++
    j       outer_loop

done_sort:
    # Sucesso → devolve 1
    li      a0, 1                 # a0 = 1 (sucesso)
    ret

fail_sort:
    # length <= 0 → falha
    li      a0, 0                 # a0 = 0 (falha)
    ret
