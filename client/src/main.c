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
#include "raylib.h"
#include "constants.h"
#include "network.h"
#include "state_manager.h"
#include "main_screen.h"
#include "player_screen.h"
#include "spectator_screen.h"
#include "client_protocol.h"

#define MAX_MSG_LEN 1024

// storage de frutas recibidas
static ServerFruit fruits[MAX_FRUITS];
static CRITICAL_SECTION fruits_lock;
static int player_id = -1;
static SOCKET global_sock = -1;
static volatile int receiver_running = 0;

// helpers
void init_fruits() {
    int i;
    for (i = 0; i < MAX_FRUITS; ++i) fruits[i].active = 0;
}

void add_or_replace_fruit_from_server(int id, int x, int y, const char* type, int points) {
    EnterCriticalSection(&fruits_lock);
    int i;
    for (i = 0; i < MAX_FRUITS; ++i) {
        if (fruits[i].active && fruits[i].id == id) {
            // update
            fruits[i].x = x; fruits[i].y = y; fruits[i].points = points;
            strncpy(fruits[i].type, type, sizeof(fruits[i].type)-1);
            LeaveCriticalSection(&fruits_lock);
            return;
        }
    }
    // insert
    for (i = 0; i < MAX_FRUITS; ++i) {
        if (!fruits[i].active) {
            fruits[i].active = 1;
            fruits[i].id = id;
            fruits[i].x = x;
            fruits[i].y = y;
            fruits[i].points = points;
            strncpy(fruits[i].type, type, sizeof(fruits[i].type)-1);
            LeaveCriticalSection(&fruits_lock);
            return;
        }
    }
    LeaveCriticalSection(&fruits_lock);
}

void remove_fruit_from_server(int id) {
    EnterCriticalSection(&fruits_lock);
    int i;
    for (i = 0; i < MAX_FRUITS; ++i) {
        if (fruits[i].active && fruits[i].id == id) {
            fruits[i].active = 0;
            break;
        }
    }
    LeaveCriticalSection(&fruits_lock);
}

// parsea mensajes como "SPAWN_FRUIT 12 100 200 MANGO 50"
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
            printf("[CLIENT] SPAWN_FRUIT %d (%s) at %d,%d pts=%d\n", fid, type, x, y, pts);
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
    // otros mensajes...
}

// hilo receptor
DWORD WINAPI receiver_thread_func(LPVOID param) {
    SOCKET sock = (SOCKET)param;
    char *msg;
    receiver_running = 1;
    while (receiver_running) {
        msg = recv_message(sock);
        if (msg == NULL) {
            // desconexión o error
            printf("[CLIENT] recv returned NULL, stopping receiver\n");
            break;
        }
        // msg es buffer estático devuelto por recv_message
        handle_server_message(msg);
    }
    receiver_running = 0;
    return 0;
}

// enviar EAT_FRUIT <clientId> <fruitId>
void send_eat_fruit(SOCKET sock, int fid) {
    if (sock == -1 || player_id == -1) return;
    char buf[MAX_MSG_LEN];
    snprintf(buf, MAX_MSG_LEN, "EAT_FRUIT %d %d\n", player_id, fid);
    send_message(sock, buf); // ya adjunta newline en send_message
}

// dibuja las frutas actuales
void draw_fruits() {
    EnterCriticalSection(&fruits_lock);
    int i;
    for (i = 0; i < MAX_FRUITS; ++i) {
        if (!fruits[i].active) continue;
        int x = fruits[i].x;
        int y = fruits[i].y;
        DrawCircle(x, y, 12, MAROON);
        DrawText(fruits[i].type, x - 20, y - 28, 10, BLACK);
        char pts[32];
        snprintf(pts, 32, "%d", fruits[i].points);
        DrawText(pts, x - 8, y + 16, 10, DARKGRAY);
    }
    LeaveCriticalSection(&fruits_lock);
}

