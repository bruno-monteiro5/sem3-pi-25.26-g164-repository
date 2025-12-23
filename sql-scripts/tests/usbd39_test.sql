-- ============================================================
-- USBD39 - Tests for the addition of a locomotive to a train
-- ============================================================

BEGIN
    ----------------------------------------------------------------
    -- SETUP: valid train and locomotive id (must succeed)
    ----------------------------------------------------------------

    LOCOMOTIVE_ASSOCIATION(5421, 5034, 3);
    DBMS_OUTPUT.PUT_LINE('SETUP 1: attempt procedure with sucessful locomotive and train ids');


    LOCOMOTIVE_ASSOCIATION(5435, 5630, 4);
    DBMS_OUTPUT.PUT_LINE('SETUP 1: attempt procedure with sucessful locomotive and train ids');

    ----------------------------------------------------------------
    -- TEST 1: duplicate train ID (should fail)
    ----------------------------------------------------------------
    BEGIN
        LOCOMOTIVE_ASSOCIATION(5421, 5036, 1);
        DBMS_OUTPUT.PUT_LINE('TEST 1: attempt procedure with duplicate train id(should fail)');

    EXCEPTION
        WHEN OTHERS THEN DBMS_OUTPUT.PUT_LINE('TEST 1: did not fail as expected');
    END;

    ----------------------------------------------------------------
    -- TEST 2: duplicate locomotive ID (should fail)
    ----------------------------------------------------------------

    BEGIN
        LOCOMOTIVE_ASSOCIATION(5437, 5630, 2);
        DBMS_OUTPUT.PUT_LINE('TEST 2: attempt procedure with duplicate locomotive id(should fail)');

    EXCEPTION
        WHEN OTHERS THEN DBMS_OUTPUT.PUT_LINE('TEST 2: did not fail as expected');
    END;

END;
/

ROLLBACK;
