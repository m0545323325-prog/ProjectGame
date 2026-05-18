package com.whatsapp;

import javax.swing.JPanel;
import javax.swing.JButton;
import javax.swing.JTextField;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.event.ActionEvent; 
import java.awt.event.ActionListener; 
import java.awt.event.KeyAdapter; 
import java.awt.event.KeyEvent; 
import javax.swing.Timer; 

public class Game extends JPanel implements ActionListener { 

    private enum GameState {
        MENU, RULES, PLAYING
    }
    
    private GameState currentState = GameState.MENU; 
    private String playerName = "שחקן"; 

    private Board board; 
    private Pacman pacman; 
    private Ghost[] ghosts; 
    private Timer renderTimer; 
    private boolean isGameOver = false; 
    private boolean isGameWon = false; // New field for win condition

    private int currentBlockSize = 0; 
    
    private Thread pacmanThread;
    private Thread[] ghostThreads;
    
    private JButton replayButton; 
    private JTextField nameField;
    private JButton startMenuButton;
    private JButton rulesButton;
    private JButton backButton;

    public Game() { 
        initGame(); 
    }

    private void initGame() { 
        setLayout(null); 
        
        SoundManager.loadBackgroundMusic("music.wav");
        
        nameField = new JTextField("הכנס שם");
        nameField.setHorizontalAlignment(JTextField.CENTER);
        nameField.setFont(new Font("Arial", Font.BOLD, 20));
        add(nameField);

        startMenuButton = new JButton("התחל משחק");
        startMenuButton.setFont(new Font("Arial", Font.BOLD, 24));
        startMenuButton.setBackground(Color.YELLOW);
        startMenuButton.setForeground(Color.BLACK);
        startMenuButton.setFocusable(false);
        startMenuButton.addActionListener(e -> startGameFromMenu());
        add(startMenuButton);

        rulesButton = new JButton("חוקים והוראות");
        rulesButton.setFont(new Font("Arial", Font.BOLD, 18));
        rulesButton.setFocusable(false);
        rulesButton.addActionListener(e -> showRules());
        add(rulesButton);

        backButton = new JButton("חזור לתפריט");
        backButton.setFont(new Font("Arial", Font.BOLD, 18));
        backButton.setVisible(false);
        backButton.setFocusable(false);
        backButton.addActionListener(e -> backToMenu());
        add(backButton);

        replayButton = new JButton("שחק שוב");
        replayButton.setVisible(false); 
        replayButton.setFocusable(false); 
        replayButton.addActionListener(e -> restartGame()); 
        add(replayButton);
        
        addKeyListener(new TAdapter()); 
        setFocusable(true); 
        setBackground(Color.BLACK); 
        
        renderTimer = new Timer(16, this); 
    }

    public void startGame() { 
        renderTimer.start(); 
    }
    
    private void startGameFromMenu() {
        String inputName = nameField.getText().trim();
        if (!inputName.isEmpty() && !inputName.equals("הכנס שם")) {
            playerName = inputName;
        }
        
        nameField.setVisible(false);
        startMenuButton.setVisible(false);
        rulesButton.setVisible(false);
        
        currentState = GameState.PLAYING; 
        restartGame(); 
    }
    
    private void showRules() {
        currentState = GameState.RULES;
        nameField.setVisible(false);
        startMenuButton.setVisible(false);
        rulesButton.setVisible(false);
        backButton.setVisible(true);
    }
    
    private void backToMenu() {
        currentState = GameState.MENU;
        backButton.setVisible(false);
        nameField.setVisible(true);
        startMenuButton.setVisible(true);
        rulesButton.setVisible(true);
    }
    
