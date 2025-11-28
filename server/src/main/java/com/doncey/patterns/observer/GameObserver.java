package com.doncey.patterns.observer;

/**
 * @brief Interfaz del patrón Observer
 * 
 * Define los eventos que ocurren en el juego y que los observadores
 * pueden escuchar. Implementa esta interfaz para reaccionar ante eventos.
 */
public interface GameObserver {
    
    /**
     * Se llama cuando una fruta es creada
     * 
     * @param fruitId ID único de la fruta
     * @param type Tipo de fruta (MANZANA, BANANO, MANGO)
     * @param x Posición X de la fruta
     * @param y Posición Y de la fruta
     * @param points Puntos que otorga la fruta
     */
    void onFruitSpawned(int fruitId, String type, int x, int y, int points);
    
    /**
     * Se llama cuando una fruta es eliminada
     * 
     * @param fruitId ID de la fruta eliminada
     */
    void onFruitRemoved(int fruitId);
    
    /**
     * Se llama cuando un jugador se conecta
     * 
     * @param playerId ID del jugador conectado
     */
    void onPlayerConnected(int playerId);
    
    /**
     * Se llama cuando un jugador se desconecta
     * 
     * @param playerId ID del jugador desconectado
     */
    void onPlayerDisconnected(int playerId);
}