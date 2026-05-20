package com.whatsapp;

import java.awt.Graphics;
import java.awt.Rectangle;

public abstract class Character { 

    protected volatile int x;
    protected volatile int y;
    
    protected volatile int dx;
    protected volatile int dy;
    
    protected final int speed = 4;
    
    protected volatile int size = 20; 

    public Character(int x, int y) { 
        this.x = x;
        this.y = y; 
    }

    protected void performMove() {
        x += dx;
        y += dy;
    }

    public abstract void move(Board board);

    public int getX() { return x; }
    
    public int getY() { return y; }
    
    public void setPosition(int x, int y) { 
        this.x = x; 
        this.y = y; 
    }

    public Rectangle getBounds() { 
        return new Rectangle(x, y, size, size);
    }
    
    public void updateSize(int newSize) { 
        this.size = newSize; 
    }

    public abstract void draw(Graphics g, int size, int screenOffsetX, int screenOffsetY);
}
