SELECT
    l.id AS line_id,
    l.name AS line_name,
    e1.name AS endpoint_a,
    e2.name AS endpoint_b,
    loc.id AS locomotive_id,
    loc.name AS locomotive_name,
    COUNT(ls.id) AS total_segments,
    ROUND(SUM(ls.length) / 1000.0, 2) AS total_distance_km,
    ROUND(
            SUM( ((ls.length / 1000.0) / LEAST(loc.op_speed, ls.speed_limit)) * 60 )
        , 2) AS total_time_minutes
FROM Line l
         JOIN Endpoint e1 ON e1.id = l.endpoint_a
         JOIN Endpoint e2 ON e2.id = l.endpoint_b
         JOIN Line_Segment ls ON ls.line = l.id
         JOIN Locomotive loc ON loc.id = 5621
         JOIN Locomotive_Gauge lg ON lg.locomotive = loc.id AND lg.gauge = l.gauge
         LEFT JOIN Electric_Locomotive el ON el.locomotive = loc.id
WHERE l.endpoint_a = 7
  AND l.endpoint_b = 5
  AND (el.locomotive IS NULL OR ls.is_electrified = 'Y')
GROUP BY l.id, l.name, e1.name, e2.name, loc.id, loc.name;
