    -- === GAUGE (by width; reference by subquery on width) ===
INSERT INTO Gauge (id, width) VALUES (1, 1435);
INSERT INTO Gauge (id, width) VALUES (2, 1668);

-- === TRACKTYPE (referenced by name) ===
INSERT INTO TrackType (name) VALUES ('Double');
INSERT INTO TrackType (name) VALUES ('Quadruple');

-- === OWNER ===
INSERT INTO Owner (vat, name, short_name) VALUES ('PT503933813', 'Infraestruturas de Portugal, SA', 'IP');

-- === OPERATOR ===
INSERT INTO Operator (vat, name, short_name) VALUES ('PT509017800', 'Medway - Operador Ferroviário de Mercadorias, S.A', 'Medway');







-- === METAFACILITY / FACILITY ===
INSERT INTO Metafacility (id, name) VALUES (1, 'São Romão');
INSERT INTO Facility (id, name, type) VALUES (1, 'Estação São Romão', 1);
INSERT INTO Metafacility (id, name) VALUES (2, 'Tamel');
INSERT INTO Facility (id, name, type) VALUES (2, 'Estação Tamel', 1);
INSERT INTO Metafacility (id, name) VALUES (3, 'Senhora das Dores');
INSERT INTO Facility (id, name, type) VALUES (3, 'Estação Senhora das Dores', 1);
INSERT INTO Metafacility (id, name) VALUES (4, 'Lousado');
INSERT INTO Facility (id, name, type) VALUES (4, 'Estação Lousado', 1);
INSERT INTO Metafacility (id, name) VALUES (5, 'Porto Campanhã');
INSERT INTO Facility (id, name, type) VALUES (5, 'Estação Porto Campanhã', 1);
INSERT INTO Metafacility (id, name) VALUES (6, 'Leandro');
INSERT INTO Facility (id, name, type) VALUES (6, 'Estação Leandro', 1);
INSERT INTO Metafacility (id, name) VALUES (7, 'Porto São Bento');
INSERT INTO Facility (id, name, type) VALUES (7, 'Estação Porto São Bento', 1);
INSERT INTO Metafacility (id, name) VALUES (8, 'Barcelos');
INSERT INTO Facility (id, name, type) VALUES (8, 'Estação Barcelos', 1);
INSERT INTO Metafacility (id, name) VALUES (9, 'Vila Nova da Cerveira');
INSERT INTO Facility (id, name, type) VALUES (9, 'Estação Vila Nova da Cerveira', 1);
INSERT INTO Metafacility (id, name) VALUES (10, 'Midões');
INSERT INTO Facility (id, name, type) VALUES (10, 'Estação Midões', 1);
INSERT INTO Metafacility (id, name) VALUES (11, 'Valença');
INSERT INTO Facility (id, name, type) VALUES (11, 'Estação Valença', 1);
INSERT INTO Metafacility (id, name) VALUES (12, 'Darque');
INSERT INTO Facility (id, name, type) VALUES (12, 'Estação Darque', 1);
INSERT INTO Metafacility (id, name) VALUES (13, 'Contumil');
INSERT INTO Facility (id, name, type) VALUES (13, 'Estação Contumil', 1);
INSERT INTO Metafacility (id, name) VALUES (14, 'Ermesinde');
INSERT INTO Facility (id, name, type) VALUES (14, 'Estação Ermesinde', 1);
INSERT INTO Metafacility (id, name) VALUES (15, 'São Frutuoso');
INSERT INTO Facility (id, name, type) VALUES (15, 'Estação São Frutuoso', 1);
INSERT INTO Metafacility (id, name) VALUES (16, 'São Pedro da Torre');
INSERT INTO Facility (id, name, type) VALUES (16, 'Estação São Pedro da Torre', 1);
INSERT INTO Metafacility (id, name) VALUES (17, 'Viana do Castelo');
INSERT INTO Facility (id, name, type) VALUES (17, 'Estação Viana do Castelo', 1);
INSERT INTO Metafacility (id, name) VALUES (18, 'Famalicão');
INSERT INTO Facility (id, name, type) VALUES (18, 'Estação Famalicão', 1);
INSERT INTO Metafacility (id, name) VALUES (19, 'Barroselas');
INSERT INTO Facility (id, name, type) VALUES (19, 'Estação Barroselas', 1);
INSERT INTO Metafacility (id, name) VALUES (20, 'Nine');
INSERT INTO Facility (id, name, type) VALUES (20, 'Estação Nine', 1);
INSERT INTO Metafacility (id, name) VALUES (21, 'Caminha');
INSERT INTO Facility (id, name, type) VALUES (21, 'Estação Caminha', 1);
INSERT INTO Metafacility (id, name) VALUES (22, 'Carvalha');
INSERT INTO Facility (id, name, type) VALUES (22, 'Estação Carvalha', 1);
INSERT INTO Metafacility (id, name) VALUES (23, 'Carreço');
INSERT INTO Facility (id, name, type) VALUES (23, 'Estação Carreço', 1);

