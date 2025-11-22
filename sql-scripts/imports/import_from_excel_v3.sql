-- Tabela Owner
INSERT INTO Owner (vat, name, short_name) VALUES ('PT503933813', 'Infraestruturas de Portugal, SA', 'IP');
INSERT INTO Owner (vat, name, short_name) VALUES ('PT501012435', 'CP Infraestruturas, SA', 'CPI');
INSERT INTO Owner (vat, name, short_name) VALUES ('PT509498700', 'Estrutura Infraestruturas Ferroviárias, SA', 'EIF');
INSERT INTO Owner (vat, name, short_name) VALUES ('PT505144939', 'Empresa de Manutenção Ferroviária, SA', 'EMEF');

-- Tabela Operator
INSERT INTO Operator (vat, name, short_name) VALUES ('PT509017800', 'Medway - Operador Ferroviário de Mercadorias, S.A', 'Medway');
INSERT INTO Operator (vat, name, short_name) VALUES ('PT507832388', 'Captrain Portugal S.A.', 'Captrain');

-- Tabela BuildingType
INSERT INTO BuildingType (id, name) VALUES (1, 'Station');
INSERT INTO BuildingType (id, name) VALUES (2, 'Warehouse');
INSERT INTO BuildingType (id, name) VALUES (3, 'Grain silo');
INSERT INTO BuildingType (id, name) VALUES (4, 'Depot');
INSERT INTO BuildingType (id, name) VALUES (5, 'Office');
INSERT INTO BuildingType (id, name) VALUES (6, 'Tunnel');
INSERT INTO BuildingType (id, name) VALUES (7, 'Bridge');
INSERT INTO BuildingType (id, name) VALUES (8, 'Terminal');
INSERT INTO BuildingType (id, name) VALUES (9, 'Freight Yard');
INSERT INTO BuildingType (id, name) VALUES (10, 'Refrigerated Area');


-- Tabela Gauge
INSERT INTO Gauge (id, width) VALUES (1, 1435);
INSERT INTO Gauge (id, width) VALUES (2, 1668);

-- Tabela Metafacility
INSERT INTO Metafacility (id, name) VALUES (1, 'São Romão');
INSERT INTO Metafacility (id, name) VALUES (2, 'Tamel');
INSERT INTO Metafacility (id, name) VALUES (3, 'Senhora das Dores');
INSERT INTO Metafacility (id, name) VALUES (4, 'Lousado');
INSERT INTO Metafacility (id, name) VALUES (5, 'Porto Campanhã');
INSERT INTO Metafacility (id, name) VALUES (6, 'Leandro');
INSERT INTO Metafacility (id, name) VALUES (7, 'Porto São Bento');
INSERT INTO Metafacility (id, name) VALUES (8, 'Barcelos');
INSERT INTO Metafacility (id, name) VALUES (9, 'Vila Nova da Cerveira');
INSERT INTO Metafacility (id, name) VALUES (10, 'Midões');
INSERT INTO Metafacility (id, name) VALUES (11, 'Valença');
INSERT INTO Metafacility (id, name) VALUES (12, 'Darque');
INSERT INTO Metafacility (id, name) VALUES (13, 'Contumil');
INSERT INTO Metafacility (id, name) VALUES (14, 'Ermesinde');
INSERT INTO Metafacility (id, name) VALUES (15, 'São Frutuoso');
INSERT INTO Metafacility (id, name) VALUES (16, 'São Pedro da Torre');
INSERT INTO Metafacility (id, name) VALUES (17, 'Viana do Castelo');
INSERT INTO Metafacility (id, name) VALUES (18, 'Famalicão');
INSERT INTO Metafacility (id, name) VALUES (19, 'Barroselas');
INSERT INTO Metafacility (id, name) VALUES (20, 'Nine');
INSERT INTO Metafacility (id, name) VALUES (21, 'Caminha');
INSERT INTO Metafacility (id, name) VALUES (22, 'Carvalha');
INSERT INTO Metafacility (id, name) VALUES (23, 'Carreço');
INSERT INTO Metafacility (id, name) VALUES (43, 'São Gemil');
INSERT INTO Metafacility (id, name) VALUES (45, 'São Mamede de Infesta');
INSERT INTO Metafacility (id, name) VALUES (48, 'Leça do Balio');
INSERT INTO Metafacility (id, name) VALUES (50, 'Leixões');

