package com.doncey.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.atomic.AtomicInteger;

import com.doncey.admin.ServerGUI;

// Manejador de clientes
public class ClientHandler implements Runnable {

    private Socket socket; // Socket de conexión con el cliente C
    private static AtomicInteger clientCounter = new AtomicInteger(0); // Contador estático de clientes
    private final Integer clientId; // ID único del cliente
    private ServerGUI serverGUI; // Referencia a la GUI del servidor (puede ser null)

    private PrintWriter out;
    private BufferedReader in;
    private volatile boolean running = true;
    
    // Constructor sin GUI (para compatibilidad)
    public ClientHandler(Socket socket) {
        this.socket = socket;
        this.clientId = clientCounter.incrementAndGet();
        this.serverGUI = null;
    }
    
    // Constructor con GUI
    public ClientHandler(Socket socket, ServerGUI serverGUI) {
        this.socket = socket;
        this.clientId = clientCounter.incrementAndGet();
        this.serverGUI = serverGUI;
    }
    
    /**
     * Método principal del thread
     * 
     * Lee mensajes del cliente C de forma continua y responde.
     * Se ejecuta en un thread separado para cada cliente.
     * El método termina cuando el cliente se desconecta.
     */
    @Override
    public void run() {
        try {
            this.in = new BufferedReader(new InputStreamReader(socket.getInputStream())); // Entrada
            this.out = new PrintWriter(socket.getOutputStream(), true); // Salida
            
            log("[Cliente #" + clientId + "]: Conectado desde " + socket.getInetAddress().getHostAddress());
            
            // registrar en GameWorld
            GameWorld.getInstance().registerPlayer(clientId, this);
            GameWorld.getInstance().registerClient(this);

            // Notificar a la GUI que se conectó un jugador
            if (serverGUI != null) {
                serverGUI.notifyPlayerConnected(clientId);
            }

            // enviar asignación de id al cliente
            sendMessage("ASSIGN_ID " + clientId);

            String message; // Mensaje del cliente
            while (running && (message = this.in.readLine()) != null) {
                if (message == null) break;
                message = message.trim();
                if (message.length() == 0) continue;

                log("[Cliente #" + clientId + "]: Recibido ( " + message + " )");

                String response = processMessage(message);
                // enviar respuesta sólo si hay algo que enviar
                if (response != null) {
                    sendMessage(response);
                    log("[Cliente #" + clientId + "]: Enviado ( " + response + " )");
                }
            }

            log("[Cliente #" + clientId + "]: Desconectado");

        } catch (IOException e) {
            log("[Cliente #" + clientId + "]: Error de I/O: " + e.getMessage());
        } finally {
            // cleanup garantizado
            cleanup();
        }
    }
    
    /**
     * Procesa un mensaje recibido del cliente
     *
     * @param message Mensaje recibido
     * @return Respuesta a enviar al cliente (o null si no corresponde enviar respuesta)
     */
    private String processMessage(String message) {
        // Mensajes de movimiento: MOVE_LEFT, MOVE_RIGHT, STOP_MOVING
        try {
            if (message.startsWith("MOVE_LEFT") || message.startsWith("MOVE_RIGHT") || message.startsWith("STOP_MOVING")) {
                GameWorld.getInstance().processPlayerCommand(clientId, message);
                return null; // No responder, el servidor broadcast la posición
            }
            
            if (message.startsWith("EAT_FRUIT")) {
                String[] parts = message.split("\\s+");
                if (parts.length >= 3) {
                    int cid = Integer.parseInt(parts[1]);
                    int fid = Integer.parseInt(parts[2]);

                    Fruit f = GameWorld.getInstance().getFruit(fid);
                    if (f != null) {
                        // remueve fruta del mundo -> broadcast REMOVE_FRUIT
                        boolean removed = GameWorld.getInstance().removeFruit(fid);
                        if (removed) {
                            if (serverGUI != null) {
                                serverGUI.removeFruitFromList(fid);
                            }

                            // broadcast de puntaje
                            GameWorld.getInstance().broadcast(
                                String.format("PLAYER_SCORE %d %d", cid, f.getPoints())
                            );

                            return "EAT_OK " + fid + " " + f.getPoints();
                        } else {
                            return "EAT_FAIL " + fid;
                        }
                    } else {
                        return "EAT_FAIL " + fid;
                    }
                } else {
                    return "ERROR invalid EAT_FRUIT";
                }
            }
        } catch (Exception ex) {
            return "ERROR " + ex.getMessage();
        }
        return null; // nada que responder
    }

    public synchronized void sendMessage(String msg) {
        // asegurar que out esté inicializado
        if (out != null) {
            out.println(msg);
        } else {
            // fallback: intentar escribir directamente al socket (opcional)
            try {
                if (socket != null && !socket.isClosed()) {
                    PrintWriter p = new PrintWriter(socket.getOutputStream(), true);
                    p.println(msg);
                }
            } catch (IOException ignored) {}
        }
    }

    private void cleanup() {
        try {
            running = false;
            // quitar del GameWorld
            GameWorld.getInstance().unregisterPlayer(clientId);
            GameWorld.getInstance().unregisterClient(this);
            if (serverGUI != null) serverGUI.notifyPlayerDisconnected(clientId);
            if (in != null) try { in.close(); } catch (IOException ignored) {}
            if (out != null) out.close();
            if (socket != null && !socket.isClosed()) socket.close();
            log("[Cliente #" + clientId + "]: Conexión cerrada");
        } catch (IOException ignored) {}
    }

    /**
     * Envía logs a la consola Y a la GUI (si existe)
     *
     * @param message Mensaje de log
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
}