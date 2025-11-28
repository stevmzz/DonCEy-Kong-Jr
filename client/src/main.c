#define NOGDI
#define NOUSER

#ifndef WIN32_LEAN_AND_MEAN
#define WIN32_LEAN_AND_MEAN
#endif
#include <winsock2.h>
#include <ws2tcpip.h>
#include <windows.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <math.h>
#include "raylib.h"
#include "constants.h"
#include "network.h"
#include "state_manager.h"
#include "main_screen.h"
#include "player_screen.h"
#include "spectator_screen.h"
#include "game_over_screen.h"
#include "client_protocol.h"
#include "input_handler.h"

#define MAX_MSG_LEN 1024

/**
 * @brief Storage global de frutas
 * 
 * Array que almacena todas las frutas activas recibidas del servidor.
 * Protegido con mutex (fruits_lock) para acceso thread-safe desde
 * el hilo receptor y el hilo principal de rendering.
 */
static ServerFruit fruits[MAX_FRUITS];
static CRITICAL_SECTION fruits_lock;
static PlayerScreen* player_screen = NULL;
static GameOverScreen* game_over_screen = NULL;
static int player_id = -1;
static SOCKET global_sock = -1;
static volatile int receiver_running = 0;
static int player_score = 0;

// Variables para estado del juego
static int game_over = 0;
static int game_over_player_id = -1;

/**
 * @brief Inicializa el array de frutas
 * 
 * Marca todas las frutas como inactivas al inicio del programa.
 */
void init_fruits() {
    for (int i = 0; i < MAX_FRUITS; ++i) {
        fruits[i].active = 0;
    }
}

/**
 * @brief Agrega o reemplaza una fruta en el storage global
 * 
 * Si ya existe una fruta con ese ID, actualiza su posición y puntos.
 * Si no existe, la agrega en el primer slot disponible.
 * Thread-safe: usa mutex para proteger acceso concurrente.
 * 
 * @param id ID único de la fruta (asignado por servidor)
 * @param x Posición X en pantalla
 * @param y Posición Y en pantalla
 * @param type Tipo de fruta ("MANZANA", "BANANO", "MANGO")
 * @param points Puntos que vale esta fruta
 */
void add_or_replace_fruit_from_server(int id, int x, int y, const char* type, int points) {
    EnterCriticalSection(&fruits_lock);

    // Buscar si ya existe y actualizar
    for (int i = 0; i < MAX_FRUITS; ++i) {
        if (fruits[i].active && fruits[i].id == id) {
            fruits[i].x = x;
            fruits[i].y = y;
            fruits[i].points = points;
            strncpy(fruits[i].type, type, sizeof(fruits[i].type) - 1);
            LeaveCriticalSection(&fruits_lock);
            return;
        }
    }

    // Buscar primer slot vacío
    for (int i = 0; i < MAX_FRUITS; ++i) {
        if (!fruits[i].active) {
            fruits[i].active = 1;
            fruits[i].id = id;
            fruits[i].x = x;
            fruits[i].y = y;
            fruits[i].points = points;
            strncpy(fruits[i].type, type, sizeof(fruits[i].type) - 1);
            LeaveCriticalSection(&fruits_lock);
            return;
        }
    }

    LeaveCriticalSection(&fruits_lock);
}

/**
 * @brief Remueve una fruta del storage global
 * 
 * @param id ID único de la fruta a remover
 */
void remove_fruit_from_server(int id) {
    EnterCriticalSection(&fruits_lock);

    for (int i = 0; i < MAX_FRUITS; ++i) {
        if (fruits[i].active && fruits[i].id == id) {
            fruits[i].active = 0;
            break;
        }
    }

    LeaveCriticalSection(&fruits_lock);
}

