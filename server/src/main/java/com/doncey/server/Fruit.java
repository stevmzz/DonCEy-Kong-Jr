package com.doncey.server;

public class Fruit {
    private final int id;
    private final int x;
    private final int y;
    private final String type;
    private final int points;

    public Fruit(int id, int x, int y, String type, int points) {
        this.id = id;
        this.x = x;
        this.y = y;
        this.type = type;
        this.points = points;
    }

    public int getId() { return id; }
    public int getX() { return x; }
    public int getY() { return y; }
    public String getType() { return type; }
    public int getPoints() { return points; }

    @Override
    public String toString() {
        return "Fruit{" + id + "," + type + "," + x + "," + y + "," + points + "}";
    }
}
