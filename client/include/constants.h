#ifndef CONSTANTS_H
#define CONSTANTS_H

/**
 * @brief Dirección IP del servidor
 * 
 * Dirección IP donde está corriendo el servidor Java.
 * Actualmente configurado en localhost (127.0.0.1) para pruebas locales.
 * 
 * Para conectar desde otra computadora, cambiar a su IP (ej: "192.168.1.100")
 */
#define SERVER_IP "127.0.0.1"

/**
 * @brief Puerto del servidor
 * 
 * Puerto TCP en el que el servidor Java está escuchando conexiones.
 * Debe coincidir con el puerto en Constants.java del servidor.
 */
#define SERVER_PORT 9999

/**
 * @brief Tamaño del buffer de comunicación
 * 
 * Tamaño máximo de bytes que se pueden enviar/recibir en un mensaje
 * entre el cliente C y el servidor Java.
 */
#define BUFFER_SIZE 1024

#endif