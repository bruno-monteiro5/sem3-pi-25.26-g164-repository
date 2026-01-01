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
#define NR_SOIL_HUMIDITY_SENSORS 2 // Sensor ID: 1, Sensor ID: 2
#define NR_ATMOSPHERIC_HUMIDITY_SENSORS 4 // Sensor ID: 3, Sensor ID:4, Sensor ID: 5, Sensor 6
#define NR_ATMOSPHERIC_TEMPERATURE_SENSORS 4 // Sensor ID: 7, Sensor ID:8, Sensor ID: 9, Sensor ID: 10
#define DATA_LENGTH 100
#define TYPE_NAME_LENGTH 30
#define UNIT_NAME_LENGTH 30
#define TOGGLE_LED_PERIOD 1000 //miliseconds
#define SOIL_HUMIDITY_PERIOD 5000
#define ATMOSPHERIC_HUMIDITY_PERIOD 2000
#define LIGHTSHOW_PERIOD 7000
#define INTER_PERIOD 700
#define ATMOSPHERIC_TEMPERATURE_PERIOD 2000
char sensor_types[][TYPE_NAME_LENGTH] =
{
"soil_humidity",
"atmospheric_humidity",
"atmospheric_temperature"
};
enum sensor_type {
SOIL_HUMIDITY = 0,
ATMOSPHERIC_HUMIDITY,
ATMOSPHERIC_TEMPERATURE
};
char sensor_units[][UNIT_NAME_LENGTH] =
{
"percentage",
"celsius",
};
enum sensor_unit {
PERCENTAGE = 0,
CELSIUS,
};
DHT dht(DHTPIN, DHTTYPE);
auto timer = timer_create_default(); // create a timer with default settings
bool toggle_led(void *) {
digitalWrite(LED_BUILTIN, !digitalRead(LED_BUILTIN)); // toggle the LED
return true; // keep timer active? true
}
bool lightshow(void *){
  digitalWrite(RED1PIN, HIGH);
  delay(INTER_PERIOD);
  digitalWrite(RED1PIN, LOW);
  digitalWrite(YLL1PIN, HIGH);
  delay(INTER_PERIOD);
  digitalWrite(YLL1PIN, LOW);
  digitalWrite(GRN1PIN, HIGH);
  delay(INTER_PERIOD);
  digitalWrite(GRN1PIN, LOW);
  digitalWrite(RED2PIN, HIGH);
  delay(INTER_PERIOD);
  digitalWrite(RED2PIN, LOW);
  digitalWrite(YLL2PIN, HIGH);
  delay(INTER_PERIOD);
  digitalWrite(YLL2PIN, LOW);
  digitalWrite(GRN2PIN, HIGH);
  delay(INTER_PERIOD);
  digitalWrite(GRN2PIN, LOW);
  return true;
}
int get_sensor_id(int min, int max){
int id = random(min, max); //generate a random value [min, max[
return id;
}
bool read_soil_humidity(void *){
int min = 1;
int max = min + NR_SOIL_HUMIDITY_SENSORS;
int id = get_sensor_id(min, max);
float r = dht.readHumidity();
if (!isnan(r)) {
send_data(id,SOIL_HUMIDITY, r, PERCENTAGE, millis());
Serial.println("Soil humidity read.");
}
return true;
}
bool read_atmospheric_humidity(void *){
int min = NR_SOIL_HUMIDITY_SENSORS +1;
int max = min + NR_ATMOSPHERIC_HUMIDITY_SENSORS;
int id = get_sensor_id(min, max);
float r = dht.readHumidity();
if (!isnan(r)) {
send_data(id,ATMOSPHERIC_HUMIDITY, r, PERCENTAGE, millis());
Serial.println("Atmospherical humidity read.");
}
return true;
}
bool read_atmospheric_temperature(void *){
int min = NR_SOIL_HUMIDITY_SENSORS + NR_ATMOSPHERIC_HUMIDITY_SENSORS +
1;
int max = min + NR_ATMOSPHERIC_TEMPERATURE_SENSORS;
int id = get_sensor_id(min, max);
float r = dht.readTemperature();
if (!isnan(r)) {
send_data(id,ATMOSPHERIC_TEMPERATURE, r, CELSIUS, millis());
Serial.println("Atmospheric temperature read.");
}
return true;
}
void send_data(int id, sensor_type t, float r, sensor_unit u, unsigned long m){
char buffer[DATA_LENGTH];
snprintf(buffer, DATA_LENGTH- 1,"sensor_id:%d#type:%s#value:%.2f#unit:%s#time:%lu", id,sensor_types[t],r, sensor_units[u], m);
Serial.println(buffer);
}

