package com.whatsapp;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import javax.swing.ImageIcon;

public class Board {

    private final int N_BLOCKS = 19;
    private final int BLOCK_SIZE = 20;
    private final int SCREEN_SIZE = N_BLOCKS * BLOCK_SIZE;
    
    private Image dot;
    private Image powerPellet;

    // 0 = empty, 1 = dot, 2 = power pellet, 3 = wall
    private final short levelData[] = {
        3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3,
        3, 1, 1, 1, 1, 1, 1, 1, 1, 3, 1, 1, 1, 1, 1, 1, 1, 1, 3,
        3, 2, 3, 3, 1, 3, 3, 3, 1, 3, 1, 3, 3, 3, 1, 3, 3, 2, 3,
        3, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 3,
        3, 1, 3, 3, 1, 3, 1, 3, 3, 3, 3, 3, 1, 3, 1, 3, 3, 1, 3,
        3, 1, 1, 1, 1, 3, 1, 1, 1, 3, 1, 1, 1, 3, 1, 1, 1, 1, 3,
        3, 3, 3, 3, 1, 3, 3, 3, 0, 3, 0, 3, 3, 3, 1, 3, 3, 3, 3,
        3, 3, 3, 3, 1, 3, 0, 0, 0, 0, 0, 0, 0, 3, 1, 3, 3, 3, 3,
        3, 3, 3, 3, 1, 3, 0, 3, 3, 0, 3, 3, 0, 3, 1, 3, 3, 3, 3,
        0, 0, 0, 0, 1, 0, 0, 3, 0, 0, 0, 3, 0, 0, 1, 0, 0, 0, 0,
        3, 3, 3, 3, 1, 3, 0, 3, 3, 3, 3, 3, 0, 3, 1, 3, 3, 3, 3,
        3, 3, 3, 3, 1, 3, 0, 0, 0, 0, 0, 0, 0, 3, 1, 3, 3, 3, 3,
        3, 3, 3, 3, 1, 3, 0, 3, 3, 3, 3, 3, 0, 3, 1, 3, 3, 3, 3,
        3, 1, 1, 1, 1, 1, 1, 1, 1, 3, 1, 1, 1, 1, 1, 1, 1, 1, 3,
        3, 2, 3, 3, 1, 3, 3, 3, 1, 3, 1, 3, 3, 3, 1, 3, 3, 2, 3,
        3, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 3,
        3, 1, 3, 3, 1, 3, 1, 3, 3, 3, 3, 3, 1, 3, 1, 3, 3, 1, 3,
        3, 1, 1, 1, 1, 3, 1, 1, 1, 3, 1, 1, 1, 3, 1, 1, 1, 1, 3,
        3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3,
    };
    
    public Board() {
        loadImages();
    }
    
    private void loadImages() {
        dot = new ImageIcon("src/main/resources/dot.png").getImage();
        powerPellet = new ImageIcon("src/main/resources/power.png").getImage();
    }

    public void drawBoard(Graphics g) {
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, SCREEN_SIZE, SCREEN_SIZE);

        for (int i = 0; i < N_BLOCKS * N_BLOCKS; i++) {
            int x = (i % N_BLOCKS) * BLOCK_SIZE;
            int y = (i / N_BLOCKS) * BLOCK_SIZE;

            if (levelData[i] == 1) {
                g.drawImage(dot, x, y, null);
            } else if (levelData[i] == 2) {
                g.drawImage(powerPellet, x, y, null);
            } else if (levelData[i] == 3) {
                g.setColor(new Color(0, 0, 128)); // Blue for walls
                g.fillRect(x, y, BLOCK_SIZE, BLOCK_SIZE);
            }
        }
    }
    
    public int getBlockSize() {
        return BLOCK_SIZE;
    }
    
    public short[] getLevelData() {
        return levelData;
    }
}