-- Tabela Facility
INSERT INTO Facility (id, name, type) VALUES (1, 'São Romão', 1);
INSERT INTO Facility (id, name, type) VALUES (2, 'Tamel', 1);
INSERT INTO Facility (id, name, type) VALUES (3, 'Senhora das Dores', 1);
INSERT INTO Facility (id, name, type) VALUES (4, 'Lousado', 1);
INSERT INTO Facility (id, name, type) VALUES (5, 'Porto Campanhã', 1);
INSERT INTO Facility (id, name, type) VALUES (6, 'Leandro', 1);
INSERT INTO Facility (id, name, type) VALUES (7, 'Porto São Bento', 1);
INSERT INTO Facility (id, name, type) VALUES (8, 'Barcelos', 1);
INSERT INTO Facility (id, name, type) VALUES (9, 'Vila Nova da Cerveira', 1);
INSERT INTO Facility (id, name, type) VALUES (10, 'Midões', 1);
INSERT INTO Facility (id, name, type) VALUES (11, 'Valença', 1);
INSERT INTO Facility (id, name, type) VALUES (12, 'Darque', 1);
INSERT INTO Facility (id, name, type) VALUES (13, 'Contumil', 1);
INSERT INTO Facility (id, name, type) VALUES (14, 'Ermesinde', 1);
INSERT INTO Facility (id, name, type) VALUES (15, 'São Frutuoso', 1);
INSERT INTO Facility (id, name, type) VALUES (16, 'São Pedro da Torre', 1);
INSERT INTO Facility (id, name, type) VALUES (17, 'Viana do Castelo', 1);
INSERT INTO Facility (id, name, type) VALUES (18, 'Famalicão', 1);
INSERT INTO Facility (id, name, type) VALUES (19, 'Barroselas', 1);
INSERT INTO Facility (id, name, type) VALUES (20, 'Nine', 1);
INSERT INTO Facility (id, name, type) VALUES (21, 'Caminha', 1);
INSERT INTO Facility (id, name, type) VALUES (22, 'Carvalha', 1);
INSERT INTO Facility (id, name, type) VALUES (23, 'Carreço', 1);
INSERT INTO Facility (id, name, type) VALUES (43, 'São Gemil', 1);
INSERT INTO Facility (id, name, type) VALUES (45, 'São Mamede de Infesta', 1);
INSERT INTO Facility (id, name, type) VALUES (48, 'Leça do Balio', 1);
INSERT INTO Facility (id, name, type) VALUES (50, 'Leixões', 1);

-- Tabela Building
INSERT INTO Building (id, type, name, facility) VALUES (100, 2, 'Armazém Campanhã Nascente', 5);
INSERT INTO Building (id, type, name, facility) VALUES (101, 4, 'Depósito de Manutenção Campanhã', 5);
INSERT INTO Building (id, type, name, facility) VALUES (102, 2, 'Armazém Viana do Castelo', 17);
INSERT INTO Building (id, type, name, facility) VALUES (103, 3, 'Silo de Grão Viana', 17);
INSERT INTO Building (id, type, name, facility) VALUES (104, 6, 'Posto de Comando Valença', 11);
INSERT INTO Building (id, type, name, facility) VALUES (105, 5, 'Escritórios Operacionais Nine', 20);
INSERT INTO Building (id, type, name, facility) VALUES (106, 2, 'Armazém Barcelos', 8);
INSERT INTO Building (id, type, name, facility) VALUES (107, 1, 'Edifício da Estação Darque', 12);
INSERT INTO Building (id, type, name, facility) VALUES (108, 4, 'Oficina Contumil', 13);

-- Tabela Endpoint
INSERT INTO Endpoint (id, name, metafacility) VALUES (7, 'Porto São Bento', 7);
INSERT INTO Endpoint (id, name, metafacility) VALUES (5, 'Porto Campanhã', 5);
INSERT INTO Endpoint (id, name, metafacility) VALUES (20, 'Nine', 20);
INSERT INTO Endpoint (id, name, metafacility) VALUES (8, 'Barcelos', 8);
INSERT INTO Endpoint (id, name, metafacility) VALUES (17, 'Viana do Castelo', 17);
INSERT INTO Endpoint (id, name, metafacility) VALUES (21, 'Caminha', 21);
INSERT INTO Endpoint (id, name, metafacility) VALUES (16, 'São Pedro da Torre', 16);
INSERT INTO Endpoint (id, name, metafacility) VALUES (11, 'Valença', 11);
INSERT INTO Endpoint (id, name, metafacility) VALUES (13, 'Contumil', 13);
INSERT INTO Endpoint (id, name, metafacility) VALUES (14, 'Ermesinde', 14);
INSERT INTO Endpoint (id, name, metafacility) VALUES (15, 'São Frutuoso', 15);
INSERT INTO Endpoint (id, name, metafacility) VALUES (18, 'Famalicão', 18);
INSERT INTO Endpoint (id, name, metafacility) VALUES (19, 'Barroselas', 19);
INSERT INTO Endpoint (id, name, metafacility) VALUES (22, 'Carvalha', 22);
INSERT INTO Endpoint (id, name, metafacility) VALUES (23, 'Carreço', 23);
INSERT INTO Endpoint (id, name, metafacility) VALUES (12, 'Darque', 12);
INSERT INTO Endpoint (id, name, metafacility) VALUES (43, 'São Gemil', 43);
INSERT INTO Endpoint (id, name, metafacility) VALUES (45, 'São Mamede de Infesta', 45);
INSERT INTO Endpoint (id, name, metafacility) VALUES (48, 'Leça do Balio', 48);
INSERT INTO Endpoint (id, name, metafacility) VALUES (50, 'Leixões', 50);

