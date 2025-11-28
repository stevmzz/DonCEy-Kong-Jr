package com.doncey.patterns.factory;

import com.doncey.server.Fruit;

/**
 * @brief Factory concreta para crear frutas
 * 
 * Implementa GameEntityFactory para la creación de objetos Fruit.
 * Utiliza el patrón Factory Method para abstraer la creación
 * de diferentes tipos de frutas (MANZANA, BANANO, MANGO).
 * 
 * Ventajas:
 * - Centraliza la lógica de creación de frutas
 * - Facilita agregar nuevos tipos de frutas en el futuro
 * - Desacopla GameWorld de la creación específica de Fruit
 */
public class FruitFactory implements GameEntityFactory {
    
    /**
     * Crea una fruta del tipo especificado
     * 
     * Valida el tipo de fruta y crea la instancia correspondiente.
     * Si el tipo es inválido, lanza una excepción.
     * 
     * @param id ID único de la fruta
     * @param x Posición X en pantalla
     * @param y Posición Y en pantalla
     * @param type Tipo de fruta (MANZANA, BANANO, MANGO)
     * @param points Puntos que otorga la fruta
     * @return Objeto Fruit creado
     * @throws IllegalArgumentException si el tipo de fruta es inválido
     */
    @Override
    public Fruit createFruit(int id, int x, int y, String type, int points) {
        if (type == null || type.trim().isEmpty()) {
            throw new IllegalArgumentException("El tipo de fruta no puede ser nulo o vacío");
        }
        
        String fruitType = type.toUpperCase().trim();
        
        // Validar que sea un tipo válido
        switch (fruitType) {
            case "MANZANA":
            case "BANANO":
            case "MANGO":
                // Crear y retornar la fruta
                return new Fruit(id, x, y, fruitType, points);
            default:
                throw new IllegalArgumentException(
                    "Tipo de fruta inválido: " + type + 
                    ". Use: MANZANA, BANANO, MANGO"
                );
        }
    }
}