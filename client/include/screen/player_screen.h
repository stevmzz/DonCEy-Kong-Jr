#ifndef PLAYER_SCREEN_H
#define PLAYER_SCREEN_H

#include "state_manager.h"

typedef struct {
    int dummy;
} PlayerScreen;

PlayerScreen* PlayerScreen_Create(void);
void PlayerScreen_Destroy(PlayerScreen* screen);
void PlayerScreen_HandleInput(PlayerScreen* screen, StateManager* state_manager);
void PlayerScreen_Update(PlayerScreen* screen);
void PlayerScreen_Render(PlayerScreen* screen);

#endif