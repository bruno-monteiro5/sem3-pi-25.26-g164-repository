/*
 * int sort_array(int* vec, int length, char order);
 *
 * a0 = vec       (pointer to integer array)
 * a1 = length    (array size)
 * a2 = order     (1 for ascending, 0 for descending)
 *
 * Return value:
 *   1 → success
 *   0 → failure (length <= 0)
 */

    .section .text
    .globl sort_array

sort_array:
    # If length <= 0 → nothing to sort → failure
    blez    a1, fail_sort         # if (length <= 0) goto fail_sort

    # Save arguments into temporaries
    mv      t0, a0                # t0 = vec
    mv      t1, a1                # t1 = length
    mv      t2, a2                # t2 = order (1 asc, 0 desc)

    # Precompute (length - 1), used many times in bubble sort
    addi    t4, t1, -1            # t4 = length - 1

    # i = 0 (outer bubble sort loop index)
    li      t3, 0                 # t3 = i = 0

outer_loop:
    # If i >= length - 1 → sorting finished
    bge     t3, t4, done_sort     # if (i >= length-1) goto done_sort

    # j = 0 (inner bubble sort loop index)
    li      t5, 0                 # t5 = j = 0

inner_loop:
    # Compute limit = (length - 1) - i
    sub     a3, t4, t3            # a3 = limit = (length - 1) - i

    # If j >= limit → go to next i
    bge     t5, a3, next_i        # if (j >= limit) goto next_i

    # -----------------------------------------------------
    # Load vec[j] into a4 and vec[j+1] into a5
    # -----------------------------------------------------
    slli    a3, t5, 2             # a3 = j * 4 (offset in bytes)
    add     a3, t0, a3            # a3 = &vec[j]
    lw      a4, 0(a3)             # a4 = vec[j]

    addi    a3, a3, 4             # a3 = &vec[j+1]
    lw      a5, 0(a3)             # a5 = vec[j+1]

    # -----------------------------------------------------
    # Decide whether to swap based on sort order
    # -----------------------------------------------------
    beqz    t2, desc_check        # if (order == 0) → descending mode

asc_check:
    # ASCENDING ORDER:
    # swap if vec[j] > vec[j+1]
    # equivalent to: if (a5 < a4)
    blt     a5, a4, do_swap       # if (a5 < a4) swap
    j       no_swap

desc_check:
    # DESCENDING ORDER:
    # swap if vec[j] < vec[j+1]
    blt     a4, a5, do_swap       # if (a4 < a5) swap

no_swap:
    # Advance j and continue inner loop
    addi    t5, t5, 1             # j++
    j       inner_loop

do_swap:
    # Current state:
    #   a3 = &vec[j+1]
    #   a4 = vec[j]
    #   a5 = vec[j+1]
    #
    # Swap so that:
    #   vec[j]   = a5
    #   vec[j+1] = a4

    sw      a4, 0(a3)             # vec[j+1] = original vec[j]
    addi    a3, a3, -4            # a3 = &vec[j]
    sw      a5, 0(a3)             # vec[j] = original vec[j+1]

    addi    t5, t5, 1             # j++
    j       inner_loop

next_i:
    # Advance i and repeat outer loop
    addi    t3, t3, 1             # i++
    j       outer_loop

done_sort:
    # Sorting successfully completed
    li      a0, 1                 # return 1 (success)
    ret

fail_sort:
    # Invalid length → cannot sort
    li      a0, 0                 # return 0 (failure)
    ret
