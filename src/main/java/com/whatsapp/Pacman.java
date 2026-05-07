package com.whatsapp; // חבילת הפרויקט

import java.awt.Color; // שימוש בצבעים (צהוב לפקמן)
import java.awt.Graphics; // מאפשר פונקציות ציור כמו fillArc
import java.awt.event.KeyEvent; // שימוש בקליטת כפתורי מקלדת

// המחלקה פקמן שיורשת מ-Character (כלומר היא חייבת לממש את move ו-draw שלה)
// בנוסף, היא מממשת את "Runnable" - מוסכמה ב-Java שאומרת: "המחלקה הזו יכולה לרוץ כתהליכון (Thread) עצמאי במעבד".
public class Pacman extends Character implements Runnable { 

    // dx ו-dy שמורים בקלט של המשתמש. req = Requested (מבוקש).
    // הפקמן שומר במשתנים אלו לאן השחקן *רוצה* לפנות (למשל, לחץ חץ למעלה אז req_dy יהיה מינוס מהירות).
    private volatile int req_dx, req_dy; 
    
    // מופע של הלוח שעליו הפקמן משחק, נשמר ב-final כדי שלא נוכל למחוק אותו בטעות בהמשך.
    private final Board board; 
    
    // בוליאני שאומר ללולאת ה-Thread שלנו להמשיך להסתובב ולהתקיים. ברגע שייהפך ל-false, ה-Thread ימות.
    private volatile boolean running = true; 
    
    // משתנה הניקוד, נשמר ב-volatile כי הטרד של ציור המסך (שקורא לו) רץ במקביל לטרד של פקמן (שמשנה לו את הערך).
    private volatile int score = 0; 
    
    // משתנה חדש ששומר את זווית הציור (מאיזו מעלה להתחיל לצייר את הפה הפתוח של פקמן).
    // ברירת מחדל: 30 מעלות (פונה ימינה).
    private volatile int viewAngle = 30; 

    // הבנאי (מייצר את אובייקט הפקמן בהתחלה). מעביר את ה-X וה-Y אל מחלקת האב דרך פקודת "super".
    public Pacman(int x, int y, Board board) { 
        super(x, y); 
        this.board = board; // שמירת מופע הלוח לשליטה עצמאית
        this.req_dx = 0; // ברירת מחדל לאן השחקן רוצה ללכת: לשום מקום.
        this.req_dy = 0; 
    }

    // פונקציית החובה מתוך ממשק Runnable! כאן הלוגיקה החיה העצמאית של פקמן רצה.
    @Override
    public void run() {
        while (running) { // כל עוד המשחק לא אמר לו לעצור...
            move(board); // הוא קורא לפונקציית התזוזה שלו ומעביר את הלוח (כדי לבדוק התנגשויות)
            
            try {
                Thread.sleep(40); // אחרי כל "פעימה" של תזוזה, התהליכון "נרדם" ל-40 מילישניות (נותן 25 פעימות בשניה). זה מונע קריסת מחשב מרוב חישובים.
            } catch (InterruptedException e) {
                // אם תקלה אילצה את הטרד להתעורר, נכבה אותו בצורה יזומה.
                running = false;
            }
        }
    }
    
    // פונקציה מסודרת לכיבוי בלולאת הטרד מבחוץ (בלי הפונקציה האלימה הישנה של Java שנקראה Thread.stop()).
    public void stopThread() {
        running = false;
    }

    // הפונקציה לקריאת הניקוד (מופעלת למשל מפאנל ה-Game כדי לצייר למעלה "Score: X").
    public int getScore() {
        return score;
    }

    // דורס (Override) את פונקציית הציור הריקה של Character.
    @Override 
    public void draw(Graphics g, int size, int screenOffsetX, int screenOffsetY) { 
        // מחשב את ה-X וה-Y הסופיים לציור על גבי ה-JPanel, תוך הוספת המרכוז הנדרש.
        int drawX = x + screenOffsetX; 
        int drawY = y + screenOffsetY; 
        
        g.setColor(Color.YELLOW); // צבע פקמן הקלאסי
        
        // מצייר צורת קשת מלאה (Pacman). 
        // אורך הקשת הוא תמיד 300 מעלות (מה שמשאיר "פה" פתוח של 60 מעלות).
        // הזווית ההתחלתית נקבעת על ידי המשתנה viewAngle שמשתנה כשהפקמן זז!
        g.fillArc(drawX, drawY, size, size, viewAngle, 300); 
    }

