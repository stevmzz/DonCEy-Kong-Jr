package com.doncey.admin;

import javax.swing.*;
import java.awt.*;

public class ServerGUI extends JFrame {
    
    private JTabbedPane tabbedPane;
    private ServerPanel serverPanel;
    private PlayersPanel playersPanel;
    
    public ServerGUI() {
        // Configuración de la ventana
        setTitle("DonCEy Kong Jr - SERVIDOR DE ADMINISTRACIÓN");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 700);
        setLocationRelativeTo(null);
        setResizable(true);
        
        // Crear barra de pestañas
        tabbedPane = new JTabbedPane();
        
        // Crear paneles
        serverPanel = new ServerPanel();
        playersPanel = new PlayersPanel();
        
        // Agregar pestañas
        tabbedPane.addTab("SERVIDOR", serverPanel);
        tabbedPane.addTab("JUGADORES", playersPanel);
        
        // Agregar la barra de pestañas al contenedor principal
        add(tabbedPane, BorderLayout.CENTER);
    }
    
    // Agrega un log a la pestaña de SERVIDOR
    public void addServerLog(String message) {
        if (serverPanel != null) {
            serverPanel.addLog(message);
        }
    }
    
    // Notifica que un jugador se conectó
    public void notifyPlayerConnected(Integer playerId) {
        if (playersPanel != null) {
            playersPanel.addPlayer(playerId);
        }
    }
    
    // Notifica que un jugador se desconectó
    public void notifyPlayerDisconnected(Integer playerId) {
        if (playersPanel != null) {
            playersPanel.removePlayer(playerId);
        }
    }
    
    // Retorna el panel del servidor
    public ServerPanel getServerPanel() {
        return serverPanel;
    }

    // Retorna el panel de jugadores
    public PlayersPanel getPlayersPanel() {
        return playersPanel;
    }
}