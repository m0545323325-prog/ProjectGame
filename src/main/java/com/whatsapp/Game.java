package com.whatsapp;

import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import javax.swing.Timer;

public class Game extends JPanel implements ActionListener {

    private Board board;
    private Pacman pacman;
    private Ghost[] ghosts;
    private Timer timer;
    private boolean inGame = false;

    public Game() {
        initGame();
    }

    private void initGame() {
        board = new Board();
        pacman = new Pacman(board.getBlockSize() * 9, board.getBlockSize() * 15);
        
        ghosts = new Ghost[4];
        ghosts[0] = new Ghost(board.getBlockSize() * 9, board.getBlockSize() * 9, Color.RED);
        ghosts[1] = new Ghost(board.getBlockSize() * 9, board.getBlockSize() * 10, Color.PINK);
        ghosts[2] = new Ghost(board.getBlockSize() * 8, board.getBlockSize() * 10, Color.CYAN);
        ghosts[3] = new Ghost(board.getBlockSize() * 10, board.getBlockSize() * 10, Color.ORANGE);

        addKeyListener(new TAdapter());
        setFocusable(true);
        setBackground(Color.BLACK);
        
        timer = new Timer(40, this);
    }

    public void startGame() {
        inGame = true;
        timer.start();
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        
        board.drawBoard(g);
        pacman.draw(g);
        for (Ghost ghost : ghosts) {
            ghost.draw(g);
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (inGame) {
            pacman.move();
            for (Ghost ghost : ghosts) {
                ghost.move(board);
            }
            checkCollisions();
            repaint();
        }
    }
    
    private void checkCollisions() {
        // Check collision with ghosts
        for (Ghost ghost : ghosts) {
            if (pacman.getBounds().intersects(ghost.getBounds())) {
                inGame = false;
                timer.stop();
            }
        }
    }

    private class TAdapter extends KeyAdapter {
        @Override
        public void keyPressed(KeyEvent e) {
            pacman.keyPressed(e);
        }
    }
}
