#include <stdlib.h>
#include <stdio.h>
#include "raylib.h"
#include "main_screen.h"

typedef struct {
    Texture2D logo;
    int loaded;
} MainScreenData;

MainScreen* MainScreen_Create(void) {
    MainScreen* screen = (MainScreen*)malloc(sizeof(MainScreen));
    if (screen == NULL) return NULL;
    
    MainScreenData* data = (MainScreenData*)malloc(sizeof(MainScreenData));
    if (data == NULL) {
        free(screen);
        return NULL;
    }
    
    // Cargar logo
    data->logo = LoadTexture("../../assets/images/title.png");
    data->loaded = (data->logo.id > 0) ? 1 : 0;
    
    if (data->loaded) {
        printf("[MAIN_SCREEN]: Logo cargado correctamente\n");
    } else {
        printf("[MAIN_SCREEN]: Advertencia - No se pudo cargar el logo\n");
    }
    
    screen->data = (void*)data;
    printf("[MAIN_SCREEN]: Creada correctamente\n");
    return screen;
}

void MainScreen_Destroy(MainScreen* screen) {
    if (screen != NULL) {
        MainScreenData* data = (MainScreenData*)screen->data;
        if (data != NULL) {
            if (data->loaded) {
                UnloadTexture(data->logo);
            }
            free(data);
        }
        free(screen);
    }
}

void MainScreen_HandleInput(MainScreen* screen, StateManager* state_manager) {
    if (screen == NULL || state_manager == NULL) return;
    
    if (IsMouseButtonPressed(MOUSE_BUTTON_LEFT)) {
        int mouse_x = GetMouseX();
        int mouse_y = GetMouseY();
        
        // Botón JUGAR (412, 350, 200, 60)
        if (mouse_x >= 412 && mouse_x <= 612 && mouse_y >= 350 && mouse_y <= 410) {
            printf("[MAIN_SCREEN]: Se presionó JUGAR\n");
            StateManager_SetNextState(state_manager, STATE_PLAYER);
        }
        
        // Botón ESPECTADOR (412, 480, 200, 60)
        if (mouse_x >= 412 && mouse_x <= 612 && mouse_y >= 480 && mouse_y <= 540) {
            printf("[MAIN_SCREEN]: Se presionó ESPECTADOR\n");
            StateManager_SetNextState(state_manager, STATE_SPECTATOR);
        }
    }
}

void MainScreen_Update(MainScreen* screen) {
    if (screen == NULL) return;
}

void MainScreen_Render(MainScreen* screen) {
    if (screen == NULL) return;
    
    ClearBackground((Color){240, 240, 245, 255});
    
    MainScreenData* data = (MainScreenData*)screen->data;
    
    // Renderizar logo si está cargado
    if (data->loaded) {
        int logo_width = 400;
        int logo_height = 150;
        int logo_x = (1024 - logo_width) / 2;
        int logo_y = 80;
        
        DrawTextureEx(data->logo, (Vector2){logo_x, logo_y}, 0.0f, 
                     (float)logo_width / data->logo.width, WHITE);
    } else {
        // Fallback si no carga la imagen
        DrawText("DONKEY KONG JR", 280, 100, 50, (Color){20, 20, 40, 255});
    }
    
    // Botón JUGAR - Más estético
    int play_button_x = 412;
    int play_button_y = 350;
    int button_width = 200;
    int button_height = 60;
    
    // Detectar hover para JUGAR
    int mouse_x = GetMouseX();
    int mouse_y = GetMouseY();
    int play_hovered = (mouse_x >= play_button_x && mouse_x <= play_button_x + button_width &&
                        mouse_y >= play_button_y && mouse_y <= play_button_y + button_height);
    
    // Dibujar botón JUGAR
    Color play_color = play_hovered ? (Color){70, 130, 255, 255} : (Color){50, 100, 255, 255};
    DrawRectangle(play_button_x, play_button_y, button_width, button_height, play_color);
    DrawRectangleLines(play_button_x, play_button_y, button_width, button_height, WHITE);
    
    int play_text_width = MeasureText("JUGAR", 32);
    DrawText("JUGAR", 
            play_button_x + (button_width - play_text_width) / 2, 
            play_button_y + 14, 
            32, WHITE);
    
    // Botón ESPECTADOR
    int spec_button_x = 412;
    int spec_button_y = 480;
    
    int spec_hovered = (mouse_x >= spec_button_x && mouse_x <= spec_button_x + button_width &&
                        mouse_y >= spec_button_y && mouse_y <= spec_button_y + button_height);
    
    // Dibujar botón ESPECTADOR
    Color spec_color = spec_hovered ? (Color){70, 130, 255, 255} : (Color){50, 100, 255, 255};
    DrawRectangle(spec_button_x, spec_button_y, button_width, button_height, spec_color);
    DrawRectangleLines(spec_button_x, spec_button_y, button_width, button_height, WHITE);
    
    int spec_text_width = MeasureText("ESPECTADOR", 28);
    DrawText("ESPECTADOR", 
            spec_button_x + (button_width - spec_text_width) / 2, 
            spec_button_y + 16, 
            28, WHITE);
}