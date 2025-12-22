SET SERVEROUTPUT ON;

DECLARE
v_result NUMBER;
BEGIN
    DBMS_OUTPUT.PUT_LINE('=== Testing USBD44_ADD_SEGMENT ===');
    DBMS_OUTPUT.PUT_LINE('');

BEGIN
        DBMS_OUTPUT.PUT_LINE('Test 1: SINGLE track without siding');
        v_result := USBD44_ADD_SEGMENT(
            p_segment_id     => 9001,
            p_is_electrified => 'Y',
            p_speed_limit    => 120,
            p_max_weight     => 8000,
            p_length         => 5000,
            p_line_id        => 1,
            p_track_type     => 'SINGLE'
        );
COMMIT;
DBMS_OUTPUT.PUT_LINE('Success: segment id = ' || v_result);
        DBMS_OUTPUT.PUT_LINE('');
EXCEPTION
        WHEN OTHERS THEN
            ROLLBACK;
            DBMS_OUTPUT.PUT_LINE('Error: ' || SQLERRM);
            DBMS_OUTPUT.PUT_LINE('');
END;

BEGIN
        DBMS_OUTPUT.PUT_LINE('Test 2: SINGLE track with siding');
        v_result := USBD44_ADD_SEGMENT(
            p_segment_id      => 9002,
            p_is_electrified  => 'Y',
            p_speed_limit     => 100,
            p_max_weight      => 8000,
            p_length          => 8000,
            p_line_id         => 1,
            p_track_type      => 'SINGLE',
            p_siding_position => 4000,
            p_siding_length   => 500
        );
COMMIT;
DBMS_OUTPUT.PUT_LINE('Success: segment id = ' || v_result);
        DBMS_OUTPUT.PUT_LINE('');
EXCEPTION
        WHEN OTHERS THEN
            ROLLBACK;
            DBMS_OUTPUT.PUT_LINE('Error: ' || SQLERRM);
            DBMS_OUTPUT.PUT_LINE('');
END;

BEGIN
        DBMS_OUTPUT.PUT_LINE('Test 3: MULTIPLE tracks');
        v_result := USBD44_ADD_SEGMENT(
            p_segment_id     => 9003,
            p_is_electrified => 'Y',
            p_speed_limit    => 140,
            p_max_weight     => 8000,
            p_length         => 10000,
            p_line_id        => 2,
            p_track_type     => 'MULTIPLE',
            p_number_tracks  => 2
        );
COMMIT;
DBMS_OUTPUT.PUT_LINE('Success: segment id = ' || v_result);
        DBMS_OUTPUT.PUT_LINE('');
EXCEPTION
        WHEN OTHERS THEN
            ROLLBACK;
            DBMS_OUTPUT.PUT_LINE('Error: ' || SQLERRM);
            DBMS_OUTPUT.PUT_LINE('');
END;

DELETE FROM Siding WHERE segment IN (9001, 9002, 9003);
DELETE FROM Single_Track WHERE segment IN (9001, 9002, 9003);
DELETE FROM Multiple_Tracks WHERE segment IN (9001, 9002, 9003);
DELETE FROM Line_Segment WHERE id IN (9001, 9002, 9003);
COMMIT;

DBMS_OUTPUT.PUT_LINE('Tests completed and cleaned up.');
END;
/
```

