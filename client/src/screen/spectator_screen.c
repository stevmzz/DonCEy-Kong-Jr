#include <stdlib.h>
#include <stdio.h>
#include "raylib.h"
#include "spectator_screen.h"

SpectatorScreen* SpectatorScreen_Create(void) {
    SpectatorScreen* screen = (SpectatorScreen*)malloc(sizeof(SpectatorScreen));
    if (screen == NULL) return NULL;
    printf("[SPECTATOR_SCREEN]: Creada correctamente\n");
    return screen;
}

void SpectatorScreen_Destroy(SpectatorScreen* screen) {
    if (screen != NULL) {
        printf("[SPECTATOR_SCREEN]: Destruida\n");
        free(screen);
    }
}

void SpectatorScreen_HandleInput(SpectatorScreen* screen, StateManager* state_manager) {
    if (screen == NULL || state_manager == NULL) return;
    
    if (IsMouseButtonPressed(MOUSE_BUTTON_LEFT)) {
        int mouse_x = GetMouseX();
        int mouse_y = GetMouseY();
        
        // Botón VOLVER (20, 20, 100, 40)
        if (mouse_x >= 20 && mouse_x <= 120 && mouse_y >= 20 && mouse_y <= 60) {
            printf("[SPECTATOR_SCREEN]: Se presionó VOLVER\n");
            StateManager_SetNextState(state_manager, STATE_MAIN_MENU);
        }
    }
}

void SpectatorScreen_Update(SpectatorScreen* screen) {
    if (screen == NULL) return;
}

void SpectatorScreen_Render(SpectatorScreen* screen) {
    if (screen == NULL) return;
    
    ClearBackground(RAYWHITE);
    
    DrawRectangle(20, 20, 100, 40, DARKGRAY);
    DrawText("VOLVER", 35, 27, 16, WHITE);
    
    DrawText("PANTALLA DE ESPECTADOR", 350, 350, 30, BLACK);
}