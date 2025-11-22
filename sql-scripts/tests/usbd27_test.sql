SET SERVEROUTPUT ON;

DECLARE
wagonsList  SYS_REFCURSOR;
    v_wagon_id  Wagon.id%TYPE;
BEGIN
    wagonsList := f_usbd27_grain_wagons(
        DATE '2025-10-03',
        DATE '2025-10-06'
    );

    DBMS_OUTPUT.PUT_LINE(
        'USBD27 - Grain wagons used in ALL trains with grain wagons ' ||
        'between 2025-10-03 and 2025-10-06'
    );
    DBMS_OUTPUT.PUT_LINE('----------------------------------------------------------');

    LOOP
FETCH wagonsList INTO v_wagon_id;
        EXIT WHEN wagonsList%NOTFOUND;

        DBMS_OUTPUT.PUT_LINE('grain_wagon_id = ' || v_wagon_id);
END LOOP;
END;
/