-- === ENDPOINT ===
INSERT INTO Endpoint (id, name, metafacility) VALUES (7, 'Porto São Bento', 7);
INSERT INTO Endpoint (id, name, metafacility) VALUES (5, 'Porto Campanhã', 5);
INSERT INTO Endpoint (id, name, metafacility) VALUES (20, 'Nine', 20);
INSERT INTO Endpoint (id, name, metafacility) VALUES (8, 'Barcelos', 8);
INSERT INTO Endpoint (id, name, metafacility) VALUES (17, 'Viana do Castelo', 17);
INSERT INTO Endpoint (id, name, metafacility) VALUES (21, 'Caminha', 21);
INSERT INTO Endpoint (id, name, metafacility) VALUES (16, 'São Pedro da Torre', 16);
INSERT INTO Endpoint (id, name, metafacility) VALUES (11, 'Valença', 11);

-- === Line ===
INSERT INTO Line (id, name, owner, endpoint_a, endpoint_b, gauge)
VALUES (1, 'Ramal São Bento - Campanhã', 'PT503933813', 7, 5,
        (SELECT id FROM Gauge WHERE width = 1668));

INSERT INTO Line (id, name, owner, endpoint_a, endpoint_b, gauge)
VALUES (2, 'Ramal Camapanhã - Nine', 'PT503933813', 5, 20,
        (SELECT id FROM Gauge WHERE width = 1668));

INSERT INTO Line (id, name, owner, endpoint_a, endpoint_b, gauge)
VALUES (3, 'Ramal Nine - Barcelos', 'PT503933813', 20, 8,
        (SELECT id FROM Gauge WHERE width = 1668));

INSERT INTO Line (id, name, owner, endpoint_a, endpoint_b, gauge)
VALUES (4, 'Ramal Barcelos - Viana', 'PT503933813', 8, 17,
        (SELECT id FROM Gauge WHERE width = 1668));

INSERT INTO Line (id, name, owner, endpoint_a, endpoint_b, gauge)
VALUES (5, 'Ramal viana - Caminha', 'PT503933813', 17, 21,
        (SELECT id FROM Gauge WHERE width = 1668));

INSERT INTO Line (id, name, owner, endpoint_a, endpoint_b, gauge)
VALUES (6, 'Ramal Caminha - Torre', 'PT503933813', 21, 16,
        (SELECT id FROM Gauge WHERE width = 1668));

INSERT INTO Line (id, name, owner, endpoint_a, endpoint_b, gauge)
VALUES (7, 'Ramal Torre - Valença', 'PT503933813', 16, 11,
        (SELECT id FROM Gauge WHERE width = 1668));

