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
 * 
 * También ejecuta un game loop para actualizar la lógica del juego
 */
public class GameServer {

    private ServerSocket serverSocket; // Socket del servidor
    private Integer clientCounter = 0; // Contador de clientes conectados
    private ServerGUI serverGUI; // Referencia a la GUI del servidor (puede ser null)
    
    private Thread gameLoopThread; // Thread del game loop
    private volatile boolean running = true; // Flag para detener el servidor
    private static final int GAME_UPDATE_RATE = 50; // ms (20 FPS)
    
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
        
        // Iniciar game loop
        gameLoopThread = new Thread(this::gameLoop);
        gameLoopThread.setName("GameLoopThread");
        gameLoopThread.start();
        
        // LOOP INFINITO: Aceptar clientes
        while (running) {
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
                
                // Ejecutar en un thread separado
                Thread clientThread = new Thread(handler);
                clientThread.setName("ClientThread-" + clientCounter);
                clientThread.start();
                
            } catch (IOException e) {
                if (running) {
                    log("[ERROR]: no se acepto el cliente: " + e.getMessage());
                }
            }
        }
    }
    
    /**
     * Game Loop - actualiza la lógica del juego a intervalos regulares
     */
    private void gameLoop() {
        log("Game Loop iniciado (actualización cada " + GAME_UPDATE_RATE + "ms)");
        
        while (running) {
            long startTime = System.currentTimeMillis();
            
            try {
                // Actualizar lógica del juego
                GameWorld.getInstance().updateGameLogic();
                
                long elapsedTime = System.currentTimeMillis() - startTime;
                long sleepTime = GAME_UPDATE_RATE - elapsedTime;
                
                if (sleepTime > 0) {
                    Thread.sleep(sleepTime);
                }
                
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            } catch (Exception e) {
                log("[ERROR]: Error en game loop: " + e.getMessage());
            }
        }
        
        log("Game Loop detenido");
    }
    
    // Detiene el servidor
    public void stop() throws IOException {
        running = false;
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