#include "DHT.h"
#include <arduino-timer.h>

#define DHTPIN 28
#define RED1PIN 13
#define RED2PIN 18
#define YLL1PIN 14
#define YLL2PIN 17
#define GRN1PIN 15
#define GRN2PIN 16
#define DHTTYPE DHT11
#define NR_SOIL_HUMIDITY_SENSORS 2
#define NR_ATMOSPHERIC_HUMIDITY_SENSORS 4
#define NR_ATMOSPHERIC_TEMPERATURE_SENSORS 4
#define DATA_LENGTH 100
#define TYPE_NAME_LENGTH 30
#define UNIT_NAME_LENGTH 30
#define TOGGLE_LED_PERIOD 1000
#define SOIL_HUMIDITY_PERIOD 5000
#define ATMOSPHERIC_HUMIDITY_PERIOD 2000
#define LIGHTSHOW_PERIOD 7000
#define ATMOSPHERIC_TEMPERATURE_PERIOD 2000

char sensor_types[][TYPE_NAME_LENGTH] = {
  "soil_humidity",
  "atmospheric_humidity",
  "atmospheric_temperature"
};

enum sensor_type {
  SOIL_HUMIDITY = 0,
  ATMOSPHERIC_HUMIDITY,
  ATMOSPHERIC_TEMPERATURE
};

char sensor_units[][UNIT_NAME_LENGTH] = {
  "percentage",
  "celsius",
};

enum sensor_unit {
  PERCENTAGE = 0,
  CELSIUS,
};

DHT dht(DHTPIN, DHTTYPE);
auto timer = timer_create_default();

bool toggle_led(void *) {
  digitalWrite(LED_BUILTIN, !digitalRead(LED_BUILTIN));
  return true;
}

int get_sensor_id(int min, int max){
  int id = random(min, max);
  return id;
}

// Turn off all LEDs for a specific track
void turn_off_all_leds_track(int track) {
  if (track == 1) {
    digitalWrite(RED1PIN, LOW);
    digitalWrite(YLL1PIN, LOW);
    digitalWrite(GRN1PIN, LOW);
  } else if (track == 2) {
    digitalWrite(RED2PIN, LOW);
    digitalWrite(YLL2PIN, LOW);
    digitalWrite(GRN2PIN, LOW);
  }
}

void send_data(int id, sensor_type t, float r, sensor_unit u, unsigned long m){
  char buffer[DATA_LENGTH];
  snprintf(buffer, DATA_LENGTH - 1, "sensor_id:%d#type:%s#value:%.2f#unit:%s#time:%lu", 
           id, sensor_types[t], r, sensor_units[u], m);
  Serial.println(buffer);
}

void setup() {
  pinMode(LED_BUILTIN, OUTPUT);
  pinMode(RED1PIN, OUTPUT);
  pinMode(RED2PIN, OUTPUT);
  pinMode(YLL1PIN, OUTPUT);
  pinMode(YLL2PIN, OUTPUT);
  pinMode(GRN1PIN, OUTPUT);
  pinMode(GRN2PIN, OUTPUT);
  
  randomSeed(analogRead(0));
  Serial.begin(9600);
  dht.begin();
  timer.every(TOGGLE_LED_PERIOD, toggle_led);
}

