CREATE TABLE Owner (
  vat        varchar2(255) NOT NULL, 
  name       varchar2(255) NOT NULL UNIQUE, 
  short_name varchar2(255) NOT NULL UNIQUE, 
  PRIMARY KEY (vat));
CREATE TABLE Line (
  id         number(10) NOT NULL, 
  name       varchar2(255) NOT NULL, 
  owner      varchar2(255) NOT NULL, 
  endpoint_a number(10) NOT NULL, 
  endpoint_b number(10) NOT NULL, 
  gauge      number(10) NOT NULL, 
  PRIMARY KEY (id));
CREATE TABLE Line_Segment (
  id             number(10) NOT NULL, 
  is_electrified char(1) NOT NULL, 
  speed_limit    number(10) NOT NULL, 
  max_weight     number(10) NOT NULL, 
  length         number(10) NOT NULL, 
  line           number(10) NOT NULL, 
  PRIMARY KEY (id));
CREATE TABLE Gauge (
  id    number(10) NOT NULL, 
  width number(10) NOT NULL UNIQUE, 
  name  varchar2(255) NOT NULL, 
  PRIMARY KEY (id));
CREATE TABLE Endpoint (
  id           number(10) NOT NULL, 
  name         varchar2(255) NOT NULL UNIQUE, 
  metafacility number(10) NOT NULL, 
  PRIMARY KEY (id));
CREATE TABLE Locomotive (
  id             number(10) NOT NULL, 
  name           varchar2(255) NOT NULL, 
  service_start  date NOT NULL, 
  total_weight   number(10) NOT NULL, 
  op_speed       number(10) NOT NULL, 
  operator       varchar2(255) NOT NULL, 
  model          number(10) NOT NULL, 
  entry_endpoint number(10) NOT NULL, 
  PRIMARY KEY (id));
CREATE TABLE Operator (
  vat        varchar2(255) NOT NULL, 
  name       varchar2(255) NOT NULL UNIQUE, 
  short_name varchar2(255) NOT NULL UNIQUE, 
  PRIMARY KEY (vat));
CREATE TABLE Wagon (
  id             number(10) NOT NULL, 
  service_start  date NOT NULL, 
  wagon_model    number(10) NOT NULL, 
  operator       varchar2(255) NOT NULL, 
  entry_endpoint number(10) NOT NULL, 
  PRIMARY KEY (id));
CREATE TABLE Wagon_Model (
  id            number(10) NOT NULL, 
  model         varchar2(255) NOT NULL, 
  maker         varchar2(255) NOT NULL, 
  number_bogies number(10) NOT NULL, 
  length        number(10) NOT NULL, 
  width         number(10) NOT NULL, 
  height        number(10) NOT NULL, 
  weight        number(10, 1) NOT NULL, 
  max_speed     number(10) NOT NULL, 
  payload       number(10, 1) NOT NULL, 
  volume        number(10, 1) NOT NULL, 
  type          varchar2(255) NOT NULL, 
  PRIMARY KEY (id));
CREATE TABLE Diesel_Locomotive_Model (
  fuel_capacity number(10) NOT NULL, 
  model         number(10) NOT NULL, 
  PRIMARY KEY (model));
CREATE TABLE Electric_Locomotive_Model (
  voltage   number(10) NOT NULL, 
  frequency number(10) NOT NULL, 
  model     number(10) NOT NULL, 
  PRIMARY KEY (model));
CREATE TABLE Building (
  id       number(10) NOT NULL, 
  type     number(10) NOT NULL, 
  name     varchar2(255) NOT NULL, 
  facility number(10) NOT NULL, 
  PRIMARY KEY (id));
CREATE TABLE Metafacility (
  id   number(10) NOT NULL, 
  name varchar2(255) NOT NULL, 
  PRIMARY KEY (id));
CREATE TABLE Facility (
  id   number(10) NOT NULL, 
  name varchar2(255) NOT NULL, 
  type number(10) NOT NULL, 
  PRIMARY KEY (id));
CREATE TABLE Freightyard (
  id number(10) NOT NULL, 
  PRIMARY KEY (id));
CREATE TABLE BuildingType (
  id   number(10) NOT NULL, 
  name varchar2(255) NOT NULL, 
  PRIMARY KEY (id));