-- Tabela Line
INSERT INTO Line (id, name, owner, endpoint_a, endpoint_b, gauge) VALUES (1, 'Ramal São Bento - Campanhã', 'PT503933813', 7, 5, 2);
INSERT INTO Line (id, name, owner, endpoint_a, endpoint_b, gauge) VALUES (2, 'Ramal Campanhã - Contumil', 'PT503933813', 5, 13, 2);
INSERT INTO Line (id, name, owner, endpoint_a, endpoint_b, gauge) VALUES (3, 'Ramal Contumil - Nine', 'PT503933813', 13, 20, 2);
INSERT INTO Line (id, name, owner, endpoint_a, endpoint_b, gauge) VALUES (4, 'Ramal Nine - Barcelos', 'PT503933813', 20, 8, 2);
INSERT INTO Line (id, name, owner, endpoint_a, endpoint_b, gauge) VALUES (5, 'Ramal Barcelos - Darque', 'PT503933813', 8, 12, 2);
INSERT INTO Line (id, name, owner, endpoint_a, endpoint_b, gauge) VALUES (6, 'Ramal Darque - Viana', 'PT503933813', 12, 17, 1);
INSERT INTO Line (id, name, owner, endpoint_a, endpoint_b, gauge) VALUES (7, 'Ramal Viana - Caminha', 'PT503933813', 17, 21, 2);
INSERT INTO Line (id, name, owner, endpoint_a, endpoint_b, gauge) VALUES (8, 'Ramal Caminha - Torre', 'PT503933813', 21, 16, 2);
INSERT INTO Line (id, name, owner, endpoint_a, endpoint_b, gauge) VALUES (9, 'Ramal Torre - Valença', 'PT503933813', 16, 11, 2);
INSERT INTO Line (id, name, owner, endpoint_a, endpoint_b, gauge) VALUES (21, 'Ramal Contumil - São Gemil', 'PT503933813', 13, 43, 1);
INSERT INTO Line (id, name, owner, endpoint_a, endpoint_b, gauge) VALUES (22, 'Ramal São Gemil - São Mamede de Infesta', 'PT503933813', 43, 45, 2);
INSERT INTO Line (id, name, owner, endpoint_a, endpoint_b, gauge) VALUES (23, 'Ramal São Mamede de Infesta - Leça do Balio', 'PT503933813', 45, 48, 2);
INSERT INTO Line (id, name, owner, endpoint_a, endpoint_b, gauge) VALUES (24, 'Ramal Leça do Balio - Leixões', 'PT503933813', 48, 50, 2);

-- Tabela Line_Segment e depois os tracktypes

INSERT INTO Line_Segment (id, is_electrified, speed_limit, max_weight, length, line) VALUES (1, 'Y', 0, 8000, 2618, 1);
INSERT INTO Line_Segment (id, is_electrified, speed_limit, max_weight, length, line) VALUES (3, 'Y', 0, 8000, 2443, 2);
INSERT INTO Line_Segment (id, is_electrified, speed_limit, max_weight, length, line) VALUES (10, 'Y', 0, 8000, 26560, 3);
INSERT INTO Line_Segment (id, is_electrified, speed_limit, max_weight, length, line) VALUES (11, 'Y', 0, 8000, 10000, 3);
INSERT INTO Line_Segment (id, is_electrified, speed_limit, max_weight, length, line) VALUES (12, 'Y', 0, 8000, 12000, 5);
INSERT INTO Line_Segment (id, is_electrified, speed_limit, max_weight, length, line) VALUES (13, 'Y', 0, 8000, 3100, 5);
INSERT INTO Line_Segment (id, is_electrified, speed_limit, max_weight, length, line) VALUES (14, 'Y', 0, 8000, 10387, 5);
INSERT INTO Line_Segment (id, is_electrified, speed_limit, max_weight, length, line) VALUES (15, 'Y', 0, 8000, 5286, 4);
INSERT INTO Line_Segment (id, is_electrified, speed_limit, max_weight, length, line) VALUES (16, 'Y', 0, 8000, 6000, 4);
INSERT INTO Line_Segment (id, is_electrified, speed_limit, max_weight, length, line) VALUES (18, 'Y', 0, 8000, 6000, 7);
INSERT INTO Line_Segment (id, is_electrified, speed_limit, max_weight, length, line) VALUES (20, 'Y', 0, 6400, 4890, 6);
INSERT INTO Line_Segment (id, is_electrified, speed_limit, max_weight, length, line) VALUES (21, 'Y', 0, 8000, 5000, 7);
INSERT INTO Line_Segment (id, is_electrified, speed_limit, max_weight, length, line) VALUES (22, 'Y', 0, 8000, 12000, 7);
INSERT INTO Line_Segment (id, is_electrified, speed_limit, max_weight, length, line) VALUES (25, 'Y', 0, 8000, 20829, 8);
INSERT INTO Line_Segment (id, is_electrified, speed_limit, max_weight, length, line) VALUES (26, 'Y', 0, 8000, 4264, 9);
INSERT INTO Line_Segment (id, is_electrified, speed_limit, max_weight, length, line) VALUES (30, 'Y', 0, 8000, 3883, 21);
INSERT INTO Line_Segment (id, is_electrified, speed_limit, max_weight, length, line) VALUES (31, 'Y', 0, 8000, 1174, 22);
INSERT INTO Line_Segment (id, is_electrified, speed_limit, max_weight, length, line) VALUES (32, 'Y', 0, 8000, 2534, 22);
INSERT INTO Line_Segment (id, is_electrified, speed_limit, max_weight, length, line) VALUES (33, 'Y', 0, 8000, 1566, 23);
INSERT INTO Line_Segment (id, is_electrified, speed_limit, max_weight, length, line) VALUES (34, 'Y', 0, 8000, 1453, 23);
INSERT INTO Line_Segment (id, is_electrified, speed_limit, max_weight, length, line) VALUES (35, 'Y', 0, 8000, 3597, 24);
INSERT INTO Line_Segment (id, is_electrified, speed_limit, max_weight, length, line) VALUES (36, 'Y', 0, 8000, 4334, 24);

