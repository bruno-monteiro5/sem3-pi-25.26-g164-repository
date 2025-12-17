CREATE OR REPLACE PROCEDURE USBD38_ADD_GAUGE (
    p_id    IN Gauge.id%TYPE,
    p_width IN Gauge.width%TYPE,
    p_name  IN Gauge.name%TYPE
)
AS
    v_cnt     NUMBER;
    v_norm_in VARCHAR2(4000);
BEGIN
    --  Validate ID
    IF p_id IS NULL OR p_id <= 0 THEN
        raise_application_error(-20038, 'Gauge id must be a positive number.');
END IF;

SELECT COUNT(*)
INTO v_cnt
FROM Gauge
WHERE id = p_id;

IF v_cnt > 0 THEN
        raise_application_error(-20038, 'Gauge id already exists: ' || p_id);
END IF;

    -- 2) Validate width
    IF p_width IS NULL OR p_width <= 0 THEN
        raise_application_error(-20038, 'Gauge width must be a positive number.');
END IF;

SELECT COUNT(*)
INTO v_cnt
FROM Gauge
WHERE width = p_width;

IF v_cnt > 0 THEN
        raise_application_error(-20038, 'Gauge width already exists: ' || p_width);
END IF;

    --  Validate name
    IF p_name IS NULL OR TRIM(p_name) IS NULL THEN
        raise_application_error(-20038, 'Gauge name must not be empty.');
END IF;

    -- Normalization: ignore spaces and case
    v_norm_in := REGEXP_REPLACE(UPPER(TRIM(p_name)), '\s+', '');

SELECT COUNT(*)
INTO v_cnt
FROM Gauge g
WHERE REGEXP_REPLACE(UPPER(TRIM(g.name)), '\s+', '') = v_norm_in;

IF v_cnt > 0 THEN
        raise_application_error(
          -20038,
          'Gauge name already exists (ignoring spaces/case): "' || p_name || '"'
        );
END IF;

    -- Insert
INSERT INTO Gauge (id, width, name)
VALUES (p_id, p_width, TRIM(p_name));
END;
/