CREATE TABLE Wagon_Model_Gauge (
  Wagon_Modelid number(10) NOT NULL, 
  Gaugeid       number(10) NOT NULL, 
  PRIMARY KEY (Wagon_Modelid, 
  Gaugeid));
CREATE TABLE Train (
  id         number(10) NOT NULL, 
  operator   varchar2(255) NOT NULL, 
  time       timestamp(0) NOT NULL, 
  path       number(10) NOT NULL, 
  "date"     date NOT NULL, 
  max_length number(10) NOT NULL, 
  PRIMARY KEY (id));
CREATE TABLE Locomotive_Train (
  locomotive number(10) NOT NULL, 
  train      number(10) NOT NULL, 
  position   number(10) NOT NULL, 
  PRIMARY KEY (locomotive, 
  train));
CREATE TABLE Freight (
  id          number(10) NOT NULL, 
  "date"      date NOT NULL, 
  origin      number(10) NOT NULL, 
  destination number(10) NOT NULL, 
  PRIMARY KEY (id));
CREATE TABLE Wagon_Freight (
  wagon   number(10) NOT NULL, 
  freight number(10) NOT NULL, 
  PRIMARY KEY (wagon, 
  freight));
CREATE TABLE Path (
  id   number(10) NOT NULL, 
  name varchar2(255) NOT NULL, 
  PRIMARY KEY (id));
CREATE TABLE Endpoint_Path (
  seq       number(10) NOT NULL, 
  path      number(10) NOT NULL, 
  endpoint  number(10) NOT NULL, 
  must_stop varchar2(1) NOT NULL, 
  PRIMARY KEY (seq, 
  path));
CREATE TABLE Single_Track (
  segment number(10) NOT NULL, 
  PRIMARY KEY (segment));
CREATE TABLE Multiple_Tracks (
  segment       number(10) NOT NULL, 
  number_tracks number(10) NOT NULL, 
  PRIMARY KEY (segment));
CREATE TABLE Siding (
  segment  number(10) NOT NULL, 
  length   number(10) NOT NULL, 
  position number(10) NOT NULL, 
  PRIMARY KEY (segment, 
  length));
CREATE TABLE Locomotive_Model (
  id                number(10) NOT NULL, 
  name              varchar2(255) NOT NULL, 
  make              varchar2(255) NOT NULL, 
  number_bogies     number(10) NOT NULL, 
  bogie_arrangement varchar2(255) NOT NULL, 
  power             number(10) NOT NULL, 
  length            number(10, 3) NOT NULL, 
  width             number(10, 3) NOT NULL, 
  height            number(10, 3) NOT NULL, 
  max_speed         number(10) NOT NULL, 
  traction          number(10) NOT NULL, 
  PRIMARY KEY (id));
CREATE TABLE Locomotive_Model_Gauge (
  Locomotive_Modelid number(10) NOT NULL, 
  Gaugeid            number(10) NOT NULL, 
  PRIMARY KEY (Locomotive_Modelid, 
  Gaugeid));
CREATE TABLE Train_Movement (
  id       number(10) GENERATED AS IDENTITY, 
  train    number(10) NOT NULL, 
  endpoint number(10) NOT NULL, 
  PRIMARY KEY (id));
CREATE TABLE Stop (
  arrival   timestamp(0) NOT NULL, 
  departure timestamp(0) NOT NULL, 
  movement  number(10) NOT NULL, 
  PRIMARY KEY (movement));
CREATE TABLE Passage (
  passage  timestamp(0) NOT NULL, 
  movement number(10) NOT NULL, 
  PRIMARY KEY (movement));
CREATE TABLE Freight_Train (
  freight number(10) NOT NULL, 
  train   number(10) NOT NULL, 
  PRIMARY KEY (freight));
CREATE TABLE Wagon_Location (
  id              number(10) NOT NULL, 
  wagon           number(10) NOT NULL, 
  endpoint        number(10) NOT NULL, 
  status          nvarchar2(255) NOT NULL CHECK(status IN ('F', 'T')), 
  event_timestamp timestamp(0) NOT NULL, 
  event_type      varchar2(255) NOT NULL CHECK(event_type IN ('A', 'D', 'P')), 
  PRIMARY KEY (id, 
  wagon));