-- === LOCOMOTIVE / SUBTYPES / GAUGE ===
INSERT INTO Locomotive (id, name, make, model, entry_year, power, acceleration, length, width, height, total_weight, number_bogies, operator, max_speed, op_speed, traction, bogie_arrangement) VALUES (5621, 'Inês', 'Siemens', 'Eurosprinter', 1995, 5600, 0, 19.2, 3.0, 4.375, 87, 2, 'PT509017800', 220.0, 70.0, 300,'Bo-Bo');
INSERT INTO Electric_Locomotive (locomotive, voltage, frequency) VALUES (5621, 25, 50);
INSERT INTO Locomotive_Gauge (gauge, locomotive) VALUES ((SELECT id FROM Gauge WHERE width=1668), 5621);
INSERT INTO Locomotive (id, name, make, model, entry_year, power, acceleration, length, width, height, total_weight, number_bogies, operator, max_speed, op_speed, traction, bogie_arrangement) VALUES (5623, 'Paz', 'Siemens', 'Eurosprinter', 1995, 5600, 0, 19.2, 3.0, 4.375, 87, 2, 'PT509017800', 220.0, 70.0, 300,'Bo-Bo');
INSERT INTO Electric_Locomotive (locomotive, voltage, frequency) VALUES (5623, 25, 50);
INSERT INTO Locomotive_Gauge (gauge, locomotive) VALUES ((SELECT id FROM Gauge WHERE width=1668), 5623);
INSERT INTO Locomotive (id, name, make, model, entry_year, power, acceleration, length, width, height, total_weight, number_bogies, operator, max_speed, op_speed, traction, bogie_arrangement) VALUES (5630, 'Helena', 'Siemens', 'Eurosprinter', 1996, 5600, 0, 19.2, 3.0, 4.375, 87, 2, 'PT509017800', 220.0, 70.0, 300,'Bo-Bo');
INSERT INTO Electric_Locomotive (locomotive, voltage, frequency) VALUES (5630, 25, 50);
INSERT INTO Locomotive_Gauge (gauge, locomotive) VALUES ((SELECT id FROM Gauge WHERE width=1668), 5630);
INSERT INTO Locomotive (id, name, make, model, entry_year, power, acceleration, length, width, height, total_weight, number_bogies, operator, max_speed, op_speed, traction, bogie_arrangement) VALUES (1903, 'Eva', '	Sorefame - Alsthom', 'CP 1900', 1981, 1623, 0, 19.084, 3.062, 4.31, 117, 2, 'PT509017800', 100.0, 42.5, 396,'Co-Co');
INSERT INTO Diesel_Locomotive (locomotive, fuel_capacity) VALUES (1903, 4882);
INSERT INTO Locomotive_Gauge (gauge, locomotive) VALUES ((SELECT id FROM Gauge WHERE width=1668), 1903);

-- === WAGON MODEL + WAGON MODEL ↔ GAUGE ===
INSERT INTO Wagon_Model (id, model, maker, number_bogies, length, width, heigth, weight, max_speed, payload, volume, type) VALUES (1245, 'Tadgs 32 94 082 3', 'Metalsines', 2, 17240, 3072, 4270, 24.0, 120, 56.0, 75.0,'Cereal wagon');
INSERT INTO Wagon_Model_Gauge (Wagon_Modelid, Gaugeid) VALUES (1245, (SELECT id FROM Gauge WHERE width=1668));
INSERT INTO Wagon_Model (id, model, maker, number_bogies, length, width, heigth, weight, max_speed, payload, volume, type) VALUES (1278, 'Tdgs 41 94 074 1', 'Equimetal', 2, 9640, 3120, 4165, 13.8, 100, 26.2, 38.0,'Cereal wagon');
INSERT INTO Wagon_Model_Gauge (Wagon_Modelid, Gaugeid) VALUES (1278, (SELECT id FROM Gauge WHERE width=1668));
INSERT INTO Wagon_Model (id, model, maker, number_bogies, length, width, heigth, weight, max_speed, payload, volume, type) VALUES (1325, 'Gabs 81 94 181 1', 'Sepsa Cometna', 2, 21700, 3180, 4170, 29.8, 100, 50.2, 110.0,'Covered wagon with sliding door');
INSERT INTO Wagon_Model_Gauge (Wagon_Modelid, Gaugeid) VALUES (1325, (SELECT id FROM Gauge WHERE width=1668));
INSERT INTO Wagon_Model (id, model, maker, number_bogies, length, width, heigth, weight, max_speed, payload, volume, type) VALUES (1104, 'Regmms 32 94 356 3', 'Metalsines', 2, 14040, 3104, 2535, 21.2, 120, 60.6, 76.3,'Container wagon (max 40'' HC)');
INSERT INTO Wagon_Model_Gauge (Wagon_Modelid, Gaugeid) VALUES (1104, (SELECT id FROM Gauge WHERE width=1668));
INSERT INTO Wagon_Model (id, model, maker, number_bogies, length, width, heigth, weight, max_speed, payload, volume, type) VALUES (985, 'Lgs 22 94 441 6', 'Metalsines', 1, 13860, 2850, 1060, 11.9, 120, 28.1, 76.3,'Container wagon (max 40'' HC)');
INSERT INTO Wagon_Model_Gauge (Wagon_Modelid, Gaugeid) VALUES (985, (SELECT id FROM Gauge WHERE width=1668));
INSERT INTO Wagon_Model (id, model, maker, number_bogies, length, width, heigth, weight, max_speed, payload, volume, type) VALUES (987, 'Sgnss 12 94 455 2', 'Emef', 2, 18116, 2950, 1030, 21.6, 120, 68.4, 76.3,'Container wagon (max 40'' HC)');
INSERT INTO Wagon_Model_Gauge (Wagon_Modelid, Gaugeid) VALUES (987, (SELECT id FROM Gauge WHERE width=1668));
INSERT INTO Wagon_Model (id, model, maker, number_bogies, length, width, heigth, weight, max_speed, payload, volume, type) VALUES (988, 'Sgnss 12 94 455 2', 'Emef', 2, 18116, 2950, 1030, 21.6, 120, 68.4, 76.3,'Container wagon (max 40'' HC)');
INSERT INTO Wagon_Model_Gauge (Wagon_Modelid, Gaugeid) VALUES (988, (SELECT id FROM Gauge WHERE width=1435));

