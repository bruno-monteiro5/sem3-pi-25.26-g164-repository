-- ============================================================
-- USBD38 - Tests for procedure USBD38_ADD_GAUGE
-- ============================================================

BEGIN
  ----------------------------------------------------------------
  -- SETUP: valid gauges (must succeed)
  ----------------------------------------------------------------
  USBD38_ADD_GAUGE(999001, 1999001, 'Gauge Test USBD38 A');
  DBMS_OUTPUT.PUT_LINE('SETUP 1: valid gauge inserted -> OK');

  USBD38_ADD_GAUGE(999002, 1999002, 'Gauge Test USBD38 B');
  DBMS_OUTPUT.PUT_LINE('SETUP 2: valid gauge inserted -> OK');

  ----------------------------------------------------------------
  -- TEST 1: duplicate ID (should fail)
  ----------------------------------------------------------------
BEGIN
    USBD38_ADD_GAUGE(999001, 1999003, 'Gauge Test USBD38 C');
    DBMS_OUTPUT.PUT_LINE('TEST 1 (duplicate ID): FAILED (should have raised an error)');
EXCEPTION
    WHEN OTHERS THEN
      DBMS_OUTPUT.PUT_LINE('TEST 1 (duplicate ID): OK -> ' || SQLERRM);
END;

  ----------------------------------------------------------------
  -- TEST 2: duplicate WIDTH (should fail)
  ----------------------------------------------------------------
BEGIN
    USBD38_ADD_GAUGE(999003, 1999002, 'Gauge Test USBD38 D');
    DBMS_OUTPUT.PUT_LINE('TEST 2 (duplicate width): FAILED (should have raised an error)');
EXCEPTION
    WHEN OTHERS THEN
      DBMS_OUTPUT.PUT_LINE('TEST 2 (duplicate width): OK -> ' || SQLERRM);
END;

  ----------------------------------------------------------------
  -- TEST 3: duplicate NAME (case + spaces ignored) (should fail)
  ----------------------------------------------------------------
BEGIN
    USBD38_ADD_GAUGE(
      999004,
      1999004,
      '  gAuGe    tEsT   uSbD38   a  '
    );
    DBMS_OUTPUT.PUT_LINE('TEST 3 (duplicate name, case/spaces): FAILED (should have raised an error)');
EXCEPTION
    WHEN OTHERS THEN
      DBMS_OUTPUT.PUT_LINE('TEST 3 (duplicate name, case/spaces): OK -> ' || SQLERRM);
END;

  ----------------------------------------------------------------
  -- TEST 4: empty name (should fail)
  ----------------------------------------------------------------
BEGIN
    USBD38_ADD_GAUGE(999005, 1999005, '   ');
    DBMS_OUTPUT.PUT_LINE('TEST 4 (empty name): FAILED (should have raised an error)');
EXCEPTION
    WHEN OTHERS THEN
      DBMS_OUTPUT.PUT_LINE('TEST 4 (empty name): OK -> ' || SQLERRM);
END;

  ----------------------------------------------------------------
  -- TEST 5: NULL name (should fail)
  ----------------------------------------------------------------
BEGIN
    USBD38_ADD_GAUGE(999006, 1999006, NULL);
    DBMS_OUTPUT.PUT_LINE('TEST 5 (NULL name): FAILED (should have raised an error)');
EXCEPTION
    WHEN OTHERS THEN
      DBMS_OUTPUT.PUT_LINE('TEST 5 (NULL name): OK -> ' || SQLERRM);
END;

  ----------------------------------------------------------------
  -- TEST 6: width = 0 (should fail)
  ----------------------------------------------------------------
BEGIN
    USBD38_ADD_GAUGE(999007, 0, 'Gauge Test USBD38 Z');
    DBMS_OUTPUT.PUT_LINE('TEST 6 (width = 0): FAILED (should have raised an error)');
EXCEPTION
    WHEN OTHERS THEN
      DBMS_OUTPUT.PUT_LINE('TEST 6 (width = 0): OK -> ' || SQLERRM);
END;

  ----------------------------------------------------------------
  -- TEST 7: negative width (should fail)
  ----------------------------------------------------------------
BEGIN
    USBD38_ADD_GAUGE(999008, -10, 'Gauge Test USBD38 Y');
    DBMS_OUTPUT.PUT_LINE('TEST 7 (negative width): FAILED (should have raised an error)');
EXCEPTION
    WHEN OTHERS THEN
      DBMS_OUTPUT.PUT_LINE('TEST 7 (negative width): OK -> ' || SQLERRM);
END;

  ----------------------------------------------------------------
  -- TEST 8: ID = 0 (should fail)
  ----------------------------------------------------------------
BEGIN
    USBD38_ADD_GAUGE(0, 1999010, 'Gauge Test USBD38 X');
    DBMS_OUTPUT.PUT_LINE('TEST 8 (id = 0): FAILED (should have raised an error)');
EXCEPTION
    WHEN OTHERS THEN
      DBMS_OUTPUT.PUT_LINE('TEST 8 (id = 0): OK -> ' || SQLERRM);
END;

  ----------------------------------------------------------------
  -- TEST 9: negative ID (should fail)
  ----------------------------------------------------------------
BEGIN
    USBD38_ADD_GAUGE(-1, 1999011, 'Gauge Test USBD38 W');
    DBMS_OUTPUT.PUT_LINE('TEST 9 (negative id): FAILED (should have raised an error)');
EXCEPTION
    WHEN OTHERS THEN
      DBMS_OUTPUT.PUT_LINE('TEST 9 (negative id): OK -> ' || SQLERRM);
END;

END;
/


ROLLBACK;