CREATE TABLE Locomotive_Location (
  id         number(10) NOT NULL, 
  locomotive number(10) NOT NULL, 
  endpoint   number(10) NOT NULL, 
  status     varchar2(255) NOT NULL CHECK(status IN ('F', 'T')), 
  event_time timestamp(0) NOT NULL, 
  event_type varchar2(255) NOT NULL CHECK(event_type IN ('A', 'D', 'P')), 
  PRIMARY KEY (id, 
  locomotive));
ALTER TABLE Line ADD CONSTRAINT FKLine262396 FOREIGN KEY (owner) REFERENCES Owner (vat);
ALTER TABLE Line_Segment ADD CONSTRAINT FKLine_Segme425123 FOREIGN KEY (line) REFERENCES Line (id);
ALTER TABLE Line ADD CONSTRAINT FKLine635842 FOREIGN KEY (endpoint_a) REFERENCES Endpoint (id);
ALTER TABLE Line ADD CONSTRAINT FKLine635841 FOREIGN KEY (endpoint_b) REFERENCES Endpoint (id);
ALTER TABLE Locomotive ADD CONSTRAINT FKLocomotive389310 FOREIGN KEY (operator) REFERENCES Operator (vat);
ALTER TABLE Wagon ADD CONSTRAINT FKWagon481525 FOREIGN KEY (wagon_model) REFERENCES Wagon_Model (id);
ALTER TABLE Building ADD CONSTRAINT FKBuilding456473 FOREIGN KEY (type) REFERENCES BuildingType (id);
ALTER TABLE Building ADD CONSTRAINT FKBuilding121622 FOREIGN KEY (facility) REFERENCES Facility (id);
ALTER TABLE Endpoint ADD CONSTRAINT FKEndpoint305011 FOREIGN KEY (metafacility) REFERENCES Metafacility (id);
ALTER TABLE Facility ADD CONSTRAINT FKFacility704827 FOREIGN KEY (id) REFERENCES Metafacility (id);
ALTER TABLE Freightyard ADD CONSTRAINT FKFreightyar260042 FOREIGN KEY (id) REFERENCES Metafacility (id);
ALTER TABLE Wagon ADD CONSTRAINT FKWagon8744 FOREIGN KEY (operator) REFERENCES Operator (vat);
ALTER TABLE Wagon_Model_Gauge ADD CONSTRAINT FKWagon_Mode599476 FOREIGN KEY (Wagon_Modelid) REFERENCES Wagon_Model (id);
ALTER TABLE Wagon_Model_Gauge ADD CONSTRAINT FKWagon_Mode72765 FOREIGN KEY (Gaugeid) REFERENCES Gauge (id);
ALTER TABLE Line ADD CONSTRAINT FKLine75626 FOREIGN KEY (gauge) REFERENCES Gauge (id);
ALTER TABLE Train ADD CONSTRAINT FKTrain278814 FOREIGN KEY (operator) REFERENCES Operator (vat);
ALTER TABLE Locomotive_Train ADD CONSTRAINT FKLocomotive690278 FOREIGN KEY (locomotive) REFERENCES Locomotive (id);
ALTER TABLE Locomotive_Train ADD CONSTRAINT FKLocomotive345179 FOREIGN KEY (train) REFERENCES Train (id);
ALTER TABLE Wagon_Freight ADD CONSTRAINT FKWagon_Frei246610 FOREIGN KEY (wagon) REFERENCES Wagon (id);
ALTER TABLE Wagon_Freight ADD CONSTRAINT FKWagon_Frei318629 FOREIGN KEY (freight) REFERENCES Freight (id);
ALTER TABLE Freight ADD CONSTRAINT FKFreight109531 FOREIGN KEY (origin) REFERENCES Endpoint (id);
ALTER TABLE Freight ADD CONSTRAINT FKFreight881821 FOREIGN KEY (destination) REFERENCES Endpoint (id);
ALTER TABLE Train ADD CONSTRAINT FKTrain985667 FOREIGN KEY (path) REFERENCES Path (id);
ALTER TABLE Endpoint_Path ADD CONSTRAINT FKEndpoint_P179752 FOREIGN KEY (endpoint) REFERENCES Endpoint (id);
ALTER TABLE Endpoint_Path ADD CONSTRAINT FKEndpoint_P852905 FOREIGN KEY (path) REFERENCES Path (id);
ALTER TABLE Single_Track ADD CONSTRAINT FKSingle_Tra576121 FOREIGN KEY (segment) REFERENCES Line_Segment (id);
ALTER TABLE Multiple_Tracks ADD CONSTRAINT FKMultiple_T771125 FOREIGN KEY (segment) REFERENCES Line_Segment (id);
ALTER TABLE Siding ADD CONSTRAINT FKSiding869925 FOREIGN KEY (segment) REFERENCES Single_Track (segment);
ALTER TABLE Locomotive ADD CONSTRAINT FKLocomotive939173 FOREIGN KEY (model) REFERENCES Locomotive_Model (id);
ALTER TABLE Locomotive_Model_Gauge ADD CONSTRAINT FKLocomotive35887 FOREIGN KEY (Locomotive_Modelid) REFERENCES Locomotive_Model (id);
ALTER TABLE Locomotive_Model_Gauge ADD CONSTRAINT FKLocomotive663176 FOREIGN KEY (Gaugeid) REFERENCES Gauge (id);
ALTER TABLE Diesel_Locomotive_Model ADD CONSTRAINT FKDiesel_Loc686371 FOREIGN KEY (model) REFERENCES Locomotive_Model (id);
ALTER TABLE Electric_Locomotive_Model ADD CONSTRAINT FKElectric_L872183 FOREIGN KEY (model) REFERENCES Locomotive_Model (id);
ALTER TABLE Train_Movement ADD CONSTRAINT FKTrain_Move863847 FOREIGN KEY (train) REFERENCES Train (id);
ALTER TABLE Train_Movement ADD CONSTRAINT FKTrain_Move54998 FOREIGN KEY (endpoint) REFERENCES Endpoint (id);
ALTER TABLE Stop ADD CONSTRAINT FKStop83748 FOREIGN KEY (movement) REFERENCES Train_Movement (id);
ALTER TABLE Passage ADD CONSTRAINT FKPassage102422 FOREIGN KEY (movement) REFERENCES Train_Movement (id);
ALTER TABLE Freight_Train ADD CONSTRAINT FKFreight_Tr432283 FOREIGN KEY (freight) REFERENCES Freight (id);
ALTER TABLE Freight_Train ADD CONSTRAINT FKFreight_Tr820123 FOREIGN KEY (train) REFERENCES Train (id);
ALTER TABLE Wagon_Location ADD CONSTRAINT FKWagon_Loca55866 FOREIGN KEY (wagon) REFERENCES Wagon (id);
ALTER TABLE Wagon_Location ADD CONSTRAINT FKWagon_Loca706875 FOREIGN KEY (endpoint) REFERENCES Endpoint (id);
ALTER TABLE Locomotive_Location ADD CONSTRAINT FKLocomotive838517 FOREIGN KEY (locomotive) REFERENCES Locomotive (id);
ALTER TABLE Locomotive_Location ADD CONSTRAINT FKLocomotive684568 FOREIGN KEY (endpoint) REFERENCES Endpoint (id);
ALTER TABLE Wagon ADD CONSTRAINT FKWagon261712 FOREIGN KEY (entry_endpoint) REFERENCES Endpoint (id);
ALTER TABLE Locomotive ADD CONSTRAINT FKLocomotive136342 FOREIGN KEY (entry_endpoint) REFERENCES Endpoint (id);
CREATE OR REPLACE TRIGGER trg_check_train_length_loco
FOR INSERT OR UPDATE OR DELETE ON Locomotive_Train
COMPOUND TRIGGER

  -- Guarda os trains afetados nesta statement (para validar no fim)
  TYPE t_train_map IS TABLE OF PLS_INTEGER INDEX BY VARCHAR2(30);
  g_train_map t_train_map;

  AFTER EACH ROW IS
    v_key VARCHAR2(30);
  BEGIN
    v_key := TO_CHAR(NVL(:NEW.train, :OLD.train));
    g_train_map(v_key) := 1;
  END AFTER EACH ROW;

  AFTER STATEMENT IS
    v_key          VARCHAR2(30);
    v_train_id     Train.id%TYPE;
    v_max_length   Train.max_length%TYPE;
    v_loco_length  NUMBER := 0;
    v_wagon_length NUMBER := 0;
  BEGIN
    v_key := g_train_map.FIRST;
    WHILE v_key IS NOT NULL LOOP
      v_train_id := TO_NUMBER(v_key);

      -- 1) comprimento máximo do train
      SELECT max_length
        INTO v_max_length
        FROM Train
       WHERE id = v_train_id;

      -- 2) soma comprimento locomotivas do train
      SELECT NVL(SUM(lm.length), 0)
        INTO v_loco_length
        FROM Locomotive_Train lt
        JOIN Locomotive l
          ON lt.locomotive = l.id
        JOIN Locomotive_Model lm
          ON l.model = lm.id
       WHERE lt.train = v_train_id;

      -- 3) soma comprimento wagons do train (via freight)
      SELECT NVL(SUM(wm.length), 0)
        INTO v_wagon_length
        FROM Freight_Train ft
        JOIN Wagon_Freight wf
          ON wf.freight = ft.freight
        JOIN Wagon w
          ON wf.wagon = w.id
        JOIN Wagon_Model wm
          ON w.wagon_model = wm.id
       WHERE ft.train = v_train_id;

      -- 4) regra
      IF v_loco_length + v_wagon_length > v_max_length THEN
        raise_application_error(
          -20033,
          'Train ' || v_train_id || ' exceeds max length (' || v_max_length || ')'
        );
      END IF;

      v_key := g_train_map.NEXT(v_key);
    END LOOP;
  END AFTER STATEMENT;

