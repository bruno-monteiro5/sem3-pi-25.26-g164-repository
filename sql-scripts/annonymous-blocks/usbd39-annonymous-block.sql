DECLARE
counter NUMBER(10);
BEGIN
    LOCOMOTIVE_ASSOCIATION(5437, 1903, 3);

SELECT COUNT(*)
INTO counter
FROM LOCOMOTIVE_TRAIN
WHERE train = 5437
  AND locomotive = 1903
  AND position = 3;


IF counter != 0 THEN
    DBMS_OUTPUT.PUT_LINE('Locomotive sucessfully added to train');
ELSE
    DBMS_OUTPUT.PUT_LINE('Locomotive not added to train');
END IF;

END;
/

