SET SERVEROUTPUT ON;

DECLARE
  -- Reused reference data
v_operator_vat Operator.vat%TYPE;

  v_ep_origin Endpoint.id%TYPE;
  v_ep_dest   Endpoint.id%TYPE;

  -- Fixture IDs (high values to avoid collisions)

  v_train_a   Train.id%TYPE := 991001;
  v_train_b   Train.id%TYPE := 991002;

  v_freight_a Freight.id%TYPE := 992001; -- associated with train A
  v_freight_b Freight.id%TYPE := 992002; -- associated with train B
  v_freight_c Freight.id%TYPE := 992003; -- NOT associated with any train


  -- Fake IDs (guaranteed non-existing)

  v_fake_freight Freight.id%TYPE;
  v_fake_train   Train.id%TYPE;

  v_ret VARCHAR2(4000);
BEGIN
------------------------------------------------------------
------ SETUP FOR TESTING -----------------------------------
------------------------------------------------------------
  DBMS_OUTPUT.PUT_LINE('SETUP');
  -- Pick any existing Operator
BEGIN
SELECT vat
INTO v_operator_vat
FROM Operator
WHERE ROWNUM = 1;

DBMS_OUTPUT.PUT_LINE('Using existing Operator VAT = ' || v_operator_vat);
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

DBMS_OUTPUT.PUT_LINE('Using Endpoints origin=' || v_ep_origin ||
                         ', destination=' || v_ep_dest);
EXCEPTION
    WHEN NO_DATA_FOUND THEN
      raise_application_error(-20041, 'At least two Endpoints are required.');
END;


  -- Make non-existing IDs
SELECT NVL(MAX(id), 0) + 999000 INTO v_fake_freight FROM Freight;
SELECT NVL(MAX(id), 0) + 999000 INTO v_fake_train   FROM Train;


-- Create Trains A and B
INSERT INTO Train (id, operator, time, path, "date", max_length)
VALUES (v_train_a, v_operator_vat, SYSTIMESTAMP, 1, TRUNC(SYSDATE), 1000);

DBMS_OUTPUT.PUT_LINE('FIXTURE: Train A inserted (id=' || v_train_a || ')');

INSERT INTO Train (id, operator, time, path, "date", max_length)
VALUES (v_train_b, v_operator_vat, SYSTIMESTAMP, 1, TRUNC(SYSDATE), 1000);

DBMS_OUTPUT.PUT_LINE('FIXTURE: Train B inserted (id=' || v_train_b || ')');



  --Create Freights A, B, C
INSERT INTO Freight (id, "date", origin, destination)
VALUES (v_freight_a, TRUNC(SYSDATE), v_ep_origin, v_ep_dest);

INSERT INTO Freight (id, "date", origin, destination)
VALUES (v_freight_b, TRUNC(SYSDATE), v_ep_origin, v_ep_dest);

INSERT INTO Freight (id, "date", origin, destination)
VALUES (v_freight_c, TRUNC(SYSDATE), v_ep_origin, v_ep_dest);

DBMS_OUTPUT.PUT_LINE('FIXTURE: Freights A, B, C inserted');


  -- Associate Freights with Trains
INSERT INTO Freight_Train (freight, train)
VALUES (v_freight_a, v_train_a);

INSERT INTO Freight_Train (freight, train)
VALUES (v_freight_b, v_train_b);

DBMS_OUTPUT.PUT_LINE('FIXTURE: Freight_Train associations inserted');


 --  TESTS
	DBMS_OUTPUT.PUT_LINE('TESTING');
  ---------------------------------------------------------------------------
  -- TEST 1: Valid removal (planned train)
  ---------------------------------------------------------------------------
  v_ret := USBD41_REMOVE_FREIGHT_FROM_TRAIN(v_freight_a, v_train_a);
  DBMS_OUTPUT.PUT_LINE('TEST 1 (valid removal): OK -> return=' || v_ret);

  ---------------------------------------------------------------------------
  -- TEST 2: Remove again (already removed)
  ---------------------------------------------------------------------------
BEGIN
    v_ret := USBD41_REMOVE_FREIGHT_FROM_TRAIN(v_freight_a, v_train_a);
    DBMS_OUTPUT.PUT_LINE('TEST 2 (remove non-existing association): FAILED');
