package com.doncey.patterns.factory;

import com.doncey.server.Fruit;

/**
 * @brief Interfaz del patrón Abstract Factory
 * 
 * Define el contrato para las factories que crean entidades del juego.
 * Específicamente para crear Fruit con diferentes tipos.
 */
public interface GameEntityFactory {
    
    /**
     * Crea una fruta del tipo especificado
     * 
     * @param id ID único de la fruta
     * @param x Posición X
     * @param y Posición Y
     * @param type Tipo de fruta (MANZANA, BANANO, MANGO)
     * @param points Puntos que otorga
     * @return Objeto Fruit creado
     */
    Fruit createFruit(int id, int x, int y, String type, int points);
}