CREATE OR REPLACE FUNCTION ADD_ELECTRIC_LOCOMOTIVE_MODEL (
    modelId           IN Locomotive_Model.id%TYPE,
    modelName         IN Locomotive_Model.name%TYPE,
    modelMake         IN Locomotive_Model.make%TYPE,
    numberBogies      IN Locomotive_Model.number_bogies%TYPE,
    bogieArrangement  IN Locomotive_Model.bogie_arrangement%TYPE,
    powerValue        IN Locomotive_Model.power%TYPE,
    modelLength       IN Locomotive_Model.length%TYPE,
    modelWidth        IN Locomotive_Model.width%TYPE,
    modelHeight       IN Locomotive_Model.height%TYPE,
    maxSpeed          IN Locomotive_Model.max_speed%TYPE,
    tractionValue     IN Locomotive_Model.traction%TYPE,
    voltageValue      IN Electric_Locomotive_Model.voltage%TYPE,
    frequencyValue    IN Electric_Locomotive_Model.frequency%TYPE,
    gaugeId           IN Gauge.id%TYPE
)
    RETURN SYS_REFCURSOR
AS
    matchingModels NUMBER;
    matchingGauges NUMBER;

    normalizedModelName Locomotive_Model.name%TYPE;
    normalizedModelMake Locomotive_Model.make%TYPE;

    resultCursor SYS_REFCURSOR;
BEGIN
    normalizedModelName := TRIM(modelName);
    normalizedModelMake := TRIM(modelMake);

    IF normalizedModelName IS NULL OR LENGTH(normalizedModelName) = 0 THEN
        raise_application_error(-20043, 'Model name is required.');
    END IF;

    IF normalizedModelMake IS NULL OR LENGTH(normalizedModelMake) = 0 THEN
        raise_application_error(-20043, 'Model make is required.');
    END IF;

    SELECT COUNT(*)
    INTO matchingModels
    FROM Locomotive_Model
    WHERE id = modelId;

    IF matchingModels > 0 THEN
        raise_application_error(-20043, 'Locomotive model id already exists.');
    END IF;

    SELECT COUNT(*)
    INTO matchingGauges
    FROM Gauge
    WHERE id = gaugeId;

    IF matchingGauges = 0 THEN
        raise_application_error(-20043, 'Gauge does not exist.');
    END IF;

    IF voltageValue IS NULL OR voltageValue <= 0 THEN
        raise_application_error(-20043, 'Voltage must be a positive number.');
    END IF;

    IF frequencyValue IS NULL OR frequencyValue <= 0 THEN
        raise_application_error(-20043, 'Frequency must be a positive number.');
    END IF;

    INSERT INTO Locomotive_Model
    (id, name, make, number_bogies, bogie_arrangement, power,
     length, width, height, max_speed, traction)
    VALUES (modelId, normalizedModelName, normalizedModelMake, numberBogies, bogieArrangement, powerValue,modelLength, modelWidth, modelHeight, maxSpeed, tractionValue);

    INSERT INTO Electric_Locomotive_Model (voltage, frequency, model)
    VALUES (voltageValue, frequencyValue, modelId);

    INSERT INTO Locomotive_Model_Gauge (Locomotive_Modelid, Gaugeid)
    VALUES (modelId, gaugeId);


    OPEN resultCursor FOR
        SELECT locomotiveModel.id,
               locomotiveModel.name,
               locomotiveModel.make,
               electricModel.voltage,
               electricModel.frequency,
               modelGauge.Gaugeid AS gauge_id
        FROM Locomotive_Model locomotiveModel
                 JOIN Electric_Locomotive_Model electricModel
                      ON electricModel.model = locomotiveModel.id
                 JOIN Locomotive_Model_Gauge modelGauge
                      ON modelGauge.Locomotive_Modelid = locomotiveModel.id
        WHERE locomotiveModel.id = modelId;

    RETURN resultCursor;
END;
/
