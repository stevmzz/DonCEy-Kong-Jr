package com.doncey.server;

import java.io.*;
import java.net.Socket;

// Manejador de clientes
public class ClientHandler implements Runnable {

    private Socket socket; // Socket de conexión con el cliente C
    private static Integer clientCounter = 0; // Contador estático para asignar IDs únicos
    private Integer clientId; // ID único del cliente
    
    // Constructor
    public ClientHandler(Socket socket) {
        this.socket = socket;
        
        synchronized (clientCounter) { 
            this.clientId = ++clientCounter;
        }
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
            System.out.println("[Cliente #" + clientId + "] Conectado desde " + socket.getInetAddress().getHostAddress()); // Log
            
            String message; // Mensaje del cliente
            while ((message = in.readLine()) != null) {
                System.out.println("[Cliente #" + clientId + "] Recibido: " + message);
                String response = processMessage(message);
                out.println(response);
                System.out.println("[Cliente #" + clientId + "] Enviado: " + response);
            }
            
            System.out.println("[Cliente #" + clientId + "] Desconectado");
            socket.close();
            
        } catch (IOException e) {
            System.err.println("[Cliente #" + clientId + "] Error de I/O: " + e.getMessage());
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
}