/**
 * @brief Procesa mensajes recibidos del servidor
 * 
 * Parsea y maneja los siguientes tipos de mensajes:
 * - ASSIGN_ID: asigna ID único al cliente
 * - PLAYER_POS: actualiza posición de un jugador en pantalla
 * - SPAWN_FRUIT: crea nueva fruta
 * - REMOVE_FRUIT: elimina fruta
 * - PLAYER_SCORE: notifica que un jugador ganó puntos
 * - GAME_OVER: notifica que un jugador murió
 * 
 * @param msg Mensaje recibido del servidor (string terminado en \n)
 */
void handle_server_message(const char* msg) {

    if (strncmp(msg, "ASSIGN_ID", 9) == 0) {
        int id = -1;
        if (sscanf(msg + 9, "%d", &id) == 1) {
            player_id = id;
            printf("[CLIENT] Asignado ID: %d\n", player_id);
        }
        return;
    }

    if (strncmp(msg, "PLAYER_POS", 10) == 0) {
        int id, x, y;
        if (sscanf(msg + 10, "%d %d %d", &id, &x, &y) == 3) {
            if (player_screen != NULL) {
                PlayerScreen_UpdatePlayerPos(player_screen, id, x, y);
            }
        }
        return;
    }

    if (strncmp(msg, "SPAWN_FRUIT", 11) == 0) {
        int fid, x, y, pts;
        char type[64];
        if (sscanf(msg + 11, "%d %d %d %63s %d", &fid, &x, &y, type, &pts) >= 4) {
            add_or_replace_fruit_from_server(fid, x, y, type, pts);
            printf("[CLIENT] SPAWN_FRUIT %d (%s) en (%d,%d) = %d pts\n", fid, type, x, y, pts);
        }
        return;
    }

    if (strncmp(msg, "REMOVE_FRUIT", 12) == 0) {
        int fid;
        if (sscanf(msg + 12, "%d", &fid) == 1) {
            remove_fruit_from_server(fid);
            printf("[CLIENT] REMOVE_FRUIT %d\n", fid);
        }
        return;
    }

    if (strncmp(msg, "PLAYER_SCORE", 12) == 0) {
        int pid, pts;
        if (sscanf(msg + 12, "%d %d", &pid, &pts) == 2) {
            printf("[CLIENT] PLAYER_SCORE: jugador %d obtuvo %d pts\n", pid, pts);
        }
        return;
    }

    // NUEVO: Procesar GAME_OVER
    if (strncmp(msg, "GAME_OVER", 9) == 0) {
        int pid;
        if (sscanf(msg + 9, "%d", &pid) == 1) {
            if (pid == player_id) {
                game_over = 1;
                game_over_player_id = pid;
                printf("[CLIENT] ¡GAME OVER! Jugador #%d murió\n", pid);
            }
        }
        return;
    }
}

/**
 * @brief Hilo receptor de mensajes del servidor
 * 
 * Se ejecuta en paralelo con el hilo principal.
 * Lee continuamente mensajes del servidor y los procesa.
 * Termina cuando receiver_running se pone en 0.
 * 
 * @param param Socket de conexión (SOCKET)
 * @return 0 al terminar
 */
DWORD WINAPI receiver_thread_func(LPVOID param) {
    SOCKET sock = (SOCKET)param;
    char *msg;
    receiver_running = 1;

    while (receiver_running) {
        msg = recv_message(sock);
        if (msg == NULL) {
            printf("[CLIENT] Receptor: conexión cerrada\n");
            break;
        }
        handle_server_message(msg);
    }

    receiver_running = 0;
    return 0;
}

/**
 * @brief Envía comando de comer fruta al servidor
 * 
 * @param sock Socket de conexión
 * @param fid ID de la fruta a comer
 */
void send_eat_fruit(SOCKET sock, int fid) {
    if (sock == -1 || player_id == -1) return;

    char buf[MAX_MSG_LEN];
    snprintf(buf, MAX_MSG_LEN, "EAT_FRUIT %d %d", player_id, fid);
    send_message(sock, buf);
}

