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


--Possible operators:
--INSERT INTO Operator (vat, name, short_name) VALUES ('PT509017800', 'Medway - Operador Ferroviário de Mercadorias, S.A', 'Medway');
--INSERT INTO Operator (vat, name, short_name) VALUES ('PT500000000', 'Comboios de Portugal', 'CP');
--INSERT INTO Operator (vat, name, short_name) VALUES ('PT509999999', 'Ferroviária Nacional de Carga', 'FNC');
--INSERT INTO Operator (vat, name, short_name) VALUES ('PT513273171', 'Captrain Portugal, SA', 'Captrain');
-- INTO Operator (vat, name, short_name) VALUES ('PT980000111', 'Alpha Trains Portugal, Lda', 'AlphaTrains');