    private void restartGame() {
        replayButton.setVisible(false);
        isGameOver = false;
        isGameWon = false; // Reset win condition
        
        stopThreads();
        
        board = new Board();
        
        int width = getWidth();
        int height = getHeight();
        int smallerDimension = Math.min(width, height);
        int blockSize = smallerDimension / board.getNBlocks();
        int remainder = blockSize % 4;
        if (remainder != 0) {
            blockSize -= remainder;
        }
        
        currentBlockSize = blockSize; 
        
        SoundManager.stopMusic();
        SoundManager.stopActiveSoundEffect();
        
        // מנסה לטעון מחדש את השמע בכל פעם שמתחילים, כך שאם יש שגיאה המשתמש יראה אותה שוב!
        SoundManager.loadBackgroundMusic("music.wav");

        pacman = new Pacman(blockSize * 9, blockSize * 15, board);
        ghosts = new Ghost[3]; 
        ghosts[0] = new Ghost(blockSize * 9, blockSize * 9, Color.RED, board, pacman); // (9,9) is a valid path
        ghosts[1] = new Ghost(blockSize * 9, blockSize * 8, Color.PINK, board, pacman); // (9,8) is a valid path
        ghosts[2] = new Ghost(blockSize * 9, blockSize * 7, Color.CYAN, board, pacman); // (9,7) is a valid path
        
        pacman.updateSize(blockSize); 
        for (Ghost ghost : ghosts) {
            ghost.updateSize(blockSize); 
        }
        
        startThreads();
        requestFocusInWindow(); 
    }

    private void updateCharacterPositions(int blockSize) { 
        if (pacman == null) return;
        
        int oldBlockSize = currentBlockSize;
        if (oldBlockSize > 0) { 
            pacman.setPosition((pacman.getX() / oldBlockSize) * blockSize, (pacman.getY() / oldBlockSize) * blockSize);
            for (Ghost ghost : ghosts) {
                ghost.setPosition((ghost.getX() / oldBlockSize) * blockSize, (ghost.getY() / oldBlockSize) * blockSize);
            }
        }
        
        pacman.updateSize(blockSize); 
        for (Ghost ghost : ghosts) {
            ghost.updateSize(blockSize); 
        }
        
        currentBlockSize = blockSize; 
    }
    
    private void startThreads() {
        pacmanThread = new Thread(pacman, "Pacman-Thread");
        pacmanThread.start();
        
        ghostThreads = new Thread[ghosts.length];
        for (int i = 0; i < ghosts.length; i++) {
            ghostThreads[i] = new Thread(ghosts[i], "Ghost-" + i + "-Thread");
            ghostThreads[i].start();
        }
    }
    
    private void stopThreads() {
        if (pacman != null) pacman.stopThread();
        if (ghosts != null) {
            for (Ghost ghost : ghosts) {
                if (ghost != null) ghost.stopThread();
            }
        }
    }

    @Override 
    public void paintComponent(Graphics g) { 
        super.paintComponent(g); 
        
        int width = getWidth(); 
        int height = getHeight(); 
        
        if (currentState == GameState.MENU) {
            drawMenu(g, width, height);
        } else if (currentState == GameState.RULES) {
            drawRules(g, width, height);
        } else if (currentState == GameState.PLAYING) {
            drawPlaying(g, width, height);
        }
    }
    
    private void drawMenu(Graphics g, int width, int height) {
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, width, height);
        
        g.setColor(Color.YELLOW);
        g.setFont(new Font("Courier New", Font.BOLD, 60));
        String title = "P A C M A N";
        FontMetrics fm = g.getFontMetrics();
        g.drawString(title, (width - fm.stringWidth(title)) / 2, height / 4);
        
        g.setColor(Color.YELLOW);
        g.fillArc((width / 2) - 100, (height / 4) + 40, 50, 50, 30, 300); 
        g.setColor(Color.RED);
        g.fillRect((width / 2) + 50, (height / 4) + 40, 50, 50); 
        g.fillOval((width / 2) + 50, (height / 4) + 25, 50, 50); 
        