-- Tabela Multiple_Tracks
INSERT INTO Multiple_Tracks (segment, number_tracks) VALUES (1, 4);
INSERT INTO Multiple_Tracks (segment, number_tracks) VALUES (3, 4);

INSERT INTO Multiple_Tracks (segment, number_tracks) VALUES (10, 2);
INSERT INTO Multiple_Tracks (segment, number_tracks) VALUES (11, 2);
INSERT INTO Multiple_Tracks (segment, number_tracks) VALUES (12, 2);
INSERT INTO Multiple_Tracks (segment, number_tracks) VALUES (13, 2);
INSERT INTO Multiple_Tracks (segment, number_tracks) VALUES (14, 2);
INSERT INTO Multiple_Tracks (segment, number_tracks) VALUES (15, 2);
INSERT INTO Multiple_Tracks (segment, number_tracks) VALUES (16, 2);
INSERT INTO Multiple_Tracks (segment, number_tracks) VALUES (20, 2);
INSERT INTO Multiple_Tracks (segment, number_tracks) VALUES (30, 2);
INSERT INTO Multiple_Tracks (segment, number_tracks) VALUES (31, 2);
INSERT INTO Multiple_Tracks (segment, number_tracks) VALUES (32, 2);
INSERT INTO Multiple_Tracks (segment, number_tracks) VALUES (33, 2);
INSERT INTO Multiple_Tracks (segment, number_tracks) VALUES (34, 2);
INSERT INTO Multiple_Tracks (segment, number_tracks) VALUES (35, 2);
INSERT INTO Multiple_Tracks (segment, number_tracks) VALUES (36, 2);

-- Tabela Single_Track
INSERT INTO Single_Track (segment) VALUES (18);
INSERT INTO Single_Track (segment) VALUES (21);
INSERT INTO Single_Track (segment) VALUES (22);
INSERT INTO Single_Track (segment) VALUES (25);
INSERT INTO Single_Track (segment) VALUES (26);

INSERT INTO Siding (segment, position, length) VALUES (21, 2000, 864);
INSERT INTO Siding (segment, position, length) VALUES (25, 11000, 266);

-- Tabela Path
INSERT INTO Path (id, NAME) VALUES (1, 'Caminho Leixões-Valença');
INSERT INTO Path (id, NAME)  VALUES (2, 'Caminho Valença-Leixões');

-- Tabela Endpoint_Path
INSERT INTO Endpoint_Path (path, seq, endpoint, must_stop) VALUES (1, 1, 50, 'Y');
INSERT INTO Endpoint_Path (path, seq, endpoint, must_stop) VALUES (1, 2, 48, 'Y');
INSERT INTO Endpoint_Path (path, seq, endpoint, must_stop) VALUES (1, 3, 45, 'Y');
INSERT INTO Endpoint_Path (path, seq, endpoint, must_stop) VALUES (1, 4, 43, 'Y');
INSERT INTO Endpoint_Path (path, seq, endpoint, must_stop) VALUES (1, 5, 13, 'N');
INSERT INTO Endpoint_Path (path, seq, endpoint, must_stop) VALUES (1, 6, 20, 'Y');
INSERT INTO Endpoint_Path (path, seq, endpoint, must_stop) VALUES (1, 7, 8, 'Y');
INSERT INTO Endpoint_Path (path, seq, endpoint, must_stop) VALUES (1, 8, 12, 'N');
INSERT INTO Endpoint_Path (path, seq, endpoint, must_stop) VALUES (1, 9, 17, 'Y');
INSERT INTO Endpoint_Path (path, seq, endpoint, must_stop) VALUES (1, 10, 21, 'N');
INSERT INTO Endpoint_Path (path, seq, endpoint, must_stop) VALUES (1, 11, 16, 'Y');
INSERT INTO Endpoint_Path (path, seq, endpoint, must_stop) VALUES (1, 12, 11, 'Y');
INSERT INTO Endpoint_Path (path, seq, endpoint, must_stop) VALUES (2, 1, 11, 'Y');
INSERT INTO Endpoint_Path (path, seq, endpoint, must_stop) VALUES (2, 2, 16, 'N');
INSERT INTO Endpoint_Path (path, seq, endpoint, must_stop) VALUES (2, 3, 21, 'Y');
INSERT INTO Endpoint_Path (path, seq, endpoint, must_stop) VALUES (2, 4, 17, 'Y');
INSERT INTO Endpoint_Path (path, seq, endpoint, must_stop) VALUES (2, 5, 12, 'Y');
INSERT INTO Endpoint_Path (path, seq, endpoint, must_stop) VALUES (2, 6, 8, 'N');
INSERT INTO Endpoint_Path (path, seq, endpoint, must_stop) VALUES (2, 7, 20, 'Y');
INSERT INTO Endpoint_Path (path, seq, endpoint, must_stop) VALUES (2, 8, 13, 'N');
INSERT INTO Endpoint_Path (path, seq, endpoint, must_stop) VALUES (2, 9, 43, 'Y');
INSERT INTO Endpoint_Path (path, seq, endpoint, must_stop) VALUES (2, 10, 45, 'Y');
INSERT INTO Endpoint_Path (path, seq, endpoint, must_stop) VALUES (2, 11, 48, 'Y');
INSERT INTO Endpoint_Path (path, seq, endpoint, must_stop) VALUES (2, 12, 50, 'Y');

