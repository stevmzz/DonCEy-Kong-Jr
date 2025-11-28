package com.doncey.server;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import com.doncey.patterns.observer.GameEventPublisher;

/**
 * GameWorld mantiene el estado de frutas (spawn/remove) y permite
 * notificar a los clientes conectados sobre estas acciones.
 * 
 * También mantiene el estado de los jugadores conectados.
 * 
 * Implementa el patrón Observer para notificar eventos importantes.
 */
public class GameWorld {
    private static GameWorld instance = null;

    private final AtomicInteger fruitIdCounter = new AtomicInteger(0);
    private final Map<Integer, Fruit> fruits = new ConcurrentHashMap<>();
    private final Map<Integer, Player> players = new ConcurrentHashMap<>();
    private final Set<ClientHandler> clients = Collections.synchronizedSet(new HashSet<>());
    
    // Publisher del patrón Observer
    private final GameEventPublisher eventPublisher = GameEventPublisher.getInstance();

    // ======== PLATAFORMAS ========

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

    public Fruit spawnFruit(String type, int x, int y, int points) {
        int id = fruitIdCounter.incrementAndGet();
        Fruit f = new Fruit(id, x, y, type, points);
        fruits.put(id, f);
        broadcast(String.format("SPAWN_FRUIT %d %d %d %s %d", id, x, y, type, points));
        
        // Notificar observadores del patrón Observer
        eventPublisher.notifyFruitSpawned(id, type, x, y, points);
        
        System.out.println("[GAMEWORLD] Fruta creada: " + f);
        return f;
    }

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

    public Fruit getFruit(int id) {
        return fruits.get(id);
    }

    public Collection<Fruit> listFruits() {
        return fruits.values();
    }

    // ======== JUGADORES ========

    public void registerPlayer(int playerId, ClientHandler handler) {
        int startX = 50;
        int startY = 400;   // justo arriba de la plataforma

        Player player = new Player(playerId, startX, startY);
        players.put(playerId, player);
        
        // Notificar observadores del patrón Observer
        eventPublisher.notifyPlayerConnected(playerId);
        

        System.out.println("[GAMEWORLD] Jugador registrado: " + player);
    }

    public void unregisterPlayer(int playerId) {
        players.remove(playerId);
        
        // Notificar observadores del patrón Observer
        eventPublisher.notifyPlayerDisconnected(playerId);
        
        System.out.println("[GAMEWORLD] Jugador removido: " + playerId);
    }

    public Player getPlayer(int playerId) {
        return players.get(playerId);
    }

    // ======== COMANDOS ========

    public void processPlayerCommand(int playerId, String command) {
        Player player = getPlayer(playerId);
        if (player == null) return;

        String[] parts = command.split("\\s+");
        if (parts.length == 0) return;

        String action = parts[0].toUpperCase();

        if (action.equals("MOVE_LEFT")) {
            player.moveLeft();
        }
        else if (action.equals("MOVE_RIGHT")) {
            player.moveRight();
        }
        else if (action.equals("STOP_MOVING")) {
            player.stopMoving();
        }
        else if (action.equals("JUMP")) {
            player.jump();
        }
    }

    // ======== GAME LOOP ========

    public void updateGameLogic() {

        for (Player player : players.values()) {

            player.update(platforms);

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

    public void unregisterClient(ClientHandler ch) {
        clients.remove(ch);
    }

    public void broadcast(String msg) {
        synchronized (clients) {
            for (ClientHandler ch : clients) {
                ch.sendMessage(msg);
            }
        }
    }

    // ======== PLATAFORMAS  ========

    public List<Platform> getPlatforms() {
        return platforms;
    }
}