EXCEPTION
    WHEN OTHERS THEN
      DBMS_OUTPUT.PUT_LINE('TEST 2 (remove non-existing association): OK -> ' || SQLERRM);
END;

  ---------------------------------------------------------------------------
  -- TEST 3: Freight associated with another train
  ---------------------------------------------------------------------------
BEGIN
    v_ret := USBD41_REMOVE_FREIGHT_FROM_TRAIN(v_freight_b, v_train_a);
    DBMS_OUTPUT.PUT_LINE('TEST 3 (freight not associated with given train): FAILED');
EXCEPTION
    WHEN OTHERS THEN
      DBMS_OUTPUT.PUT_LINE('TEST 3 (freight not associated with given train): OK -> ' || SQLERRM);
END;

  ---------------------------------------------------------------------------
  -- TEST 4: Freight not associated with any train
  ---------------------------------------------------------------------------
BEGIN
    v_ret := USBD41_REMOVE_FREIGHT_FROM_TRAIN(v_freight_c, v_train_a);
    DBMS_OUTPUT.PUT_LINE('TEST 4 (freight not associated with any train): FAILED');
EXCEPTION
    WHEN OTHERS THEN
      DBMS_OUTPUT.PUT_LINE('TEST 4 (freight not associated with any train): OK -> ' || SQLERRM);
END;

  ---------------------------------------------------------------------------
  -- TEST 5: Non-existing freight
  ---------------------------------------------------------------------------
BEGIN
    v_ret := USBD41_REMOVE_FREIGHT_FROM_TRAIN(v_fake_freight, v_train_a);
    DBMS_OUTPUT.PUT_LINE('TEST 5 (non-existing freight): FAILED');
EXCEPTION
    WHEN OTHERS THEN
      DBMS_OUTPUT.PUT_LINE('TEST 5 (non-existing freight): OK -> ' || SQLERRM);
END;

  ---------------------------------------------------------------------------
  -- TEST 6: Non-existing train
  ---------------------------------------------------------------------------
BEGIN
    v_ret := USBD41_REMOVE_FREIGHT_FROM_TRAIN(v_freight_b, v_fake_train);
    DBMS_OUTPUT.PUT_LINE('TEST 6 (non-existing train): FAILED');
EXCEPTION
    WHEN OTHERS THEN
      DBMS_OUTPUT.PUT_LINE('TEST 6 (non-existing train): OK -> ' || SQLERRM);
END;

  ---------------------------------------------------------------------------
  -- TEST 7: Invalid input values
  ---------------------------------------------------------------------------
BEGIN
    v_ret := USBD41_REMOVE_FREIGHT_FROM_TRAIN(NULL, v_train_a);
    DBMS_OUTPUT.PUT_LINE('TEST 7A (NULL freight id): FAILED');
EXCEPTION
    WHEN OTHERS THEN
      DBMS_OUTPUT.PUT_LINE('TEST 7A (NULL freight id): OK -> ' || SQLERRM);
END;

BEGIN
    v_ret := USBD41_REMOVE_FREIGHT_FROM_TRAIN(v_freight_b, NULL);
    DBMS_OUTPUT.PUT_LINE('TEST 7B (NULL train id): FAILED');
EXCEPTION
    WHEN OTHERS THEN
      DBMS_OUTPUT.PUT_LINE('TEST 7B (NULL train id): OK -> ' || SQLERRM);
END;

BEGIN
    v_ret := USBD41_REMOVE_FREIGHT_FROM_TRAIN(0, v_train_a);
    DBMS_OUTPUT.PUT_LINE('TEST 7C (freight id = 0): FAILED');
EXCEPTION
    WHEN OTHERS THEN
      DBMS_OUTPUT.PUT_LINE('TEST 7C (freight id = 0): OK -> ' || SQLERRM);
END;

BEGIN
    v_ret := USBD41_REMOVE_FREIGHT_FROM_TRAIN(v_freight_b, 0);
    DBMS_OUTPUT.PUT_LINE('TEST 7D (train id = 0): FAILED');
EXCEPTION
    WHEN OTHERS THEN
      DBMS_OUTPUT.PUT_LINE('TEST 7D (train id = 0): OK -> ' || SQLERRM);
END;

  DBMS_OUTPUT.PUT_LINE('=== USBD41 TESTS END (ROLLBACK) ===');
END;
/
ROLLBACK;
