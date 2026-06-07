package com.whatsapp;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Font;
import java.awt.FontMetrics;
import javax.swing.ImageIcon;

public class Board {

    private final int N_BLOCKS = 19;
    private int blockSize;

    private final short levelData[] = {
            3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3,
            3, 1, 1, 1, 1, 1, 1, 1, 1, 3, 1, 1, 1, 1, 1, 1, 1, 1, 3,
            3, 2, 3, 3, 1, 3, 3, 3, 1, 3, 1, 3, 3, 3, 1, 3, 3, 2, 3,
            3, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 3,
            3, 1, 3, 3, 1, 3, 1, 3, 3, 3, 3, 3, 1, 3, 1, 3, 3, 1, 3,
            3, 1, 1, 1, 1, 3, 1, 1, 1, 3, 1, 1, 1, 3, 1, 1, 1, 1, 3,
            3, 3, 3, 3, 1, 3, 3, 3, 0, 3, 0, 3, 3, 3, 1, 3, 3, 3, 3,
            3, 3, 3, 3, 1, 3, 0, 0, 0, 0, 0, 0, 0, 3, 1, 3, 3, 3, 3,
            3, 3, 3, 3, 1, 3, 0, 3, 3, 1, 3, 3, 0, 3, 1, 3, 3, 3, 3,
            0, 0, 0, 0, 1, 0, 0, 3, 1, 2, 1, 3, 0, 0, 1, 0, 0, 0, 0,
            3, 3, 3, 3, 1, 3, 0, 3, 3, 3, 3, 3, 0, 3, 1, 3, 3, 3, 3,
            3, 3, 3, 3, 1, 3, 0, 0, 0, 0, 0, 0, 0, 3, 1, 3, 3, 3, 3,
            3, 3, 3, 3, 1, 3, 0, 3, 3, 3, 3, 3, 0, 3, 1, 3, 3, 3, 3,
            3, 1, 1, 1, 1, 1, 1, 1, 1, 3, 1, 1, 1, 1, 1, 1, 1, 1, 3,
            3, 2, 3, 3, 1, 3, 3, 3, 1, 3, 1, 3, 3, 3, 1, 3, 3, 2, 3,
            3, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 3,
            3, 1, 3, 3, 1, 3, 1, 3, 3, 3, 3, 3, 1, 3, 1, 3, 3, 1, 3,
            3, 1, 1, 1, 1, 3, 1, 1, 1, 1, 1, 1, 1, 3, 1, 1, 1, 1, 3,
            3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3,
    };

    /**
     * Constructor for the Board class. Initializes the game board.
     */
    public Board() {
    }

    /**
     * Draws the game board, including walls and items, onto the provided Graphics context.
     * Calculates block size and offsets to center the board dynamically.
     * @param g The Graphics object used for drawing.
     * @param width The current width of the drawing area.
     * @param height The current height of the drawing area.
     */
    public void drawBoard(Graphics g, int width, int height) {
        
        int smallerDimension = Math.min(width, height);
        
        blockSize = smallerDimension / N_BLOCKS;
        
        int remainder = blockSize % 4;
        //נתיב
        if (remainder != 0) {
            blockSize -= remainder;
        }
        
        int boardSize = N_BLOCKS * blockSize;
        
        int screenOffsetX = (width - boardSize) / 2;
        int screenOffsetY = (height - boardSize) / 2;

        g.setColor(Color.BLACK);
        g.fillRect(0, 0, width, height);

        Font smallMusicFont = new Font("SansSerif", Font.PLAIN, Math.max(10, blockSize / 2));
        
        Font largeMusicFont = new Font("SansSerif", Font.BOLD, Math.max(16, (int)(blockSize * 0.8)));

        for (int i = 0; i < N_BLOCKS * N_BLOCKS; i++) {
            
            int x = (i % N_BLOCKS) * blockSize + screenOffsetX;
            
            int y = (i / N_BLOCKS) * blockSize + screenOffsetY;
            // תו קטן
            if (levelData[i] == 1) {
                g.setColor(Color.WHITE);
                g.setFont(smallMusicFont);
                FontMetrics fm = g.getFontMetrics();
                
                String note = "\u266A";
                
                int textX = x + (blockSize - fm.stringWidth(note)) / 2;
                
                int textY = y + ((blockSize - fm.getHeight()) / 2) + fm.getAscent();
                
                g.drawString(note, textX, textY);
                //תו  גדול
            } else if (levelData[i] == 2) {
                g.setColor(Color.WHITE);
                g.setFont(largeMusicFont);
                FontMetrics fm = g.getFontMetrics();
                
                String note = "\u266B";
                
                int textX = x + (blockSize - fm.stringWidth(note)) / 2;
                int textY = y + ((blockSize - fm.getHeight()) / 2) + fm.getAscent();
                
                g.drawString(note, textX, textY);
                //בלוק
            } else if (levelData[i] == 3) {
                g.setColor(new Color(2, 2, 237));
                g.fillRect(x, y, blockSize, blockSize);
            }
        }
    }

    /**
     * Returns the current size of a single block on the board in pixels.
     * @return The block size.
     */
    public int getBlockSize() { return blockSize; }
    
    /**
     * Returns the number of blocks along one dimension of the square board (e.g., 19).
     * @return The number of blocks.
     */
    public int getNBlocks() { return N_BLOCKS; }
    
    /**
     * Returns the array representing the current state of the game level data.
     * @return A short array where each element represents a block's content (0=empty, 1=small item, 2=large item, 3=wall).
     */
    public short[] getLevelData() { return levelData; }
    
    /**
     * Marks an item at a specific index on the board as eaten by setting its value to 0 (empty).
     * @param index The index of the item in the levelData array to be eaten.
     */
    public void eatItem(int index) {
        if (index >= 0 && index < levelData.length) {
            levelData[index] = 0;
        }
    }

    /**
     * Checks if all edible items (small and large music notes) have been eaten on the board.
     * @return true if all items are eaten, false otherwise.
     */
    public boolean isAllItemsEaten() {
        for (int i = 0; i < levelData.length; i++) {
            if (levelData[i] == 1 || levelData[i] == 2) {
                return false;
            }
        }
        return true;
    }
}
