CREATE OR REPLACE FUNCTION USBD36_ADD_BUILDING (
    p_id       IN Building.id%TYPE,
    p_type     IN Building.type%TYPE,
    p_name     IN Building.name%TYPE,
    p_facility IN Building.facility%TYPE
)
    RETURN SYS_REFCURSOR
AS
    v_cnt     NUMBER;
    v_norm_in VARCHAR2(4000);
    v_cur     SYS_REFCURSOR;
BEGIN
    ----------------------------------------------------------------
    -- 1) Validate building ID
    ----------------------------------------------------------------
    IF p_id IS NULL OR p_id <= 0 THEN
        raise_application_error(-20036, 'Building id must be a positive number.');
    END IF;

    SELECT COUNT(*)
    INTO v_cnt
    FROM Building
    WHERE id = p_id;

    IF v_cnt > 0 THEN
        raise_application_error(-20036, 'Building id already exists: ' || p_id);
    END IF;

    ----------------------------------------------------------------
    -- 2) Validate facility exists
    ----------------------------------------------------------------
    IF p_facility IS NULL OR p_facility <= 0 THEN
        raise_application_error(-20036, 'Facility id must be a positive number.');
    END IF;

    SELECT COUNT(*)
    INTO v_cnt
    FROM Facility
    WHERE id = p_facility;

    IF v_cnt = 0 THEN
        raise_application_error(-20036, 'Facility does not exist: ' || p_facility);
    END IF;

    ----------------------------------------------------------------
    -- 3) Validate building type exists
    ----------------------------------------------------------------
    IF p_type IS NULL OR p_type <= 0 THEN
        raise_application_error(-20036, 'Building type must be a positive number.');
    END IF;

    SELECT COUNT(*)
    INTO v_cnt
    FROM BuildingType
    WHERE id = p_type;

    IF v_cnt = 0 THEN
        raise_application_error(-20036, 'Building type does not exist: ' || p_type);
    END IF;

    ----------------------------------------------------------------
    -- 4) Validate name
    ----------------------------------------------------------------
    IF p_name IS NULL OR TRIM(p_name) IS NULL THEN
        raise_application_error(-20036, 'Building name must not be empty.');
    END IF;

    -- Normalization: ignore spaces and case
    v_norm_in := REGEXP_REPLACE(UPPER(TRIM(p_name)), '\s+', '');

    SELECT COUNT(*)
    INTO v_cnt
    FROM Building b
    WHERE b.facility = p_facility
      AND REGEXP_REPLACE(UPPER(TRIM(b.name)), '\s+', '') = v_norm_in;

    IF v_cnt > 0 THEN
        raise_application_error(
                -20036,
                'Building name already exists in this facility (ignoring spaces/case): "' || p_name || '"'
        );
    END IF;

    ----------------------------------------------------------------
    -- 5) Insert
    ----------------------------------------------------------------
    INSERT INTO Building (id, type, name, facility)
    VALUES (p_id, p_type, TRIM(p_name), p_facility);

    ----------------------------------------------------------------
    -- 6) Return inserted row
    ----------------------------------------------------------------
    OPEN v_cur FOR
        SELECT b.id,
               b.type,
               bt.name AS type_name,
               b.name,
               b.facility
        FROM Building b
                 JOIN BuildingType bt ON bt.id = b.type
        WHERE b.id = p_id;

    RETURN v_cur;
END;
/
