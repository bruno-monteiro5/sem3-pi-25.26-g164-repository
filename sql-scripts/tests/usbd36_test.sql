BEGIN
    -- insert válido
    ADD_BUILDING(999001, 2, 'Building Test USBD36 A', 5);
    DBMS_OUTPUT.PUT_LINE('TEST OK: building inserted');

    -- facility não existe
    ADD_BUILDING(999002, 2, 'Building Test USBD36 B', 999999);
    DBMS_OUTPUT.PUT_LINE('TEST ERROR: should not insert (facility)');
EXCEPTION
    WHEN OTHERS THEN
        DBMS_OUTPUT.PUT_LINE('TEST OK: facility validation works');
END;
/

BEGIN
    -- building type não existe
    ADD_BUILDING(999003, 999999, 'Building Test USBD36 C', 5);
    DBMS_OUTPUT.PUT_LINE('TEST ERROR: should not insert (type)');
EXCEPTION
    WHEN OTHERS THEN
        DBMS_OUTPUT.PUT_LINE('TEST OK: building type validation works');
END;
/

BEGIN
    -- nome vazio
    ADD_BUILDING(999004, 2, '   ', 5);
    DBMS_OUTPUT.PUT_LINE('TEST ERROR: should not insert (empty name)');
EXCEPTION
    WHEN OTHERS THEN
        DBMS_OUTPUT.PUT_LINE('TEST OK: name validation works');
END;
/

BEGIN
    -- nome NULL
    ADD_BUILDING(999005, 2, NULL, 5);
    DBMS_OUTPUT.PUT_LINE('TEST ERROR: should not insert (null name)');
EXCEPTION
    WHEN OTHERS THEN
        DBMS_OUTPUT.PUT_LINE('TEST OK: name validation works');
END;
/

ROLLBACK;
