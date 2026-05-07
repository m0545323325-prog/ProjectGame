package com.whatsapp;

import java.awt.Graphics;
import java.awt.Rectangle;

public abstract class Character {

    protected int x;
    protected int y;
    protected int dx;
    protected int dy;
    protected int speed = 2;
    protected final int SIZE = 20;

    public Character(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public void move() {
        x += dx;
        y += dy;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }
    
    public Rectangle getBounds() {
        return new Rectangle(x, y, SIZE, SIZE);
    }

    public abstract void draw(Graphics g);
}
