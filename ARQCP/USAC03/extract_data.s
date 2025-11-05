    .text
    .globl extract_data
# int extract_data(char* str, char* token, char* unit, int* value)
#   a0 = str   (input)
#   a1 = token (input)
#   a2 = unit  (output buffer)
#   a3 = value (output pointer)
# Return in a0: 1 on success; 0 on failure

extract_data:
    # Argument validation
    beqz a0, ERROR
    beqz a1, ERROR
    beqz a2, ERROR
    beqz a3, ERROR

    # Local aliases
    mv   t0, a0       # t0 := cursor into source string (str)
    mv   t1, a1       # t1 := base of token pattern
    mv   t2, a2       # t2 := destination buffer for unit
    mv   t3, a3       # t3 := &value (output integer pointer)

    # Initialize outputs to a safe failure state: unit="", *value=0
    sb   zero, 0(t2)
    sw   zero, 0(t3)


# Main scanning loop over segments separated by '#'
NEXT_SEGMENT:
    lbu  t4, 0(t0)         # peek current char
    beqz t4, NOT_FOUND     # end of string: token not found

# Skip leading spaces between segments to tolerate " ...#  NEXT"
SKIP_SPACES:
    lbu  t4, 0(t0)
    beqz t4, NOT_FOUND
    li   t5, ' '
    bne  t4, t5, CHECK_TOKEN
    addi t0, t0, 1
    j    SKIP_SPACES


# Token prefix match: strncmp(segment_start, token) == 0
# t6 walks the string segment; a4 walks the token
# Success when *a4 == '\0' (token fully matched)
CHECK_TOKEN:
    mv   t6, t0            # t6 := scan ptr at segment start
    mv   a4, t1            # a4 := token ptr
COMPARE_LOOP:
    lbu  t4, 0(t6)
    lbu  t5, 0(a4)
    beqz t5, TOKEN_MATCHED     # token fully matched
    beqz t4, NEXT_HASH         # hit end of str before token ends → next segment
    bne  t4, t5, NEXT_HASH     # mismatch → skip to next segment
    addi t6, t6, 1
    addi a4, a4, 1
    j    COMPARE_LOOP

# TOKEN matched. a4 will scan the remainder of this segment.
# We now search for literal "&unit:" and then copy its payload.
TOKEN_MATCHED:
    mv   a4, t6            # a4 := position just after token

# Find "&unit:" by naive literal matching ----
    la   a5, UNIT_LABEL    # a5 := pattern "&unit:" (NUL-terminated)
FIND_UNIT:
    lbu  t4, 0(a4)         # current char in segment
    beqz t4, NOT_FOUND     # malformed segment (no &unit:)
    lbu  t5, 0(a5)         # current char in pattern
    beqz t5, FOUND_UNIT    # pattern fully matched: a4 points to first unit char
    bne  t4, t5, INC_UNIT_SEARCH
    addi a4, a4, 1
    addi a5, a5, 1
    j    FIND_UNIT
INC_UNIT_SEARCH:
    addi a4, a4, 1         # shift text by one; restart pattern
    la   a5, UNIT_LABEL
    j    FIND_UNIT

# a4 is positioned at the first byte of the unit payload (no extra addi!)
FOUND_UNIT:
    mv   a6, t2            # a6 := write cursor into unit buffer

# Copy unit until '&' or NUL; then NUL-terminate ----
COPY_UNIT:
    lbu  t4, 0(a4)
    beqz t4, UNIT_DONE
    li   t5, '&'
    beq  t4, t5, UNIT_DONE
    sb   t4, 0(a6)
    addi a6, a6, 1
    addi a4, a4, 1
    j    COPY_UNIT
UNIT_DONE:
    sb   zero, 0(a6)


# Now find literal "&value:" and parse decimal digits into *value.
    la   a5, VALUE_LABEL   # a5 := pattern "&value:"
FIND_VALUE:
    lbu  t4, 0(a4)
    beqz t4, NOT_FOUND     # malformed segment (no &value:)
    lbu  t5, 0(a5)
    beqz t5, FOUND_VALUE   # pattern fully matched: a4 at first digit (if any)
    bne  t4, t5, INC_VAL_SEARCH
    addi a4, a4, 1
    addi a5, a5, 1
    j    FIND_VALUE
INC_VAL_SEARCH:
    addi a4, a4, 1
    la   a5, VALUE_LABEL
    j    FIND_VALUE

# a4 points at first digit (if present). Accumulate base-10 value in a7.
FOUND_VALUE:
    li   a7, 0             # a7 := accumulator (unsigned)
PARSE_DIGIT:
    lbu  t4, 0(a4)
    beqz t4, DONE_OK       # allow zero or more digits; stop on NUL
    li   t5, '0'
    blt  t4, t5, DONE_OK   # stop if < '0'
    li   t5, '9'
    bgt  t4, t5, DONE_OK   # stop if > '9'
    addi t4, t4, -48       # t4 := (char - '0')

    # a7 = a7 * 10  (RV32I: implement via (a7<<3) + (a7<<1))
    slli t5, a7, 3
    slli t6, a7, 1
    add  a7, t5, t6
    add  a7, a7, t4

    addi a4, a4, 1
    j    PARSE_DIGIT



# Success path: write *value, return 1
DONE_OK:
    sw   a7, 0(t3)
    li   a0, 1
    ret




# Skip to next segment: advance t0 until '#' or NUL; if '#', step over it.
NEXT_HASH:
    lbu  t4, 0(t0)
    beqz t4, NOT_FOUND
    li   t5, '#'
    beq  t4, t5, AFTER_HASH
    addi t0, t0, 1
    j    NEXT_HASH
AFTER_HASH:
    addi t0, t0, 1
    j    NEXT_SEGMENT



# NOT_FOUND and ERROR share the same epilogue block.
NOT_FOUND:
ERROR:
    sb   zero, 0(t2)
    sw   zero, 0(t3)
    li   a0, 0
    ret




    .data
# Byte strings with implicit trailing NUL (GAS syntax).
UNIT_LABEL:   .asciz "&unit:"
VALUE_LABEL:  .asciz "&value:"
