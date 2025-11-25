#include <stdlib.h>
#include <stdio.h>
#include "raylib.h"
#include "player_screen.h"

PlayerScreen* PlayerScreen_Create(void) {
    PlayerScreen* screen = (PlayerScreen*)malloc(sizeof(PlayerScreen));
    if (screen == NULL) return NULL;
    printf("[PLAYER_SCREEN]: Creada correctamente\n");
    return screen;
}

void PlayerScreen_Destroy(PlayerScreen* screen) {
    if (screen != NULL) {
        printf("[PLAYER_SCREEN]: Destruida\n");
        free(screen);
    }
}

void PlayerScreen_HandleInput(PlayerScreen* screen, StateManager* state_manager) {
    if (screen == NULL || state_manager == NULL) return;
    
    if (IsMouseButtonPressed(MOUSE_BUTTON_LEFT)) {
        int mouse_x = GetMouseX();
        int mouse_y = GetMouseY();
        
        // Botón VOLVER (20, 20, 100, 40)
        if (mouse_x >= 20 && mouse_x <= 120 && mouse_y >= 20 && mouse_y <= 60) {
            printf("[PLAYER_SCREEN]: Se presionó VOLVER\n");
            StateManager_SetNextState(state_manager, STATE_MAIN_MENU);
        }
    }
}

void PlayerScreen_Update(PlayerScreen* screen) {
    if (screen == NULL) return;
}

void PlayerScreen_Render(PlayerScreen* screen) {
    if (screen == NULL) return;
    
    ClearBackground(RAYWHITE);
    
    DrawRectangle(20, 20, 100, 40, DARKGRAY);
    DrawText("VOLVER", 35, 27, 16, WHITE);
    
    DrawText("PANTALLA DE JUGAR", 400, 350, 30, BLACK);
}