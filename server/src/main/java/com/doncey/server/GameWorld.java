package com.doncey.server;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * GameWorld mantiene el estado de frutas (spawn/remove) y permite
 * notificar a los clientes conectados sobre estas acciones.
 */
public class GameWorld {
    private static GameWorld instance = null;

    private final AtomicInteger fruitIdCounter = new AtomicInteger(0);
    private final Map<Integer, Fruit> fruits = new ConcurrentHashMap<>();
    // Lista de client handlers registrados para broadcast
    private final Set<ClientHandler> clients = Collections.synchronizedSet(new HashSet<>());

    private GameWorld() { }

    public static synchronized GameWorld getInstance() {
        if (instance == null) instance = new GameWorld();
        return instance;
    }

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

    // registro de clientes para broadcast
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
