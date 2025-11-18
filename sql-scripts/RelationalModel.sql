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
                              track_type     number(10) NOT NULL,
                              PRIMARY KEY (id));
CREATE TABLE Gauge (
                       id    number(10) NOT NULL,
                       width number(10) NOT NULL UNIQUE,
                       PRIMARY KEY (id));
CREATE TABLE Endpoint (
                          id           number(10) NOT NULL,
                          name         varchar2(255) NOT NULL UNIQUE,
                          metafacility number(10) NOT NULL,
                          PRIMARY KEY (id));
CREATE TABLE Locomotive (
                            id                number(10) NOT NULL,
                            name              varchar2(255) NOT NULL,
                            make              varchar2(255) NOT NULL,
                            model             varchar2(255) NOT NULL,
                            entry_year        number(10) NOT NULL,
                            power             number(10) NOT NULL,
                            acceleration      number(10) NOT NULL,
                            length            number(10, 3) NOT NULL,
                            width             number(10, 3) NOT NULL,
                            height            number(10, 3) NOT NULL,
                            total_weight      number(10) NOT NULL,
                            number_bogies     number(10) NOT NULL,
                            operator          varchar2(255) NOT NULL,
                            max_speed         number(10, 1) NOT NULL,
                            op_speed          number(10) NOT NULL,
                            traction          number(10) NOT NULL,
                            bogie_arrangement number(10),
                            PRIMARY KEY (id));
CREATE TABLE Operator (
                          vat        varchar2(255) NOT NULL,
                          name       varchar2(255) NOT NULL UNIQUE,
                          short_name varchar2(255) NOT NULL UNIQUE,
                          PRIMARY KEY (vat));
CREATE TABLE Wagon (
                       id           number(10) NOT NULL,
                       service_year number(10) NOT NULL,
                       wagon_model  number(10) NOT NULL,
                       operator     varchar2(255) NOT NULL,
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
CREATE TABLE Locomotive_Gauge (
                                  gauge      number(10) NOT NULL,
                                  locomotive number(10) NOT NULL,
                                  PRIMARY KEY (gauge,
                                               locomotive));
CREATE TABLE Diesel_Locomotive (
                                   locomotive    number(10) NOT NULL,
                                   fuel_capacity number(10) NOT NULL,
                                   PRIMARY KEY (locomotive));
CREATE TABLE Electric_Locomotive (
                                     locomotive number(10) NOT NULL,
                                     voltage    number(10) NOT NULL,
                                     frequency  number(10) NOT NULL,
                                     PRIMARY KEY (locomotive));
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
CREATE TABLE TrackType (
                           id   number(10) NOT NULL,
                           name varchar2(255) NOT NULL UNIQUE,
                           PRIMARY KEY (id));
CREATE TABLE Wagon_Model_Gauge (
                                   Wagon_Modelid number(10) NOT NULL,
                                   Gaugeid       number(10) NOT NULL,
                                   PRIMARY KEY (Wagon_Modelid,
                                                Gaugeid));
CREATE TABLE Train (
                       id       number(10) NOT NULL,
                       operator varchar2(255) NOT NULL,
                       time     timestamp(0) NOT NULL,
                       path     number(10) NOT NULL,
                       "date"   date NOT NULL,
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
                         train       number(10) NOT NULL,
                         PRIMARY KEY (id));
CREATE TABLE Wagon_Freight (
                               wagon   number(10) NOT NULL,
                               freight number(10) NOT NULL,
                               PRIMARY KEY (wagon,
                                            freight));
CREATE TABLE Path (
                      id      number(10) NOT NULL,
                      "start" number(10) NOT NULL,
                      "end"   number(10) NOT NULL,
                      PRIMARY KEY (id));
CREATE TABLE Endpoint_Path (
                               path      number(10) NOT NULL,
                               seq       number(10) NOT NULL,
                               endpoint  number(10) NOT NULL,
                               must_stop varchar2(1) NOT NULL,
                               PRIMARY KEY (path,
                                            seq));
ALTER TABLE Line ADD CONSTRAINT FKLine262396 FOREIGN KEY (owner) REFERENCES Owner (vat);
ALTER TABLE Line_Segment ADD CONSTRAINT FKLine_Segme425123 FOREIGN KEY (line) REFERENCES Line (id);
ALTER TABLE Line ADD CONSTRAINT FKLine635842 FOREIGN KEY (endpoint_a) REFERENCES Endpoint (id);
ALTER TABLE Line ADD CONSTRAINT FKLine635841 FOREIGN KEY (endpoint_b) REFERENCES Endpoint (id);
ALTER TABLE Locomotive ADD CONSTRAINT FKLocomotive389310 FOREIGN KEY (operator) REFERENCES Operator (vat);
ALTER TABLE Wagon ADD CONSTRAINT FKWagon481525 FOREIGN KEY (wagon_model) REFERENCES Wagon_Model (id);
ALTER TABLE Locomotive_Gauge ADD CONSTRAINT FKLocomotive197195 FOREIGN KEY (locomotive) REFERENCES Locomotive (id);
ALTER TABLE Locomotive_Gauge ADD CONSTRAINT FKLocomotive865928 FOREIGN KEY (gauge) REFERENCES Gauge (id);
ALTER TABLE Diesel_Locomotive ADD CONSTRAINT FKDiesel_Loc692799 FOREIGN KEY (locomotive) REFERENCES Locomotive (id);
ALTER TABLE Electric_Locomotive ADD CONSTRAINT FKElectric_L473046 FOREIGN KEY (locomotive) REFERENCES Locomotive (id);
ALTER TABLE Building ADD CONSTRAINT FKBuilding456473 FOREIGN KEY (type) REFERENCES BuildingType (id);
ALTER TABLE Building ADD CONSTRAINT FKBuilding121622 FOREIGN KEY (facility) REFERENCES Facility (id);
ALTER TABLE Endpoint ADD CONSTRAINT FKEndpoint305011 FOREIGN KEY (metafacility) REFERENCES Metafacility (id);
ALTER TABLE Facility ADD CONSTRAINT FKFacility704827 FOREIGN KEY (id) REFERENCES Metafacility (id);
ALTER TABLE Freightyard ADD CONSTRAINT FKFreightyar260042 FOREIGN KEY (id) REFERENCES Metafacility (id);
ALTER TABLE Line_Segment ADD CONSTRAINT FKLine_Segme515959 FOREIGN KEY (track_type) REFERENCES TrackType (id);
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
ALTER TABLE Freight ADD CONSTRAINT FKFreight671762 FOREIGN KEY (train) REFERENCES Train (id);
ALTER TABLE Path ADD CONSTRAINT FKPath989620 FOREIGN KEY ("start") REFERENCES Endpoint (id);
ALTER TABLE Path ADD CONSTRAINT FKPath332544 FOREIGN KEY ("end") REFERENCES Endpoint (id);
ALTER TABLE Train ADD CONSTRAINT FKTrain985667 FOREIGN KEY (path) REFERENCES Path (id);
ALTER TABLE Endpoint_Path ADD CONSTRAINT FKEndpoint_P179752 FOREIGN KEY (endpoint) REFERENCES Endpoint (id);
ALTER TABLE Endpoint_Path ADD CONSTRAINT FKEndpoint_P852905 FOREIGN KEY (path) REFERENCES Path (id);