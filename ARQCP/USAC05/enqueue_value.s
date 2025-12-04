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
    # Loads
    lw      t0, 0(a2)      # t0 = *nelem
    lw      t1, 0(a4)      # t1 = *head
    lw      t2, 0(a3)      # t2 = *tail

    # Verify if buffer is full: *nelem == length ?
    beq     t0, a1, buffer_full

    # If is not full
    addi    t0, t0, 1      # nelem++
    sw      t0, 0(a2)      # *nelem = nelem
    j       write_value

buffer_full:
    # advance tail (remove oldest)
    addi    t2, t2, 1      # tail++
    bne     t2, a1, tail_nowrap
    li      t2, 0          # if tail == length -> tail = 0
tail_nowrap:
    sw      t2, 0(a3)      # *tail = tail

write_value:
    # Write in buffer[head]
    slli    t3, t1, 2      # t3 = head * 4
    add     t3, a0, t3     # t3 = &buffer[head]
    sw      a5, 0(t3)      # buffer[head] = value

    # Avançar head
    addi    t1, t1, 1      # head++
    bne     t1, a1, head_nowrap
    li      t1, 0          # if head == length -> head = 0
head_nowrap:
    sw      t1, 0(a4)      # *head = head

    # Return: 1 if buffer if full now, else ret 0
    beq     t0, a1, ret_one
    li      a0, 0
    ret

ret_one:
    li      a0, 1
    ret
