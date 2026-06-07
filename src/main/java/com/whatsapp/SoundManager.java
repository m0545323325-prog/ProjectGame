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

    //רעש שאוכל תווים
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

    /**
     * Notifies the SoundManager that an item has been eaten.
     * This resets the timer for pausing background music and ensures music is playing.
     */
    public static void notifyEat() {
        lastEatTime = System.currentTimeMillis();
        
        if (backgroundMusic != null) {
            if (!isMusicPlaying) {
                isMusicPlaying = true;
                backgroundMusic.loop(Clip.LOOP_CONTINUOUSLY);
            }
        }
    }

    /**
     * Pauses the background music if it is currently playing.
     */
    public static void pauseMusic() {
        if (backgroundMusic != null && isMusicPlaying) {
            backgroundMusic.stop();
            isMusicPlaying = false;
        }
    }
    
    /**
     * Stops the background music and resets its position to the beginning.
     */
    public static void stopMusic() {
        if (backgroundMusic != null) {
            backgroundMusic.stop();
            backgroundMusic.setFramePosition(0); 
            isMusicPlaying = false;
        }
    }

    /**
     * Plays a one-shot sound effect from the specified file in a new thread.
     * Handles WAV file format conversion if necessary.
     * @param soundFileName The name of the sound effect file to play (e.g., "gameover.wav").
     */
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
                // Silently ignore side effect errors
            }
        }).start();
    }
    
    /**
     * Stops and closes the currently active sound effect, if any.
     */
    public static void stopActiveSoundEffect() {
        if (activeSoundEffect != null && activeSoundEffect.isRunning()) {
            activeSoundEffect.stop();
            activeSoundEffect.close();
            activeSoundEffect = null;
        }
    }
}
