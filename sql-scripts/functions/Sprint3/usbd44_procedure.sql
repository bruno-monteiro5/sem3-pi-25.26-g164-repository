CREATE OR REPLACE PROCEDURE USBD44_ADD_SEGMENT (
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
)
AS
    v_cnt        NUMBER;
    v_track_type VARCHAR2(20);
BEGIN
    ----------------------------------------------------------------------
    -- Basic input validations
    ----------------------------------------------------------------------
    IF p_segment_id IS NULL OR p_segment_id <= 0 THEN
        raise_application_error(-20044, 'Segment id must be a positive number.');
END IF;

    IF p_line_id IS NULL OR p_line_id <= 0 THEN
        raise_application_error(-20044, 'Line id must be a positive number.');
END IF;

    IF p_is_electrified IS NULL OR UPPER(TRIM(p_is_electrified)) NOT IN ('Y', 'N') THEN
        raise_application_error(-20044, 'is_electrified must be Y or N.');
END IF;

    IF p_speed_limit IS NULL OR p_speed_limit <= 0 THEN
        raise_application_error(-20044, 'speed_limit must be > 0.');
END IF;

    IF p_max_weight IS NULL OR p_max_weight <= 0 THEN
        raise_application_error(-20044, 'max_weight must be > 0.');
END IF;

    IF p_length IS NULL OR p_length <= 0 THEN
        raise_application_error(-20044, 'length must be > 0.');
END IF;

    IF p_track_type IS NULL OR TRIM(p_track_type) IS NULL THEN
        raise_application_error(-20044, 'track_type is required (SINGLE or MULTIPLE).');
END IF;

    v_track_type := UPPER(TRIM(p_track_type));
    IF v_track_type NOT IN ('SINGLE', 'MULTIPLE') THEN
        raise_application_error(-20044, 'track_type must be SINGLE or MULTIPLE.');
END IF;

    ----------------------------------------------------------------------
    -- Existence validations
    ----------------------------------------------------------------------
SELECT COUNT(*) INTO v_cnt FROM Line_Segment WHERE id = p_segment_id;
IF v_cnt > 0 THEN
        raise_application_error(-20044, 'Segment id already exists: ' || p_segment_id);
END IF;

SELECT COUNT(*) INTO v_cnt FROM Line WHERE id = p_line_id;
IF v_cnt = 0 THEN
        raise_application_error(-20044, 'Line id does not exist: ' || p_line_id);
END IF;

    ----------------------------------------------------------------------
    -- Track type specific validations
    ----------------------------------------------------------------------
    IF v_track_type = 'MULTIPLE' THEN
        IF p_number_tracks IS NULL OR p_number_tracks < 2 THEN
            raise_application_error(-20044, 'number_tracks must be provided and >= 2 for MULTIPLE track.');
END IF;

        -- siding should not be provided for MULTIPLE (optional rule, but helps standardize)
        IF p_siding_position IS NOT NULL OR p_siding_length IS NOT NULL THEN
            raise_application_error(-20044, 'Siding is only supported for SINGLE track segments.');
END IF;
END IF;

    IF v_track_type = 'SINGLE' THEN
        -- Either both siding params are provided or none
        IF (p_siding_position IS NULL AND p_siding_length IS NOT NULL) OR
           (p_siding_position IS NOT NULL AND p_siding_length IS NULL) THEN
            raise_application_error(-20044, 'To add a siding, both siding_position and siding_length must be provided.');
END IF;

        -- If a siding is provided, validate basic coherence
        IF p_siding_position IS NOT NULL AND p_siding_length IS NOT NULL THEN
            IF p_siding_position <= 0 OR p_siding_position >= p_length THEN
                raise_application_error(-20044, 'siding_position must be > 0 and < segment length.');
END IF;

            IF p_siding_length <= 0 OR p_siding_length >= p_length THEN
                raise_application_error(-20044, 'siding_length must be > 0 and < segment length.');
END IF;
END IF;
END IF;

    ----------------------------------------------------------------------
    -- Insert base segment
    ----------------------------------------------------------------------
INSERT INTO Line_Segment (id, is_electrified, speed_limit, max_weight, length, line)
VALUES (p_segment_id, UPPER(TRIM(p_is_electrified)), p_speed_limit, p_max_weight, p_length, p_line_id);

----------------------------------------------------------------------
-- Insert specialization + optional siding
----------------------------------------------------------------------
IF v_track_type = 'SINGLE' THEN
        INSERT INTO Single_Track (segment)
        VALUES (p_segment_id);

        IF p_siding_position IS NOT NULL AND p_siding_length IS NOT NULL THEN
            INSERT INTO Siding (segment, position, length)
            VALUES (p_segment_id, p_siding_position, p_siding_length);
END IF;

ELSE
        INSERT INTO Multiple_Tracks (segment, number_tracks)
        VALUES (p_segment_id, p_number_tracks);
END IF;

END;
/
