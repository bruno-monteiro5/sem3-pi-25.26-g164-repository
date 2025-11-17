/*
 * int sort_array(int* vec, int length, char order);
 *
 * a0 = vec
 * a1 = length
 * a2 = order  (1 ascendente, 0 descendente)
 *
 * Retorno:
 *   1 → se length > 0 (por enquanto não ordena)
 *   0 → se length <= 0
 */

    .section .text
    .globl sort_array

sort_array:
    # Validar parâmetro length
    # Se length <= 0 → falha
    blez    a1, fail_sort         # if (length <= 0) goto fail_sort

    # (Por enquanto ainda não ordenamos o array)
    # Consideramos que a operação "teve sucesso"
    li      a0, 1                 # a0 = 1 (sucesso)
    ret

fail_sort:
    li      a0, 0                 # a0 = 0 (falha)
    ret
