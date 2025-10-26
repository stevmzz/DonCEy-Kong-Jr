#ifndef NETWORK_H
#define NETWORK_H

#include <winsock2.h>

/**
 * @brief Conecta el cliente al servidor TCP
 * 
 * Establece una conexión TCP con el servidor en la dirección IP y puerto
 * especificados. Realiza la inicialización de Winsock, crea el socket,
 * y conecta al servidor.
 * 
 * @param ip Dirección IP del servidor (ej: "127.0.0.1")
 * @param port Puerto del servidor (ej: 9999)
 * 
 * @return Socket válido (> 0) si la conexión fue exitosa
 * @return -1 si ocurrió un error durante la conexión
 */
int connect_to_server(const char *ip, int port);

/**
 * @brief Envía un mensaje al servidor
 * 
 * Transmite un mensaje de texto al servidor a través del socket.
 * El mensaje debe estar terminado en '\n' para que el servidor lo reciba
 * correctamente (usa readLine() en Java).
 * 
 * @param sock Socket de conexión (obtenido de connect_to_server)
 * @param msg Mensaje a enviar (debe incluir '\n' al final)
 * 
 * @return 0 si el envío fue exitoso
 * @return -1 si ocurrió un error al enviar el mensaje
 */
int send_message(SOCKET sock, const char *msg);

/**
 * @brief Recibe un mensaje del servidor
 * 
 * Lee un mensaje enviado por el servidor. El mensaje se almacena en
 * un buffer interno y se devuelve como string.
 * 
 * @param sock Socket de conexión (obtenido de connect_to_server)
 * 
 * @return Pointer al string recibido si fue exitoso
 * @return NULL si ocurrió un error o se desconectó el servidor
 */
char* recv_message(SOCKET sock);

/**
 * @brief Cierra la conexión con el servidor
 * 
 * Libera los recursos de la conexión TCP de forma segura.
 * Cierra el socket y limpia Winsock.
 * 
 * Debe llamarse siempre antes de terminar el programa.
 * 
 * @param sock Socket a cerrar (obtenido de connect_to_server)
 */
void close_connection(SOCKET sock);

#endif