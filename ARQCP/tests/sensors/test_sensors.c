#include "../unity/unity.h"
#include "../../sensors.h"
#include "../../manager.h"
#include <string.h>

void setUp(void) {}
void tearDown(void) {}

// Extrair dados de linha de temperatura válida
void test_extract_data_from_valid_temperature_line(void) {
    char line[] = "sensor_id:1#type:temperature#value:22.5#unit:celsius#time:1000";

    int sensor_id;
    char type[50];
    float value;
    char unit[50];

    int result = parse_arduino_sensor_line(line, &sensor_id, type, &value, unit);

    TEST_ASSERT_EQUAL_INT(1, result);
    TEST_ASSERT_EQUAL_INT(1, sensor_id);
    TEST_ASSERT_EQUAL_STRING("temperature", type);
    TEST_ASSERT_FLOAT_WITHIN(0.1, 22.5, value);
    TEST_ASSERT_EQUAL_STRING("celsius", unit);
}

// Extrair dados de linha de humidade válida
void test_extract_data_from_valid_humidity_line(void) {
    char line[] = "sensor_id:2#type:humidity#value:65.0#unit:percentage#time:2000";

    int sensor_id;
    char type[50];
    float value;
    char unit[50];

    int result = parse_arduino_sensor_line(line, &sensor_id, type, &value, unit);

    TEST_ASSERT_EQUAL_INT(1, result);
    TEST_ASSERT_EQUAL_INT(2, sensor_id);
    TEST_ASSERT_EQUAL_STRING("humidity", type);
    TEST_ASSERT_FLOAT_WITHIN(0.1, 65.0, value);
    TEST_ASSERT_EQUAL_STRING("percentage", unit);
}

// Rejeitar linha sem identificador de sensor
void test_reject_line_without_sensor_id(void) {
    char invalid_line[] = "type:temperature#value:20#unit:celsius";

    int sensor_id;
    char type[50];
    float value;
    char unit[50];

    int result = parse_arduino_sensor_line(invalid_line, &sensor_id, type, &value, unit);

    TEST_ASSERT_EQUAL_INT(0, result);
}

// Rejeitar linha vazia
void test_reject_empty_line(void) {
    char empty_line[] = "";

    int sensor_id;
    char type[50];
    float value;
    char unit[50];

    int result = parse_arduino_sensor_line(empty_line, &sensor_id, type, &value, unit);

    TEST_ASSERT_EQUAL_INT(0, result);
}

// Processar resposta com múltiplos sensores
void test_process_response_with_multiple_sensors(void) {
    Manager* manager = init_manager("nonexistent.cfg");

    char response[] = "sensor_id:1#type:temperature#value:23.0#unit:celsius#time:1000"
                     "sensor_id:2#type:humidity#value:70.0#unit:percentage#time:1000";

    int readings_processed = process_arduino_sensor_response(manager, response);

    TEST_ASSERT_EQUAL_INT(2, readings_processed);

    free_manager(manager);
}

// Processar apenas dados de temperatura
void test_process_only_temperature_data(void) {
    Manager* manager = init_manager("nonexistent.cfg");

    char response[] = "sensor_id:1#type:temperature#value:25.0#unit:celsius#time:1000";

    int readings_processed = process_arduino_sensor_response(manager, response);

    TEST_ASSERT_EQUAL_INT(1, readings_processed);

    TEST_ASSERT_EQUAL_INT(25, manager->sensor_data.current_temp);

    free_manager(manager);
}

// Processar apenas dados de humidade
void test_process_only_humidity_data(void) {
    Manager* manager = init_manager("nonexistent.cfg");

    char response[] = "sensor_id:2#type:humidity#value:60.0#unit:percentage#time:1000";

    int readings_processed = process_arduino_sensor_response(manager, response);

    TEST_ASSERT_EQUAL_INT(1, readings_processed);

    TEST_ASSERT_EQUAL_INT(60, manager->sensor_data.current_hum);

    free_manager(manager);
}

// Obter valores atuais dos sensores
void test_get_current_sensor_values(void) {
    Manager* manager = init_manager("nonexistent.cfg");

    manager->sensor_data.current_temp = 22;
    manager->sensor_data.current_hum = 65;

    int temperature, humidity;
    get_sensor_data(manager, &temperature, &humidity);

    TEST_ASSERT_EQUAL_INT(22, temperature);
    TEST_ASSERT_EQUAL_INT(65, humidity);

    free_manager(manager);
}

// Processar várias leituras seguidas
void test_process_consecutive_readings(void) {
    Manager* manager = init_manager("nonexistent.cfg");

    process_arduino_sensor_response(manager,
        "sensor_id:1#type:temperature#value:20.0#unit:celsius#time:1000");

    process_arduino_sensor_response(manager,
        "sensor_id:1#type:temperature#value:22.0#unit:celsius#time:2000");

    process_arduino_sensor_response(manager,
        "sensor_id:1#type:temperature#value:24.0#unit:celsius#time:3000");

    TEST_ASSERT_EQUAL_INT(24, manager->sensor_data.current_temp);

    free_manager(manager);
}

int main(void) {
    UNITY_BEGIN();

    RUN_TEST(test_extract_data_from_valid_temperature_line);
    RUN_TEST(test_extract_data_from_valid_humidity_line);
    RUN_TEST(test_reject_line_without_sensor_id);
    RUN_TEST(test_reject_empty_line);
    RUN_TEST(test_process_response_with_multiple_sensors);
    RUN_TEST(test_process_only_temperature_data);
    RUN_TEST(test_process_only_humidity_data);
    RUN_TEST(test_get_current_sensor_values);
    RUN_TEST(test_process_consecutive_readings);

    return UNITY_END();
}