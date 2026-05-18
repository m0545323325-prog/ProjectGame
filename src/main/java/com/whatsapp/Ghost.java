package com.whatsapp;

import java.awt.Color; 
import java.awt.Graphics; 
import java.awt.Image; 
import javax.imageio.ImageIO; 
import java.io.File;
import java.util.ArrayList; 
import java.util.List; 
import java.util.Random; 

public class Ghost extends Character implements Runnable { 

    private final Color color; 
    private final Random random = new Random(); 
    private final Board board; 
    private final Pacman pacman; 
    private volatile boolean running = true; 
    
    private Image ghostImage;

    public Ghost(int x, int y, Color color, Board board, Pacman pacman) { 
        super(x, y); 
        this.color = color; 
        this.board = board; 
        this.pacman = pacman; 
        
        try {
            ghostImage = ImageIO.read(new File("src/main/resources/ghost.jpg"));
        } catch (Exception e) {
            ghostImage = null; 
            System.out.println("Error loading ghost.jpg: " + e.getMessage()); // Debug print
        }
        
        // Initialize dx, dy to 0. The move method will determine the first actual direction.
        this.dx = 0;
        this.dy = 0;
    }

    @Override
    public void run() {
        while (running) {
            move(board); 
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                running = false;
            }
        }
    }
    
    public void stopThread() {
        running = false;
    }

    @Override 
    public void draw(Graphics g, int size, int screenOffsetX, int screenOffsetY) { 
        int drawX = x + screenOffsetX;
        int drawY = y + screenOffsetY;
        
        if (ghostImage != null) {
            g.drawImage(ghostImage, drawX, drawY, size, size, null);
        } else {
            g.setColor(color); 
            g.fillRect(drawX, drawY, size, size); 
            
            g.setColor(Color.WHITE);
            g.fillOval(drawX + size / 5, drawY + size / 4, size / 4, size / 4); 
            g.fillOval(drawX + size / 2, drawY + size / 4, size / 4, size / 4); 
            
            g.setColor(Color.BLACK);
            g.fillOval(drawX + size / 4, drawY + size / 3, size / 6, size / 6); 
            g.fillOval(drawX + size / 2 + size/10, drawY + size / 3, size / 6, size / 6);
        }
    }
    
    @Override 
    public void move(Board board) { 
        if (board.getBlockSize() == 0) return;

        if (x % board.getBlockSize() == 0 && y % board.getBlockSize() == 0) {
            int currentXBlock = x / board.getBlockSize();
            int currentYBlock = y / board.getBlockSize();
            int nBlocks = board.getNBlocks();
            short[] levelData = board.getLevelData();

            int pacmanXBlock = pacman.getX() / board.getBlockSize();
            int pacmanYBlock = pacman.getY() / board.getBlockSize();

            if (currentYBlock >= 0 && currentYBlock < nBlocks && currentXBlock >= 0 && currentXBlock < nBlocks) {

                List<int[]> validMoves = new ArrayList<>(); // All moves that are not walls
                List<Double> validDistances = new ArrayList<>();

                // Check right
                if (currentXBlock < nBlocks - 1 && levelData[currentYBlock * nBlocks + currentXBlock + 1] != 3) {
                    validMoves.add(new int[]{speed, 0});
                    validDistances.add(calculateManhattanDistance(currentXBlock + 1, currentYBlock, pacmanXBlock, pacmanYBlock));
                }
                // Check left
                if (currentXBlock > 0 && levelData[currentYBlock * nBlocks + currentXBlock - 1] != 3) {
                    validMoves.add(new int[]{-speed, 0});
                    validDistances.add(calculateManhattanDistance(currentXBlock - 1, currentYBlock, pacmanXBlock, pacmanYBlock));
                }
                // Check down
                if (currentYBlock < nBlocks - 1 && levelData[(currentYBlock + 1) * nBlocks + currentXBlock] != 3) {
                    validMoves.add(new int[]{0, speed});
                    validDistances.add(calculateManhattanDistance(currentXBlock, currentYBlock + 1, pacmanXBlock, pacmanYBlock));
                }
                // Check up
                if (currentYBlock > 0 && levelData[(currentYBlock - 1) * nBlocks + currentXBlock] != 3) {
                    validMoves.add(new int[]{0, -speed});
                    validDistances.add(calculateManhattanDistance(currentXBlock, currentYBlock - 1, pacmanXBlock, pacmanYBlock));
                }

                List<int[]> possibleMoves = new ArrayList<>(); // Moves that are not reversing
                List<Double> distances = new ArrayList<>();

                boolean isInitialMove = (dx == 0 && dy == 0);

                if (isInitialMove) {
                    // For the very first move, any valid direction is possible.
                    possibleMoves.addAll(validMoves);
                    distances.addAll(validDistances);
                } else {
                    // For subsequent moves, filter out direct reversals.
                    for (int i = 0; i < validMoves.size(); i++) {
                        int[] move = validMoves.get(i);
                        // Check if this move is a direct reversal of the current direction
                        if (!((move[0] == -dx && move[1] == -dy) || (move[0] == dx && move[1] == dy && dx == 0 && dy == 0))) {
                            possibleMoves.add(move);
                            distances.add(validDistances.get(i));
                        }
                    }

                    // If all non-reversing moves are blocked, then reversing is the only option.
                    if (possibleMoves.isEmpty() && !validMoves.isEmpty()) {
                        // Find the reverse move in validMoves and add it.
                        for (int i = 0; i < validMoves.size(); i++) {
                            int[] move = validMoves.get(i);
                            if (move[0] == -dx && move[1] == -dy) {
                                possibleMoves.add(move);
                                distances.add(validDistances.get(i));
                                break; // Only one reverse move possible
                            }
                        }
                    }
                }

                if (!possibleMoves.isEmpty()) {
                    double minDistance = Double.MAX_VALUE;
                    for (Double dist : distances) {
                        if (dist < minDistance) {
                            minDistance = dist;
                        }
                    }

                    List<int[]> bestMoves = new ArrayList<>();
                    for (int i = 0; i < possibleMoves.size(); i++) {
                        if (distances.get(i) == minDistance) {
                            bestMoves.add(possibleMoves.get(i));
                        }
                    }

                    int[] chosenMove = bestMoves.get(random.nextInt(bestMoves.size()));

                    dx = chosenMove[0];
                    dy = chosenMove[1];

                } else {
                    // This case should ideally not be reached if the maze is valid and not a dead end.
                    // If it is reached, it means the ghost is completely trapped or has no valid moves.
                    // Reversing current direction as a last resort.
                    dx = -dx;
                    dy = -dy;
                }
            } else {
                 // Ghost is somehow outside the board boundaries, reverse direction.
                 dx = -dx;
                 dy = -dy;
            }
        }
        
        performMove(); 
    }

    private double calculateManhattanDistance(int ghostX, int ghostY, int pacmanX, int pacmanY) {
        return Math.abs(ghostX - pacmanX) + Math.abs(ghostY - pacmanY);
    }
}
