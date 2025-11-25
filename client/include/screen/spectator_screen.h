#ifndef SPECTATOR_SCREEN_H
#define SPECTATOR_SCREEN_H

#include "state_manager.h"

typedef struct {
    int dummy;
} SpectatorScreen;

SpectatorScreen* SpectatorScreen_Create(void);
void SpectatorScreen_Destroy(SpectatorScreen* screen);
void SpectatorScreen_HandleInput(SpectatorScreen* screen, StateManager* state_manager);
void SpectatorScreen_Update(SpectatorScreen* screen);
void SpectatorScreen_Render(SpectatorScreen* screen);

#endif