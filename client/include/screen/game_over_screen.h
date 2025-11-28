#ifndef GAME_OVER_SCREEN_H
#define GAME_OVER_SCREEN_H

#include "state_manager.h"

typedef struct {
    int dummy;
} GameOverScreen;

GameOverScreen* GameOverScreen_Create(void);
void GameOverScreen_Destroy(GameOverScreen* screen);
void GameOverScreen_HandleInput(GameOverScreen* screen, StateManager* state_manager);
void GameOverScreen_Update(GameOverScreen* screen);
void GameOverScreen_Render(GameOverScreen* screen);

#endif