-- === WAGON ===
INSERT INTO Wagon (id, volume, tare, wagon_model, operator) VALUES (3563077, 1, 1, 1104, 'PT509017800');
INSERT INTO Wagon (id, volume, tare, wagon_model, operator) VALUES (3563078, 1, 1, 1104, 'PT509017800');
INSERT INTO Wagon (id, volume, tare, wagon_model, operator) VALUES (3563079, 1, 1, 1104, 'PT509017800');
INSERT INTO Wagon (id, volume, tare, wagon_model, operator) VALUES (3563080, 1, 1, 1104, 'PT509017800');
INSERT INTO Wagon (id, volume, tare, wagon_model, operator) VALUES (3563081, 1, 1, 1104, 'PT509017800');
INSERT INTO Wagon (id, volume, tare, wagon_model, operator) VALUES (3563082, 1, 1, 1104, 'PT509017800');
INSERT INTO Wagon (id, volume, tare, wagon_model, operator) VALUES (3563083, 1, 1, 1104, 'PT509017800');
INSERT INTO Wagon (id, volume, tare, wagon_model, operator) VALUES (3563084, 1, 1, 1104, 'PT509017800');
INSERT INTO Wagon (id, volume, tare, wagon_model, operator) VALUES (3563085, 1, 1, 1104, 'PT509017800');
INSERT INTO Wagon (id, volume, tare, wagon_model, operator) VALUES (3563086, 1, 1, 1104, 'PT509017800');
INSERT INTO Wagon (id, volume, tare, wagon_model, operator) VALUES (3563087, 1, 1, 1104, 'PT509017800');
INSERT INTO Wagon (id, volume, tare, wagon_model, operator) VALUES (3563088, 1, 1, 1104, 'PT509017800');
INSERT INTO Wagon (id, volume, tare, wagon_model, operator) VALUES (3563089, 1, 1, 1104, 'PT509017800');
INSERT INTO Wagon (id, volume, tare, wagon_model, operator) VALUES (3563090, 1, 1, 1104, 'PT509017800');
INSERT INTO Wagon (id, volume, tare, wagon_model, operator) VALUES (3563091, 1, 1, 1104, 'PT509017800');
INSERT INTO Wagon (id, volume, tare, wagon_model, operator) VALUES (3563092, 1, 1, 1104, 'PT509017800');
INSERT INTO Wagon (id, volume, tare, wagon_model, operator) VALUES (823045, 1, 1, 1245, 'PT509017800');
INSERT INTO Wagon (id, volume, tare, wagon_model, operator) VALUES (823046, 1, 1, 1245, 'PT509017800');
INSERT INTO Wagon (id, volume, tare, wagon_model, operator) VALUES (823047, 1, 1, 1245, 'PT509017800');
INSERT INTO Wagon (id, volume, tare, wagon_model, operator) VALUES (823048, 1, 1, 1245, 'PT509017800');
INSERT INTO Wagon (id, volume, tare, wagon_model, operator) VALUES (741001, 1, 1, 1278, 'PT509017800');
INSERT INTO Wagon (id, volume, tare, wagon_model, operator) VALUES (741002, 1, 1, 1278, 'PT509017800');
INSERT INTO Wagon (id, volume, tare, wagon_model, operator) VALUES (741003, 1, 1, 1278, 'PT509017800');
INSERT INTO Wagon (id, volume, tare, wagon_model, operator) VALUES (741004, 1, 1, 1278, 'PT509017800');
INSERT INTO Wagon (id, volume, tare, wagon_model, operator) VALUES (741005, 1, 1, 1278, 'PT509017800');
INSERT INTO Wagon (id, volume, tare, wagon_model, operator) VALUES (741006, 1, 1, 1278, 'PT509017800');
INSERT INTO Wagon (id, volume, tare, wagon_model, operator) VALUES (1811010, 1, 1, 1325, 'PT509017800');
INSERT INTO Wagon (id, volume, tare, wagon_model, operator) VALUES (1811011, 1, 1, 1325, 'PT509017800');
INSERT INTO Wagon (id, volume, tare, wagon_model, operator) VALUES (1811012, 1, 1, 1325, 'PT509017800');
INSERT INTO Wagon (id, volume, tare, wagon_model, operator) VALUES (1811013, 1, 1, 1325, 'PT509017800');
INSERT INTO Wagon (id, volume, tare, wagon_model, operator) VALUES (1811014, 1, 1, 1325, 'PT509017800');

