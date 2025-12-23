DECLARE
    counter NUMBER(10);
BEGIN
    LOCOMOTIVE_ASSOCIATION(5421, 5621, 3);

    SELECT *
    INTO counter
    FROM LOCOMOTIVE_TRAIN
    WHERE train = 5421 AND locomotive = 5621 AND position = 3;

    IF counter != 0 THEN
       DBMS_OUTPUT.PUT_LINE('Locomotive sucessfully added to train');
    END IF;

END;
/

