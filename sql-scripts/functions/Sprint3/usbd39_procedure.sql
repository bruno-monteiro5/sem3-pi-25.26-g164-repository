CREATE OR REPLACE PROCEDURE LOCOMOTIVE_ASSOCIATION(train_id IN Train.id%TYPE, locomotive_id IN Locomotive.id%TYPE, pos NUMBER)
IS
       counter NUMBER;
BEGIN

    -- Checks if the train exists
    SELECT COUNT(*)
    INTO counter
    FROM TRAIN
    WHERE id = train_id;

    IF counter = 0 THEN
       raise_application_error(-20001, 'Train does not exist');
    END IF;

    -- Now checks if the locomotive exists
    SELECT COUNT(*)
    INTO counter
    FROM LOCOMOTIVE
    WHERE id = locomotive_id;

    IF counter = 0 THEN
       raise_application_error(-20002, 'Locomotive does not exist');
    END IF;

    -- Checks if the locomotive and the train already are associated

    SELECT COUNT(*)
    INTO counter
    FROM LOCOMOTIVE_TRAIN
    where locomotive = locomotive_id AND train = train_id;

    IF counter != 0 THEN
       raise_application_error(-20003, 'Locomotive already associated to the train');
    END IF;

    INSERT INTO LOCOMOTIVE_TRAIN (locomotive, train, position) VALUES (locomotive_id, train_id, pos);

END LOCOMOTIVE_ASSOCIATION;
/