package com.doncey.patterns.observer;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @brief Subject del patrón Observer
 * 
 * Mantiene una lista de observadores y notifica a todos cuando ocurren
 * eventos importantes del juego.
 * 
 * Implementa Singleton para asegurar una única instancia en todo el servidor.
 */
public class GameEventPublisher {
    
    private static GameEventPublisher instance = null;
    
    /**
     * Lista thread-safe de observadores
     * 
     * Usa CopyOnWriteArrayList porque:
     * - Las suscripciones son raras
     * - Las notificaciones son frecuentes
     * - No necesita locks explícitos
     */
    private final List<GameObserver> observers = new CopyOnWriteArrayList<>();
    
    private GameEventPublisher() { }
    
    /**
     * Obtiene la instancia única del publisher (Singleton)
     * 
     * @return La instancia única de GameEventPublisher
     */
    public static synchronized GameEventPublisher getInstance() {
        if (instance == null) {
            instance = new GameEventPublisher();
        }
        return instance;
    }
    
    /**
     * Registra un observador para recibir notificaciones
     * 
     * @param observer El observador a registrar
     */
    public synchronized void subscribe(GameObserver observer) {
        if (observer != null && !observers.contains(observer)) {
            observers.add(observer);
            System.out.println("[OBSERVER] Se registró: " + observer.getClass().getSimpleName());
        }
    }
    
    /**
     * Desregistra un observador
     * 
     * @param observer El observador a remover
     */
    public synchronized void unsubscribe(GameObserver observer) {
        if (observer != null) {
            observers.remove(observer);
            System.out.println("[OBSERVER] Se desregistró: " + observer.getClass().getSimpleName());
        }
    }
    
    /**
     * Notifica a todos los observadores que una fruta fue creada
     * 
     * @param fruitId ID de la fruta
     * @param type Tipo de fruta
     * @param x Posición X
     * @param y Posición Y
     * @param points Puntos que otorga
     */
    public void notifyFruitSpawned(int fruitId, String type, int x, int y, int points) {
        for (GameObserver observer : observers) {
            observer.onFruitSpawned(fruitId, type, x, y, points);
        }
    }
    
    /**
     * Notifica a todos los observadores que una fruta fue eliminada
     * 
     * @param fruitId ID de la fruta eliminada
     */
    public void notifyFruitRemoved(int fruitId) {
        for (GameObserver observer : observers) {
            observer.onFruitRemoved(fruitId);
        }
    }
    
    /**
     * Notifica a todos los observadores que un jugador se conectó
     * 
     * @param playerId ID del jugador conectado
     */
    public void notifyPlayerConnected(int playerId) {
        for (GameObserver observer : observers) {
            observer.onPlayerConnected(playerId);
        }
    }
    
    /**
     * Notifica a todos los observadores que un jugador se desconectó
     * 
     * @param playerId ID del jugador desconectado
     */
    public void notifyPlayerDisconnected(int playerId) {
        for (GameObserver observer : observers) {
            observer.onPlayerDisconnected(playerId);
        }
    }
}