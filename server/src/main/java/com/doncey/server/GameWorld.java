package com.doncey.server;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import com.doncey.patterns.observer.GameEventPublisher;
import com.doncey.patterns.factory.FruitFactory;
import com.doncey.patterns.factory.GameEntityFactory;

/**
 * GameWorld mantiene el estado de frutas (spawn/remove), plataformas y jugadores.
 * Permite notificar a los clientes conectados sobre estas acciones.
 * 
 * Implementa el patrón Observer para notificar eventos importantes.
 * Implementa el patrón Factory para crear frutas de forma desacoplada.
 */
public class GameWorld {
    private static GameWorld instance = null;

    private final AtomicInteger fruitIdCounter = new AtomicInteger(0);
    private final Map<Integer, Fruit> fruits = new ConcurrentHashMap<>();
    private final Map<Integer, Player> players = new ConcurrentHashMap<>();
    private final Set<ClientHandler> clients = Collections.synchronizedSet(new HashSet<>());
    private final List<Platform> platforms = new ArrayList<>();
    
    // Publisher del patrón Observer
    private final GameEventPublisher eventPublisher = GameEventPublisher.getInstance();
    
    // Factory del patrón Factory para crear frutas
    private final GameEntityFactory fruitFactory = new FruitFactory();

    // ======== CONSTRUCTOR ========

    private GameWorld() {
        // =======================
        //  PLATAFORMAS DEL NIVEL
        // =======================

        // === CAFÉS (las largas marrón oscuro) ===
        platforms.add(new Platform(0, 210, 680, 25));
        platforms.add(new Platform(210, 350, 200, 25));
        platforms.add(new Platform(640, 240, 200, 25));
        platforms.add(new Platform(160, 520, 250, 25));
        platforms.add(new Platform(800, 420, 220, 25));

        // === VERDES (las que tienen césped) ===
        platforms.add(new Platform(0, 720, 350, 25));
        platforms.add(new Platform(410, 680, 110, 25));
        platforms.add(new Platform(727, 640, 110, 25));
        platforms.add(new Platform(900, 600, 110, 25));
        platforms.add(new Platform(585, 720, 100, 25));
        
        System.out.println("[GAMEWORLD] Plataformas cargadas: " + platforms.size());
    }

    public static synchronized GameWorld getInstance() {
        if (instance == null) instance = new GameWorld();
        return instance;
    }

    // ======== FRUTAS ========

    /**
     * Genera una fruta en una posición especificada
     * 
     * Utiliza el patrón Factory (FruitFactory) para crear la fruta
     * de forma desacoplada, permitiendo validación y extensión futura.
     * 
     * @param type Tipo de fruta (MANZANA, BANANO, MANGO)
     * @param x Posición X
     * @param y Posición Y
     * @param points Puntos que vale la fruta
     * @return Objeto Fruit creado
     * @throws IllegalArgumentException si el tipo de fruta es inválido
     */
    public Fruit spawnFruit(String type, int x, int y, int points) {
        int id = fruitIdCounter.incrementAndGet();
        
        // Usar la factory para crear la fruta
        Fruit f = fruitFactory.createFruit(id, x, y, type, points);
        
        fruits.put(id, f);
        broadcast(String.format("SPAWN_FRUIT %d %d %d %s %d", id, x, y, type, points));
        
        // Notificar observadores del patrón Observer
        eventPublisher.notifyFruitSpawned(id, type, x, y, points);
        
        System.out.println("[GAMEWORLD] Fruta creada: " + f);
        return f;
    }

    /**
     * Elimina una fruta del mundo
     * 
     * @param id ID de la fruta
     * @return true si fue eliminada, false si no existía
     */
    public boolean removeFruit(int id) {
        Fruit removed = fruits.remove(id);
        if (removed != null) {
            broadcast(String.format("REMOVE_FRUIT %d", id));
            
            // Notificar observadores del patrón Observer
            eventPublisher.notifyFruitRemoved(id);
            
            System.out.println("[GAMEWORLD] Fruta removida: " + removed);
            return true;
        }
        return false;
    }

    /**
     * Obtiene una fruta por ID
     * 
     * @param id ID de la fruta
     * @return Objeto Fruit o null si no existe
     */
    public Fruit getFruit(int id) {
        return fruits.get(id);
    }

    /**
     * Lista todas las frutas activas
     * 
     * @return Colección de frutas
     */
    public Collection<Fruit> listFruits() {
        return fruits.values();
    }

    // ======== JUGADORES ========