END;
/
;
/
CREATE OR REPLACE TRIGGER trg_check_train_length_freight
FOR INSERT OR UPDATE OR DELETE ON Freight_Train
COMPOUND TRIGGER

  -- trains afetados nesta statement
  TYPE t_train_map IS TABLE OF PLS_INTEGER INDEX BY VARCHAR2(30);
  g_train_map t_train_map;

  AFTER EACH ROW IS
    v_key VARCHAR2(30);
  BEGIN
    v_key := TO_CHAR(NVL(:NEW.train, :OLD.train));
    g_train_map(v_key) := 1;
  END AFTER EACH ROW;

  AFTER STATEMENT IS
    v_key          VARCHAR2(30);
    v_train_id     Train.id%TYPE;
    v_max_length   Train.max_length%TYPE;
    v_loco_length  NUMBER := 0;
    v_wagon_length NUMBER := 0;
  BEGIN
    v_key := g_train_map.FIRST;
    WHILE v_key IS NOT NULL LOOP
      v_train_id := TO_NUMBER(v_key);

      -- max_length do train
      SELECT max_length
        INTO v_max_length
        FROM Train
       WHERE id = v_train_id;

      -- soma comprimento locomotivas (do train)
      SELECT NVL(SUM(lm.length), 0)
        INTO v_loco_length
        FROM Locomotive_Train lt
        JOIN Locomotive l
          ON lt.locomotive = l.id
        JOIN Locomotive_Model lm
          ON l.model = lm.id
       WHERE lt.train = v_train_id;

      -- soma comprimento wagons (do train) via freights
      SELECT NVL(SUM(wm.length), 0)
        INTO v_wagon_length
        FROM Freight_Train ft
        JOIN Wagon_Freight wf
          ON wf.freight = ft.freight
        JOIN Wagon w
          ON wf.wagon = w.id
        JOIN Wagon_Model wm
          ON w.wagon_model = wm.id
       WHERE ft.train = v_train_id;

      IF v_loco_length + v_wagon_length > v_max_length THEN
        raise_application_error(
          -20034,
          'Train ' || v_train_id || ' exceeds max length (' || v_max_length || ')'
        );
      END IF;

      v_key := g_train_map.NEXT(v_key);
    END LOOP;
  END AFTER STATEMENT;

END;
/
;
/
CREATE OR REPLACE TRIGGER trg_init_wagon_current_location
AFTER INSERT ON Wagon
FOR EACH ROW
BEGIN
  INSERT INTO Wagon_Current_Location
    (id, wagon, endpoint, status, event_timestamp, event_type)
  VALUES
    (1, :NEW.id, :NEW.entry_endpoint, 'F',
     CAST(:NEW.service_start AS TIMESTAMP), 'A');
END;
/
CREATE OR REPLACE TRIGGER trg_init_locomotive_current_location
AFTER INSERT ON Locomotive
FOR EACH ROW
BEGIN
  INSERT INTO Locomotive_Location
    (id, locomotive, endpoint, status, event_time, event_type)
  VALUES
    (1, :NEW.id, :NEW.entry_endpoint, 'F',
     CAST(:NEW.service_start AS TIMESTAMP), 'A');
END;
/
