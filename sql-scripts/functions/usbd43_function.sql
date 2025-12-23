CREATE OR REPLACE FUNCTION USBD43_ADD_ELECTRIC_LOCOMOTIVE_MODEL (
    p_model_id          IN Locomotive_Model.id%TYPE,
    p_name              IN Locomotive_Model.name%TYPE,
    p_make              IN Locomotive_Model.make%TYPE,
    p_number_bogies     IN Locomotive_Model.number_bogies%TYPE,
    p_bogie_arrangement IN Locomotive_Model.bogie_arrangement%TYPE,
    p_power             IN Locomotive_Model.power%TYPE,
    p_length            IN Locomotive_Model.length%TYPE,
    p_width             IN Locomotive_Model.width%TYPE,
    p_height            IN Locomotive_Model.height%TYPE,
    p_max_speed         IN Locomotive_Model.max_speed%TYPE,
    p_traction          IN Locomotive_Model.traction%TYPE,
    p_voltage           IN Electric_Locomotive_Model.voltage%TYPE,
    p_frequency         IN Electric_Locomotive_Model.frequency%TYPE,
    p_gauge_id          IN Gauge.id%TYPE
)
    RETURN SYS_REFCURSOR
AS
    v_cnt  NUMBER;
    v_cur  SYS_REFCURSOR;
    v_name Locomotive_Model.name%TYPE;
    v_make Locomotive_Model.make%TYPE;
BEGIN
    v_name := TRIM(p_name);
    v_make := TRIM(p_make);

    IF v_name IS NULL OR LENGTH(v_name) = 0 THEN
        raise_application_error(-20043, 'Model name is required.');
    END IF;

    IF v_make IS NULL OR LENGTH(v_make) = 0 THEN
        raise_application_error(-20043, 'Model make is required.');
    END IF;

    -- id não pode existir
    SELECT COUNT(*)
    INTO v_cnt
    FROM Locomotive_Model
    WHERE id = p_model_id;

    IF v_cnt > 0 THEN
        raise_application_error(-20043, 'Locomotive model id already exists.');
    END IF;

    -- gauge tem de existir
    SELECT COUNT(*)
    INTO v_cnt
    FROM Gauge
    WHERE id = p_gauge_id;

    IF v_cnt = 0 THEN
        raise_application_error(-20043, 'Gauge does not exist.');
    END IF;

    -- voltage/frequency mínimas
    IF p_voltage IS NULL OR p_voltage <= 0 THEN
        raise_application_error(-20043, 'Voltage must be a positive number.');
    END IF;

    IF p_frequency IS NULL OR p_frequency <= 0 THEN
        raise_application_error(-20043, 'Frequency must be a positive number.');
    END IF;

    -- inserir modelo base
    INSERT INTO Locomotive_Model
    (id, name, make, number_bogies, bogie_arrangement, power,
     length, width, height, max_speed, traction)
    VALUES
        (p_model_id, v_name, v_make, p_number_bogies, p_bogie_arrangement, p_power,
         p_length, p_width, p_height, p_max_speed, p_traction);

    --  inserir parte elétrica
    INSERT INTO Electric_Locomotive_Model (voltage, frequency, model)
    VALUES (p_voltage, p_frequency, p_model_id);

    -- associar gauge
    INSERT INTO Locomotive_Model_Gauge (Locomotive_Modelid, Gaugeid)
    VALUES (p_model_id, p_gauge_id);


    OPEN v_cur FOR
        SELECT lm.id,
               lm.name,
               lm.make,
               elm.voltage,
               elm.frequency,
               lmg.Gaugeid AS gauge_id
        FROM Locomotive_Model lm
                 JOIN Electric_Locomotive_Model elm ON elm.model = lm.id
                 JOIN Locomotive_Model_Gauge lmg ON lmg.Locomotive_Modelid = lm.id
        WHERE lm.id = p_model_id;

    RETURN v_cur;
END;
/