void loop() {
  timer.tick();
  
  String cmd = Serial.readStringUntil('\n');
  cmd.trim();
  
  if (cmd == "GTH") {
    // Read all sensors
    int min, max, id;
    float r;
    String output = ""; // Single output string
    
    // Temperature
    min = NR_SOIL_HUMIDITY_SENSORS + NR_ATMOSPHERIC_HUMIDITY_SENSORS + 1;
    max = min + NR_ATMOSPHERIC_TEMPERATURE_SENSORS;
    id = get_sensor_id(min, max);
    r = dht.readTemperature();
    if (!isnan(r)) {
      char buffer[DATA_LENGTH];
      snprintf(buffer, DATA_LENGTH - 1, "sensor_id:%d#type:%s#value:%.2f#unit:%s#time:%lu", 
               id, sensor_types[ATMOSPHERIC_TEMPERATURE], r, sensor_units[CELSIUS], millis());
      output += String(buffer) + "#";
    }
    
    // Atmospheric Humidity
    min = NR_SOIL_HUMIDITY_SENSORS + 1;
    max = min + NR_ATMOSPHERIC_HUMIDITY_SENSORS;
    id = get_sensor_id(min, max);
    r = dht.readHumidity();
    if (!isnan(r)) {
      char buffer[DATA_LENGTH];
      snprintf(buffer, DATA_LENGTH - 1, "sensor_id:%d#type:%s#value:%.2f#unit:%s#time:%lu", 
               id, sensor_types[ATMOSPHERIC_HUMIDITY], r, sensor_units[PERCENTAGE], millis());
      output += String(buffer) + "#";
    }
    
    // Soil Humidity
    min = 1;
    max = min + NR_SOIL_HUMIDITY_SENSORS;
    id = get_sensor_id(min, max);
    r = dht.readHumidity();
    if (!isnan(r)) {
      char buffer[DATA_LENGTH];
      snprintf(buffer, DATA_LENGTH - 1, "sensor_id:%d#type:%s#value:%.2f#unit:%s#time:%lu", 
               id, sensor_types[SOIL_HUMIDITY], r, sensor_units[PERCENTAGE], millis());
      output += String(buffer) + "#";
    }
    
    // Send all as one line
    Serial.println(output);
    
  } else if (cmd.length() >= 5 && cmd[0] == 'R' && cmd[1] == 'E') {
    int track = (cmd[3] == '0' && cmd[4] == '1') ? 1 : ((cmd[3] == '0' && cmd[4] == '2') ? 2 : 0);
    
    if (track == 1) {
      turn_off_all_leds_track(1);
      digitalWrite(RED1PIN, HIGH);
      Serial.println("Track 1: Red LED ON");
    } else if (track == 2) {
      turn_off_all_leds_track(2);
      digitalWrite(RED2PIN, HIGH);
      Serial.println("Track 2: Red LED ON");
    } else {
      Serial.println("Invalid track number");
    }
    
  } else if (cmd.length() >= 5 && cmd[0] == 'Y' && cmd[1] == 'E') {
    int track = (cmd[3] == '0' && cmd[4] == '1') ? 1 : ((cmd[3] == '0' && cmd[4] == '2') ? 2 : 0);
    
    if (track == 1) {
      turn_off_all_leds_track(1);
      digitalWrite(YLL1PIN, HIGH);
      Serial.println("Track 1: Yellow LED ON");
    } else if (track == 2) {
      turn_off_all_leds_track(2);
      digitalWrite(YLL2PIN, HIGH);
      Serial.println("Track 2: Yellow LED ON");
    } else {
      Serial.println("Invalid track number");
    }
    
  } else if (cmd.length() >= 5 && cmd[0] == 'G' && cmd[1] == 'E') {
    int track = (cmd[3] == '0' && cmd[4] == '1') ? 1 : ((cmd[3] == '0' && cmd[4] == '2') ? 2 : 0);
    
    if (track == 1) {
      turn_off_all_leds_track(1);
      digitalWrite(GRN1PIN, HIGH);
      Serial.println("Track 1: Green LED ON");
    } else if (track == 2) {
      turn_off_all_leds_track(2);
      digitalWrite(GRN2PIN, HIGH);
      Serial.println("Track 2: Green LED ON");
    } else {
      Serial.println("Invalid track number");
    }
    
  } else if (cmd.length() >= 5 && cmd[0] == 'R' && cmd[1] == 'B') {
    int track = (cmd[3] == '0' && cmd[4] == '1') ? 1 : ((cmd[3] == '0' && cmd[4] == '2') ? 2 : 0);
    
    if (track == 1) {
      turn_off_all_leds_track(1);
      for (int i = 0; i < 5; i++) {
        digitalWrite(RED1PIN, HIGH);
        delay(300);
        digitalWrite(RED1PIN, LOW);
        delay(300);
      }
      Serial.println("Track 1: Red LED Blinked");
    } else if (track == 2) {
      turn_off_all_leds_track(2);
      for (int i = 0; i < 5; i++) {
        digitalWrite(RED2PIN, HIGH);
        delay(300);
        digitalWrite(RED2PIN, LOW);
        delay(300);
      }
      Serial.println("Track 2: Red LED Blinked");
    } else {
      Serial.println("Invalid track number");
    }
  }
}