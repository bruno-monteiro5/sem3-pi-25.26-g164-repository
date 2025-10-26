SELECT
    l.id AS line_id,
    l.name AS line_name,
    e1.name AS endpoint_a,
    e2.name AS endpoint_b,
    loc.id AS locomotive_id,
    loc.name AS locomotive_name,
    COUNT(ls.id) AS total_segments,
    ROUND(SUM(ls.length) / 1000.0, 2) AS total_distance_km,
    ROUND(SUM((ls.length / 1000.0) / loc.op_speed ) * 60, 2) AS total_time_minutes
FROM Line l
         JOIN Endpoint e1 ON l.endpoint_a = e1.id
         JOIN Endpoint e2 ON l.endpoint_b = e2.id
         JOIN Line_Segment ls ON ls.line = l.id
         JOIN Locomotive loc ON loc.id = 5621
         JOIN Locomotive_Gauge lg ON lg.locomotive = loc.id
WHERE l.endpoint_a = 21
  AND l.endpoint_b = 16
  AND l.gauge = lg.gauge
GROUP BY l.id, l.name, e1.name, e2.name, loc.id, loc.name;