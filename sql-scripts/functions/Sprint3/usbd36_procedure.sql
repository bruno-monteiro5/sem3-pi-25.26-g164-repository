CREATE OR REPLACE PROCEDURE ADD_BUILDING (
    buildingId       IN Building.id%TYPE,
    buildingTypeId   IN Building.type%TYPE,
    buildingName     IN Building.name%TYPE,
    facilityId       IN Building.facility%TYPE
)
AS
    matchingFacilities     NUMBER;
    matchingBuildingTypes  NUMBER;
    normalizedBuildingName Building.name%TYPE;
BEGIN
    SELECT COUNT(*)
    INTO matchingFacilities
    FROM Facility
    WHERE id = facilityId;

    IF matchingFacilities = 0 THEN
        raise_application_error(-20036, 'Facility does not exist.');
    END IF;

    SELECT COUNT(*)
    INTO matchingBuildingTypes
    FROM BuildingType
    WHERE id = buildingTypeId;

    IF matchingBuildingTypes = 0 THEN
        raise_application_error(-20036, 'Building type does not exist.');
    END IF;

    normalizedBuildingName := TRIM(buildingName);

    IF normalizedBuildingName IS NULL OR LENGTH(normalizedBuildingName) = 0 THEN
        raise_application_error(-20036, 'Building name is required.');
    END IF;

    INSERT INTO Building (id, type, name, facility)
    VALUES (buildingId, buildingTypeId, normalizedBuildingName, facilityId);
END;
/
