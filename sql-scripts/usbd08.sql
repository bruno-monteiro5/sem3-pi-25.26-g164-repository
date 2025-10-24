-- Diesel
SELECT l.*, d.fuel_capacity
FROM   Locomotive l
JOIN   Diesel_Locomotive d ON d.locomotive = l.id;

--Electric
SELECT l.*, e.voltage, e.frequency
FROM   Locomotive l
JOIN   Electric_Locomotive e ON e.locomotive = l.id;
