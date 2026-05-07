package com.whatsapp;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import java.io.File;

public class SoundManager {

    // הפונקציה מוגדרת כ-static כדי שנוכל לקרוא לה מכל מקום בלי ליצור אובייקט
    public static void playSound(String soundFileName) {
        // אנחנו מריצים את הצליל בתהליכון (Thread) נפרד 
        // כדי שההמתנה לטעינת הקובץ לא תתקע את המשחק או תגרום ל"קפיצות" (Lag)
        new Thread(() -> {
            try {
                // מחפש את הקובץ בתיקיית ה-resources
                File soundFile = new File("src/main/resources/" + soundFileName);
                
                if (soundFile.exists()) {
                    AudioInputStream audioIn = AudioSystem.getAudioInputStream(soundFile);
                    Clip clip = AudioSystem.getClip();
                    clip.open(audioIn);
                    clip.start(); // מתחיל לנגן
                } else {
                    // אם הקובץ לא נמצא, רק נדפיס אזהרה קטנה לקונסולה
                    System.out.println("Sound file not found: " + soundFileName);
                }
            } catch (Exception e) {
                System.out.println("Error playing sound: " + e.getMessage());
            }
        }).start();
    }
}
