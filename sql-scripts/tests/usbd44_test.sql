SET SERVEROUTPUT ON;

DECLARE
BEGIN
    DBMS_OUTPUT.PUT_LINE('=== Testing USBD44_ADD_SEGMENT (procedure) ===');
    DBMS_OUTPUT.PUT_LINE('');

    -------------------------------------------------------------------
    -- Test 1: SINGLE track without siding (should succeed)
    -------------------------------------------------------------------
BEGIN
        DBMS_OUTPUT.PUT_LINE('Test 1: SINGLE track without siding');
        USBD44_ADD_SEGMENT(
            p_segment_id     => 9001,
            p_is_electrified => 'Y',
            p_speed_limit    => 120,
            p_max_weight     => 8000,
            p_length         => 5000,
            p_line_id        => 1,
            p_track_type     => 'SINGLE'
        );
        DBMS_OUTPUT.PUT_LINE('TEST 1: OK');
        DBMS_OUTPUT.PUT_LINE('');
EXCEPTION
        WHEN OTHERS THEN
            DBMS_OUTPUT.PUT_LINE('TEST 1: FAILED -> ' || SQLERRM);
            DBMS_OUTPUT.PUT_LINE('');
END;

    -------------------------------------------------------------------
    -- Test 2: SINGLE track with siding (should succeed)
    -------------------------------------------------------------------
BEGIN
        DBMS_OUTPUT.PUT_LINE('Test 2: SINGLE track with siding');
        USBD44_ADD_SEGMENT(
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
        DBMS_OUTPUT.PUT_LINE('TEST 2: OK');
        DBMS_OUTPUT.PUT_LINE('');
EXCEPTION
        WHEN OTHERS THEN
            DBMS_OUTPUT.PUT_LINE('TEST 2: FAILED -> ' || SQLERRM);
            DBMS_OUTPUT.PUT_LINE('');
END;

    -------------------------------------------------------------------
    -- Test 3: MULTIPLE tracks (should succeed)
    -------------------------------------------------------------------
BEGIN
        DBMS_OUTPUT.PUT_LINE('Test 3: MULTIPLE tracks');
        USBD44_ADD_SEGMENT(
            p_segment_id     => 9003,
            p_is_electrified => 'Y',
            p_speed_limit    => 140,
            p_max_weight     => 8000,
            p_length         => 10000,
            p_line_id        => 2,
            p_track_type     => 'MULTIPLE',
            p_number_tracks  => 2
        );
        DBMS_OUTPUT.PUT_LINE('TEST 3: OK');
        DBMS_OUTPUT.PUT_LINE('');
EXCEPTION
        WHEN OTHERS THEN
            DBMS_OUTPUT.PUT_LINE('TEST 3: FAILED -> ' || SQLERRM);
            DBMS_OUTPUT.PUT_LINE('');
END;

    -------------------------------------------------------------------
    -- Test 4: Duplicate segment id (should fail)
    -------------------------------------------------------------------
BEGIN
        DBMS_OUTPUT.PUT_LINE('Test 4: Duplicate segment id');
        USBD44_ADD_SEGMENT(
            p_segment_id     => 9001,
            p_is_electrified => 'N',
            p_speed_limit    => 90,
            p_max_weight     => 7000,
            p_length         => 3000,
            p_line_id        => 1,
            p_track_type     => 'SINGLE'
        );
        DBMS_OUTPUT.PUT_LINE('TEST 4: FAILED (should have raised error)');
        DBMS_OUTPUT.PUT_LINE('');
EXCEPTION
        WHEN OTHERS THEN
            DBMS_OUTPUT.PUT_LINE('TEST 4: OK -> ' || SQLERRM);
            DBMS_OUTPUT.PUT_LINE('');
END;

    -------------------------------------------------------------------
    -- Test 5: Invalid track_type (should fail)
    -------------------------------------------------------------------
BEGIN
        DBMS_OUTPUT.PUT_LINE('Test 5: Invalid track_type');
        USBD44_ADD_SEGMENT(
            p_segment_id     => 9004,
            p_is_electrified => 'Y',
            p_speed_limit    => 120,
            p_max_weight     => 8000,
            p_length         => 5000,
            p_line_id        => 1,
            p_track_type     => 'DOUBLE'
        );
        DBMS_OUTPUT.PUT_LINE('TEST 5: FAILED (should have raised error)');
        DBMS_OUTPUT.PUT_LINE('');
EXCEPTION
        WHEN OTHERS THEN
            DBMS_OUTPUT.PUT_LINE('TEST 5: OK -> ' || SQLERRM);
            DBMS_OUTPUT.PUT_LINE('');
END;

    -------------------------------------------------------------------
    -- Test 6: MULTIPLE without number_tracks (should fail)
    -------------------------------------------------------------------
BEGIN
        DBMS_OUTPUT.PUT_LINE('Test 6: MULTIPLE without number_tracks');
        USBD44_ADD_SEGMENT(
            p_segment_id     => 9005,
            p_is_electrified => 'Y',
            p_speed_limit    => 120,
            p_max_weight     => 8000,
            p_length         => 5000,
            p_line_id        => 1,
            p_track_type     => 'MULTIPLE'
        );
        DBMS_OUTPUT.PUT_LINE('TEST 6: FAILED (should have raised error)');
        DBMS_OUTPUT.PUT_LINE('');
EXCEPTION
        WHEN OTHERS THEN
            DBMS_OUTPUT.PUT_LINE('TEST 6: OK -> ' || SQLERRM);
            DBMS_OUTPUT.PUT_LINE('');
END;

    -------------------------------------------------------------------
    -- Test 7: SINGLE with only one siding param (should fail)
    -------------------------------------------------------------------
BEGIN
        DBMS_OUTPUT.PUT_LINE('Test 7: SINGLE with incomplete siding params');
        USBD44_ADD_SEGMENT(
            p_segment_id      => 9006,
            p_is_electrified  => 'Y',
            p_speed_limit     => 120,
            p_max_weight      => 8000,
            p_length          => 5000,
            p_line_id         => 1,
            p_track_type      => 'SINGLE',
            p_siding_position => 2500,
            p_siding_length   => NULL
        );
        DBMS_OUTPUT.PUT_LINE('TEST 7: FAILED (should have raised error)');
        DBMS_OUTPUT.PUT_LINE('');
EXCEPTION
        WHEN OTHERS THEN
            DBMS_OUTPUT.PUT_LINE('TEST 7: OK -> ' || SQLERRM);
            DBMS_OUTPUT.PUT_LINE('');
END;

    DBMS_OUTPUT.PUT_LINE('=== Tests finished (rolling back all changes) ===');
END;
/
ROLLBACK;