    /**
     * Registra un nuevo jugador en el mundo
     * 
     * @param playerId ID del jugador
     * @param handler ClientHandler asociado
     */
    public void registerPlayer(int playerId, ClientHandler handler) {
        int startX = 50;
        int startY = 400;   // justo arriba de la plataforma

        Player player = new Player(playerId, startX, startY);
        players.put(playerId, player);
        
        // Notificar observadores del patrón Observer
        eventPublisher.notifyPlayerConnected(playerId);
        
        System.out.println("[GAMEWORLD] Jugador registrado: " + player);
    }

    /**
     * Desregistra un jugador del mundo
     * 
     * @param playerId ID del jugador
     */
    public void unregisterPlayer(int playerId) {
        players.remove(playerId);
        
        // Notificar observadores del patrón Observer
        eventPublisher.notifyPlayerDisconnected(playerId);
        
        System.out.println("[GAMEWORLD] Jugador removido: " + playerId);
    }

    /**
     * Obtiene un jugador por ID
     * 
     * @param playerId ID del jugador
     * @return Objeto Player o null si no existe
     */
    public Player getPlayer(int playerId) {
        return players.get(playerId);
    }

    /**
     * Lista todos los jugadores activos
     * 
     * @return Colección de jugadores
     */
    public Collection<Player> listPlayers() {
        return players.values();
    }

    // ======== COMANDOS ========

    /**
     * Procesa comandos del cliente
     * 
     * @param playerId ID del jugador
     * @param command Comando a procesar (MOVE_LEFT, MOVE_RIGHT, STOP_MOVING, JUMP)
     */
    public void processPlayerCommand(int playerId, String command) {
        Player player = getPlayer(playerId);
        if (player == null) return;

        String[] parts = command.split("\\s+");
        if (parts.length == 0) return;

        String action = parts[0].toUpperCase();

        switch (action) {
            case "MOVE_LEFT":
                player.moveLeft();
                break;
            case "MOVE_RIGHT":
                player.moveRight();
                break;
            case "STOP_MOVING":
                player.stopMoving();
                break;
            case "JUMP":
                player.jump();
                break;
            default:
                System.out.println("[GAMEWORLD] Comando desconocido: " + action);
        }
    }

    // ======== GAME LOOP ========

    /**
     * Actualiza la lógica del juego cada frame
     * 
     * Procesa:
     * - Actualización de posición de jugadores (con colisiones)
     * - Envío de posiciones a todos los clientes
     */
    public void updateGameLogic() {
        for (Player player : players.values()) {
            // Actualizar posición del jugador pasando las plataformas
            player.update(platforms);
            
            // Si está muerto, no enviar más posiciones
            if (!player.isAlive()) continue;
            
            // Enviar posición actualizada a todos los clientes
            broadcast(player.getPositionMessage());
        }
    }

    /**
     * Notifica que un jugador murió
     * Envía mensaje GAME_OVER a todos los clientes
     * 
     * @param playerId ID del jugador que murió
     */
    public void playerDied(int playerId) {
        Player player = getPlayer(playerId);
        if (player != null) {
            System.out.println("[GAMEWORLD] Jugador #" + playerId + " murió");
            broadcast("GAME_OVER " + playerId);
            
            // Notificar observadores del patrón Observer
            eventPublisher.notifyPlayerDisconnected(playerId);
        }
    }

    // ======== CLIENTES ========

    /**
     * Registra un cliente para recibir broadcasts
     * 
     * @param ch ClientHandler del cliente
     */
    public void registerClient(ClientHandler ch) {
        clients.add(ch);

        // Enviar todas las frutas actuales al conectarse
        for (Fruit f : listFruits()) {
            ch.sendMessage(String.format(
                "SPAWN_FRUIT %d %d %d %s %d",
                f.getId(),
                f.getX(),
                f.getY(),
                f.getType(),
                f.getPoints()
            ));
        }
    }

    /**
     * Desregistra un cliente
     * 
     * @param ch ClientHandler del cliente
     */
    public void unregisterClient(ClientHandler ch) {
        clients.remove(ch);
    }

    /**
     * Envía un mensaje a todos los clientes (broadcast)
     * 
     * @param msg Mensaje a enviar
     */
    public void broadcast(String msg) {
        synchronized (clients) {
            for (ClientHandler ch : clients) {
                ch.sendMessage(msg);
            }
        }
    }

    // ======== PLATAFORMAS ========

    /**
     * Obtiene la lista de plataformas del nivel
     * 
     * @return Lista de plataformas
     */
    public List<Platform> getPlatforms() {
        return platforms;
    }

    /**
     * Obtiene una plataforma por índice
     * 
     * @param index Índice de la plataforma
     * @return Objeto Platform
     */
    public Platform getPlatform(int index) {
        if (index >= 0 && index < platforms.size()) {
            return platforms.get(index);
        }
        return null;
    }

    /**
     * Obtiene el total de plataformas
     * 
     * @return Cantidad de plataformas
     */
    public int getPlatformCount() {
        return platforms.size();
    }
}