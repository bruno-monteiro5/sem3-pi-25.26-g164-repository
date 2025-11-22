SET SERVEROUTPUT ON;

DECLARE
endpointsList   SYS_REFCURSOR;
    v_seq           Endpoint_Path.seq%TYPE;
    v_endpoint_id   Endpoint.id%TYPE;
    v_endpoint_name Endpoint.name%TYPE;
BEGIN

    -- Choose the train here:
    endpointsList := f_usbd25_endpoints_no_stop(5437);
    -- Possible train ids:
    -- 5421
    -- 5435
    -- 5437


    DBMS_OUTPUT.PUT_LINE('USBD25 - Endpoints (must_stop = N) for train 5437');
    DBMS_OUTPUT.PUT_LINE('-------------------------------------------------');

    LOOP
FETCH endpointsList INTO v_seq, v_endpoint_id, v_endpoint_name;
        EXIT WHEN endpointsList%NOTFOUND;

        DBMS_OUTPUT.PUT_LINE(
            'seq=' || v_seq ||
            ' | endpoint_id=' || v_endpoint_id ||
            ' | name=' || v_endpoint_name
        );
END LOOP;
END;
/