void setup() {
// put your setup code here, to run once:
// initialize digital pin LED_BUILTIN as an output.
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
//timer.every(ATMOSPHERIC_TEMPERATURE_PERIOD, read_atmospheric_temperature);
//timer.every(ATMOSPHERIC_HUMIDITY_PERIOD, read_atmospheric_humidity);
//timer.every(SOIL_HUMIDITY_PERIOD, read_soil_humidity);
}

void loop() {
timer.tick(); // tick the time

String cmd = Serial.readStringUntil('\n');


if(cmd == "GTH"){
  read_atmospheric_temperature(nullptr);
  read_atmospheric_humidity(nullptr);
  read_soil_humidity(nullptr);

  
}else if(cmd[0] == 'R' && cmd[1] == 'E'){
  if(cmd[4] == '1' && cmd[3] == '0'){
  if(digitalRead(RED1PIN) == LOW){
    Serial.println("Turn on the red light");
    digitalWrite(RED1PIN, HIGH);
  }else{
    Serial.println("Turn off the red light");
    digitalWrite(RED1PIN, LOW);
  }
  }else if(cmd[4]=='2' && cmd[3]=='0'){
    if(digitalRead(RED2PIN) == LOW){
    Serial.println("Turn on the red light");
    digitalWrite(RED2PIN, HIGH);
    }else{
    Serial.println("Turn off the red light");
    digitalWrite(RED2PIN, LOW);
  } 
  }else{
    Serial.println("Invalid track number");
  }
}else if(cmd[0] == 'Y' && cmd[1] == 'E'){
  if(cmd[4] == '1' && cmd[3] == '0'){
    if(digitalRead(YLL1PIN) == LOW){
    Serial.println("Turn on the yellow light");
    digitalWrite(YLL1PIN, HIGH);
    }else{
    Serial.println("Turn off the yellow light");
    digitalWrite(YLL1PIN, LOW);
    }
  }else if(cmd[4]=='2' && cmd[3]=='0'){
    if(digitalRead(YLL2PIN) == LOW){
    Serial.println("Turn on the yellow light");
    digitalWrite(YLL2PIN, HIGH);
    }else{
    Serial.println("Turn off the yellow light");
    digitalWrite(YLL2PIN, LOW);
    }
  }else{
    Serial.println("Invalid track number");
  }
}else if(cmd[0] == 'G' && cmd[1] == 'E'){
  if(cmd[4] == '1' && cmd[3] == '0'){
    if(digitalRead(GRN1PIN) == LOW){
      Serial.println("Turn on the green light");
      digitalWrite(GRN1PIN, HIGH);
    }else{
      Serial.println("Turn off the green light");
      digitalWrite(GRN1PIN, LOW);
    }
  }else if(cmd[4]=='2' && cmd[3]=='0'){
    if(digitalRead(GRN2PIN) == LOW){
      Serial.println("Turn on the green light");
      digitalWrite(GRN2PIN, HIGH);
    }else{
      Serial.println("Turn off the green light");
      digitalWrite(GRN2PIN, LOW);
    }
  }else{
    Serial.println("Invalid track number");
  }

}else if(cmd[0] == 'R' && cmd[1] == 'B'){
  if(cmd[4] == '1' && cmd[3] == '0'){
    Serial.println("Blink the red light");
    digitalWrite(RED1PIN, HIGH);
    delay(500);
    digitalWrite(RED1PIN, LOW);
  }else if(cmd[4]=='2' && cmd[3]=='0'){
    Serial.println("Blink the red light");
    digitalWrite(RED2PIN, HIGH);
    delay(500);
    digitalWrite(RED2PIN, LOW);
  }else{
    Serial.println("Invalid track number");
  }
}
}
