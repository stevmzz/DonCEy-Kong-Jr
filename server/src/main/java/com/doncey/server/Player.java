package com.doncey.server;

import java.util.List;

public class Player {

    private final int id;
    private int x;
    private int y;
    private int prevY;   

    private boolean movingLeft = false;
    private boolean movingRight = false;

    private float velocityY = 0;
    private boolean onGround = false;

    private static final float GRAVITY = 1.2f;
    private static final float JUMP_FORCE = -20.0f;
    private static final int WALK_SPEED = 5;

    private static final int WIDTH = 32;
    private static final int HEIGHT = 48;

    private static final int MIN_X = 0;
    private static final int MAX_X = 1024 - WIDTH;
    private static final int FLOOR_Y = 768 - HEIGHT;

    public Player(int id, int startX, int startY) {
        this.id = id;
        this.x = startX;
        this.y = startY;
        this.prevY = startY;
        this.velocityY = 0;
        this.onGround = false;
    }

    // ==========================
    // CONTROLES
    // ==========================

    public void moveLeft() { movingLeft = true; }
    public void moveRight() { movingRight = true; }

    public void stopMoving() {
        movingLeft = false;
        movingRight = false;
    }

    public void jump() {
        if (onGround) {
            velocityY = JUMP_FORCE;
            onGround = false;
            System.out.println("[PLAYER] SALTO");
        }
    }

    // ==========================
    // UPDATE (FÍSICAS)
    // ==========================

    public void update(List<Platform> platforms) {

        // Guardar la posición previa ANTES de mover
        prevY = y;

        // Movimiento horizontal
        if (movingLeft) x -= WALK_SPEED;
        if (movingRight) x += WALK_SPEED;

        // Gravedad
        velocityY += GRAVITY;
        y += velocityY;

        // Aún no está en suelo
        onGround = false;

        // ----------- COLISIÓN CON PLATAFORMAS -----------
        for (Platform p : platforms) {

            if (velocityY >= 0 && p.collides(this)) {

                // Aterriza encima de la plataforma
                y = p.getY() - HEIGHT;

                velocityY = 0;
                onGround = true;
                break;
            }
        }

        // ----------- COLISIÓN CON EL SUELO -----------
        if (y >= FLOOR_Y) {
            y = FLOOR_Y;
            velocityY = 0;
            onGround = true;
        }

        // ----------- LÍMITES DE PANTALLA -----------
        if (x < MIN_X) x = MIN_X;
        if (x > MAX_X) x = MAX_X;
    }

    // ==========================
    // GETTERS
    // ==========================

    public int getId() { return id; }
    public int getX() { return x; }
    public int getY() { return y; }
    public int getPrevY() { return prevY; } // ← GETTER NUEVO
    public int getWidth() { return WIDTH; }
    public int getHeight() { return HEIGHT; }

    public String getPositionMessage() {
        return String.format("PLAYER_POS %d %d %d", id, x, y);
    }
}
