SELECT
    Endpoint_Path.seq,
    Endpoint.id   AS endpoint_id,
    Endpoint.name AS endpoint_name
FROM Train
         JOIN Path ON Path.id = Train.path
         JOIN Endpoint_Path ON Endpoint_Path.path = Path.id
         JOIN Endpoint ON Endpoint.id = Endpoint_Path.endpoint
WHERE Train.id = 5437                        -- choose the train here
  AND Endpoint_Path.must_stop = 'N'
-- ORDER BY Endpoint_Path.seq;

-- Possible train ids:
-- 5421
-- 5435
-- 5437
