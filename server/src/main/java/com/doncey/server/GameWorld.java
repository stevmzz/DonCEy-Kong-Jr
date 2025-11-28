package com.doncey.server;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * GameWorld mantiene el estado de frutas (spawn/remove) y permite
 * notificar a los clientes conectados sobre estas acciones.
 * 
 * También mantiene el estado de los jugadores conectados
 */
public class GameWorld {
    private static GameWorld instance = null;

    private final AtomicInteger fruitIdCounter = new AtomicInteger(0);
    private final Map<Integer, Fruit> fruits = new ConcurrentHashMap<>();
    private final Map<Integer, Player> players = new ConcurrentHashMap<>();
    // Lista de client handlers registrados para broadcast
    private final Set<ClientHandler> clients = Collections.synchronizedSet(new HashSet<>());

    private GameWorld() { }

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
        System.out.println("[GAMEWORLD] Fruta creada: " + f);
        return f;
    }

    public boolean removeFruit(int id) {
        Fruit removed = fruits.remove(id);
        if (removed != null) {
            broadcast(String.format("REMOVE_FRUIT %d", id));
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
        Player player = new Player(playerId, 100, 400);
        players.put(playerId, player);
        System.out.println("[GAMEWORLD] Jugador registrado: " + player);
    }
    
    public void unregisterPlayer(int playerId) {
        players.remove(playerId);
        System.out.println("[GAMEWORLD] Jugador removido: " + playerId);
    }
    
    public Player getPlayer(int playerId) {
        return players.get(playerId);
    }
    
    // Procesar comandos de movimiento del cliente
    public void processPlayerCommand(int playerId, String command) {
        Player player = getPlayer(playerId);
        if (player == null) return;
        
        String[] parts = command.split("\\s+");
        if (parts.length == 0) return;
        
        String action = parts[0].toUpperCase();
        
        if (action.equals("MOVE_LEFT")) {
            player.moveLeft();
        } else if (action.equals("MOVE_RIGHT")) {
            player.moveRight();
        } else if (action.equals("STOP_MOVING")) {
            player.stopMoving();
        }
    }
    
    // Actualizar lógica del juego (cada frame)
    public void updateGameLogic() {
        for (Player player : players.values()) {
            player.update();
            broadcast(player.getPositionMessage());
        }
    }

    // ======== CLIENTES ========
    public void registerClient(ClientHandler ch) {
        clients.add(ch);
        // al registrarse, enviar estado actual (spawn de todas las frutas)
        for (Fruit f : listFruits()) {
            ch.sendMessage(String.format("SPAWN_FRUIT %d %d %d %s %d", f.getId(), f.getX(), f.getY(), f.getType(), f.getPoints()));
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
}