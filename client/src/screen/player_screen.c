#define NOGDI
#define NOUSER

#ifndef WIN32_LEAN_AND_MEAN
#define WIN32_LEAN_AND_MEAN
#endif

#include "raylib.h"
#include <winsock2.h>
#include <stdlib.h>
#include <stdio.h>
#include <string.h>
#include "player_screen.h"

PlayerScreen* PlayerScreen_Create(void) {
    PlayerScreen* screen = (PlayerScreen*)malloc(sizeof(PlayerScreen));
    if (screen == NULL) return NULL;
    
    screen->player.width = 32;
    screen->player.height = 48;
    screen->player.x = 100;
    screen->player.y = 400;
    screen->player.id = -1;
    screen->initialized = 1;
    
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
        
        if (mouse_x >= 20 && mouse_x <= 120 && mouse_y >= 20 && mouse_y <= 60) {
            printf("[PLAYER_SCREEN]: Se presionó VOLVER\n");
            StateManager_SetNextState(state_manager, STATE_MAIN_MENU);
        }
    }
}

void PlayerScreen_Update(PlayerScreen* screen) {
    if (screen == NULL) return;
}

void PlayerScreen_UpdatePlayerPos(PlayerScreen* screen, int id, int x, int y) {
    if (screen == NULL) return;
    screen->player.id = id;
    screen->player.x = x;
    screen->player.y = y;
}

void PlayerScreen_Render(PlayerScreen* screen) {
    if (screen == NULL) return;
    
    ClearBackground(RAYWHITE);
    
    GamePlayer* p = &screen->player;
    
    DrawRectangle(20, 20, 100, 40, DARKGRAY);
    DrawText("VOLVER", 35, 27, 16, WHITE);
    
    DrawRectangle(p->x, p->y, p->width, p->height, RED);
    DrawRectangleLines(p->x, p->y, p->width, p->height, (Color){139, 0, 0, 255});
    
    char player_id_text[32];
    snprintf(player_id_text, 32, "Jr #%d", p->id);
    DrawText(player_id_text, p->x - 10, p->y - 30, 12, WHITE);
    
    DrawText("DonCEy Kong Jr - MOVIMIENTO", 400, 50, 20, BLACK);
    
    char pos_text[64];
    snprintf(pos_text, 64, "Position: (%d, %d)", p->x, p->y);
    DrawText(pos_text, 400, 80, 12, GRAY);
    
    snprintf(player_id_text, 32, "Player ID: %d", p->id);
    DrawText(player_id_text, 400, 100, 12, GRAY);
    
    DrawText("Controls: LEFT/RIGHT (flechas o A/D)", 400, 750, 12, GRAY);
}