-- === LINE SEGMENT (no gauge) ===
INSERT INTO Line_Segment (id, is_electrified, speed_limit, max_weight, length, line, track_type) VALUES (1, 'Y', 120, 8000, 2618, 1, (SELECT id FROM TrackType WHERE name='Quadruple'));
INSERT INTO Line_Segment (id, is_electrified, speed_limit, max_weight, length, line, track_type) VALUES (10, 'Y', 120, 8000, 29003, 2, (SELECT id FROM TrackType WHERE name='Double'));
INSERT INTO Line_Segment (id, is_electrified, speed_limit, max_weight, length, line, track_type) VALUES (11, 'Y', 120, 8000, 10000, 2, (SELECT id FROM TrackType WHERE name='Double'));
INSERT INTO Line_Segment (id, is_electrified, speed_limit, max_weight, length, line, track_type) VALUES (15, 'Y', 120, 8000, 5286, 3, (SELECT id FROM TrackType WHERE name='Double'));
INSERT INTO Line_Segment (id, is_electrified, speed_limit, max_weight, length, line, track_type) VALUES (16, 'Y', 120, 8000, 6000, 3,(SELECT id FROM TrackType WHERE name='Double'));
INSERT INTO Line_Segment (id, is_electrified, speed_limit, max_weight, length, line, track_type) VALUES (14, 'Y', 120, 8000, 10387, 4, (SELECT id FROM TrackType WHERE name='Double'));
INSERT INTO Line_Segment (id, is_electrified, speed_limit, max_weight, length, line, track_type) VALUES (12, 'Y', 120, 8000, 12000, 4, (SELECT id FROM TrackType WHERE name='Double'));
INSERT INTO Line_Segment (id, is_electrified, speed_limit, max_weight, length, line, track_type) VALUES (13, 'Y', 120, 6400, 8000, 4, (SELECT id FROM TrackType WHERE name='Double'));
INSERT INTO Line_Segment (id, is_electrified, speed_limit, max_weight, length, line, track_type) VALUES (20, 'Y', 120, 8000, 6000, 5, (SELECT id FROM TrackType WHERE name='Double'));
INSERT INTO Line_Segment (id, is_electrified, speed_limit, max_weight, length, line, track_type) VALUES (21, 'Y', 120, 8000, 3000, 5, (SELECT id FROM TrackType WHERE name='Double'));
INSERT INTO Line_Segment (id, is_electrified, speed_limit, max_weight, length, line, track_type) VALUES (22, 'Y', 120, 8000, 15000, 5, (SELECT id FROM TrackType WHERE name='Double'));
INSERT INTO Line_Segment (id, is_electrified, speed_limit, max_weight, length, line, track_type) VALUES (25, 'Y', 120, 8000, 20829, 6, (SELECT id FROM TrackType WHERE name='Double'));
INSERT INTO Line_Segment (id, is_electrified, speed_limit, max_weight, length, line, track_type) VALUES (26, 'Y', 120, 8000, 4264, 7, (SELECT id FROM TrackType WHERE name='Double'));





















