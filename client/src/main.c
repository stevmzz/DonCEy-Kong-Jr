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
#include "client_protocol.h"

#define MAX_MSG_LEN 1024

// ==============================
// STORAGE GLOBAL DE FRUTAS
// ==============================
static ServerFruit fruits[MAX_FRUITS];
static CRITICAL_SECTION fruits_lock;
static int player_id = -1;
static SOCKET global_sock = -1;
static volatile int receiver_running = 0;
static int player_score = 0;   // ✅ SCORE GLOBAL VISIBLE PARA PLAYER Y SPECTATOR

// ==============================
// INICIALIZACIÓN DE FRUTAS
// ==============================
void init_fruits() {
    for (int i = 0; i < MAX_FRUITS; ++i) fruits[i].active = 0;
}

// ==============================
// AGREGAR / REEMPLAZAR FRUTA
// ==============================
void add_or_replace_fruit_from_server(int id, int x, int y, const char* type, int points) {
    EnterCriticalSection(&fruits_lock);

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

// ==============================
// REMOVER FRUTA
// ==============================
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

// ==============================
// MANEJO DE MENSAJES DEL SERVIDOR
// ==============================
void handle_server_message(const char* msg) {

    if (strncmp(msg, "ASSIGN_ID", 9) == 0) {
        int id = -1;
        if (sscanf(msg + 9, "%d", &id) == 1) {
            player_id = id;
            printf("[CLIENT] Assigned id %d\n", player_id);
        }
        return;
    }

    if (strncmp(msg, "SPAWN_FRUIT", 11) == 0) {
        int fid, x, y, pts;
        char type[64];
        if (sscanf(msg + 11, "%d %d %d %63s %d", &fid, &x, &y, type, &pts) >= 4) {
            add_or_replace_fruit_from_server(fid, x, y, type, pts);
            printf("[CLIENT] SPAWN_FRUIT %d (%s) at %d,%d (%d pts)\n", fid, type, x, y, pts);
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
            printf("[CLIENT] PLAYER_SCORE player %d got %d pts\n", pid, pts);
        }
        return;
    }
}

// ==============================
// HILO RECEPTOR
// ==============================
DWORD WINAPI receiver_thread_func(LPVOID param) {
    SOCKET sock = (SOCKET)param;
    char *msg;
    receiver_running = 1;

    while (receiver_running) {
        msg = recv_message(sock);
        if (msg == NULL) {
            printf("[CLIENT] recv returned NULL, stopping receiver\n");
            break;
        }
        handle_server_message(msg);
    }

    receiver_running = 0;
    return 0;
}

// ==============================
// ENVÍO DE COMER FRUTA
// ==============================
void send_eat_fruit(SOCKET sock, int fid) {
    if (sock == -1 || player_id == -1) return;

    char buf[MAX_MSG_LEN];
    snprintf(buf, MAX_MSG_LEN, "EAT_FRUIT %d %d", player_id, fid);
    send_message(sock, buf);
}

// ==============================
// DIBUJAR FRUTAS CON FORMAS Y COLORES
// ==============================
void draw_fruits() {
    EnterCriticalSection(&fruits_lock);

    for (int i = 0; i < MAX_FRUITS; ++i) {
        if (!fruits[i].active) continue;

        int x = fruits[i].x;
        int y = fruits[i].y;

        // =========================
        // MANZANA → CÍRCULO ROJO
        // =========================
        if (strcmp(fruits[i].type, "MANZANA") == 0) {
            DrawCircle(x, y, 14, RED);
        }

        // =========================
        // BANANO → RECTÁNGULO AMARILLO (MÁS LARGO)
        // =========================
        else if (strcmp(fruits[i].type, "BANANO") == 0) {
            DrawRectangle(x - 25, y - 8, 50, 16, YELLOW); // ✅ más largo
        }

        // =========================
        // MANGO → HEXÁGONO ROSADO (FORMA CORRECTA)
        // =========================
        else if (strcmp(fruits[i].type, "MANGO") == 0) {
            // ✅ DrawPoly asegura que siempre se vea
            DrawPoly((Vector2){x, y}, 6, 16.0f, 0.0f, PINK);
        }

        // =========================
        // TEXTO Y PUNTOS
        // =========================
        DrawText(fruits[i].type, x - 28, y - 32, 10, BLACK);

        char pts[16];
        snprintf(pts, sizeof(pts), "%d", fruits[i].points);
        DrawText(pts, x - 6, y + 18, 10, DARKGRAY);
    }

    LeaveCriticalSection(&fruits_lock);
}

// ==============================
// DIBUJAR SCORE (PLAYER Y SPECTATOR)
// ==============================
void draw_score() {
    char score_text[64];
    snprintf(score_text, sizeof(score_text), "PUNTOS: %d", player_score);
    DrawText(score_text, 820, 20, 24, BLACK);
}

// ==============================
// MAIN
// ==============================
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

    StateManager* state_manager = StateManager_Create();
    MainScreen* main_screen = MainScreen_Create();
    PlayerScreen* player_screen = PlayerScreen_Create();
    SpectatorScreen* spectator_screen = SpectatorScreen_Create();

    while (!WindowShouldClose()) {

        GameState current_state = StateManager_GetCurrentState(state_manager);

        // DETECCIÓN DE CLICK PARA COMER
        if (IsMouseButtonPressed(MOUSE_BUTTON_LEFT) && current_state == STATE_PLAYER) {

            int mx = GetMouseX();
            int my = GetMouseY();

            EnterCriticalSection(&fruits_lock);

            for (int i = 0; i < MAX_FRUITS; ++i) {
                if (!fruits[i].active) continue;

                int dx = mx - fruits[i].x;
                int dy = my - fruits[i].y;
                int dist2 = dx * dx + dy * dy;

                if (dist2 <= (20 * 20)) {

                    int fid = fruits[i].id;
                    int pts = fruits[i].points;

                    fruits[i].active = 0;
                    player_score += pts;  // ✅ SUMA DE PUNTOS

                    LeaveCriticalSection(&fruits_lock);

                    if (global_sock != -1)
                        send_eat_fruit(global_sock, fid);

                    break;
                }
            }

            LeaveCriticalSection(&fruits_lock);
        }

        // ==============================
        // INPUT
        // ==============================
        switch (current_state) {
            case STATE_MAIN_MENU:   MainScreen_HandleInput(main_screen, state_manager); break;
            case STATE_PLAYER:      PlayerScreen_HandleInput(player_screen, state_manager); break;
            case STATE_SPECTATOR:   SpectatorScreen_HandleInput(spectator_screen, state_manager); break;
            default: break;
        }

        StateManager_Update(state_manager);
        current_state = StateManager_GetCurrentState(state_manager);

        // ==============================
        // UPDATE
        // ==============================
        switch (current_state) {
            case STATE_MAIN_MENU:   MainScreen_Update(main_screen); break;
            case STATE_PLAYER:      PlayerScreen_Update(player_screen); break;
            case STATE_SPECTATOR:   SpectatorScreen_Update(spectator_screen); break;
            default: break;
        }

        // ==============================
        // RENDER
        // ==============================
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

            default: break;
        }

        EndDrawing();
    }

    receiver_running = 0;
    Sleep(100);

    if (global_sock != -1) close_connection(global_sock);

    SpectatorScreen_Destroy(spectator_screen);
    PlayerScreen_Destroy(player_screen);
    MainScreen_Destroy(main_screen);
    StateManager_Destroy(state_manager);

    CloseWindow();
    DeleteCriticalSection(&fruits_lock);

    printf("\n[SALIDA]: Programa finalizado\n\n");
    return 0;
}
