package com.whatsapp;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.KeyEvent;

public class Pacman extends Character implements Runnable { 

    private volatile int req_dx, req_dy; 
    
    private final Board board; 
    
    private volatile boolean running = true; 
    
    private volatile int score = 0; 
    
    private volatile int viewAngle = 30; 


    public Pacman(int x, int y, Board board) { 
        super(x, y); 
        this.board = board; 
        this.req_dx = 0; 
        this.req_dy = 0; 
    }


     //מזיזה את פקמן באופן רציף ועוצרת לזמן קצר.
    @Override
    public void run() {
        while (running) {
            move(board); 
            
            try {
                Thread.sleep(40);
            } catch (InterruptedException e) {
                running = false;
            }
        }
    }
    
    //עוצר תפקמן אחרי שמת
    public void stopThread() {
        running = false;
    }

    // מחזירה את הניקוד הנוכחי של פקמן.

    public int getScore() {
        return score;
    }

    /**
      מציירת את פקמן על המסך.
     */
    @Override 
    public void draw(Graphics g, int size, int screenOffsetX, int screenOffsetY) { 
        int drawX = x + screenOffsetX; 
        int drawY = y + screenOffsetY; 
        
        g.setColor(Color.YELLOW); 
        
        g.fillArc(drawX, drawY, size, size, viewAngle, 300);
    }

    /**
     *לוגיקת התנועה של פקמן, כולל בדיקת התנגשויות עם קירות, אכילת פריטים ושיגור דרך מנהרות.
     */
    @Override 
    public void move(Board board) { 
        int blockSize = board.getBlockSize(); 
        if (blockSize == 0) return; 

        if (x % blockSize == 0 && y % blockSize == 0) {
            
            int currentXBlock = x / blockSize; 
            int currentYBlock = y / blockSize; 
            int nBlocks = board.getNBlocks(); 
            short[] levelData = board.getLevelData(); 
            
            int currentBlockIndex = currentYBlock * nBlocks + currentXBlock;

            if (currentYBlock >= 0 && currentYBlock < nBlocks && currentXBlock >= 0 && currentXBlock < nBlocks) {
                
                // לוגיקת אכילת פריטים
                if (levelData[currentBlockIndex] == 1) { 
                    board.eatItem(currentBlockIndex); 
                    score += 1; 
                    SoundManager.notifyEat(); 
                } else if (levelData[currentBlockIndex] == 2) { 
                    board.eatItem(currentBlockIndex); 
                    score += 4; 
                    SoundManager.notifyEat(); 
                }

                // בדיקה אם תנועה מבוקשת אפשרית
                boolean requestedMoveIsPossible = false;
                
                if (req_dx < 0) { 
                    if (currentYBlock == 9 && currentXBlock == 0) requestedMoveIsPossible = true; 
                    else if (currentXBlock > 0 && levelData[currentYBlock * nBlocks + currentXBlock - 1] != 3) requestedMoveIsPossible = true;
                
                } else if (req_dx > 0) { 
                    if (currentYBlock == 9 && currentXBlock == nBlocks - 1) requestedMoveIsPossible = true; 
                    else if (currentXBlock < nBlocks - 1 && levelData[currentYBlock * nBlocks + currentXBlock + 1] != 3) requestedMoveIsPossible = true;
                
                } else if (req_dy < 0) { 
                    if (currentYBlock > 0 && levelData[(currentYBlock - 1) * nBlocks + currentXBlock] != 3) requestedMoveIsPossible = true;
                
                } else if (req_dy > 0) { 
                    if (currentYBlock < nBlocks - 1 && levelData[(currentYBlock + 1) * nBlocks + currentXBlock] != 3) requestedMoveIsPossible = true;
                }

                // ציור התנועה
                if (requestedMoveIsPossible) {
                    dx = req_dx;
                    dy = req_dy;
                    
                    if (dx > 0) {
                        viewAngle = 30; 
                    } else if (dx < 0) {
                        viewAngle = 210; 
                    } else if (dy > 0) {
                        viewAngle = 300; 
                    } else if (dy < 0) {
                        viewAngle = 120; 
                    }
                }

                // בדיקה אם הכיוון הנוכחי מוביל לקיר
                boolean currentDirectionLeadsToWall = false;
                
                if (dx < 0) { 
                    if (!(currentYBlock == 9 && currentXBlock == 0) && (currentXBlock == 0 || levelData[currentYBlock * nBlocks + currentXBlock - 1] == 3)) {
                        currentDirectionLeadsToWall = true; 
                    }
                } else if (dx > 0) { 
                    if (!(currentYBlock == 9 && currentXBlock == nBlocks - 1) && (currentXBlock == nBlocks - 1 || levelData[currentYBlock * nBlocks + currentXBlock + 1] == 3)) {
                        currentDirectionLeadsToWall = true; 
                    }
                } else if (dy < 0) { 
                    if (currentYBlock == 0 || levelData[(currentYBlock - 1) * nBlocks + currentXBlock] == 3) {
                        currentDirectionLeadsToWall = true; 
                    }
                } else if (dy > 0) { 
                    if (currentYBlock == nBlocks - 1 || levelData[(currentYBlock + 1) * nBlocks + currentXBlock] == 3) {
                        currentDirectionLeadsToWall = true; 
                    }
                }

                // אם הכיוון הנוכחי מוביל לקיר, עצור את פקמן.
                if (currentDirectionLeadsToWall) {
                    dx = 0;
                    dy = 0;
                }
            }
        }

        performMove();

        // לוגיקת שיגור מנהרה
        int nBlocks = board.getNBlocks();
        if (y == 9 * blockSize) {
            if (x < -size) { 
                x = nBlocks * blockSize; 
            } 
            else if (x > nBlocks * blockSize) { 
                x = -size; 
            }
        }
    }

    /**
     * מטפלת בלחיצות מקשים של המשתמש כדי לשנות את הכיוון המבוקש של פקמן.
     */
    public void keyPressed(KeyEvent e) { 
        int key = e.getKeyCode(); 

        if (key == KeyEvent.VK_LEFT) { 
            req_dx = -speed; 
            req_dy = 0; 
        } else if (key == KeyEvent.VK_RIGHT) { 
            req_dx = speed; 
            req_dy = 0;
        } else if (key == KeyEvent.VK_UP) { 
            req_dx = 0;
            req_dy = -speed; 
        } else if (key == KeyEvent.VK_DOWN) { 
            req_dx = 0;
            req_dy = speed;
        }
    }
}
