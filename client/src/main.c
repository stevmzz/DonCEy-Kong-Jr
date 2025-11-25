#define NOGDI
#define NOUSER

#include <stdio.h>
#include <stdlib.h>
#include "raylib.h"
#include "constants.h"
#include "network.h"
#include "state_manager.h"
#include "main_screen.h"
#include "player_screen.h"
#include "spectator_screen.h"

int main(void) {
    SOCKET sock;
    
    printf("\n========== DonCEy Kong Jr - Cliente C ==========\n\n");
    
    printf("[CLIENTE]: Intentando conectar a %s:%d...\n", SERVER_IP, SERVER_PORT);
    sock = connect_to_server(SERVER_IP, SERVER_PORT);
    
    if (sock == -1) {
        printf("[ERROR]: No se pudo conectar al servidor\n");
        printf("[CLIENTE]: Continuando sin servidor...\n\n");
    } else {
        printf("[CLIENTE]: Conectado al servidor\n\n");
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
                PlayerScreen_Render(player_screen);
                break;
            case STATE_SPECTATOR:
                SpectatorScreen_Render(spectator_screen);
                break;
            case STATE_EXIT:
                break;
        }
        
        EndDrawing();
        
        if (current_state == STATE_EXIT) {
            break;
        }
    }
    
    SpectatorScreen_Destroy(spectator_screen);
    PlayerScreen_Destroy(player_screen);
    MainScreen_Destroy(main_screen);
    StateManager_Destroy(state_manager);
    CloseWindow();
    
    if (sock != -1) {
        close_connection(sock);
        printf("[CLIENTE]: Desconectado del servidor\n");
    }
    
    printf("\n[SALIDA]: Programa finalizado\n\n");
    
    return 0;
}