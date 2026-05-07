package com.whatsapp;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.swing.JOptionPane;
import java.io.File;

public class SoundManager {

    private static Clip backgroundMusic;
    private static Clip activeSoundEffect;
    
    private static volatile long lastEatTime = 0;
    private static volatile boolean isMusicPlaying = false;
    
    private static Thread monitorThread;

    public static void loadBackgroundMusic(String fileName) {
        try {
            File soundFile = new File("src/main/resources/" + fileName);
            if (!soundFile.exists()) {
                JOptionPane.showMessageDialog(null, "הקובץ " + fileName + " לא נמצא בתיקיית resources!\nנא לוודא ששם הקובץ תקין.", "שגיאת קובץ חסר", JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            try {
                AudioInputStream audioIn = AudioSystem.getAudioInputStream(soundFile);
                backgroundMusic = AudioSystem.getClip();
                backgroundMusic.open(audioIn);
                
            } catch (Exception directEx) {
                try {
                    AudioInputStream sourceStream = AudioSystem.getAudioInputStream(soundFile);
                    AudioFormat baseFormat = sourceStream.getFormat();
                    AudioFormat decodedFormat = new AudioFormat(
                            AudioFormat.Encoding.PCM_SIGNED,
                            baseFormat.getSampleRate(),
                            16,
                            baseFormat.getChannels(),
                            baseFormat.getChannels() * 2,
                            baseFormat.getSampleRate(),
                            false
                    );
                    AudioInputStream decodedAudioIn = AudioSystem.getAudioInputStream(decodedFormat, sourceStream);
                    backgroundMusic = AudioSystem.getClip();
                    backgroundMusic.open(decodedAudioIn);
                    
                } catch (Exception conversionEx) {
                    JOptionPane.showMessageDialog(null, "קובץ השמע " + fileName + " פגום או לא נתמך על ידי Java!\nנסה להמיר אותו מחדש באתר אינטרנט לפורמט WAV של 16-bit.\n" + directEx.getMessage(), "שגיאת שמע", JOptionPane.ERROR_MESSAGE);
                    return;
                }
            }
            
            if (monitorThread == null) {
                monitorThread = new Thread(() -> {
                    while (true) {
                        try {
                            Thread.sleep(50);
                            // הגדלתי את זמן ההמתנה ל-1500 מילישניות (שנייה וחצי)
                            if (isMusicPlaying && System.currentTimeMillis() - lastEatTime > 1500) {
                                pauseMusic();
                            }
                        } catch (InterruptedException e) {
                            break;
                        }
                    }
                });
                monitorThread.setDaemon(true);
                monitorThread.start();
            }
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void notifyEat() {
        lastEatTime = System.currentTimeMillis();
        
        if (backgroundMusic != null) {
            if (!isMusicPlaying) {
                isMusicPlaying = true;
                // הפונקציה loop כבר מתחילה את הניגון בעצמה, אין צורך לקרוא גם ל-start()
                // שגרם כנראה להתנגשויות בתוך מנוע השמע של Java.
                backgroundMusic.loop(Clip.LOOP_CONTINUOUSLY);
            }
        }
    }

    public static void pauseMusic() {
        if (backgroundMusic != null && isMusicPlaying) {
            backgroundMusic.stop(); // עושה Pause
            isMusicPlaying = false;
        }
    }
    
    public static void stopMusic() {
        if (backgroundMusic != null) {
            backgroundMusic.stop();
            backgroundMusic.setFramePosition(0); 
            isMusicPlaying = false;
        }
    }

    public static void playSound(String soundFileName) {
        new Thread(() -> {
            try {
                File soundFile = new File("src/main/resources/" + soundFileName);
                if (soundFile.exists()) {
                    try {
                        AudioInputStream audioIn = AudioSystem.getAudioInputStream(soundFile);
                        Clip clip = AudioSystem.getClip();
                        clip.open(audioIn);
                        activeSoundEffect = clip;
                        clip.start();
                    } catch (Exception e) {
                        AudioInputStream sourceStream = AudioSystem.getAudioInputStream(soundFile);
                        AudioFormat baseFormat = sourceStream.getFormat();
                        AudioFormat decodedFormat = new AudioFormat(
                                AudioFormat.Encoding.PCM_SIGNED,
                                baseFormat.getSampleRate(), 16, baseFormat.getChannels(),
                                baseFormat.getChannels() * 2, baseFormat.getSampleRate(), false);
                        AudioInputStream decodedAudioIn = AudioSystem.getAudioInputStream(decodedFormat, sourceStream);
                        Clip clip = AudioSystem.getClip();
                        clip.open(decodedAudioIn);
                        activeSoundEffect = clip;
                        clip.start();
                    }
                }
            } catch (Exception e) {
                // מתעלם בשקט משגיאות של אפקטים צדדיים
            }
        }).start();
    }
    
    public static void stopActiveSoundEffect() {
        if (activeSoundEffect != null && activeSoundEffect.isRunning()) {
            activeSoundEffect.stop();
            activeSoundEffect.close();
            activeSoundEffect = null;
        }
    }
}
