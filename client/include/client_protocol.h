// client_protocol.h
#ifndef CLIENT_PROTOCOL_H
#define CLIENT_PROTOCOL_H

#define MAX_FRUITS 1024

typedef struct {
    int id;
    int x;
    int y;
    char type[32];
    int points;
    int active;
} ServerFruit;

#endif
