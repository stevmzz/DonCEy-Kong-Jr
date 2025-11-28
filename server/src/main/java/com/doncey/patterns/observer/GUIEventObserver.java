package com.doncey.patterns.observer;

import com.doncey.admin.ServerGUI;

/**
 * @brief Observador concreto del patrón Observer
 * 
 * Implementa GameObserver para reaccionar a eventos del juego
 * y actualizar la GUI del servidor.
 */
public class GUIEventObserver implements GameObserver {
    
    private ServerGUI serverGUI;
    
    /**
     * Constructor del observador
     * 
     * @param serverGUI Referencia a la GUI del servidor
     */
    public GUIEventObserver(ServerGUI serverGUI) {
        this.serverGUI = serverGUI;
    }
    
    @Override
    public void onFruitSpawned(int fruitId, String type, int x, int y, int points) {
        if (serverGUI != null) {
            String message = String.format(
                "[EVENTO] Fruta creada: %s (ID:%d) en (X:%d, Y:%d) = %d pts",
                type, fruitId, x, y, points
            );
            serverGUI.addServerLog(message);
        }
    }
    
    @Override
    public void onFruitRemoved(int fruitId) {
        if (serverGUI != null) {
            String message = String.format(
                "[EVENTO] Fruta eliminada (ID:%d)",
                fruitId
            );
            serverGUI.addServerLog(message);
        }
    }
    
    @Override
    public void onPlayerConnected(int playerId) {
        if (serverGUI != null) {
            String message = String.format(
                "[EVENTO] Jugador conectado (ID:%d)",
                playerId
            );
            serverGUI.addServerLog(message);
        }
    }
    
    @Override
    public void onPlayerDisconnected(int playerId) {
        if (serverGUI != null) {
            String message = String.format(
                "[EVENTO] Jugador desconectado (ID:%d)",
                playerId
            );
            serverGUI.addServerLog(message);
        }
    }
}