package com.doncey.server;

/**
 * Donkey Kong Jr - Jugador
 * Maneja el estado y lógica del jugador
 */
public class Player {
    private final int id;
    private int x;
    private int y;
    
    // Control de movimiento
    private boolean movingLeft = false;
    private boolean movingRight = false;
    
    // Velocidad de caminata
    private static final int WALK_SPEED = 5;
    
    // Dimensiones del jugador
    private static final int WIDTH = 32;
    private static final int HEIGHT = 48;
    
    // Límites de la pantalla
    private static final int MIN_X = 0;
    private static final int MAX_X = 1024 - WIDTH;
    
    public Player(int id, int startX, int startY) {
        this.id = id;
        this.x = startX;
        this.y = startY;
    }
    
    // Control del jugador - movimiento
    public void moveLeft() {
        movingLeft = true;
    }
    
    public void moveRight() {
        movingRight = true;
    }
    
    public void stopMoving() {
        movingLeft = false;
        movingRight = false;
    }
    
    /**
    * Pierde una vida
    */
    public void loseLive() {
        System.out.println("[PLAYER] Jugador #" + id + " perdió una vida");
    }

    /**
     * Verifica si el jugador aún está vivo
     * 
     * @return true si tiene vidas > 0
     */
    public boolean isAlive() {
        return true;
    }

    // Actualización del jugador cada frame
    public void update() {
        if (movingLeft) {
            x -= WALK_SPEED;
        }
        if (movingRight) {
            x += WALK_SPEED;
        }
        
        // Limitar posición a pantalla
        if (x < MIN_X) x = MIN_X;
        if (x > MAX_X) x = MAX_X;
    }
    
    // Getters
    public int getId() { return id; }
    public int getX() { return x; }
    public int getY() { return y; }
    public int getWidth() { return WIDTH; }
    public int getHeight() { return HEIGHT; }
    
    // Mensaje para enviar al cliente
    public String getPositionMessage() {
        return String.format("PLAYER_POS %d %d %d", id, x, y);
    }
    
    @Override
    public String toString() {
        return "Player{" + id + ", pos=(" + x + "," + y + ")}";
    }
}