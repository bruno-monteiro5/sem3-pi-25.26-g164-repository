CREATE OR REPLACE FUNCTION f_usbd25_endpoints_no_stop (
    p_train_id IN Train.id%TYPE
) RETURN SYS_REFCURSOR
AS
    endpointsList SYS_REFCURSOR;
BEGIN
OPEN endpointsList FOR
SELECT
    Endpoint_Path.seq,
    Endpoint.id   AS endpoint_id,
    Endpoint.name AS endpoint_name
FROM Train
         JOIN Path ON Path.id = Train.path
         JOIN Endpoint_Path ON Endpoint_Path.path = Path.id
         JOIN Endpoint ON Endpoint.id = Endpoint_Path.endpoint
WHERE Train.id = p_train_id
  AND Endpoint_Path.must_stop = 'N';

RETURN endpointsList;
END;
/
