DECLARE
    buildingCursor SYS_REFCURSOR;

    insertedBuildingId       NUMBER;
    insertedBuildingTypeId   NUMBER;
    insertedBuildingName     VARCHAR2(255);
    insertedFacilityId       NUMBER;
BEGIN
    -- insert válido
    buildingCursor := ADD_BUILDING(999001, 2, 'Building Test USBD36 A', 5);

    FETCH buildingCursor
        INTO insertedBuildingId, insertedBuildingTypeId, insertedBuildingName, insertedFacilityId;

    DBMS_OUTPUT.PUT_LINE('TEST OK: building inserted');

    CLOSE buildingCursor;

    -- facility nao existe
    BEGIN
        buildingCursor := ADD_BUILDING(999002, 2, 'Building Test USBD36 B', 999999);
        DBMS_OUTPUT.PUT_LINE('TEST ERROR: should not insert (facility)');
        CLOSE buildingCursor;
    EXCEPTION
        WHEN OTHERS THEN
            DBMS_OUTPUT.PUT_LINE('TEST OK: facility check works');
    END;

    -- building type nao existe
    BEGIN
        buildingCursor := ADD_BUILDING(999003, 999999, 'Building Test USBD36 C', 5);
        DBMS_OUTPUT.PUT_LINE('TEST ERROR: should not insert (type)');
        CLOSE buildingCursor;
    EXCEPTION
        WHEN OTHERS THEN
            DBMS_OUTPUT.PUT_LINE('TEST OK: type check works');
    END;

    -- empty name
    BEGIN
        buildingCursor := ADD_BUILDING(999004, 2, '   ', 5);
        DBMS_OUTPUT.PUT_LINE('TEST ERROR: should not insert (empty name)');
        CLOSE buildingCursor;
    EXCEPTION
        WHEN OTHERS THEN
            DBMS_OUTPUT.PUT_LINE('TEST OK: name check works');
    END;

    -- NULL name
    BEGIN
        buildingCursor := ADD_BUILDING(999005, 2, NULL, 5);
        DBMS_OUTPUT.PUT_LINE('TEST  ERROR: should not insert (null name)');
        CLOSE buildingCursor;
    EXCEPTION
        WHEN OTHERS THEN
            DBMS_OUTPUT.PUT_LINE('TEST OK: name check works');
    END;

END;
/
ROLLBACK;
