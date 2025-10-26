#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include "constants.h"
#include "network.h"

/**
 * @brief Punto de entrada del programa cliente
 * 
 * @return 0 si el programa se ejecutó correctamente
 * @return 1 si ocurrió un error al conectar al servidor
 */
int main() {
    SOCKET sock; // Socket para la conexión al servidor
    char buffer[BUFFER_SIZE]; // Buffer para mensajes del usuario
    char *respuesta; // Puntero para la respuesta del servidor
    
    printf("\n========== DonCEy Kong Jr - Cliente C ==========\n\n");
    
    // Intentar conectar al servidor
    printf("[CLIENTE] Intentando conectar a %s:%d...\n", SERVER_IP, SERVER_PORT);
    sock = connect_to_server(SERVER_IP, SERVER_PORT);
    
    // Verificar si la conexión fue exitosa
    if (sock == -1) {
        printf("[ERROR] No se pudo conectar al servidor\n");
        return 1;
    }
    
    // Conexión exitosa
    printf("[CLIENTE] Conectado al servidor\n");
    printf("[CLIENTE] Escribe algun mensaje (9 para salir)\n\n");
    
    // LOOP CONTINUO - Leer mensajes del usuario
    while (1) {
        // Mostrar prompt
        printf("[TU] ");
        fflush(stdout);
        
        // Leer input del usuario desde teclado
        fgets(buffer, BUFFER_SIZE, stdin);
        
        // Remover el salto de línea que fgets agrega automáticamente
        size_t len = strlen(buffer);
        if (len > 0 && buffer[len - 1] == '\n') {
            buffer[len - 1] = '\0';
        }
        
        // Si el usuario escribe "9", salir del loop
        if (strcmp(buffer, "9") == 0) {
            break;
        }
        
        // Si el usuario solo presiona Enter (mensaje vacío), continuar sin enviar
        if (strlen(buffer) == 0) {
            continue;
        }
        
        // Enviar el mensaje al servidor
        if (send_message(sock, buffer) == -1) {
            printf("[ERROR] No se pudo enviar el mensaje\n");
            break;
        }
        
        // Recibir respuesta del servidor
        respuesta = recv_message(sock);
        
        // Verificar si se recibió respuesta válida
        if (respuesta == NULL) {
            printf("[ERROR] No se recibio respuesta del servidor\n");
            break;
        }
        
        // Mostrar la respuesta del servidor
        printf("[SERVIDOR] %s\n", respuesta);
    }
    
    // Cerrar la conexión de forma segura
    close_connection(sock);
    printf("[CLIENTE] Desconectado\n");
    
    return 0;
}