SELECT tt.name AS track_type, COUNT(*) AS num_segments
FROM Line_Segment ls
JOIN Line l ON l.id = ls.line
JOIN TrackType tt ON tt.id = ls.track_type
WHERE l.owner = 'PT503933813'
GROUP BY tt.name;

--Possible owners
--INSERT INTO Owner (vat, name, short_name) VALUES ('PT503933813', 'Infraestruturas de Portugal, SA', 'IP');
-- INTO Owner (vat, name, short_name) VALUES ('PT501012435', 'CP Infraestruturas, SA', 'CPI');
--INSERT INTO Owner (vat, name, short_name) VALUES ('PT509498700', 'Estrutura Infraestruturas Ferroviárias, SA', 'EIF');
--INSERT INTO Owner (vat, name, short_name) VALUES ('PT505144939', ' Empresa de Manutenção Ferroviária, SA', 'EMEF');