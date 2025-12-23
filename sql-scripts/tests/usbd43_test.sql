DECLARE
    c SYS_REFCURSOR;

    v_id NUMBER;
    v_name VARCHAR2(255);
    v_make VARCHAR2(255);
    v_voltage NUMBER;
    v_frequency NUMBER;
    v_gauge NUMBER;
BEGIN
    -- válido (deve passar)
    c := USBD43_ADD_ELECTRIC_LOCOMOTIVE_MODEL(
            99001,
            'Model Test USBD43',
            'TestMaker',
            2,
            'Bo-Bo',
            5000,
            19.200,
            3.000,
            4.300,
            200,
            300,
            25,
            50,
            2
         );

    FETCH c INTO v_id, v_name, v_make, v_voltage, v_frequency, v_gauge;
    DBMS_OUTPUT.PUT_LINE('TEST 1 (valid): OK -> id='||v_id||', '||v_name||', '||v_make||', '||v_voltage||'kV '||v_frequency||'Hz, gauge='||v_gauge);
    CLOSE c;

    -- id repetido (deve falhar)
    BEGIN
        c := USBD43_ADD_ELECTRIC_LOCOMOTIVE_MODEL(
                99001,
                'Dup',
                'Dup',
                2,
                'Bo-Bo',
                5000,
                19.200,
                3.000,
                4.300,
                200,
                300,
                25,
                50,
                2
             );
        DBMS_OUTPUT.PUT_LINE('TEST 2 (duplicate id): FAILED');
        CLOSE c;
    EXCEPTION
        WHEN OTHERS THEN
            DBMS_OUTPUT.PUT_LINE('TEST 2 (duplicate id): OK -> ' || SQLERRM);
    END;

    -- gauge não existe (deve falhar)
    BEGIN
        c := USBD43_ADD_ELECTRIC_LOCOMOTIVE_MODEL(
                99002,
                'Gauge Fail',
                'TestMaker',
                2,
                'Bo-Bo',
                5000,
                19.200,
                3.000,
                4.300,
                200,
                300,
                25,
                50,
                999999
             );
        DBMS_OUTPUT.PUT_LINE('TEST 3 (invalid gauge): FAILED');
        CLOSE c;
    EXCEPTION
        WHEN OTHERS THEN
            DBMS_OUTPUT.PUT_LINE('TEST 3 (invalid gauge): OK -> ' || SQLERRM);
    END;

    -- name só espaços (deve falhar)
    BEGIN
        c := USBD43_ADD_ELECTRIC_LOCOMOTIVE_MODEL(
                99003,
                '   ',
                'TestMaker',
                2,
                'Bo-Bo',
                5000,
                19.200,
                3.000,
                4.300,
                200,
                300,
                25,
                50,
                2
             );
        DBMS_OUTPUT.PUT_LINE('TEST 4 (empty name): FAILED');
        CLOSE c;
    EXCEPTION
        WHEN OTHERS THEN
            DBMS_OUTPUT.PUT_LINE('TEST 4 (empty name): OK -> ' || SQLERRM);
    END;

END;
/
ROLLBACK;
