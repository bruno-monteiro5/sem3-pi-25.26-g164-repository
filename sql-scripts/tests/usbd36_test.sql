DECLARE
    c SYS_REFCURSOR;

    v_id       NUMBER;
    v_type     NUMBER;
    v_name     VARCHAR2(255);
    v_facility NUMBER;
BEGIN
    -- insert válido (deve passar)
    c := USBD36_ADD_BUILDING(999001, 2, 'Building Test USBD36 A', 5);

    FETCH c INTO v_id, v_type, v_name, v_facility;
    DBMS_OUTPUT.PUT_LINE('TEST 1 (valid): OK -> id='||v_id||', type='||v_type||', name='||v_name||', facility='||v_facility);
    CLOSE c;

    -- facility does not exist (deve falhar)
    BEGIN
        c := USBD36_ADD_BUILDING(999002, 2, 'Building Test USBD36 B', 999999);
        DBMS_OUTPUT.PUT_LINE('TEST 2 (facility not exists): FAILED');
        CLOSE c;
    EXCEPTION
        WHEN OTHERS THEN
            DBMS_OUTPUT.PUT_LINE('TEST 2 (facility not exists): OK -> ' || SQLERRM);
    END;

    -- type does not exist (deve falhar)
    BEGIN
        c := USBD36_ADD_BUILDING(999003, 999999, 'Building Test USBD36 C', 5);
        DBMS_OUTPUT.PUT_LINE('TEST 3 (type not exists): FAILED');
        CLOSE c;
    EXCEPTION
        WHEN OTHERS THEN
            DBMS_OUTPUT.PUT_LINE('TEST 3 (type not exists): OK -> ' || SQLERRM);
    END;

    -- empty name (deve falhar)
    BEGIN
        c := USBD36_ADD_BUILDING(999004, 2, '   ', 5);
        DBMS_OUTPUT.PUT_LINE('TEST 4 (empty name): FAILED');
        CLOSE c;
    EXCEPTION
        WHEN OTHERS THEN
            DBMS_OUTPUT.PUT_LINE('TEST 4 (empty name): OK -> ' || SQLERRM);
    END;

    -- NULL name (deve falhar)
    BEGIN
        c := USBD36_ADD_BUILDING(999005, 2, NULL, 5);
        DBMS_OUTPUT.PUT_LINE('TEST 5 (NULL name): FAILED');
        CLOSE c;
    EXCEPTION
        WHEN OTHERS THEN
            DBMS_OUTPUT.PUT_LINE('TEST 5 (NULL name): OK -> ' || SQLERRM);
    END;

END;
/
ROLLBACK;
