--Electric
SELECT l.*, e.voltage, e.frequency, g.width AS gauge_width
FROM Locomotive l
JOIN Electric_Locomotive e ON e.locomotive = l.id
JOIN   Locomotive_Gauge lg   ON lg.locomotive = l.id
JOIN Gauge g ON g.id = lg.gauge
WHERE l.operator = 'PT509017800'
AND g.id = 2;

--Diesel
SELECT l.*, d.fuel_capacity, g.width AS gauge_width
FROM   Locomotive l
JOIN   Diesel_Locomotive d ON d.locomotive = l.id
JOIN   Locomotive_Gauge lg ON lg.locomotive = l.id
JOIN Gauge g ON g.id = lg.gauge
WHERE  l.operator = 'PT509017800'
AND  g.id = 2;