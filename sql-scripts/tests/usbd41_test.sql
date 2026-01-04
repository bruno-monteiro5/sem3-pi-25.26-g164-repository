SET SERVEROUTPUT ON;

DECLARE
v_operator_vat Operator.vat%TYPE;

  v_ep_origin Endpoint.id%TYPE;
  v_ep_dest   Endpoint.id%TYPE;

  v_fixed_date      DATE      := DATE '2025-01-01';
  v_fixed_timestamp TIMESTAMP := TIMESTAMP '2025-01-01 10:00:00';

  v_train_a   Train.id%TYPE := 991001;
  v_train_b   Train.id%TYPE := 991002;

  v_freight_a Freight.id%TYPE := 992001;
  v_freight_b Freight.id%TYPE := 992002;
  v_freight_c Freight.id%TYPE := 992003;

  v_fake_freight Freight.id%TYPE;
  v_fake_train   Train.id%TYPE;

BEGIN
  DBMS_OUTPUT.PUT_LINE('SETUP');
  -- Pick any existing Operator
BEGIN
SELECT vat
INTO v_operator_vat
FROM Operator
WHERE ROWNUM = 1;
EXCEPTION
    WHEN NO_DATA_FOUND THEN
      raise_application_error(-20041, 'No Operator found in database.');
END;


  -- Pick two distinct existing Endpoints
BEGIN
SELECT e1.id, e2.id
INTO v_ep_origin, v_ep_dest
FROM (SELECT id FROM Endpoint ORDER BY id) e1,
     (SELECT id FROM Endpoint ORDER BY id) e2
WHERE e1.id <> e2.id
  AND ROWNUM = 1;
EXCEPTION
    WHEN NO_DATA_FOUND THEN
      raise_application_error(-20041, 'At least two Endpoints are required.');
END;


  -- Make non-existing IDs
SELECT NVL(MAX(id), 0) + 999000 INTO v_fake_freight FROM Freight;
SELECT NVL(MAX(id), 0) + 999000 INTO v_fake_train   FROM Train;


-- Create Trains A and B
INSERT INTO Train (id, operator, time, path, "date", max_length)
VALUES (v_train_a, v_operator_vat, v_fixed_timestamp, 1, v_fixed_date, 1000);

INSERT INTO Train (id, operator, time, path, "date", max_length)
VALUES (v_train_b, v_operator_vat, v_fixed_timestamp, 1, v_fixed_date, 1000);

INSERT INTO Freight (id, "date", origin, destination)
VALUES (v_freight_a, v_fixed_date, v_ep_origin, v_ep_dest);

INSERT INTO Freight (id, "date", origin, destination)
VALUES (v_freight_b, v_fixed_date, v_ep_origin, v_ep_dest);

INSERT INTO Freight (id, "date", origin, destination)
VALUES (v_freight_c, v_fixed_date, v_ep_origin, v_ep_dest);

INSERT INTO Freight_Train (freight, train)
VALUES (v_freight_a, v_train_a);

INSERT INTO Freight_Train (freight, train)
VALUES (v_freight_b, v_train_b);

DBMS_OUTPUT.PUT_LINE('TESTING');

  -- TEST 1: valid removal of freight from planned train
  USBD41_REMOVE_FREIGHT_FROM_TRAIN(v_freight_a, v_train_a);
  DBMS_OUTPUT.PUT_LINE('TEST 1: OK');

  -- TEST 2: remove freight already removed (no association)
BEGIN
    USBD41_REMOVE_FREIGHT_FROM_TRAIN(v_freight_a, v_train_a);
    DBMS_OUTPUT.PUT_LINE('TEST 2: FAILED');
EXCEPTION
    WHEN OTHERS THEN
      DBMS_OUTPUT.PUT_LINE('TEST 2: OK -> ' || SQLERRM);
END;

  -- TEST 3: freight associated with a different train
BEGIN
    USBD41_REMOVE_FREIGHT_FROM_TRAIN(v_freight_b, v_train_a);
    DBMS_OUTPUT.PUT_LINE('TEST 3: FAILED');
EXCEPTION
    WHEN OTHERS THEN
      DBMS_OUTPUT.PUT_LINE('TEST 3: OK -> ' || SQLERRM);
END;

  -- TEST 4: freight not associated with any train
BEGIN
    USBD41_REMOVE_FREIGHT_FROM_TRAIN(v_freight_c, v_train_a);
    DBMS_OUTPUT.PUT_LINE('TEST 4: FAILED');
EXCEPTION
    WHEN OTHERS THEN
      DBMS_OUTPUT.PUT_LINE('TEST 4: OK -> ' || SQLERRM);
END;

  -- TEST 5: non-existing freight
BEGIN
    USBD41_REMOVE_FREIGHT_FROM_TRAIN(v_fake_freight, v_train_a);
    DBMS_OUTPUT.PUT_LINE('TEST 5: FAILED');
EXCEPTION
    WHEN OTHERS THEN
      DBMS_OUTPUT.PUT_LINE('TEST 5: OK -> ' || SQLERRM);
END;

  -- TEST 6: non-existing train
BEGIN
    USBD41_REMOVE_FREIGHT_FROM_TRAIN(v_freight_b, v_fake_train);
    DBMS_OUTPUT.PUT_LINE('TEST 6: FAILED');
EXCEPTION
    WHEN OTHERS THEN
      DBMS_OUTPUT.PUT_LINE('TEST 6: OK -> ' || SQLERRM);
END;

  -- TEST 7A: NULL freight id
BEGIN
    USBD41_REMOVE_FREIGHT_FROM_TRAIN(NULL, v_train_a);
    DBMS_OUTPUT.PUT_LINE('TEST 7A: FAILED');
EXCEPTION
    WHEN OTHERS THEN
      DBMS_OUTPUT.PUT_LINE('TEST 7A: OK -> ' || SQLERRM);
END;

  -- TEST 7B: NULL train id
BEGIN
    USBD41_REMOVE_FREIGHT_FROM_TRAIN(v_freight_b, NULL);
    DBMS_OUTPUT.PUT_LINE('TEST 7B: FAILED');
EXCEPTION
    WHEN OTHERS THEN
      DBMS_OUTPUT.PUT_LINE('TEST 7B: OK -> ' || SQLERRM);
END;

  -- TEST 7C: freight id = 0
BEGIN
    USBD41_REMOVE_FREIGHT_FROM_TRAIN(0, v_train_a);
    DBMS_OUTPUT.PUT_LINE('TEST 7C: FAILED');
EXCEPTION
    WHEN OTHERS THEN
      DBMS_OUTPUT.PUT_LINE('TEST 7C: OK -> ' || SQLERRM);
END;

  -- TEST 7D: train id = 0
BEGIN
    USBD41_REMOVE_FREIGHT_FROM_TRAIN(v_freight_b, 0);
    DBMS_OUTPUT.PUT_LINE('TEST 7D: FAILED');
EXCEPTION
    WHEN OTHERS THEN
      DBMS_OUTPUT.PUT_LINE('TEST 7D: OK -> ' || SQLERRM);
END;

END;
/
ROLLBACK;