package com.whatsapp;

import javax.swing.JFrame;

public class Main {


    public static void main(String[] args) {
        
        JFrame frame = new JFrame("Pacman");
        
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        frame.setSize(600, 800);
        
        frame.setResizable(true);
        
        Game game = new Game();
        
        frame.add(game);
        
        frame.setLocationRelativeTo(null);
        
        frame.setVisible(true);
        
        game.startGame();
    }
}
