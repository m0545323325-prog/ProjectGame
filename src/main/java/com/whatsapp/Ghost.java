package com.whatsapp;

import java.awt.Color;
import java.awt.Graphics;
import java.util.Random;

public class Ghost extends Character {

    private Color color;
    private Random random = new Random();

    public Ghost(int x, int y, Color color) {
        super(x, y);
        this.color = color;
        this.speed = 1; // Ghosts are a bit slower
        
        // Initial random direction
        int randomDirection = random.nextInt(4);
        if (randomDirection == 0) { dx = speed; }
        else if (randomDirection == 1) { dx = -speed; }
        else if (randomDirection == 2) { dy = speed; }
        else { dy = -speed; }
    }

    @Override
    public void draw(Graphics g) {
        g.setColor(color);
        g.fillRect(x, y, SIZE, SIZE);
        
        g.setColor(Color.WHITE);
        g.fillOval(x + 4, y + 5, 5, 5);
        g.fillOval(x + 11, y + 5, 5, 5);
        
        g.setColor(Color.BLACK);
        g.fillOval(x + 5, y + 6, 3, 3);
        g.fillOval(x + 12, y + 6, 3, 3);
    }
    
    public void move(Board board) {
        // Simple random movement logic
        int currentXBlock = x / board.getBlockSize();
        int currentYBlock = y / board.getBlockSize();
        int currentBlockIndex = currentYBlock * 19 + currentXBlock;
        
        short[] levelData = board.getLevelData();

        if (x % board.getBlockSize() == 0 && y % board.getBlockSize() == 0) {
            int[] possibleDx = new int[4];
            int[] possibleDy = new int[4];
            int count = 0;

            if (dx == 0) { // Moving vertically
                if (levelData[currentBlockIndex - 1] != 3) { // Check left
                    possibleDx[count] = -speed;
                    possibleDy[count] = 0;
                    count++;
                }
                if (levelData[currentBlockIndex + 1] != 3) { // Check right
                    possibleDx[count] = speed;
                    possibleDy[count] = 0;
                    count++;
                }
            }
            
            if (dy == 0) { // Moving horizontally
                 if (levelData[currentBlockIndex - 19] != 3) { // Check up
                    possibleDx[count] = 0;
                    possibleDy[count] = -speed;
                    count++;
                }
                if (levelData[currentBlockIndex + 19] != 3) { // Check down
                    possibleDx[count] = 0;
                    possibleDy[count] = speed;
                    count++;
                }
            }
            
            if (count > 0) {
                int choice = random.nextInt(count);
                dx = possibleDx[choice];
                dy = possibleDy[choice];
            } else { // Dead end, turn back
                dx = -dx;
                dy = -dy;
            }
        }
        
        move();
    }
}
