package com.doncey.server;

import com.doncey.admin.ServerGUI;
import java.io.*;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.atomic.AtomicInteger;

// Manejador de clientes
public class ClientHandler implements Runnable {

    private Socket socket; // Socket de conexión con el cliente C
    private static AtomicInteger clientCounter = new AtomicInteger(0); // Contador estático de clientes
    private Integer clientId; // ID único del cliente
    private ServerGUI serverGUI; // Referencia a la GUI del servidor (puede ser null)
    
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
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream())); // Entrada
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true); // Salida
            
            log("[Cliente #" + clientId + "]: Conectado desde " + socket.getInetAddress().getHostAddress());
            
            String message; // Mensaje del cliente
            while ((message = in.readLine()) != null) {
                log("[Cliente #" + clientId + "]: Recibido " + "( " + message + " )");
                String response = processMessage(message);
                out.println(response);
                log("[Cliente #" + clientId + "]: Enviado " + "( " + response + " )");
            }
            
            log("[Cliente #" + clientId + "]: Desconectado");
            socket.close();
            
        } catch (IOException e) {
            log("[Cliente #" + clientId + "]: Error de I/O: " + e.getMessage());
        }
    }
    
    /**
     * Procesa un mensaje recibido del cliente
     * 
     * @param message Mensaje recibido
     * @return Respuesta a enviar al cliente
     */
    private String processMessage(String message) {
        // Aquí iría la lógica de procesamiento de mensajes 
        return message;
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