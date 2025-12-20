SET SERVEROUTPUT ON;

DECLARE
v_new_facility_id NUMBER := 99;
BEGIN
    DBMS_OUTPUT.PUT_LINE('=== Testing USBD35_ADD_FACILITY ===');
    DBMS_OUTPUT.PUT_LINE('');

BEGIN
        DBMS_OUTPUT.PUT_LINE('Test 1: Adding valid facility...');
        USBD35_ADD_FACILITY(
            p_id   => v_new_facility_id,
            p_name => 'Test Facility Porto',
            p_type => 1  -- 'Station' (existe no BuildingType)
        );
        DBMS_OUTPUT.PUT_LINE('✓ Success: Facility added with id=' || v_new_facility_id);
        DBMS_OUTPUT.PUT_LINE('');
EXCEPTION
        WHEN OTHERS THEN
            DBMS_OUTPUT.PUT_LINE('✗ Error: ' || SQLERRM);
END;

BEGIN
        DBMS_OUTPUT.PUT_LINE('Test 2: Trying to add facility with duplicate id...');
        USBD35_ADD_FACILITY(
            p_id   => v_new_facility_id,
            p_name => 'Another Facility',
            p_type => 2
        );
        DBMS_OUTPUT.PUT_LINE('✗ Error: Should have raised duplicate id error!');
EXCEPTION
        WHEN OTHERS THEN
            DBMS_OUTPUT.PUT_LINE('✓ Expected error: ' || SQLERRM);
END;
    DBMS_OUTPUT.PUT_LINE('');

BEGIN
        DBMS_OUTPUT.PUT_LINE('Test 3: Trying to add facility with empty name...');
        USBD35_ADD_FACILITY(
            p_id   => 100,
            p_name => '   ',
            p_type => 1
        );
        DBMS_OUTPUT.PUT_LINE('✗ Error: Should have raised empty name error!');
EXCEPTION
        WHEN OTHERS THEN
            DBMS_OUTPUT.PUT_LINE('✓ Expected error: ' || SQLERRM);
END;
    DBMS_OUTPUT.PUT_LINE('');

BEGIN
        DBMS_OUTPUT.PUT_LINE('Test 4: Trying to add facility with invalid type...');
        USBD35_ADD_FACILITY(
            p_id   => 101,
            p_name => 'Test Facility Invalid Type',
            p_type => 9999  -- não existe
        );
        DBMS_OUTPUT.PUT_LINE('✗ Error: Should have raised invalid type error!');
EXCEPTION
        WHEN OTHERS THEN
            DBMS_OUTPUT.PUT_LINE('✓ Expected error: ' || SQLERRM);
END;
    DBMS_OUTPUT.PUT_LINE('');

DELETE FROM Facility WHERE id = v_new_facility_id;
DELETE FROM Metafacility WHERE id = v_new_facility_id;
COMMIT;
DBMS_OUTPUT.PUT_LINE('Test data cleaned up.');
END;
/