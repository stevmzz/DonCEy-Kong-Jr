#ifndef STATE_MANAGER_H
#define STATE_MANAGER_H

typedef enum {
    STATE_MAIN_MENU,
    STATE_PLAYER,
    STATE_SPECTATOR,
    STATE_GAME_OVER,
    STATE_EXIT
} GameState;

typedef struct {
    GameState current_state;
    GameState next_state;
    int state_changed;
} StateManager;

StateManager* StateManager_Create(void);
void StateManager_Destroy(StateManager* manager);
void StateManager_SetNextState(StateManager* manager, GameState next_state);
void StateManager_Update(StateManager* manager);
GameState StateManager_GetCurrentState(StateManager* manager);
int StateManager_HasStateChanged(StateManager* manager);

#endif