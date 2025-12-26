DECLARE
    modelCursor SYS_REFCURSOR;

    insertedModelId NUMBER;
    insertedModelName VARCHAR2(255);
    insertedModelMake VARCHAR2(255);
    insertedVoltage NUMBER;
    insertedFrequency NUMBER;
    insertedGaugeId NUMBER;
BEGIN
    -- insert valido
    modelCursor := ADD_ELECTRIC_LOCOMOTIVE_MODEL(99001,'Model Test USBD43','TestMaker',2,'Bo-Bo',5000,19.200,3.000,4.300, 200, 300, 25,50,2);

    FETCH modelCursor
        INTO insertedModelId, insertedModelName, insertedModelMake, insertedVoltage, insertedFrequency, insertedGaugeId;

    DBMS_OUTPUT.PUT_LINE('TEST OK: model inserted');
    CLOSE modelCursor;

    -- id duplicado
    BEGIN
        modelCursor := ADD_ELECTRIC_LOCOMOTIVE_MODEL(99001,'Dup','Dup',2,'Bo-Bo',5000,19.200,3.000,4.300,200,300,25,50, 2);

        DBMS_OUTPUT.PUT_LINE('TEST ERROR: should fail (duplicate id)');
        CLOSE modelCursor;
    EXCEPTION
        WHEN OTHERS THEN
            DBMS_OUTPUT.PUT_LINE('TEST OK: duplicate id check works');
    END;

    -- gauge invalido
    BEGIN
        modelCursor := ADD_ELECTRIC_LOCOMOTIVE_MODEL(99002,'Gauge Fail','TestMaker', 2,'Bo-Bo',5000,19.200,3.000,4.300,200,300,25,50,999999);

        DBMS_OUTPUT.PUT_LINE('TEST  ERROR: should fail (invalid gauge)');
        CLOSE modelCursor;
    EXCEPTION
        WHEN OTHERS THEN
            DBMS_OUTPUT.PUT_LINE('TEST  OK: gauge check works');
    END;

    -- empty name
    BEGIN
        modelCursor := ADD_ELECTRIC_LOCOMOTIVE_MODEL(99003,'   ','TestMaker',2,'Bo-Bo',5000,19.200,3.000,4.300,200,300,25,50,2);

        DBMS_OUTPUT.PUT_LINE('TEST ERROR: should fail (empty name)');
        CLOSE modelCursor;
    EXCEPTION
        WHEN OTHERS THEN
            DBMS_OUTPUT.PUT_LINE('TEST OK: name check works');
    END;

END;
/
ROLLBACK;