    // מתודת התנועה העיקרית והמורכבת של הפקמן, שמוודאת התנגשויות ואכילת נתונים (נמצאת ב-Override).
    @Override 
    public void move(Board board) { 
        int blockSize = board.getBlockSize(); // שואל את הלוח מה גודל משבצת כעת
        if (blockSize == 0) return; // הגנה אם הלוח טרם אותחל

        // התנאי החשוב ביותר במשחק! פקמן בודק שינוי כיוון *רק* כשהוא עומד בדיוק בצומת של הלוח! (ללא שארית בחלוקה בגודל הבלוק)
        if (x % blockSize == 0 && y % blockSize == 0) {
            
            // ממיר את המיקום בפיקסלים למיקום "לוגי" של אינדקס טבלה (עמודה ושורה במערך 19x19).
            int currentXBlock = x / blockSize; // איזה עמודה אני?
            int currentYBlock = y / blockSize; // איזה שורה אני?
            int nBlocks = board.getNBlocks(); // שואף 19 מהלוח
            short[] levelData = board.getLevelData(); // מקבל את המפה הפיזית עם המספרים 0 עד 3
            
            // מחשב באיזה תא ממשי (במערך בעל מימד 1) פקמן נמצא עכשיו: הנוסחה לשטח דו ממדי בחד-ממדי.
            int currentBlockIndex = currentYBlock * nBlocks + currentXBlock;

            // מוודא (בדיקת גבולות בטוחה) שאנחנו בתוך המערך כדי שלא נקבל OutOfBoundsException.
            if (currentYBlock >= 0 && currentYBlock < nBlocks && currentXBlock >= 0 && currentXBlock < nBlocks) {
                
                // === לוגיקת "אכילת" התווים ===
                if (levelData[currentBlockIndex] == 1) { // 1 אומר שיש פה במשבצת עכשיו תו רגיל (נקודה)
                    board.eatItem(currentBlockIndex); // אומר ללוח: "תחליף את המשבצת הזו ל-0, אכלתי אותה!"
                    score += 1; // מוסיף 1 לניקוד שלי
                    SoundManager.notifyEat(); // מודיע לצליל הרקע שאכלתי כעת, אז המנגינה תמשיך להתנגן ולא תיעצר
                } else if (levelData[currentBlockIndex] == 2) { // 2 אומר שיש פה במשבצת תו גדול במיוחד
                    board.eatItem(currentBlockIndex); // מחליף ל-0
                    score += 4; // תו גדול שווה 4 נקודות
                    SoundManager.notifyEat(); // מנגינת הרקע נשמרת פעילה!
                }

                // === בדיקה האם פנייה חדשה אפשרית ===
                // המערכת בודקת האם הכיוון שהשחקן ביקש (דרך המקלדת, ע"י שינוי req_dx, req_dy) הוא בכלל חוקי ואין קיר.
                boolean requestedMoveIsPossible = false;
                
                if (req_dx < 0) { // המשתמש לחץ פנייה שמאלה (מינוס ב-X)
                    // אם זה צד שמאל על שורה 9 - זו מנהרה! אפשר לעבור! אחרת - אם אין קיר (3) בשמאל, מותר לעבור.
                    if (currentYBlock == 9 && currentXBlock == 0) requestedMoveIsPossible = true; 
                    else if (currentXBlock > 0 && levelData[currentYBlock * nBlocks + currentXBlock - 1] != 3) requestedMoveIsPossible = true;
                
                } else if (req_dx > 0) { // פנייה ימינה
                    // אם זה במנהרה מותר, או אם אין קיר בימין, מותר.
                    if (currentYBlock == 9 && currentXBlock == nBlocks - 1) requestedMoveIsPossible = true; 
                    else if (currentXBlock < nBlocks - 1 && levelData[currentYBlock * nBlocks + currentXBlock + 1] != 3) requestedMoveIsPossible = true;
                
                } else if (req_dy < 0) { // פנייה למעלה
                    if (currentYBlock > 0 && levelData[(currentYBlock - 1) * nBlocks + currentXBlock] != 3) requestedMoveIsPossible = true;
                
                } else if (req_dy > 0) { // פנייה למטה
                    if (currentYBlock < nBlocks - 1 && levelData[(currentYBlock + 1) * nBlocks + currentXBlock] != 3) requestedMoveIsPossible = true;
                }

                // אם הכיוון החדש שנלחץ חוקי וריק - אנחנו מכניסים אותו להווה ולתנועה האקטיבית (dx, dy)!
                if (requestedMoveIsPossible) {
                    dx = req_dx;
                    dy = req_dy;
                    
                    // עדכון זווית הפה של פקמן לפי הכיוון האקטיבי החדש שלו
                    if (dx > 0) {
                        viewAngle = 30; // ימינה: מתחיל מ-30 מעלות
                    } else if (dx < 0) {
                        viewAngle = 210; // שמאלה: 180 + 30
                    } else if (dy > 0) {
                        viewAngle = 300; // למטה: 270 + 30
                    } else if (dy < 0) {
                        viewAngle = 120; // למעלה: 90 + 30
                    }
                }

                // === בדיקה האם התנועה הנוכחית מתנגשת בקיר במקרה ===
                // למה צריך שוב? בגלל שפקמן ממשיך לנוע ישר אם לא נותנים לו הוראה חדשה. עלינו לעצור אותו כשהוא מגיע לקיר.
                boolean currentDirectionLeadsToWall = false;
                
                if (dx < 0) { // אם אנחנו בתנועה אקטיבית שמאלה
                    // אם זה לא מנהרה, ובמשבצת אחת שמאלה מאיתנו יש ערך 3 (קיר)
                    if (!(currentYBlock == 9 && currentXBlock == 0) && (currentXBlock == 0 || levelData[currentYBlock * nBlocks + currentXBlock - 1] == 3)) {
                        currentDirectionLeadsToWall = true; // נתקענו!
                    }
                } else if (dx > 0) { // אם זזים אקטיבית ימינה
                    if (!(currentYBlock == 9 && currentXBlock == nBlocks - 1) && (currentXBlock == nBlocks - 1 || levelData[currentYBlock * nBlocks + currentXBlock + 1] == 3)) {
                        currentDirectionLeadsToWall = true; // נתקענו בקיר מימין!
                    }
                } else if (dy < 0) { // זזים למעלה
                    if (currentYBlock == 0 || levelData[(currentYBlock - 1) * nBlocks + currentXBlock] == 3) {
                        currentDirectionLeadsToWall = true; // קיר מלמעלה!
                    }
                } else if (dy > 0) { // זזים למטה
                    if (currentYBlock == nBlocks - 1 || levelData[(currentYBlock + 1) * nBlocks + currentXBlock] == 3) {
                        currentDirectionLeadsToWall = true; // קיר מלמטה!
                    }
                }

                // אם אכן פקמן הולך לקיר בכיוונו הנוכחי – בולמים אותו באפס (0), כך שהוא ממתין מול הקיר לפעולת המשתמש.
                if (currentDirectionLeadsToWall) {
                    dx = 0;
                    dy = 0;
                }
            }
        }

        // אחרי כל החישובים, תזיז את הדמות פיזית (מחבר x עם dx, ומחבר y עם dy בפונקציית האב).
        performMove();

        // === לוגיקת שיגור של מנהרה (Tunnel Wrap-Around) ===
        int nBlocks = board.getNBlocks();
        // רק אם הגענו במדויק לשורת המנהרה האופקית (אינדקס 9)...
        if (y == 9 * blockSize) {
            // אם יצאנו לחלוטין מגבולות המסך השמאלי...
            if (x < -size) { 
                x = nBlocks * blockSize; // משגרים לחלק הימני ביותר האפשרי!
            } 
            // ואם יצאנו מהחלק הימני...
            else if (x > nBlocks * blockSize) { 
                x = -size; // משגרים אל מתחת לאפס בצד שמאל!
            }
        }
    }

    // מתודה הפועלת כאשר לחצו על החיצים במקלדת (נקראת על ידי מתודת ההאזנה שב-Game.java)
    public void keyPressed(KeyEvent e) { 
        int key = e.getKeyCode(); // קבלת הקוד המספרי של המקש הנלחץ במקלדת

        if (key == KeyEvent.VK_LEFT) { // אם נלחץ חץ שמאל
            req_dx = -speed; // המהירות האופקית המבוקשת הופכת לשלילית (שמאלה)
            req_dy = 0; // ביטול מהירות אנכית כדי שלא נזוז באלכסון (אסור בפקמן)
        } else if (key == KeyEvent.VK_RIGHT) { // חץ ימינה
            req_dx = speed; 
            req_dy = 0;
        } else if (key == KeyEvent.VK_UP) { // חץ למעלה
            req_dx = 0;
            req_dy = -speed; // המהירות האנכית שלילית כי 0 ב-Y זה למעלה במסכים! (למטה זה מספר גדל).
        } else if (key == KeyEvent.VK_DOWN) { // חץ למטה
            req_dx = 0;
            req_dy = speed; // Y חיובי = תנועה למטה
        }
    }
}