-- Tabela Train
INSERT INTO Train (id, operator, time, path, "date", "start", "end") VALUES (5421, 'PT509017800', TO_TIMESTAMP('09:45:00','HH24:MI:SS'), 1, TO_DATE('2025-10-03','YYYY-MM-DD'),50, 11);
INSERT INTO Train (id, operator, time, path, "date", "start", "end") VALUES (5435, 'PT509017800', TO_TIMESTAMP('18:00:00','HH24:MI:SS'), 2, TO_DATE('2025-10-03','YYYY-MM-DD'),11, 50);
INSERT INTO Train (id, operator, time, path, "date", "start", "end") VALUES (5437, 'PT509017800', TO_TIMESTAMP('10:00:00','HH24:MI:SS'), 2, TO_DATE('2025-10-06','YYYY-MM-DD'),11 ,50);

-- Tabela Locomotive/Tipos/Gauge
INSERT INTO Locomotive (id, name, make, model, entry_year, power, acceleration, length, width, height, total_weight, number_bogies, operator, max_speed, op_speed, traction, bogie_arrangement) VALUES (5621, 'Inês', 'Siemens', 'Eurosprinter', 1995, 5600, 0, 19.2, 3.0, 4.375, 87, 2, 'PT509017800', 220.0, 70, 300,'Bo-Bo');
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


INSERT INTO Locomotive (id, name, make, model, entry_year, power, acceleration, length, width, height, total_weight, number_bogies, operator, max_speed, op_speed, traction, bogie_arrangement) VALUES (5034, 'Adriana', 'Stadler', 'E4000', 2017, 3178, 0, 23.02, 3000, 4.264, 124, 3, 'PT509017800', 120.0, 100.0, 400,'Co-Co');
INSERT INTO Diesel_Locomotive (locomotive, fuel_capacity) VALUES (5034, 6700);
INSERT INTO Locomotive_Gauge (gauge, locomotive) VALUES ((SELECT id FROM Gauge WHERE width=1668), 5034);

INSERT INTO Locomotive (id, name, make, model, entry_year, power, acceleration, length, width, height, total_weight, number_bogies, operator, max_speed, op_speed, traction, bogie_arrangement) VALUES (5036, 'Marina', 'Stadler', 'E4000', 2017, 3178, 0, 23.02, 3000, 4.264, 124, 3, 'PT509017800', 120.0, 100.0, 400,'Co-Co');
INSERT INTO Diesel_Locomotive (locomotive, fuel_capacity) VALUES (5036, 6700);
INSERT INTO Locomotive_Gauge (gauge, locomotive) VALUES ((SELECT id FROM Gauge WHERE width=1668), 5036);

INSERT INTO Locomotive (id, name, make, model, entry_year, power, acceleration, length, width, height, total_weight, number_bogies, operator, max_speed, op_speed, traction, bogie_arrangement) VALUES (335001, 'Não', 'Stadler', 'E4000', 2019, 3178, 0, 23.02, 3000, 4.264, 124, 3, 'PT507832388', 120.0, 100.0, 400,'Co-Co');
INSERT INTO Diesel_Locomotive (locomotive, fuel_capacity) VALUES (335001, 6700);
INSERT INTO Locomotive_Gauge (gauge, locomotive) VALUES ((SELECT id FROM Gauge WHERE width=1668), 335001);

INSERT INTO Locomotive (id, name, make, model, entry_year, power, acceleration, length, width, height, total_weight, number_bogies, operator, max_speed, op_speed, traction, bogie_arrangement) VALUES (335003, 'Especificado', 'Stadler', 'E4000', 2019, 3178, 0, 23.02, 3000, 4.264, 124, 3, 'PT507832388', 120.0, 100.0, 400,'Co-Co');
INSERT INTO Diesel_Locomotive (locomotive, fuel_capacity) VALUES (335003, 6700);
INSERT INTO Locomotive_Gauge (gauge, locomotive) VALUES ((SELECT id FROM Gauge WHERE width=1668), 335003);



-- Tabela Wagon_Model + Gauge
INSERT INTO Wagon_Model (id, model, maker, number_bogies, length, width, height, weight, max_speed, payload, volume, type) VALUES (1245, 'Tadgs 32 94 082 3', 'Metalsines', 2, 17240, 3072, 4270, 24.0, 120, 56.0, 75.0,'Cereal wagon');
INSERT INTO Wagon_Model_Gauge (Wagon_Modelid, Gaugeid) VALUES (1245, (SELECT id FROM Gauge WHERE width=1668));

