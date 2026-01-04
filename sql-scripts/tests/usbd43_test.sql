BEGIN
    -- insert válido
    ADD_ELECTRIC_LOCOMOTIVE_MODEL(
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
    DBMS_OUTPUT.PUT_LINE('TEST 1 OK: electric locomotive model inserted');

    -- id duplicado
    ADD_ELECTRIC_LOCOMOTIVE_MODEL(
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
    DBMS_OUTPUT.PUT_LINE('TEST ERROR: should fail (duplicate id)');
EXCEPTION
    WHEN OTHERS THEN
        DBMS_OUTPUT.PUT_LINE('TEST OK: duplicate id validation works');
END;
/

BEGIN
    -- gauge inválido
    ADD_ELECTRIC_LOCOMOTIVE_MODEL(
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
    DBMS_OUTPUT.PUT_LINE('TEST ERROR: should fail (invalid gauge)');
EXCEPTION
    WHEN OTHERS THEN
        DBMS_OUTPUT.PUT_LINE('TEST OK: gauge validation works');
END;
/

BEGIN
    -- nome vazio
    ADD_ELECTRIC_LOCOMOTIVE_MODEL(
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
    DBMS_OUTPUT.PUT_LINE('TEST ERROR: should fail (empty name)');
EXCEPTION
    WHEN OTHERS THEN
        DBMS_OUTPUT.PUT_LINE('TEST OK: name validation works');
END;
/

ROLLBACK;
