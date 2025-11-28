#include "platform.h"

PlatformData platforms[] = {

    // ============================
    //  PLATAFORMAS VERDES (NO TOCADAS)
    // ============================

    { .x = 0,   .y = 720, .width = 350, .height = 25, .type = 1 },
    { .x = 410, .y = 680, .width = 110, .height = 25, .type = 1 },
    { .x = 585, .y = 720, .width = 100, .height = 25, .type = 1 },
    { .x = 727, .y = 640, .width = 110, .height = 25, .type = 1 },
    { .x = 900, .y = 600, .width = 110, .height = 25, .type = 1 },

    // ============================
    //  PLATAFORMAS CAFÉ (NUEVAS)
    // ============================

    { .x = 0,   .y = 210, .width = 680, .height = 25, .type = 2 },
    { .x = 210, .y = 350, .width = 200, .height = 25, .type = 2 },
    { .x = 640, .y = 240, .width = 200, .height = 25, .type = 2 },
    { .x = 160, .y = 520, .width = 250, .height = 25, .type = 2 },
    { .x = 800, .y = 420, .width = 220, .height = 25, .type = 2 }
};

int platformCount = sizeof(platforms) / sizeof(platforms[0]);
