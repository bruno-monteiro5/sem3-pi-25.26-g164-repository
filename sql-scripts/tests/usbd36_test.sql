SET SERVEROUTPUT ON;

DECLARE
    c SYS_REFCURSOR;

    v_id NUMBER;
    v_type NUMBER;
    v_type_name VARCHAR2(255);
    v_name VARCHAR2(255);
    v_facility NUMBER;
BEGIN
    ----------------------------------------------------------------
    -- SETUP 1: insert válido
    ----------------------------------------------------------------
    c := USBD36_ADD_BUILDING(999001, 2, 'Building Test USBD36 A', 5);

    FETCH c INTO v_id, v_type, v_type_name, v_name, v_facility;
    DBMS_OUTPUT.PUT_LINE('SETUP 1: OK -> id='||v_id||', type='||v_type||' ('||v_type_name||'), name='||v_name||', facility='||v_facility);
    CLOSE c;

    ----------------------------------------------------------------
    -- SETUP 2: outro insert válido
    ----------------------------------------------------------------
    c := USBD36_ADD_BUILDING(999002, 4, 'Building Test USBD36 B', 5);

    FETCH c INTO v_id, v_type, v_type_name, v_name, v_facility;
    DBMS_OUTPUT.PUT_LINE('SETUP 2: OK -> id='||v_id||', type='||v_type||' ('||v_type_name||'), name='||v_name||', facility='||v_facility);
    CLOSE c;

    ----------------------------------------------------------------
    -- TEST 1: duplicate ID
    ----------------------------------------------------------------
    BEGIN
        c := USBD36_ADD_BUILDING(999001, 2, 'Building Test USBD36 C', 5);
        DBMS_OUTPUT.PUT_LINE('TEST 1 (duplicate ID): FAILED');
        CLOSE c;
    EXCEPTION
        WHEN OTHERS THEN
            DBMS_OUTPUT.PUT_LINE('TEST 1 (duplicate ID): OK -> ' || SQLERRM);
    END;

    ----------------------------------------------------------------
    -- TEST 2: facility does not exist
    ----------------------------------------------------------------
    BEGIN
        c := USBD36_ADD_BUILDING(999003, 2, 'Building Test USBD36 D', 999999);
        DBMS_OUTPUT.PUT_LINE('TEST 2 (facility not exists): FAILED');
        CLOSE c;
    EXCEPTION
        WHEN OTHERS THEN
            DBMS_OUTPUT.PUT_LINE('TEST 2 (facility not exists): OK -> ' || SQLERRM);
    END;

    ----------------------------------------------------------------
    -- TEST 3: type does not exist
    ----------------------------------------------------------------
    BEGIN
        c := USBD36_ADD_BUILDING(999004, 999999, 'Building Test USBD36 E', 5);
        DBMS_OUTPUT.PUT_LINE('TEST 3 (type not exists): FAILED');
        CLOSE c;
    EXCEPTION
        WHEN OTHERS THEN
            DBMS_OUTPUT.PUT_LINE('TEST 3 (type not exists): OK -> ' || SQLERRM);
    END;

    ----------------------------------------------------------------
    -- TEST 4: empty name
    ----------------------------------------------------------------
    BEGIN
        c := USBD36_ADD_BUILDING(999005, 2, '   ', 5);
        DBMS_OUTPUT.PUT_LINE('TEST 4 (empty name): FAILED');
        CLOSE c;
    EXCEPTION
        WHEN OTHERS THEN
            DBMS_OUTPUT.PUT_LINE('TEST 4 (empty name): OK -> ' || SQLERRM);
    END;

    ----------------------------------------------------------------
    -- TEST 5: NULL name
    ----------------------------------------------------------------
    BEGIN
        c := USBD36_ADD_BUILDING(999006, 2, NULL, 5);
        DBMS_OUTPUT.PUT_LINE('TEST 5 (NULL name): FAILED');
        CLOSE c;
    EXCEPTION
        WHEN OTHERS THEN
            DBMS_OUTPUT.PUT_LINE('TEST 5 (NULL name): OK -> ' || SQLERRM);
    END;

    ----------------------------------------------------------------
    -- TEST 6: duplicate name in same facility (case + spaces ignored)
    ----------------------------------------------------------------
    BEGIN
        c := USBD36_ADD_BUILDING(
                999007,
                2,
                '  bUiLdInG    tEsT   uSbD36   a  ',
                5
             );
        DBMS_OUTPUT.PUT_LINE('TEST 6 (duplicate name, case/spaces): FAILED');
        CLOSE c;
    EXCEPTION
        WHEN OTHERS THEN
            DBMS_OUTPUT.PUT_LINE('TEST 6 (duplicate name, case/spaces): OK -> ' || SQLERRM);
    END;

    ----------------------------------------------------------------
    -- TEST 7: id = 0
    ----------------------------------------------------------------
    BEGIN
        c := USBD36_ADD_BUILDING(0, 2, 'Building Test USBD36 X', 5);
        DBMS_OUTPUT.PUT_LINE('TEST 7 (id = 0): FAILED');
        CLOSE c;
    EXCEPTION
        WHEN OTHERS THEN
            DBMS_OUTPUT.PUT_LINE('TEST 7 (id = 0): OK -> ' || SQLERRM);
    END;

    ----------------------------------------------------------------
    -- TEST 8: negative id
    ----------------------------------------------------------------
    BEGIN
        c := USBD36_ADD_BUILDING(-1, 2, 'Building Test USBD36 W', 5);
        DBMS_OUTPUT.PUT_LINE('TEST 8 (negative id): FAILED');
        CLOSE c;
    EXCEPTION
        WHEN OTHERS THEN
            DBMS_OUTPUT.PUT_LINE('TEST 8 (negative id): OK -> ' || SQLERRM);
    END;

    ----------------------------------------------------------------
    -- TEST 9: type <= 0
    ----------------------------------------------------------------
    BEGIN
        c := USBD36_ADD_BUILDING(999008, 0, 'Building Test USBD36 Y', 5);
        DBMS_OUTPUT.PUT_LINE('TEST 9 (type <= 0): FAILED');
        CLOSE c;
    EXCEPTION
        WHEN OTHERS THEN
            DBMS_OUTPUT.PUT_LINE('TEST 9 (type <= 0): OK -> ' || SQLERRM);
    END;

    ----------------------------------------------------------------
    -- TEST 10: facility <= 0
    ----------------------------------------------------------------
    BEGIN
        c := USBD36_ADD_BUILDING(999009, 2, 'Building Test USBD36 Z', 0);
        DBMS_OUTPUT.PUT_LINE('TEST 10 (facility <= 0): FAILED');
        CLOSE c;
    EXCEPTION
        WHEN OTHERS THEN
            DBMS_OUTPUT.PUT_LINE('TEST 10 (facility <= 0): OK -> ' || SQLERRM);
    END;

END;
/
ROLLBACK;