/**
 * @brief Renderiza todas las frutas activas en pantalla
 * 
 * Cada tipo de fruta tiene una representación visual diferente:
 * - MANZANA: círculo rojo
 * - BANANO: rectángulo amarillo
 * - MANGO: hexágono rosa
 * 
 * Dibuja el nombre y los puntos debajo de cada fruta.
 * Thread-safe: usa mutex para proteger lectura de array.
 */
void draw_fruits() {
    EnterCriticalSection(&fruits_lock);

    for (int i = 0; i < MAX_FRUITS; ++i) {
        if (!fruits[i].active) continue;

        int x = fruits[i].x;
        int y = fruits[i].y;

        // MANZANA → Círculo rojo
        if (strcmp(fruits[i].type, "MANZANA") == 0) {
            DrawCircle(x, y, 14, RED);
        }
        // BANANO → Rectángulo amarillo (alargado)
        else if (strcmp(fruits[i].type, "BANANO") == 0) {
            DrawRectangle(x - 25, y - 8, 50, 16, YELLOW);
        }
        // MANGO → Hexágono rosa
        else if (strcmp(fruits[i].type, "MANGO") == 0) {
            DrawPoly((Vector2){x, y}, 6, 16.0f, 0.0f, PINK);
        }

        // Etiqueta y puntos
        DrawText(fruits[i].type, x - 28, y - 32, 10, BLACK);

        char pts[16];
        snprintf(pts, sizeof(pts), "%d", fruits[i].points);
        DrawText(pts, x - 6, y + 18, 10, DARKGRAY);
    }

    LeaveCriticalSection(&fruits_lock);
}

/**
 * @brief Renderiza el score en la esquina superior derecha
 * 
 * Visible tanto en STATE_PLAYER como en STATE_SPECTATOR.
 */
void draw_score() {
    char score_text[64];
    snprintf(score_text, sizeof(score_text), "PUNTOS: %d", player_score);
    DrawText(score_text, 820, 20, 24, BLACK);
}

/**
 * @brief Función principal
 * 
 * Inicializa conexión con servidor (si está disponible),
 * crea ventana Raylib, maneja game loop principal,
 * y limpia recursos al salir.
 */
