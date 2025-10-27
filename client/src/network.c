#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <winsock2.h>
#include <ws2tcpip.h>
#include "constants.h"
#include "network.h"

#pragma comment(lib, "ws2_32.lib") // Enlazar con la librería Winsock2

// Conecta el cliente al servidor TCP
int connect_to_server(const char *ip, int port) {
    WSADATA wsaData;
    SOCKET sock;
    struct sockaddr_in serverAddr;
    
    // Inicializar Winsock
    if (WSAStartup(MAKEWORD(2, 2), &wsaData) != 0) {
        printf("[ERROR]: WSAStartup fallo\n");
        return -1;
    }
    
    // Crear socket TCP
    sock = socket(AF_INET, SOCK_STREAM, IPPROTO_TCP);
    if (sock == INVALID_SOCKET) {
        printf("[ERROR]: socket() fallo con error %d\n", WSAGetLastError());
        WSACleanup();
        return -1;
    }
    
    memset(&serverAddr, 0, sizeof(serverAddr)); // Preparar estructura con datos del servidor
    serverAddr.sin_family = AF_INET; // Familia de direcciones: IPv4
    serverAddr.sin_port = htons(port); // Puerto del servidor (convertir a formato de red)
    
    // Convertir dirección IP de texto a formato binario
    if (inet_pton(AF_INET, ip, &serverAddr.sin_addr) <= 0) {
        printf("[ERROR]: Direccion IP invalida: %s\n", ip);
        closesocket(sock);
        WSACleanup();
        return -1;
    }
    
    // Intentar conectar al servidor
    if (connect(sock, (struct sockaddr *)&serverAddr, sizeof(serverAddr)) == SOCKET_ERROR) {
        printf("[ERROR]: No se pudo conectar a %s:%d\n", ip, port);
        printf("[ERROR]: Verifica que el servidor esté escuchando\n");
        closesocket(sock);
        WSACleanup();
        return -1;
    }
    
    // Conexión exitosa
    return sock;
}

// Envía un mensaje al servidor
int send_message(SOCKET sock, const char *msg) {
    int bytesSent;
    
    // Crear buffer con mensaje
    char buffer[BUFFER_SIZE];
    snprintf(buffer, BUFFER_SIZE, "%s\n", msg);
    
    // Enviar el mensaje completo
    bytesSent = send(sock, buffer, strlen(buffer), 0);
    
    // Verificar si hubo error en el envío
    if (bytesSent == SOCKET_ERROR) {
        printf("[ERROR]: send() fallo con error %d\n", WSAGetLastError());
        return -1;
    }
    
    return 0;
}

// Recibe un mensaje del servidor
char* recv_message(SOCKET sock) {
    // Buffer estático para almacenar el mensaje (estático: se reutiliza en cada llamada)
    static char buffer[BUFFER_SIZE];
    int recvSize;
    
    // Limpiar el buffer antes de recibir
    memset(buffer, 0, BUFFER_SIZE);
    
    // Recibir datos del servidor
    recvSize = recv(sock, buffer, BUFFER_SIZE - 1, 0);
    
    // Verificar si hubo error o desconexión
    if (recvSize <= 0) {
        if (recvSize == 0) {
            // recvSize == 0 significa que el servidor cerró la conexión
            printf("[INFO]: El servidor cerro la conexión\n");
        } else {
            // recvSize < 0 significa error
            printf("[ERROR]: recv() fallo con error %d\n", WSAGetLastError());
        }
        return NULL;
    }
    
    // Agregar terminador de string para usar buffer como string
    buffer[recvSize] = '\0';
    
    // Devolver el buffer con el mensaje
    return buffer;
}

// Cierra la conexión con el servidor
void close_connection(SOCKET sock) {
    // Verificar si el socket es válido
    if (sock != -1) {
        // Cerrar el socket TCP
        shutdown(sock, SD_BOTH);
        
        // closesocket() libera el socket
        closesocket(sock);
    }

    // Libera todos los recursos inicializados por WSAStartup()
    WSACleanup();
}