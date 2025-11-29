package com.doncey.server;

import java.util.List;

/**
 * Player - Jugador del juego DonCEy Kong Jr
 * 
 * Maneja el estado, física y controles del jugador.
 * 
 * Características:
 * - Movimiento horizontal (izquierda/derecha)
 * - Salto con gravedad realista
 * - Colisión con plataformas
 * - Sistema de vidas (loseLive, isAlive)
 */
public class Player {
    private final int id;
    private int x;
    private int y;
    private int prevY;   
    private boolean alive = true;
    
    // Control de movimiento
    private boolean movingLeft = false;
    private boolean movingRight = false;
    
    // Física
    private float velocityY = 0;
    private boolean onGround = false;
    
    // Constantes de física
    private static final float GRAVITY = 1.2f;
    private static final float JUMP_FORCE = -20.0f;
    private static final int WALK_SPEED = 5;
    
    // Dimensiones
    private static final int WIDTH = 32;
    private static final int HEIGHT = 48;
    
    // Límites de pantalla
    private static final int MIN_X = 0;
    private static final int MAX_X = 1024 - WIDTH;
    private static final int FLOOR_Y = 768 - HEIGHT;
    public static final int SCREEN_HEIGHT = 768;
    
    /**
     * Constructor del Jugador
     * 
     * @param id ID único del jugador
     * @param startX Posición X inicial
     * @param startY Posición Y inicial
     */
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
    
    /**
     * Inicia movimiento hacia la izquierda
     */
    public void moveLeft() { 
        movingLeft = true; 
    }
    
    /**
     * Inicia movimiento hacia la derecha
     */
    public void moveRight() { 
        movingRight = true; 
    }
    
    /**
     * Detiene todo movimiento horizontal
     */
    public void stopMoving() {
        movingLeft = false;
        movingRight = false;
    }
    
    /**
     * Realiza un salto si está en el suelo
     */
    public void jump() {
        if (onGround) {
            velocityY = JUMP_FORCE;
            onGround = false;
            System.out.println("[PLAYER #" + id + "] SALTO");
        }
    }
    
    // ==========================
    // UPDATE (FÍSICA Y COLISIONES)
    // ==========================
    
    /**
     * Actualiza la lógica del jugador cada frame
     * 
     * Procesa:
     * - Movimiento horizontal
     * - Física de gravedad y salto
     * - Colisión con plataformas
     * - Colisión con suelo
     * - Límites de pantalla
     * 
     * @param platforms Lista de plataformas para detectar colisiones
     */
    public void update(List<Platform> platforms) {
        // Guardar la posición previa ANTES de mover
        prevY = y;
        
        // ----------- MOVIMIENTO HORIZONTAL -----------
        if (movingLeft) {
            x -= WALK_SPEED;
        }
        if (movingRight) {
            x += WALK_SPEED;
        }
        
        // ----------- FÍSICA DE GRAVEDAD -----------
        velocityY += GRAVITY;
        y += (int)velocityY;
        
        // Aún no está en suelo (se resetea cada frame)
        onGround = false;
        
        // ----------- COLISIÓN CON PLATAFORMAS -----------
        if (platforms != null) {
            for (Platform p : platforms) {
                if (velocityY >= 0 && p.collides(this)) {
                    // Aterriza encima de la plataforma
                    y = p.getY() - HEIGHT;
                    velocityY = 0;
                    onGround = true;
                    break;
                }
            }
        }
        
        // ----------- COLISIÓN CON EL SUELO -----------
        if (y >= FLOOR_Y) {
            y = FLOOR_Y;
            velocityY = 0;
            onGround = true;
        }
        
        // ----------- LÍMITES DE PANTALLA HORIZONTAL -----------
        if (x < MIN_X) x = MIN_X;
        if (x > MAX_X) x = MAX_X;

        // ============================================================
        //         MUERTE: cuando la parte de abajo toca el borde
        // ============================================================

        if (y + HEIGHT >= SCREEN_HEIGHT) {
            die();
            GameWorld.getInstance().playerDied(id);
        }
    }
    
    // ==========================
    // SISTEMA DE VIDAS
    // ==========================
    
    /**
     * Pierde una vida
     */
    public void loseLive() {
        System.out.println("[PLAYER #" + id + "] Perdió una vida");
    }

    public void die() {
        alive = false;
        // Mostrar mensaje o animación de muerte si querés
        System.out.println("Jugador " + id + " murió");
    }

    /**
     * Verifica si el jugador aún está vivo
     * 
     * @return true si tiene vidas > 0
     */
    public boolean isAlive() {
        return true;  // TODO: Conectar con sistema de vidas real
    }
    
    // ==========================
    // GETTERS
    // ==========================
    
    /**
     * Obtiene el ID del jugador
     */
    public int getId() { 
        return id; 
    }
    
    /**
     * Obtiene posición X actual
     */
    public int getX() { 
        return x; 
    }
    
    /**
     * Obtiene posición Y actual
     */
    public int getY() { 
        return y; 
    }
    
    /**
     * Obtiene la posición Y anterior (antes de este frame)
     */
    public int getPrevY() { 
        return prevY; 
    }
    
    /**
     * Obtiene el ancho del jugador
     */
    public int getWidth() { 
        return WIDTH; 
    }
    
    /**
     * Obtiene el alto del jugador
     */
    public int getHeight() { 
        return HEIGHT; 
    }
    
    /**
     * Verifica si el jugador está en el suelo
     */
    public boolean isOnGround() {
        return onGround;
    }
    
    /**
     * Obtiene la velocidad Y actual
     */
    public float getVelocityY() {
        return velocityY;
    }
    
    // ==========================
    // MENSAJES AL CLIENTE
    // ==========================
    
    /**
     * Genera el mensaje para enviar posición al cliente
     * 
     * Formato: "PLAYER_POS id x y"
     */
    public String getPositionMessage() {
        return String.format("PLAYER_POS %d %d %d", id, x, y);
    }
    
    @Override
    public String toString() {
        return "Player{id=" + id + ", pos=(" + x + "," + y + "), onGround=" + onGround + "}";
    }
}