-- === LINE SEGMENT (no gauge)  ESTÁ MAL POR AGORA MAS ACHO QUE DEVE ESTAR CORRETO===
INSERT INTO Line_Segment (id, is_electrified, speed_limit, max_weight, length, line, gauge, track_type) VALUES (1, 'Y', 120, 8000, 2618, 1, (SELECT gauge FROM Line WHERE id=1), (SELECT id FROM TrackType WHERE name='Quadruple'));
INSERT INTO Line_Segment (id, is_electrified, speed_limit, max_weight, length, line, gauge, track_type) VALUES (10, 'Y', 120, 8000, 29003, 2, (SELECT gauge FROM Line WHERE id=2), (SELECT id FROM TrackType WHERE name='Double'));
INSERT INTO Line_Segment (id, is_electrified, speed_limit, max_weight, length, line, gauge, track_type) VALUES (11, 'Y', 120, 8000, 10000, 2, (SELECT gauge FROM Line WHERE id=2), (SELECT id FROM TrackType WHERE name='Double'));
INSERT INTO Line_Segment (id, is_electrified, speed_limit, max_weight, length, line, gauge, track_type) VALUES (15, 'Y', 120, 8000, 5286, 3, (SELECT gauge FROM Line WHERE id=3), (SELECT id FROM TrackType WHERE name='Double'));
INSERT INTO Line_Segment (id, is_electrified, speed_limit, max_weight, length, line, gauge, track_type) VALUES (16, 'Y', 120, 8000, 6000, 3, (SELECT gauge FROM Line WHERE id=3), (SELECT id FROM TrackType WHERE name='Double'));
INSERT INTO Line_Segment (id, is_electrified, speed_limit, max_weight, length, line, gauge, track_type) VALUES (14, 'Y', 120, 8000, 10387, 4, (SELECT gauge FROM Line WHERE id=4), (SELECT id FROM TrackType WHERE name='Double'));
INSERT INTO Line_Segment (id, is_electrified, speed_limit, max_weight, length, line, gauge, track_type) VALUES (12, 'Y', 120, 8000, 12000, 4, (SELECT gauge FROM Line WHERE id=4), (SELECT id FROM TrackType WHERE name='Double'));
INSERT INTO Line_Segment (id, is_electrified, speed_limit, max_weight, length, line, gauge, track_type) VALUES (13, 'Y', 120, 6400, 8000, 4, (SELECT gauge FROM Line WHERE id=4), (SELECT id FROM TrackType WHERE name='Double'));
INSERT INTO Line_Segment (id, is_electrified, speed_limit, max_weight, length, line, gauge, track_type) VALUES (20, 'Y', 120, 8000, 6000, 5, (SELECT gauge FROM Line WHERE id=5), (SELECT id FROM TrackType WHERE name='Double'));
INSERT INTO Line_Segment (id, is_electrified, speed_limit, max_weight, length, line, gauge, track_type) VALUES (21, 'Y', 120, 8000, 3000, 5, (SELECT gauge FROM Line WHERE id=5), (SELECT id FROM TrackType WHERE name='Double'));
INSERT INTO Line_Segment (id, is_electrified, speed_limit, max_weight, length, line, gauge, track_type) VALUES (22, 'Y', 120, 8000, 15000, 5, (SELECT gauge FROM Line WHERE id=5), (SELECT id FROM TrackType WHERE name='Double'));
INSERT INTO Line_Segment (id, is_electrified, speed_limit, max_weight, length, line, gauge, track_type) VALUES (25, 'Y', 120, 8000, 20829, 6, (SELECT gauge FROM Line WHERE id=6), (SELECT id FROM TrackType WHERE name='Double'));
INSERT INTO Line_Segment (id, is_electrified, speed_limit, max_weight, length, line, gauge, track_type) VALUES (26, 'Y', 120, 8000, 4264, 7, (SELECT gauge FROM Line WHERE id=7), (SELECT id FROM TrackType WHERE name='Double'));








COMMIT;