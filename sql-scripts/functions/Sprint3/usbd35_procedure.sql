CREATE OR REPLACE PROCEDURE USBD35_ADD_FACILITY (
    p_id   IN Facility.id%TYPE,
    p_name IN Facility.name%TYPE,
    p_type IN Facility.type%TYPE
)
AS
    v_cnt NUMBER;
BEGIN
    -- Validate facility ID (must be positive and unique)
    IF p_id IS NULL OR p_id <= 0 THEN
        raise_application_error(-20035, 'Facility id must be a positive number.');
    END IF;

    SELECT COUNT(*) INTO v_cnt FROM Metafacility WHERE id = p_id;
    IF v_cnt > 0 THEN
        raise_application_error(-20035, 'Facility id already exists: ' || p_id);
    END IF;

    -- Validate facility name (must not be empty or NULL)
    IF p_name IS NULL OR TRIM(p_name) IS NULL THEN
        raise_application_error(-20035, 'Facility name must not be empty.');
    END IF;

    -- Validate facility type (must not be NULL and must exist)
    IF p_type IS NULL THEN
        raise_application_error(-20035, 'Facility type must not be null.');
    END IF;
    SELECT COUNT(*) INTO v_cnt FROM BuildingType WHERE id = p_type;
    IF v_cnt = 0 THEN
        raise_application_error(-20035, 'BuildingType id does not exist: ' || p_type);
    END IF;

    -- Insert into parent table Metafacility
    INSERT INTO Metafacility (id, name)
    VALUES (p_id, TRIM(p_name));

    -- Insert into child table Facility
    INSERT INTO Facility (id, name, type)
    VALUES (p_id, TRIM(p_name), p_type);
END;
/