int main(void) {

    SOCKET sock;

    InitializeCriticalSection(&fruits_lock);
    init_fruits();

    printf("\n========== DonCEy Kong Jr - Cliente C ==========\n\n");

    printf("[CLIENTE]: Intentando conectar a %s:%d...\n", SERVER_IP, SERVER_PORT);
    sock = connect_to_server(SERVER_IP, SERVER_PORT);

    if (sock == -1) {
        printf("[ERROR]: No se pudo conectar al servidor\n");
        printf("[CLIENTE]: Continuando sin servidor...\n\n");
    } else {
        printf("[CLIENTE]: Conectado al servidor\n\n");
        global_sock = sock;
        CreateThread(NULL, 0, receiver_thread_func, (LPVOID)sock, 0, NULL);
    }

    InitWindow(1024, 768, "DonCEy Kong Jr");
    SetTargetFPS(60);
    printf("[RAYLIB]: Ventana creada (1024x768)\n");

    StateManager* state_manager = StateManager_Create();
    MainScreen* main_screen = MainScreen_Create();
    player_screen = PlayerScreen_Create();
    game_over_screen = GameOverScreen_Create();
    SpectatorScreen* spectator_screen = SpectatorScreen_Create();

    printf("[INICIALIZACION]: Todos los recursos gráficos creados\n\n");

    // ============================================
    // GAME LOOP PRINCIPAL
    // ============================================
    while (!WindowShouldClose()) {

        GameState current_state = StateManager_GetCurrentState(state_manager);

        // NUEVO: Detectar si hubo game over
        if (game_over == 1 && current_state == STATE_PLAYER) {
            StateManager_SetNextState(state_manager, STATE_GAME_OVER);
            game_over = 0;
        }

        // Detección de click para comer frutas (solo en STATE_PLAYER)
        if (IsMouseButtonPressed(MOUSE_BUTTON_LEFT) && current_state == STATE_PLAYER) {
            int mx = GetMouseX();
            int my = GetMouseY();

            EnterCriticalSection(&fruits_lock);

            for (int i = 0; i < MAX_FRUITS; ++i) {
                if (!fruits[i].active) continue;

                int dx = mx - fruits[i].x;
                int dy = my - fruits[i].y;
                int dist2 = dx * dx + dy * dy;

                // Si clickeó cerca de la fruta (radio ~20px)
                if (dist2 <= (20 * 20)) {
                    int fid = fruits[i].id;
                    int pts = fruits[i].points;

                    fruits[i].active = 0;
                    player_score += pts;

                    LeaveCriticalSection(&fruits_lock);

                    if (global_sock != -1) {
                        send_eat_fruit(global_sock, fid);
                    }

                    break;
                }
            }

            LeaveCriticalSection(&fruits_lock);
        }

        // INPUT HANDLING
        switch (current_state) {
            case STATE_MAIN_MENU:
                MainScreen_HandleInput(main_screen, state_manager);
                break;
            case STATE_PLAYER:
                InputHandler_Update(global_sock, player_id);
                PlayerScreen_HandleInput(player_screen, state_manager);
                break;
            case STATE_SPECTATOR:
                SpectatorScreen_HandleInput(spectator_screen, state_manager);
                break;
            case STATE_GAME_OVER:
                GameOverScreen_HandleInput(game_over_screen, state_manager);
                break;
            case STATE_EXIT:
                break;
        }

        StateManager_Update(state_manager);
        current_state = StateManager_GetCurrentState(state_manager);

        if (StateManager_HasStateChanged(state_manager)) {
            switch (current_state) {
                case STATE_MAIN_MENU:
                    printf("[ESTADO]: Cambio a MAIN_MENU\n");
                    break;
                case STATE_PLAYER:
                    printf("[ESTADO]: Cambio a PLAYER\n");
                    break;
                case STATE_SPECTATOR:
                    printf("[ESTADO]: Cambio a SPECTATOR\n");
                    break;
                case STATE_GAME_OVER:
                    printf("[ESTADO]: Cambio a GAME_OVER\n");
                    break;
                case STATE_EXIT:
                    break;
            }
        }

        // UPDATE LOGIC
        switch (current_state) {
            case STATE_MAIN_MENU:
                MainScreen_Update(main_screen);
                break;
            case STATE_PLAYER:
                PlayerScreen_Update(player_screen);
                break;
            case STATE_SPECTATOR:
                SpectatorScreen_Update(spectator_screen);
                break;
            case STATE_GAME_OVER:
                GameOverScreen_Update(game_over_screen);
                break;
            default:
                break;
        }

        // RENDER
        BeginDrawing();

        switch (current_state) {
            case STATE_MAIN_MENU:
                MainScreen_Render(main_screen);
                break;

            case STATE_PLAYER:
                PlayerScreen_Render(player_screen);
                draw_fruits();
                draw_score();
                break;

            case STATE_SPECTATOR:
                SpectatorScreen_Render(spectator_screen);
                draw_fruits();
                draw_score();
                break;

            case STATE_GAME_OVER:
                GameOverScreen_Render(game_over_screen);
                break;

            default:
                break;
        }

        EndDrawing();

        if (current_state == STATE_EXIT) {
            break;
        }
    }

    // CLEANUP
    receiver_running = 0;
    Sleep(100);

    if (global_sock != -1) {
        close_connection(global_sock);
    }

    GameOverScreen_Destroy(game_over_screen);
    SpectatorScreen_Destroy(spectator_screen);
    PlayerScreen_Destroy(player_screen);
    MainScreen_Destroy(main_screen);
    StateManager_Destroy(state_manager);

    CloseWindow();
    DeleteCriticalSection(&fruits_lock);

    printf("\n[SALIDA]: Programa finalizado\n\n");

    return 0;
}