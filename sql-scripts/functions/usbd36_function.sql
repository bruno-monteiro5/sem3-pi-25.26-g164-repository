CREATE OR REPLACE FUNCTION USBD36_ADD_BUILDING (
    p_id       IN Building.id%TYPE,
    p_type     IN Building.type%TYPE,
    p_name     IN Building.name%TYPE,
    p_facility IN Building.facility%TYPE
)
    RETURN SYS_REFCURSOR
AS
    v_cnt NUMBER;
    v_cur SYS_REFCURSOR;
BEGIN
    -- facility tem de existir
    SELECT COUNT(*)
    INTO v_cnt
    FROM Facility
    WHERE id = p_facility;

    IF v_cnt = 0 THEN
        raise_application_error(-20036, 'Facility does not exist.');
    END IF;

    -- type tem de existir
    SELECT COUNT(*)
    INTO v_cnt
    FROM BuildingType
    WHERE id = p_type;

    IF v_cnt = 0 THEN
        raise_application_error(-20036, 'Building type does not exist.');
    END IF;

    -- name não pode ser vazio
    IF p_name IS NULL OR LENGTH(TRIM(p_name)) = 0 THEN
        raise_application_error(-20036, 'Building name is required.');
    END IF;

    -- insert
    INSERT INTO Building (id, type, name, facility)
    VALUES (p_id, p_type, TRIM(p_name), p_facility);

    -- devolver o registo inserido
    OPEN v_cur FOR
        SELECT id, type, name, facility
        FROM Building
        WHERE id = p_id;

    RETURN v_cur;
END;
/