INSERT INTO Wagon_Model (id, model, maker, number_bogies, length, width, height, weight, max_speed, payload, volume, type) VALUES (1278, 'Tdgs 41 94 074 1', 'Equimetal', 2, 9640, 3120, 4165, 13.8, 100, 26.2, 38.0,'Cereal wagon');
INSERT INTO Wagon_Model_Gauge (Wagon_Modelid, Gaugeid) VALUES (1278, (SELECT id FROM Gauge WHERE width=1668));

INSERT INTO Wagon_Model (id, model, maker, number_bogies, length, width, height, weight, max_speed, payload, volume, type) VALUES (1325, 'Gabs 81 94 181 1', 'Sepsa Cometna', 2, 21700, 3180, 4170, 29.8, 100, 50.2, 110.0,'Covered wagon with sliding door');
INSERT INTO Wagon_Model_Gauge (Wagon_Modelid, Gaugeid) VALUES (1325, (SELECT id FROM Gauge WHERE width=1668));

INSERT INTO Wagon_Model (id, model, maker, number_bogies, length, width, height, weight, max_speed, payload, volume, type) VALUES (1104, 'Regmms 32 94 356 3', 'Metalsines', 2, 14040, 3104, 2535, 21.2, 120, 60.6, 76.3,'Container wagon (max 40'' HC)');
INSERT INTO Wagon_Model_Gauge (Wagon_Modelid, Gaugeid) VALUES (1104, (SELECT id FROM Gauge WHERE width=1668));

INSERT INTO Wagon_Model (id, model, maker, number_bogies, length, width, height, weight, max_speed, payload, volume, type) VALUES (985, 'Lgs 22 94 441 6', 'Metalsines', 1, 13860, 2850, 1060, 11.9, 120, 28.1, 76.3,'Container wagon (max 40'' HC)');
INSERT INTO Wagon_Model_Gauge (Wagon_Modelid, Gaugeid) VALUES (985, (SELECT id FROM Gauge WHERE width=1668));

INSERT INTO Wagon_Model (id, model, maker, number_bogies, length, width, height, weight, max_speed, payload, volume, type) VALUES (987, 'Sgnss 12 94 455 2', 'Emef', 2, 18116, 2950, 1030, 21.6, 120, 68.4, 76.3,'Container wagon (max 40'' HC)');
INSERT INTO Wagon_Model_Gauge (Wagon_Modelid, Gaugeid) VALUES (987, (SELECT id FROM Gauge WHERE width=1668));

INSERT INTO Wagon_Model (id, model, maker, number_bogies, length, width, height, weight, max_speed, payload, volume, type) VALUES (988, 'Sgnss 12 94 455 2', 'Emef', 2, 18116, 2950, 1030, 21.6, 120, 68.4, 76.3,'Container wagon (max 40'' HC)');
INSERT INTO Wagon_Model_Gauge (Wagon_Modelid, Gaugeid) VALUES (988, (SELECT id FROM Gauge WHERE width=1435));


INSERT INTO Wagon_Model (id, model, maker, number_bogies, length, width, height, weight, max_speed, payload, volume, type) VALUES (1525, 'Zaes 81 94 788', 'Equimetal', 2, 13800, 2950, 4226, 22.9, 120, 57.1, 64.6,'Biodiesel wagaon');
INSERT INTO Wagon_Model_Gauge (Wagon_Modelid, Gaugeid) VALUES (1525, (SELECT id FROM Gauge WHERE width=1668));

INSERT INTO Wagon_Model (id, model, maker, number_bogies, length, width, height, weight, max_speed, payload, volume, type) VALUES (1523, 'Zaes 81 94 788', 'Equimetal', 2, 13800, 2950, 4226, 22.9, 120, 57.1, 64.6,'Biodiesel wagaon');
INSERT INTO Wagon_Model_Gauge (Wagon_Modelid, Gaugeid) VALUES (1523, (SELECT id FROM Gauge WHERE width=1435));

INSERT INTO Wagon_Model (id, model, maker, number_bogies, length, width, height, weight, max_speed, payload, volume, type) VALUES (1212, 'Kbs 41 94 333', 'Equimetal', 2, 13800, 2842, 3300, 14.2, 100, 25.8, 62.7,'Wood wagon');
INSERT INTO Wagon_Model_Gauge (Wagon_Modelid, Gaugeid) VALUES (1212, (SELECT id FROM Gauge WHERE width=1668));

