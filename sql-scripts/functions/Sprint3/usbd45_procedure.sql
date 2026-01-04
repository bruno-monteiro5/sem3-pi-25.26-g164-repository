CREATE OR REPLACE PROCEDURE line_creation(lid IN Line.id%TYPE, na IN Line.name%TYPE, owner IN Line.owner%TYPE, num IN NUMBER, elec IN Line_Segment.is_electrified%TYPE, speed IN NUMBER, weight IN NUMBER, leng IN NUMBER, gauge IN NUMBER, enda IN NUMBER, endb IN NUMBER, sid IN LINE_SEGMENT.ID%TYPE)
    IS
    counter NUMBER;
    i NUMBER := 0;
BEGIN
    -- Checks if the line already exists
    SELECT COUNT(*) INTO counter FROM Line WHERE id = lid;

    IF counter != 0 THEN
        RAISE_APPLICATION_ERROR(-20001, 'Line already exists');
    END IF;

    -- Checks if the number of segments is at least one
    IF num < 1 THEN
        RAISE_APPLICATION_ERROR(-20002, 'Line must have at least one segment');
    END IF;

    -- Checks if all other values are valid
    SELECT COUNT(*) INTO counter FROM OWNER WHERE VAT = owner;

    IF counter = 0 THEN
        RAISE_APPLICATION_ERROR(-20003, 'Owner does not exist');
    END IF;

    IF elec NOT IN ('N', 'Y') THEN
        RAISE_APPLICATION_ERROR(-20004, 'Electrification status is invalid');
    END IF;

    SELECT COUNT(*) INTO counter FROM GAUGE WHERE id = gauge;
    IF counter = 0 THEN
        RAISE_APPLICATION_ERROR(-20005, 'Gauge does not exist');
    END IF;

    SELECT COUNT(*) INTO counter FROM ENDPOINT WHERE id = enda;
    IF counter = 0 THEN
        RAISE_APPLICATION_ERROR(-20006, 'Endpoint A does not exist');
    END IF;

    SELECT COUNT(*) INTO counter FROM ENDPOINT WHERE id = endb;
    IF counter = 0 THEN
        RAISE_APPLICATION_ERROR(-20007, 'Endpoint B does not exist');
    END IF;

    -- Insert the line first
    INSERT INTO Line (id, name, owner, endpoint_a, endpoint_b, gauge) VALUES (lid, na, owner, enda, endb, gauge);

    -- Insert segments
    WHILE i < num LOOP
            SELECT COUNT(*) INTO counter FROM LINE_SEGMENT WHERE id = sid + i;
            IF counter != 0 THEN
                RAISE_APPLICATION_ERROR(-20008, 'Line segment ' || (sid + i) || ' already exists');
            END IF;

            i := i + 1;
        END LOOP;

    i := 0;

    WHILE i < num LOOP

            INSERT INTO LINE_SEGMENT (id, IS_ELECTRIFIED, SPEED_LIMIT, MAX_WEIGHT, LENGTH, LINE) VALUES (sid + i, elec, speed, weight, leng, lid);

            i := i + 1;

        end loop;

    COMMIT;

END line_creation;
/