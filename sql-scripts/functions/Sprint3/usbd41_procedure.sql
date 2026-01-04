CREATE OR REPLACE PROCEDURE USBD41_REMOVE_FREIGHT_FROM_TRAIN (
  p_freight_id IN Freight.id%TYPE,
  p_train_id   IN Train.id%TYPE
)
IS
  v_count       NUMBER;
  v_train_in_ft Train.id%TYPE;
BEGIN
  --  Basic input validation
  IF p_freight_id IS NULL OR p_freight_id <= 0 THEN
    raise_application_error(-20041, 'Freight id is required and must be > 0.');
END IF;

  IF p_train_id IS NULL OR p_train_id <= 0 THEN
    raise_application_error(-20041, 'Train id is required and must be > 0.');
END IF;

  --  Freight must exist
SELECT COUNT(*)
INTO v_count
FROM Freight
WHERE id = p_freight_id;

IF v_count = 0 THEN
    raise_application_error(-20041, 'Freight does not exist.');
END IF;

  --  Train must exist
SELECT COUNT(*)
INTO v_count
FROM Train
WHERE id = p_train_id;

IF v_count = 0 THEN
    raise_application_error(-20041, 'Train does not exist.');
END IF;

  --  Train must be "planned": no movements registered yet
SELECT COUNT(*)
INTO v_count
FROM Train_Movement
WHERE train = p_train_id;

IF v_count > 0 THEN
    raise_application_error(-20041, 'Train is not planned (it already has recorded movements).');
END IF;

  --  Freight must be associated with the given train
BEGIN
SELECT train
INTO v_train_in_ft
FROM Freight_Train
WHERE freight = p_freight_id;

IF v_train_in_ft <> p_train_id THEN
      raise_application_error(-20041, 'Freight is not associated with the given train.');
END IF;

EXCEPTION
    WHEN NO_DATA_FOUND THEN
      raise_application_error(-20041, 'Freight is not associated with any train.');
END;

  --  Remove association
DELETE FROM Freight_Train
WHERE freight = p_freight_id
  AND train   = p_train_id;

IF SQL%ROWCOUNT = 0 THEN
    raise_application_error(-20041, 'Nothing to remove (association not found).');
END IF;

END;
/