int main(void) {
    SOCKET sock;
    
    // init crit section
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
        // lanzar hilo receptor
        CreateThread(NULL, 0, receiver_thread_func, (LPVOID)sock, 0, NULL);
    }
    
    InitWindow(1024, 768, "DonCEy Kong Jr");
    SetTargetFPS(60);
    printf("[RAYLIB]: Ventana creada (1024x768)\n");
    
    StateManager* state_manager = StateManager_Create();
    MainScreen* main_screen = MainScreen_Create();
    PlayerScreen* player_screen = PlayerScreen_Create();
    SpectatorScreen* spectator_screen = SpectatorScreen_Create();
    
    printf("[INICIALIZACIÓN]: Todos los recursos gráficos creados\n\n");
    
    while (!WindowShouldClose()) {
        GameState current_state = StateManager_GetCurrentState(state_manager);
        
        // entrada del mouse para comer frutas (solo en PLAYER)
        if (IsMouseButtonPressed(MOUSE_BUTTON_LEFT) && current_state == STATE_PLAYER) {
            int mx = GetMouseX(), my = GetMouseY();
            // buscar fruta cercana
            EnterCriticalSection(&fruits_lock);
            int i;
            for (i = 0; i < MAX_FRUITS; ++i) {
                if (!fruits[i].active) continue;
                int dx = mx - fruits[i].x;
                int dy = my - fruits[i].y;
                int dist2 = dx*dx + dy*dy;
                if (dist2 <= (20*20)) {
                    // comer fruta localmente (optimista)
                    int fid = fruits[i].id;
                    fruits[i].active = 0;
                    LeaveCriticalSection(&fruits_lock);
                    if (global_sock != -1) send_eat_fruit(global_sock, fid);
                    break;
                }
            }
            LeaveCriticalSection(&fruits_lock);
        }

        switch (current_state) {
            case STATE_MAIN_MENU:
                MainScreen_HandleInput(main_screen, state_manager);
                break;
            case STATE_PLAYER:
                PlayerScreen_HandleInput(player_screen, state_manager);
                break;
            case STATE_SPECTATOR:
                SpectatorScreen_HandleInput(spectator_screen, state_manager);
                break;
            case STATE_EXIT:
                break;
        }
        
        StateManager_Update(state_manager);
        current_state = StateManager_GetCurrentState(state_manager);
        
        if (StateManager_HasStateChanged(state_manager)) {
            switch (current_state) {
                case STATE_MAIN_MENU:
                    printf("[ESTADO]: Cambió a MAIN_MENU\n");
                    break;
                case STATE_PLAYER:
                    printf("[ESTADO]: Cambió a PLAYER\n");
                    break;
                case STATE_SPECTATOR:
                    printf("[ESTADO]: Cambió a SPECTATOR\n");
                    break;
                case STATE_EXIT:
                    break;
            }
        }
        
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
            case STATE_EXIT:
                break;
        }
        
        BeginDrawing();

        switch (current_state) {
            case STATE_MAIN_MENU:
                MainScreen_Render(main_screen);
                break;

            case STATE_PLAYER:
                // primero deja que el player render haga su ClearBackground() y UI
                PlayerScreen_Render(player_screen);
                // luego dibujamos las frutas recibidas del servidor (por encima de la UI)
                draw_fruits();
                break;

            case STATE_SPECTATOR:
                SpectatorScreen_Render(spectator_screen);
                // si quieres que el espectador vea las frutas:
                draw_fruits();
                break;

            case STATE_EXIT:
                break;
        }

        EndDrawing();
        
        if (current_state == STATE_EXIT) {
            break;
        }
    }

    // cleanup
    receiver_running = 0;
    Sleep(100);
    if (global_sock != -1) close_connection(global_sock);

    SpectatorScreen_Destroy(spectator_screen);
    PlayerScreen_Destroy(player_screen);
    MainScreen_Destroy(main_screen);
    StateManager_Destroy(state_manager);
    CloseWindow();
    DeleteCriticalSection(&fruits_lock);
    
    if (sock != -1) {
        close_connection(sock);
        printf("[CLIENTE]: Desconectado del servidor\n");
    }
    
    printf("\n[SALIDA]: Programa finalizado\n\n");
    
    return 0;
}