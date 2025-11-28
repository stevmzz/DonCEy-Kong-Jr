#define NOGDI
#define NOUSER

#ifndef WIN32_LEAN_AND_MEAN
#define WIN32_LEAN_AND_MEAN
#endif

#include "raylib.h"
#include <winsock2.h>
#include <ws2tcpip.h>
#include <stdio.h>
#include "input_handler.h"
#include "network.h"

static int last_key_state = 0;

void InputHandler_Update(SOCKET sock, int player_id) {
    if (sock == -1 || player_id == -1) return;
    
    int key_state = 0;


    // MOVIMIENTO HORIZONTAL
    if (IsKeyDown(KEY_LEFT) || IsKeyDown(KEY_A)) {
        key_state = 1;
    } 
    else if (IsKeyDown(KEY_RIGHT) || IsKeyDown(KEY_D)) {
        key_state = 2;
    } 
    else {
        key_state = 0;
    }
    
    if (key_state != last_key_state) {
        if (key_state == 1) {
            send_message(sock, "MOVE_LEFT");
        } 
        else if (key_state == 2) {
            send_message(sock, "MOVE_RIGHT");
        } 
        else {
            send_message(sock, "STOP_MOVING");
        }
        last_key_state = key_state;
    }

    // SALTO 
    if (IsKeyPressed(KEY_SPACE)) {
        send_message(sock, "JUMP");
    }
}
