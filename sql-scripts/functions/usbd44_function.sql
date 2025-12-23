CREATE OR REPLACE FUNCTION USBD44_ADD_SEGMENT (
    p_segment_id      IN Line_Segment.id%TYPE,
    p_is_electrified  IN Line_Segment.is_electrified%TYPE,
    p_speed_limit     IN Line_Segment.speed_limit%TYPE,
    p_max_weight      IN Line_Segment.max_weight%TYPE,
    p_length          IN Line_Segment.length%TYPE,
    p_line_id         IN Line_Segment.line%TYPE,
    p_track_type      IN VARCHAR2,
    p_number_tracks   IN Multiple_Tracks.number_tracks%TYPE DEFAULT NULL,
    p_siding_position IN Siding.position%TYPE DEFAULT NULL,
    p_siding_length   IN Siding.length%TYPE DEFAULT NULL
) RETURN NUMBER
AS
    v_cnt NUMBER;
BEGIN
    -- Validate segment_id does not exist
SELECT COUNT(*) INTO v_cnt FROM Line_Segment WHERE id = p_segment_id;
IF v_cnt > 0 THEN
        raise_application_error(-20044, 'Segment id already exists: ' || p_segment_id);
END IF;

    -- Validate line exists
SELECT COUNT(*) INTO v_cnt FROM Line WHERE id = p_line_id;
IF v_cnt = 0 THEN
        raise_application_error(-20044, 'Line id does not exist: ' || p_line_id);
END IF;

    -- Validate track_type
    IF UPPER(p_track_type) NOT IN ('SINGLE', 'MULTIPLE') THEN
        raise_application_error(-20044, 'track_type must be SINGLE or MULTIPLE.');
END IF;

    -- Insert Line_Segment
INSERT INTO Line_Segment (id, is_electrified, speed_limit, max_weight, length, line)
VALUES (p_segment_id, p_is_electrified, p_speed_limit, p_max_weight, p_length, p_line_id);

-- Insert track type specialization
IF UPPER(p_track_type) = 'SINGLE' THEN
        INSERT INTO Single_Track (segment)
        VALUES (p_segment_id);

        -- If siding parameters provided, add siding
        IF p_siding_position IS NOT NULL AND p_siding_length IS NOT NULL THEN
            INSERT INTO Siding (segment, position, length)
            VALUES (p_segment_id, p_siding_position, p_siding_length);
END IF;

    ELSIF UPPER(p_track_type) = 'MULTIPLE' THEN
        INSERT INTO Multiple_Tracks (segment, number_tracks)
        VALUES (p_segment_id, p_number_tracks);
END IF;

RETURN p_segment_id;

EXCEPTION
    WHEN OTHERS THEN
        ROLLBACK;
        RAISE;
END;
/