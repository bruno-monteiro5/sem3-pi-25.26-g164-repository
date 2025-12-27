DECLARE
counter NUMBER(10);
BEGIN
    LINE_CREATION(30, 'Linha test', 'PT503933813', 4, 'Y', 300, 3000, 500, 2, 20, 48, 80);

SELECT COUNT(*)
INTO counter
FROM LINE
WHERE id = 30
  AND name = 'Linha test'
  AND owner = 'PT503933813';


IF counter != 0 THEN
    DBMS_OUTPUT.PUT_LINE('Locomotive sucessfully added to train');
ELSE
    DBMS_OUTPUT.PUT_LINE('Locomotive not added to train');
END IF;

END;
/