-- Tabela Wagon
INSERT INTO Wagon (id, service_year, wagon_model, operator) VALUES (3563077, 1987, 1104, 'PT509017800');
INSERT INTO Wagon (id, service_year, wagon_model, operator) VALUES (3563078, 1987, 1104, 'PT509017800');
INSERT INTO Wagon (id, service_year, wagon_model, operator) VALUES (3563079, 1987, 1104, 'PT509017800');
INSERT INTO Wagon (id, service_year, wagon_model, operator) VALUES (3563080, 1987, 1104, 'PT509017800');
INSERT INTO Wagon (id, service_year, wagon_model, operator) VALUES (3563081, 1987, 1104, 'PT509017800');
INSERT INTO Wagon (id, service_year, wagon_model, operator) VALUES (3563082, 1987, 1104, 'PT509017800');
INSERT INTO Wagon (id, service_year, wagon_model, operator) VALUES (3563083, 1987, 1104, 'PT509017800');
INSERT INTO Wagon (id, service_year, wagon_model, operator) VALUES (3563084, 1987, 1104, 'PT509017800');
INSERT INTO Wagon (id, service_year, wagon_model, operator) VALUES (3563085, 1987, 1104, 'PT509017800');
INSERT INTO Wagon (id, service_year, wagon_model, operator) VALUES (3563086, 1987, 1104, 'PT509017800');
INSERT INTO Wagon (id, service_year, wagon_model, operator) VALUES (3563087, 1987, 1104, 'PT509017800');
INSERT INTO Wagon (id, service_year, wagon_model, operator) VALUES (3563088, 1987, 1104, 'PT509017800');
INSERT INTO Wagon (id, service_year, wagon_model, operator) VALUES (3563089, 1987, 1104, 'PT509017800');
INSERT INTO Wagon (id, service_year, wagon_model, operator) VALUES (3563090, 1987, 1104, 'PT509017800');
INSERT INTO Wagon (id, service_year, wagon_model, operator) VALUES (3563091, 1987, 1104, 'PT509017800');
INSERT INTO Wagon (id, service_year, wagon_model, operator) VALUES (3563092, 1987, 1104, 'PT509017800');
INSERT INTO Wagon (id, service_year, wagon_model, operator) VALUES (823045, 1990, 1245, 'PT509017800');
INSERT INTO Wagon (id, service_year, wagon_model, operator) VALUES (823046, 1990, 1245, 'PT509017800');
INSERT INTO Wagon (id, service_year, wagon_model, operator) VALUES (823047, 1990, 1245, 'PT509017800');
INSERT INTO Wagon (id, service_year, wagon_model, operator) VALUES (823048, 1990, 1245, 'PT509017800');
INSERT INTO Wagon (id, service_year, wagon_model, operator) VALUES (741001, 1977, 1278, 'PT509017800');
INSERT INTO Wagon (id, service_year, wagon_model, operator) VALUES (741002, 1977, 1278, 'PT509017800');
INSERT INTO Wagon (id, service_year, wagon_model, operator) VALUES (741003, 1977, 1278, 'PT509017800');
INSERT INTO Wagon (id, service_year, wagon_model, operator) VALUES (741004, 1977, 1278, 'PT509017800');
INSERT INTO Wagon (id, service_year, wagon_model, operator) VALUES (741005, 1977, 1278, 'PT509017800');
INSERT INTO Wagon (id, service_year, wagon_model, operator) VALUES (741006, 1977, 1278, 'PT509017800');
INSERT INTO Wagon (id, service_year, wagon_model, operator) VALUES (1811010, 1977, 1325, 'PT509017800');
INSERT INTO Wagon (id, service_year, wagon_model, operator) VALUES (1811011, 1977, 1325, 'PT509017800');
INSERT INTO Wagon (id, service_year, wagon_model, operator) VALUES (1811012, 1977, 1325, 'PT509017800');
INSERT INTO Wagon (id, service_year, wagon_model, operator) VALUES (1811013, 1977, 1325, 'PT509017800');
INSERT INTO Wagon (id, service_year, wagon_model, operator) VALUES (1811014, 1977, 1325, 'PT509017800');

INSERT INTO Wagon (id, service_year, wagon_model, operator) VALUES (3330001, 2005, 1212, 'PT509017800');
INSERT INTO Wagon (id, service_year, wagon_model, operator) VALUES (3330002, 2005, 1212, 'PT509017800');
INSERT INTO Wagon (id, service_year, wagon_model, operator) VALUES (3330003, 2005, 1212, 'PT509017800');
INSERT INTO Wagon (id, service_year, wagon_model, operator) VALUES (3330004, 2005, 1212, 'PT509017800');
INSERT INTO Wagon (id, service_year, wagon_model, operator) VALUES (3330005, 2005, 1212, 'PT509017800');
INSERT INTO Wagon (id, service_year, wagon_model, operator) VALUES (3330006, 2005, 1212, 'PT509017800');
INSERT INTO Wagon (id, service_year, wagon_model, operator) VALUES (3330007, 2005, 1212, 'PT509017800');
INSERT INTO Wagon (id, service_year, wagon_model, operator) VALUES (3330008, 2005, 1212, 'PT509017800');
INSERT INTO Wagon (id, service_year, wagon_model, operator) VALUES (3330009, 2005, 1212, 'PT509017800');
INSERT INTO Wagon (id, service_year, wagon_model, operator) VALUES (3330010, 2005, 1212, 'PT509017800');

