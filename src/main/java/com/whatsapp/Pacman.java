package com.whatsapp;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.KeyEvent;

public class Pacman extends Character {

    public Pacman(int x, int y) {
        super(x, y);
    }

    @Override
    public void draw(Graphics g) {
        g.setColor(Color.YELLOW);
        g.fillArc(x, y, SIZE, SIZE, 30, 300);
    }

    public void keyPressed(KeyEvent e) {
        int key = e.getKeyCode();

        if (key == KeyEvent.VK_LEFT) {
            dx = -speed;
            dy = 0;
        } else if (key == KeyEvent.VK_RIGHT) {
            dx = speed;
            dy = 0;
        } else if (key == KeyEvent.VK_UP) {
            dx = 0;
            dy = -speed;
        } else if (key == KeyEvent.VK_DOWN) {
            dx = 0;
            dy = speed;
        }
    }
}
