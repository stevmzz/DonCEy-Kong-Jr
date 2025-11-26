package com.doncey.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.doncey.admin.ServerGUI;
import com.doncey.utils.Constants;

/**
 * El servidor escucha en un puerto específico y acepta conexiones
 * de clientes C. Cada cliente es manejado en un thread separado
 * por una instancia de ClientHandler.
 */
public class GameServer {

    private ServerSocket serverSocket; // Socket del servidor
    private Integer clientCounter = 0; // Contador de clientes conectados
    private ServerGUI serverGUI; // Referencia a la GUI del servidor (puede ser null)
    
    // Constructor sin GUI (para compatibilidad)
    public GameServer() throws IOException {
        this.serverSocket = new ServerSocket(Constants.SERVER_PORT);
        this.serverGUI = null;
    }
    
    // Constructor con GUI
    public GameServer(ServerGUI serverGUI) throws IOException {
        this.serverSocket = new ServerSocket(Constants.SERVER_PORT);
        this.serverGUI = serverGUI;
    }
    
    /**
     * Inicia el servidor
     * 
     * El servidor escucha de forma continua en el puerto especificado.
     * Para cada cliente que se conecta, crea un nuevo ClientHandler
     * y lo ejecuta en un thread separado.
     */
    public void start() {
        log("Servidor iniciado");
        log("Escuchando en puerto " + Constants.SERVER_PORT);
        log("Esperando conexiones de clientes...");
        
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
                log("Nuevo cliente conectado (Total: " + clientCounter + ")");
                // Crear ClientHandler para este cliente (pasando ServerGUI)
                ClientHandler handler = new ClientHandler(clientSocket, serverGUI);
                GameWorld.getInstance().registerClient(handler);
                
                // Ejecutar en un thread separado
                Thread clientThread = new Thread(handler);
                clientThread.setName("ClientThread-" + clientCounter);
                clientThread.start();
                
            } catch (IOException e) {
                log("[ERROR]: no se acepto el cliente: " + e.getMessage());
            }
        }
    }
    
    // Detiene el servidor
    public void stop() throws IOException {
        if (serverSocket != null && !serverSocket.isClosed()) {
            serverSocket.close();
            log("Servidor detenido");
        }
    }
    
    /**
     * Envía logs a la consola Y a la GUI (si existe)
     */
    private void log(String message) {
        String timestamp = new SimpleDateFormat("HH:mm:ss").format(new Date());
        String logMessage = "[" + timestamp + "] > " + message;
        
        // Imprimir en consola
        System.out.println(logMessage);
        
        // Enviar a GUI si existe
        if (serverGUI != null) {
            serverGUI.addServerLog(logMessage);
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
            System.err.println("[ERROR]: No se pudo iniciar el servidor: " + e.getMessage());
            System.err.println("[ERROR]: Verifica que el puerto " + Constants.SERVER_PORT + " este disponible");
        }
    }
}