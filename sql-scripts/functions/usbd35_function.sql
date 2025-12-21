CREATE OR REPLACE FUNCTION USBD35_ADD_FACILITY (
    p_id   IN Facility.id%TYPE,
    p_name IN Facility.name%TYPE,
    p_type IN Facility.type%TYPE
) RETURN NUMBER
AS
    v_cnt NUMBER;
BEGIN
    IF p_id IS NULL OR p_id <= 0 THEN
        raise_application_error(-20035, 'Facility id must be a positive number.');
END IF;

SELECT COUNT(*)
INTO v_cnt
FROM Metafacility
WHERE id = p_id;

IF v_cnt > 0 THEN
        raise_application_error(-20035, 'Facility id already exists: ' || p_id);
END IF;

    IF p_name IS NULL OR TRIM(p_name) IS NULL THEN
        raise_application_error(-20035, 'Facility name must not be empty.');
END IF;

    IF p_type IS NULL THEN
        raise_application_error(-20035, 'Facility type must not be null.');
END IF;

SELECT COUNT(*)
INTO v_cnt
FROM BuildingType
WHERE id = p_type;

IF v_cnt = 0 THEN
        raise_application_error(-20035, 'BuildingType id does not exist: ' || p_type);
END IF;

    -- 4) Inserir na tabela Metafacility (pai)
INSERT INTO Metafacility (id, name)
VALUES (p_id, TRIM(p_name));

-- 5) Inserir na tabela Facility (filho)
INSERT INTO Facility (id, name, type)
VALUES (p_id, TRIM(p_name), p_type);

RETURN p_id;

EXCEPTION
    WHEN OTHERS THEN
        ROLLBACK;
        RAISE;
END;
/