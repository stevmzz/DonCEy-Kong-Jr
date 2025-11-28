#include <stdlib.h>
#include <stdio.h>
#include "raylib.h"
#include "game_over_screen.h"

GameOverScreen* GameOverScreen_Create(void) {
    GameOverScreen* screen = (GameOverScreen*)malloc(sizeof(GameOverScreen));
    if (screen == NULL) return NULL;
    
    screen->dummy = 0;
    printf("[GAME_OVER_SCREEN]: Creada correctamente\n");
    return screen;
}

void GameOverScreen_Destroy(GameOverScreen* screen) {
    if (screen != NULL) {
        printf("[GAME_OVER_SCREEN]: Destruida\n");
        free(screen);
    }
}

void GameOverScreen_HandleInput(GameOverScreen* screen, StateManager* state_manager) {
    if (screen == NULL || state_manager == NULL) return;
    
    if (IsMouseButtonPressed(MOUSE_BUTTON_LEFT)) {
        int mouse_x = GetMouseX();
        int mouse_y = GetMouseY();
        
        // Botón VOLVER AL MENU (centro de pantalla, abajo)
        // Coordenadas: x=362-662, y=550-610
        if (mouse_x >= 362 && mouse_x <= 662 && mouse_y >= 550 && mouse_y <= 610) {
            printf("[GAME_OVER_SCREEN]: Se presionó VOLVER AL MENU\n");
            StateManager_SetNextState(state_manager, STATE_MAIN_MENU);
        }
    }
}

void GameOverScreen_Update(GameOverScreen* screen) {
    if (screen == NULL) return;
}

void GameOverScreen_Render(GameOverScreen* screen) {
    if (screen == NULL) return;
    
    ClearBackground(RAYWHITE);
    
    // Fondo oscuro semi-transparente
    DrawRectangle(0, 0, 1024, 768, (Color){0, 0, 0, 150});
    
    // Título "GAME OVER"
    DrawText("GAME OVER", 300, 150, 80, RED);
    
    // Subtítulo (COLOR ROJO OSCURO)
    DrawText("¡Donkey Kong Jr ha caído!", 250, 280, 40, (Color){139, 0, 0, 255});
    
    // Explicación
    DrawText("Inténtalo de nuevo", 300, 380, 32, GRAY);
    
    // Botón VOLVER AL MENU
    int button_x = 362;
    int button_y = 550;
    int button_width = 300;
    int button_height = 60;
    
    // Detectar hover
    int mouse_x = GetMouseX();
    int mouse_y = GetMouseY();
    int hovered = (mouse_x >= button_x && mouse_x <= button_x + button_width &&
                   mouse_y >= button_y && mouse_y <= button_y + button_height);
    
    // Dibujar botón
    Color button_color = hovered ? (Color){200, 50, 50, 255} : (Color){150, 30, 30, 255};
    DrawRectangle(button_x, button_y, button_width, button_height, button_color);
    DrawRectangleLines(button_x, button_y, button_width, button_height, WHITE);
    
    // Texto del botón
    int text_width = MeasureText("VOLVER AL MENU", 28);
    DrawText("VOLVER AL MENU", 
            button_x + (button_width - text_width) / 2, 
            button_y + 16, 
            28, WHITE);
}