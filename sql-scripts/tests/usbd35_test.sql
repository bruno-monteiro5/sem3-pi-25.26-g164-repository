SET SERVEROUTPUT ON;

BEGIN
  DBMS_OUTPUT.PUT_LINE('=== USBD35_ADD_FACILITY Test ===');

  -------------------------------------------------------------------
  -- Setup: insert a valid facility (should succeed without error)
  -------------------------------------------------------------------
  USBD35_ADD_FACILITY(999001, 'Test Facility Alpha', 1);
  DBMS_OUTPUT.PUT_LINE('Setup: Facility 999001 inserted -> OK');

  -------------------------------------------------------------------
  -- TEST 1: Duplicate ID (should fail – ID 999001 already exists)
  -------------------------------------------------------------------
BEGIN
    USBD35_ADD_FACILITY(999001, 'Duplicate ID Facility', 2);
    DBMS_OUTPUT.PUT_LINE('TEST 1 (duplicate id): FAILED (error not raised)');
EXCEPTION
    WHEN OTHERS THEN
      DBMS_OUTPUT.PUT_LINE('TEST 1 (duplicate id): OK -> ' || SQLERRM);
END;

  -------------------------------------------------------------------
  -- TEST 2: Empty name (should fail – name cannot be just whitespace)
  -------------------------------------------------------------------
BEGIN
    USBD35_ADD_FACILITY(999002, '   ', 1);
    DBMS_OUTPUT.PUT_LINE('TEST 2 (empty name): FAILED (error not raised)');
EXCEPTION
    WHEN OTHERS THEN
      DBMS_OUTPUT.PUT_LINE('TEST 2 (empty name): OK -> ' || SQLERRM);
END;

  -------------------------------------------------------------------
  -- TEST 3: NULL name (should fail – name cannot be NULL)
  -------------------------------------------------------------------
BEGIN
    USBD35_ADD_FACILITY(999003, NULL, 1);
    DBMS_OUTPUT.PUT_LINE('TEST 3 (NULL name): FAILED (error not raised)');
EXCEPTION
    WHEN OTHERS THEN
      DBMS_OUTPUT.PUT_LINE('TEST 3 (NULL name): OK -> ' || SQLERRM);
END;

  -------------------------------------------------------------------
  -- TEST 4: Invalid type (should fail – type id does not exist)
  -------------------------------------------------------------------
BEGIN
    USBD35_ADD_FACILITY(999004, 'Invalid Type Facility', 9999);
    DBMS_OUTPUT.PUT_LINE('TEST 4 (invalid type): FAILED (error not raised)');
EXCEPTION
    WHEN OTHERS THEN
      DBMS_OUTPUT.PUT_LINE('TEST 4 (invalid type): OK -> ' || SQLERRM);
END;

  -------------------------------------------------------------------
  -- TEST 5: NULL type (should fail – type cannot be NULL)
  -------------------------------------------------------------------
BEGIN
    USBD35_ADD_FACILITY(999005, 'No Type Facility', NULL);
    DBMS_OUTPUT.PUT_LINE('TEST 5 (NULL type): FAILED (error not raised)');
EXCEPTION
    WHEN OTHERS THEN
      DBMS_OUTPUT.PUT_LINE('TEST 5 (NULL type): OK -> ' || SQLERRM);
END;

  -------------------------------------------------------------------
  -- TEST 6: ID = 0 (should fail – id must be > 0)
  -------------------------------------------------------------------
BEGIN
    USBD35_ADD_FACILITY(0, 'Invalid ID Facility A', 1);
    DBMS_OUTPUT.PUT_LINE('TEST 6 (id = 0): FAILED (error not raised)');
EXCEPTION
    WHEN OTHERS THEN
      DBMS_OUTPUT.PUT_LINE('TEST 6 (id = 0): OK -> ' || SQLERRM);
END;

  -------------------------------------------------------------------
  -- TEST 7: Negative ID (should fail – id must be > 0)
  -------------------------------------------------------------------
BEGIN
    USBD35_ADD_FACILITY(-5, 'Invalid ID Facility B', 1);
    DBMS_OUTPUT.PUT_LINE('TEST 7 (negative id): FAILED (error not raised)');
EXCEPTION
    WHEN OTHERS THEN
      DBMS_OUTPUT.PUT_LINE('TEST 7 (negative id): OK -> ' || SQLERRM);
END;

END;
/
ROLLBACK;
