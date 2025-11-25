#include <stdlib.h>
#include "state_manager.h"

StateManager* StateManager_Create(void) {
    StateManager* manager = (StateManager*)malloc(sizeof(StateManager));
    if (manager == NULL) return NULL;
    
    manager->current_state = STATE_MAIN_MENU;
    manager->next_state = STATE_MAIN_MENU;
    manager->state_changed = 0;
    
    return manager;
}

void StateManager_Destroy(StateManager* manager) {
    if (manager != NULL) {
        free(manager);
    }
}

void StateManager_SetNextState(StateManager* manager, GameState next_state) {
    if (manager != NULL && manager->next_state != next_state) {
        manager->next_state = next_state;
    }
}

void StateManager_Update(StateManager* manager) {
    if (manager != NULL) {
        if (manager->current_state != manager->next_state) {
            manager->current_state = manager->next_state;
            manager->state_changed = 1;
        } else {
            manager->state_changed = 0;
        }
    }
}

GameState StateManager_GetCurrentState(StateManager* manager) {
    if (manager != NULL) {
        return manager->current_state;
    }
    return STATE_EXIT;
}

int StateManager_HasStateChanged(StateManager* manager) {
    if (manager != NULL) {
        return manager->state_changed;
    }
    return 0;
}