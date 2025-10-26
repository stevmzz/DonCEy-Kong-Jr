package com.doncey.server;

import com.doncey.utils.Constants;
import java.io.*;
import java.net.*;

/**
 * El servidor escucha en un puerto específico y acepta conexiones
 * de clientes C. Cada cliente es manejado en un thread separado
 * por una instancia de ClientHandler.
 */
public class GameServer {

    private ServerSocket serverSocket; // Socket del servidor
    private Integer clientCounter = 0; // Contador de clientes conectados
    
    // Constructor
    public GameServer() throws IOException {
        this.serverSocket = new ServerSocket(Constants.SERVER_PORT);
    }
    
    /**
     * Inicia el servidor
     * 
     * El servidor escucha de forma continua en el puerto especificado.
     * Para cada cliente que se conecta, crea un nuevo ClientHandler
     * y lo ejecuta en un thread separado.
     */
    public void start() {
        System.out.println("[SERVIDOR] Escuchando en puerto " + Constants.SERVER_PORT);
        System.out.println("[SERVIDOR] Esperando conexiones de clientes...");
        System.out.println();
        
        // LOOP INFINITO: Aceptar clientes
        while (true) {
            try {
                // Aceptar conexión de un cliente
                Socket clientSocket = serverSocket.accept();
                
                // Incrementar contador de clientes
                synchronized (this) {
                    clientCounter++;
                }
                
                // Log de nueva conexión
                System.out.println("[SERVIDOR] Nuevo cliente conectado (Total: " + clientCounter + ")");
                
                // Crear ClientHandler para este cliente
                ClientHandler handler = new ClientHandler(clientSocket);
                
                // Ejecutar en un thread separado
                Thread clientThread = new Thread(handler);
                clientThread.setName("ClientThread-" + clientCounter);
                clientThread.start();
                
            } catch (IOException e) {
                System.err.println("[SERVIDOR] Error al aceptar cliente: " + e.getMessage());
            }
        }
    }
    
    // Detiene el servidor
    public void stop() throws IOException {
        if (serverSocket != null && !serverSocket.isClosed()) {
            serverSocket.close();
            System.out.println("[SERVIDOR] Servidor detenido");
        }
    }
    
    /**
     * Método principal de inicio del servidor
     *      
     * @param args Argumentos de línea de comandos (no usados)
     */
    public static void main(String[] args) {
        try {
            GameServer server = new GameServer();
            server.start();
        } catch (IOException e) {
            System.err.println("[ERROR] No se pudo iniciar el servidor: " + e.getMessage());
            System.err.println("[ERROR] Verifica que el puerto " + Constants.SERVER_PORT + " esté disponible");
        }
    }
}