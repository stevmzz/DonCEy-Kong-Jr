package com.doncey.server;

public class Platform {
    private final int x;
    private final int y;
    private final int width;
    private final int height;

    public Platform(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public boolean collides(Player p) {

        boolean horizontal =
                p.getX() < x + width &&
                p.getX() + p.getWidth() > x;

        // El jugador viene cayendo y toca la parte superior de la plataforma
        boolean vertical =
                p.getY() + p.getHeight() >= y &&
                p.getPrevY() + p.getHeight() <= y;

        return horizontal && vertical;
    }

    public int getY() { return y; }
    public int getX() { return x; }
    public int getWidth() { return width; }
    public int getHeight() { return height; }
}
