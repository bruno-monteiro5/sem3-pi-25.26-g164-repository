-- ============================================================
-- USBD45 - Tests for the creation of a line and all of its included segments
-- ============================================================
BEGIN
    ----------------------------------------------------------------
    -- SETUP: valid line and its segments
    ----------------------------------------------------------------
    LINE_CREATION(25, 'Skibidi biden dop dop', 'PT503933813', 4, 'Y', 300, 3000, 500, 2, 20, 48, 4);
    --                                                                           ^^^^^ comprimento adicionado
    DBMS_OUTPUT.PUT_LINE('SETUP 1: attempt procedure with sucessful line creation');

    LINE_CREATION(26, 'SkoinkleKoinkle', 'PT509498700', 4, 'N', 400, 5000, 450, 2, 8, 43, 39);
    DBMS_OUTPUT.PUT_LINE('SETUP 2: attempt procedure with sucessful line creation');

    ----------------------------------------------------------------
    -- TEST 1: duplicate line ID (should fail)
    ----------------------------------------------------------------
BEGIN
        LINE_CREATION(25, 'Skuloong', 'PT505144939', 4, 'Y', 300, 3000, 500, 2, 20, 48, 60);
        DBMS_OUTPUT.PUT_LINE('TEST 1: attempt procedure with duplicate line id (should fail)');
EXCEPTION
        WHEN OTHERS THEN DBMS_OUTPUT.PUT_LINE('TEST 1: failed as expected - ' || SQLERRM);
END;

    ----------------------------------------------------------------
    -- TEST 2: invalid sid ID (should fail)
    ----------------------------------------------------------------
BEGIN
        LINE_CREATION(29, 'SkULLMANE', 'PT505144939', 4, 'N', 400, 5000, 450, 2, 8, 43, 10);
        DBMS_OUTPUT.PUT_LINE('TEST 2: attempt procedure with duplicate segment id (should fail)');
EXCEPTION
        WHEN OTHERS THEN DBMS_OUTPUT.PUT_LINE('TEST 2: failed as expected - ' || SQLERRM);
END;

    ----------------------------------------------------------------
    -- TEST 3: invalid owner (should fail)
    ----------------------------------------------------------------
BEGIN
        LINE_CREATION(30, 'SKEELMANR', 'PT023002010210202122020', 4, 'Y', 500, 9000, 50000, 2, 8, 43, 90);
        DBMS_OUTPUT.PUT_LINE('TEST 3: attempt procedure with invalid owner (should fail)');
EXCEPTION
        WHEN OTHERS THEN DBMS_OUTPUT.PUT_LINE('TEST 3: failed as expected - ' || SQLERRM);
END;

    ----------------------------------------------------------------
    -- TEST 4: invalid electricity value (should fail)
    ----------------------------------------------------------------
BEGIN
        LINE_CREATION(30, 'SKEELEREEREERE', 'PT505144939', 4, 'E', 300, 5000, 50000, 2, 43, 8, 80);
        DBMS_OUTPUT.PUT_LINE('TEST 4: attempt procedure with invalid electricity value (should fail)');
EXCEPTION
        WHEN OTHERS THEN DBMS_OUTPUT.PUT_LINE('TEST 4: failed as expected - ' || SQLERRM);
END;

    ----------------------------------------------------------------
    -- TEST 5: invalid endpoint A (should fail)
    ----------------------------------------------------------------
BEGIN
        LINE_CREATION(31, 'VALKAN', 'PT503933813', 4, 'Y', 300, 3000, 50000, 2, 29020, 48, 84);
        DBMS_OUTPUT.PUT_LINE('TEST 5: attempt procedure with invalid endpoint A (should fail)');
EXCEPTION
        WHEN OTHERS THEN DBMS_OUTPUT.PUT_LINE('TEST 5: failed as expected - ' || SQLERRM);
END;

    ----------------------------------------------------------------
    -- TEST 6: invalid endpoint B (should fail)
    ----------------------------------------------------------------
BEGIN
        LINE_CREATION(32, 'VUKKUNUR', 'PT503933813', 4, 'Y', 300, 3000, 50000, 2, 43, 434242323, 84);
        DBMS_OUTPUT.PUT_LINE('TEST 6: attempt procedure with invalid endpoint B (should fail)');
EXCEPTION
        WHEN OTHERS THEN DBMS_OUTPUT.PUT_LINE('TEST 6: failed as expected - ' || SQLERRM);
END;

END;
/

ROLLBACK;