        nameField.setBounds((width / 2) - 100, height / 2 - 30, 200, 40);
        startMenuButton.setBounds((width / 2) - 120, height / 2 + 40, 240, 60);
        rulesButton.setBounds((width / 2) - 100, height / 2 + 130, 200, 40);
    }
    
    private void drawRules(Graphics g, int width, int height) {
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, width, height);
        
        g.setColor(Color.YELLOW);
        g.setFont(new Font("Arial", Font.BOLD, 40));
        String title = "חוקים והוראות";
        FontMetrics fm = g.getFontMetrics();
        g.drawString(title, (width - fm.stringWidth(title)) / 2, 80);
        
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.PLAIN, 20));
        
        String[] rules = {
            "ברוך הבא למשחק פקמן המוזיקלי!",
            "",
            "1. השתמש בחיצים במקלדת כדי להזיז את פקמן:",
            "   [חץ למעלה] - תנועה למעלה",
            "   [חץ למטה]  - תנועה למטה",
            "   [חץ ימינה] - תנועה ימינה",
            "   [חץ שמאלה] - תנועה שמאלה",
            "",
            "2. המטרה: לאכול את כל תווי הנגינה שבמפה.",
            "   * תו קטן (♪) נותן נקודה 1.",
            "   * תו כוח גדול (♫) נותן 4 נקודות.",
            "",
            "3. היזהר מרוחות הרפאים! מגע בהן שווה לפסילה.",
            "4. מנהרות בצדדים מאפשרות להשתגר לצד השני."
        };
        
        int yOffset = 150;
        for (String rule : rules) {
            int strWidth = g.getFontMetrics().stringWidth(rule);
            g.drawString(rule, (width - strWidth) / 2, yOffset);
            yOffset += 35;
        }
        
        backButton.setBounds((width / 2) - 100, height - 100, 200, 50);
    }
    
    private void drawPlaying(Graphics g, int width, int height) {
        board.drawBoard(g, width, height); 
        
        int newBlockSize = board.getBlockSize(); 
        if (newBlockSize != currentBlockSize) { 
            updateCharacterPositions(newBlockSize); 
        }
        
        if (replayButton != null && isGameOver) {
            replayButton.setBounds(width / 2 - 60, height / 2 + 30, 120, 40);
        }

        if (pacman != null) { 
            int boardSize = board.getNBlocks() * newBlockSize;
            int screenOffsetX = (width - boardSize) / 2;
            int screenOffsetY = (height - boardSize) / 2;

            pacman.draw(g, newBlockSize, screenOffsetX, screenOffsetY); 
            for (Ghost ghost : ghosts) {
                ghost.draw(g, newBlockSize, screenOffsetX, screenOffsetY); 
            }
            
            g.setColor(Color.WHITE);
            g.setFont(new Font("Arial", Font.BOLD, 16));
            g.drawString("שחקן: " + playerName + " | Score: " + pacman.getScore(), 10, 20); 
        }
        
        if (isGameOver) {
            g.setColor(Color.WHITE); 
            g.setFont(new Font("Arial", Font.BOLD, 40)); 
            String msg;
            if (isGameWon) {
                msg = "כל הכבוד!"; // Well Done!
            } else {
                msg = "איזה לוזררר!!!"; // What a loser!!!
            }
            FontMetrics fm = g.getFontMetrics();
            int msgWidth = fm.stringWidth(msg);
            g.drawString(msg, (width - msgWidth) / 2, height / 2 - 20); 
        }
    }

    @Override 
    public void actionPerformed(ActionEvent e) { 
        if (currentState == GameState.PLAYING && pacman != null) { 
            checkCollisions(); 
        }
        repaint(); 
    }
    
    private void checkCollisions() { 
        if (isGameOver) return;
        
        for (Ghost ghost : ghosts) { 
            if (pacman.getBounds().intersects(ghost.getBounds())) { 
                isGameOver = true; 
                isGameWon = false; // Ensure game won is false if collided with ghost
                
                SoundManager.stopMusic(); 
                SoundManager.playSound("gameover.wav");

                stopThreads(); 
                replayButton.setVisible(true); 
                break; 
            }
        }

        // Check for win condition after Pacman has potentially eaten an item
        if (pacman != null && board.isAllItemsEaten()) {
            isGameOver = true;
            isGameWon = true;
            
            SoundManager.stopMusic();
            // Optionally play a win sound here
            // SoundManager.playSound("win.wav"); 

            stopThreads();
            replayButton.setVisible(true);
        }
    }

    private class TAdapter extends KeyAdapter { 
        @Override 
        public void keyPressed(KeyEvent e) { 
            if (currentState == GameState.PLAYING && pacman != null && !isGameOver) { 
                pacman.keyPressed(e);
            }
        }
    }
}
