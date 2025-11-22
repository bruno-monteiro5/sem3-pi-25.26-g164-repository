SET SERVEROUTPUT ON;

DECLARE
percList        SYS_REFCURSOR;
    v_operator      Operator.short_name%TYPE;
    v_total_loco    NUMBER;
    v_electric_loco NUMBER;
    v_percentage    NUMBER;
BEGIN
    percList := f_usbd29_electric_percentage('CapTrain');

    DBMS_OUTPUT.PUT_LINE('USBD29 - Percentage of electric locomotives');
    DBMS_OUTPUT.PUT_LINE('-------------------------------------------');

    LOOP
FETCH percList
        INTO v_operator, v_total_loco, v_electric_loco, v_percentage;
        EXIT WHEN percList%NOTFOUND;

        DBMS_OUTPUT.PUT_LINE(
            'operator='   || v_operator ||
            ' | total='   || v_total_loco ||
            ' | electric='|| v_electric_loco ||
            ' | percentage=' || v_percentage || '%'
        );
END LOOP;
END;
/