-- Tabela Locomotive_Train
INSERT INTO Locomotive_Train (locomotive, train, position) VALUES (5621, 5421, 1);
INSERT INTO Locomotive_Train (locomotive, train, position) VALUES (5623, 5421, 2);
INSERT INTO Locomotive_Train (locomotive, train, position) VALUES (5623, 5435, 1);
INSERT INTO Locomotive_Train (locomotive, train, position) VALUES (5621, 5437, 1);

-- Tabela Freight+Wagon_Freight
INSERT INTO Freight (id, "date", origin, destination, train) VALUES (2001,TO_DATE('2025-10-03','YYYY-MM-DD'), 50, 12, 5421);
INSERT INTO Wagon_Freight (wagon, freight) VALUES (3330001, 2001);
INSERT INTO Wagon_Freight (wagon, freight) VALUES (3330002, 2001);
INSERT INTO Wagon_Freight (wagon, freight) VALUES (3330003, 2001);
INSERT INTO Wagon_Freight (wagon, freight) VALUES (3330004, 2001);
INSERT INTO Wagon_Freight (wagon, freight) VALUES (3330005, 2001);
INSERT INTO Wagon_Freight (wagon, freight) VALUES (3330006, 2001);

INSERT INTO Freight (id, "date", origin, destination, train) VALUES (2002,TO_DATE('2025-10-03','YYYY-MM-DD'), 13, 11, 5421);
INSERT INTO Wagon_Freight (wagon, freight) VALUES (3563089, 2002);

INSERT INTO Freight (id, "date", origin, destination, train) VALUES (2003,TO_DATE('2025-10-03','YYYY-MM-DD'), 50, 11, 5421);
INSERT INTO Wagon_Freight (wagon, freight) VALUES (1811011, 2003);
INSERT INTO Wagon_Freight (wagon, freight) VALUES (1811012, 2003);

INSERT INTO Freight (id, "date", origin, destination, train) VALUES (2004,TO_DATE('2025-10-03','YYYY-MM-DD'), 50, 21, 5421);
INSERT INTO Wagon_Freight (wagon, freight) VALUES (1811013, 2004);

INSERT INTO Freight (id, "date", origin, destination, train) VALUES (2005,TO_DATE('2025-10-03','YYYY-MM-DD'), 20, 11, 5421);
INSERT INTO Wagon_Freight (wagon, freight) VALUES (3563077, 2005);
INSERT INTO Wagon_Freight (wagon, freight) VALUES (3563078, 2005);
INSERT INTO Wagon_Freight (wagon, freight) VALUES (3563079, 2005);
INSERT INTO Wagon_Freight (wagon, freight) VALUES (3563080, 2005);

INSERT INTO Freight (id, "date", origin, destination, train) VALUES (2006,TO_DATE('2025-10-03','YYYY-MM-DD'), 12, 50, 5435);
INSERT INTO Wagon_Freight (wagon, freight) VALUES (3330003, 2006);
INSERT INTO Wagon_Freight (wagon, freight) VALUES (3330007, 2006);

INSERT INTO Freight (id, "date", origin, destination, train) VALUES (2050,TO_DATE('2025-10-06','YYYY-MM-DD'), 12, 50, 5437);
INSERT INTO Wagon_Freight (wagon, freight) VALUES (3330001, 2050);
INSERT INTO Wagon_Freight (wagon, freight) VALUES (3330002, 2050);
INSERT INTO Wagon_Freight (wagon, freight) VALUES (3330003, 2050);
INSERT INTO Wagon_Freight (wagon, freight) VALUES (3330004, 2050);
INSERT INTO Wagon_Freight (wagon, freight) VALUES (3330005, 2050);
INSERT INTO Wagon_Freight (wagon, freight) VALUES (3330006, 2050);

INSERT INTO Freight (id, "date", origin, destination, train) VALUES (2051,TO_DATE('2025-10-06','YYYY-MM-DD'), 11, 50, 5437);
INSERT INTO Wagon_Freight (wagon, freight) VALUES (1811011, 2051);
INSERT INTO Wagon_Freight (wagon, freight) VALUES (1811012, 2051);
INSERT INTO Wagon_Freight (wagon, freight) VALUES (3563077, 2051);
INSERT INTO Wagon_Freight (wagon, freight) VALUES (3563078, 2051);
INSERT INTO Wagon_Freight (wagon, freight) VALUES (3563079, 2051);
INSERT INTO Wagon_Freight (wagon, freight) VALUES (3563080, 2051);


INSERT INTO Freight (id, "date", origin, destination, train) VALUES (3000, DATE '2025-10-03', 50, 12, 5421);
INSERT INTO Wagon_Freight (wagon, freight) VALUES (823046, 3000);



INSERT INTO Locomotive (id, name, make, model, entry_year, power, acceleration, length, width, height, total_weight, number_bogies, operator, max_speed, op_speed, traction, bogie_arrangement) VALUES (335006, 'Trovão', 'Stadler', 'E4000', 2019, 3178, 0, 23.02, 3000, 4.264, 124, 3, 'PT507832388', 120.0, 100.0, 400,'Co-Co');
INSERT INTO Electric_Locomotive (locomotive, voltage, frequency) VALUES (335006, 25, 50);
INSERT INTO Locomotive_Gauge (gauge, locomotive) VALUES ((SELECT id FROM Gauge WHERE width = 1668), 335006);