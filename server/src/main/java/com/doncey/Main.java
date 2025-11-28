package com.doncey;

import javax.swing.SwingUtilities;
import com.doncey.admin.ServerGUI;
import com.doncey.server.GameServer;
import com.doncey.patterns.observer.GameEventPublisher;
import com.doncey.patterns.observer.GUIEventObserver;

// Clase principal que inicia el servidor de juego
public class Main {
    public static void main(String[] args) {
        // Crear la GUI primero
        ServerGUI[] serverGUIRef = new ServerGUI[1];
        SwingUtilities.invokeLater(() -> {
            ServerGUI serverGUI = new ServerGUI();
            serverGUIRef[0] = serverGUI;
            serverGUI.setVisible(true);
            GameEventPublisher.getInstance().subscribe(new GUIEventObserver(serverGUI));
        });
        
        // Esperar a que la GUI se cree
        while (serverGUIRef[0] == null) {
            try {
                Thread.sleep(100);

            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        
        // Iniciar el servidor con referencia a la GUI
        new Thread(() -> {
            try {
                GameServer server = new GameServer(serverGUIRef[0]);
                server.start();

            } catch (Exception e) {
                System.err.println("[ERROR] > " + e.getMessage());
            }
        }).start();
    }
}