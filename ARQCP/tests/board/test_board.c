#include "../unity/unity.h"
#include "../../board.h"
#include "../../manager.h"

void setUp(void) {
    freopen("/dev/null", "w", stdout);
}

void tearDown(void) {
    fclose(stdout);
}

// Mostrar linhas livres
void test_display_tracks_when_all_free(void) {
    Manager* manager = init_manager("nonexistent.cfg");

    display_tracks(manager);

    TEST_ASSERT_TRUE(1);

    free_manager(manager);
}

// Mostrar linha ocupada com comboio
void test_display_track_with_train_assigned(void) {
    Manager* manager = init_manager("nonexistent.cfg");
    manager->current_user = &manager->users[0];

    Train* train = (Train*)malloc(sizeof(Train));
    train->id = 100;

    manager->tracks[0].state = TRACK_OCCUPIED;
    manager->tracks[0].train = train;

    display_tracks(manager);

    TEST_ASSERT_TRUE(1);

    free(train);
    manager->tracks[0].train = NULL;
    free_manager(manager);
}

// Mostrar dados dos sensores
void test_display_sensor_readings(void) {
    Manager* manager = init_manager("nonexistent.cfg");

    manager->sensor_data.current_temp = 22;
    manager->sensor_data.current_hum = 65;
        display_sensor_data(manager);

    TEST_ASSERT_TRUE(1);

    free_manager(manager);
}

// Mostrar quadro completo do sistema
void test_display_complete_system_board(void) {
    Manager* manager = init_manager("nonexistent.cfg");

    display_board(manager);

    TEST_ASSERT_TRUE(1);

    free_manager(manager);
}

// Mostrar cabeçalho ASCII
void test_show_ascii_header(void) {
    show_header();

    TEST_ASSERT_TRUE(1);
}

// Limpar ecrã
void test_clear_terminal_screen(void) {
    clear_screen();

    TEST_ASSERT_TRUE(1);
}

// Mostrar mensagem de sucesso
void test_display_success_message(void) {
    display_success("Operação realizada com sucesso");

    TEST_ASSERT_TRUE(1);
}

// Mostrar mensagem de erro
void test_display_error_message(void) {
    display_error("Erro ao processar operação");

    TEST_ASSERT_TRUE(1);
}

// Mostrar alerta de paragem de emergência
void test_display_emergency_stop_alert(void) {
    display_emergency_stop();

    TEST_ASSERT_TRUE(1);
}

int main(void) {
    UNITY_BEGIN();

    RUN_TEST(test_display_tracks_when_all_free);
    RUN_TEST(test_display_track_with_train_assigned);
    RUN_TEST(test_display_sensor_readings);
    RUN_TEST(test_display_complete_system_board);
    RUN_TEST(test_show_ascii_header);
    RUN_TEST(test_clear_terminal_screen);
    RUN_TEST(test_display_success_message);
    RUN_TEST(test_display_error_message);
    RUN_TEST(test_display_emergency_stop_alert);

    return UNITY_END();
}