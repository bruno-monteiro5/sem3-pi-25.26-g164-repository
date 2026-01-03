#include "../unity/unity.h"
#include "../../manager.h"

void setUp(void) {}
void tearDown(void) {}

// Inicialização cria estrutura válida
void test_manager_initializes_with_default_values(void) {
    Manager* manager = init_manager("nonexistent.cfg");

    TEST_ASSERT_NOT_NULL(manager);
    TEST_ASSERT_EQUAL_INT(1, manager->num_users);
    TEST_ASSERT_EQUAL_INT(2, manager->num_tracks);
    TEST_ASSERT_EQUAL_STRING("admin", manager->users[0].username);

    free_manager(manager);
}

// Login com credenciais corretas
void test_login_succeeds_with_correct_credentials(void) {
    Manager* manager = init_manager("nonexistent.cfg");

    User* logged_user = login_user(manager, "admin", "ADMIN");

    TEST_ASSERT_NOT_NULL(logged_user);
    TEST_ASSERT_EQUAL_STRING("admin", logged_user->username);
    TEST_ASSERT_EQUAL_PTR(logged_user, manager->current_user);

    free_manager(manager);
}

// Login falha com password errada
void test_login_fails_with_wrong_password(void) {
    Manager* manager = init_manager("nonexistent.cfg");

    User* logged_user = login_user(manager, "admin", "WRONG_PASSWORD");

    TEST_ASSERT_NULL(logged_user);
    TEST_ASSERT_NULL(manager->current_user);

    free_manager(manager);
}

// Login falha com username inexistente
void test_login_fails_with_nonexistent_username(void) {
    Manager* manager = init_manager("nonexistent.cfg");

    User* logged_user = login_user(manager, "hacker", "PASSWORD");

    TEST_ASSERT_NULL(logged_user);

    free_manager(manager);
}

// Atribuir comboio a track livre
void test_assign_train_to_available_track(void) {
    Manager* manager = init_manager("nonexistent.cfg");
    manager->current_user = &manager->users[0];

    int assigned_track_id = -1;
    int result = assign_train_to_track(manager, 100, &assigned_track_id);

    TEST_ASSERT_EQUAL_INT(1, result);
    TEST_ASSERT_EQUAL_INT(1, assigned_track_id);
    TEST_ASSERT_EQUAL_INT(TRACK_OCCUPIED, manager->tracks[0].state);
    TEST_ASSERT_EQUAL_INT(100, manager->tracks[0].train_id);

    free_manager(manager);
}

// Atribuir múltiplos comboios
void test_assign_multiple_trains_to_different_tracks(void) {
    Manager* manager = init_manager("nonexistent.cfg");
    manager->current_user = &manager->users[0];

    int first_track_id, second_track_id;
    int result_first = assign_train_to_track(manager, 100, &first_track_id);
    int result_second = assign_train_to_track(manager, 200, &second_track_id);

    TEST_ASSERT_EQUAL_INT(1, result_first);
    TEST_ASSERT_EQUAL_INT(1, result_second);
    TEST_ASSERT_EQUAL_INT(1, first_track_id);
    TEST_ASSERT_EQUAL_INT(2, second_track_id);
    TEST_ASSERT_EQUAL_INT(100, manager->tracks[0].train_id);
    TEST_ASSERT_EQUAL_INT(200, manager->tracks[1].train_id);

    free_manager(manager);
}

// Falha ao atribuir quando não há tracks livres
void test_assign_fails_when_no_tracks_available(void) {
    Manager* manager = init_manager("nonexistent.cfg");
    manager->current_user = &manager->users[0];

    int track_id_first, track_id_second, track_id_third;
    assign_train_to_track(manager, 100, &track_id_first);
    assign_train_to_track(manager, 200, &track_id_second);
    int result = assign_train_to_track(manager, 300, &track_id_third);

    TEST_ASSERT_EQUAL_INT(0, result);

    free_manager(manager);
}

// Libertar track ocupada
void test_free_occupied_track(void) {
    Manager* manager = init_manager("nonexistent.cfg");
    manager->current_user = &manager->users[0];

    int track_id;
    assign_train_to_track(manager, 100, &track_id);
    int result = free_track(manager, track_id);

    TEST_ASSERT_EQUAL_INT(1, result);
    TEST_ASSERT_EQUAL_INT(TRACK_FREE, manager->tracks[0].state);
    TEST_ASSERT_EQUAL_INT(0, manager->tracks[0].train_id);

    free_manager(manager);
}

// Libertar e reutilizar track
void test_free_and_reuse_track(void) {
    Manager* manager = init_manager("nonexistent.cfg");
    manager->current_user = &manager->users[0];

    int first_track_id, second_track_id;

    // Atribui comboio 100
    assign_train_to_track(manager, 100, &first_track_id);
    TEST_ASSERT_EQUAL_INT(1, first_track_id);

    // Liberta track
    free_track(manager, first_track_id);

    // Reutiliza mesma track para comboio 200
    assign_train_to_track(manager, 200, &second_track_id);
    TEST_ASSERT_EQUAL_INT(1, second_track_id);
    TEST_ASSERT_EQUAL_INT(200, manager->tracks[0].train_id);

    free_manager(manager);
}

int main(void) {
    UNITY_BEGIN();

    RUN_TEST(test_manager_initializes_with_default_values);
    RUN_TEST(test_login_succeeds_with_correct_credentials);
    RUN_TEST(test_login_fails_with_wrong_password);
    RUN_TEST(test_login_fails_with_nonexistent_username);
    RUN_TEST(test_assign_train_to_available_track);
    RUN_TEST(test_assign_multiple_trains_to_different_tracks);
    RUN_TEST(test_assign_fails_when_no_tracks_available);
    RUN_TEST(test_free_occupied_track);
    RUN_TEST(test_free_and_reuse_track);

    return UNITY_END();
}