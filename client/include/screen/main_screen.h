#ifndef MAIN_SCREEN_H
#define MAIN_SCREEN_H

#include "state_manager.h"

typedef struct {
    void* data;
} MainScreen;

MainScreen* MainScreen_Create(void);
void MainScreen_Destroy(MainScreen* screen);
void MainScreen_HandleInput(MainScreen* screen, StateManager* state_manager);
void MainScreen_Update(MainScreen* screen);
void MainScreen_Render(MainScreen* screen);

#endif