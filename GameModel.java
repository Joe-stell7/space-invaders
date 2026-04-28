import java.awt.Rectangle;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class GameModel {
    public static final int WIDTH = 800;
    public static final int HEIGHT = 600;

    public static final int PLAYER_WIDTH = 60;
    public static final int PLAYER_HEIGHT = 20;
    public static final int PLAYER_Y = 540;
    public static final int PLAYER_SPEED = 12;

    public static final int ALIEN_ROWS = 5;
    public static final int ALIEN_COLS = 11;
    public static final int ALIEN_WIDTH = 40;
    public static final int ALIEN_HEIGHT = 25;
    public static final int ALIEN_H_GAP = 15;
    public static final int ALIEN_V_GAP = 12;
    public static final int ALIEN_START_X = 80;
    public static final int ALIEN_START_Y = 60;
    public static final int ALIEN_DROP_STEP = 20;

    public static final int PLAYER_BULLET_WIDTH = 4;
    public static final int PLAYER_BULLET_HEIGHT = 12;
    public static final int PLAYER_BULLET_SPEED = 12;

    public static final int ALIEN_BULLET_WIDTH = 4;
    public static final int ALIEN_BULLET_HEIGHT = 12;
    public static final int ALIEN_BULLET_SPEED = 6;

    public static final int SHIELD_COUNT = 4;
    public static final int SHIELD_BLOCK_SIZE = 8;
    public static final int SHIELD_ROWS = 4;
    public static final int SHIELD_COLS = 6;
    public static final int SHIELD_START_Y = 430;

    private static final String HIGH_SCORE_FILE = "highscore.txt";

    private int playerX;
    private boolean[][] aliens;
    private int alienOffsetX;
    private int alienOffsetY;
    private int alienDirection;

    private Rectangle playerBullet;
    private List<Rectangle> alienBullets;

    private boolean[][][] shields;

    private int score;
    private int lives;
    private int level;
    private int highScore;

    private Random random;
    private int tickCount;

    public GameModel() {
        random = new Random();
        alienBullets = new ArrayList<>();
        loadHighScore();
        resetGame();
    }

    public void resetGame() {
        score = 0;
        lives = 3;
        level = 1;
        tickCount = 0;
        playerX = (WIDTH - PLAYER_WIDTH) / 2;
        playerBullet = null;
        alienBullets.clear();
        resetAliensWave();
    }

    public void movePlayerLeft() {
        playerX -= PLAYER_SPEED;
        if (playerX < 0) {
            playerX = 0;
        }
    }

    public void movePlayerRight() {
        playerX += PLAYER_SPEED;
        if (playerX > WIDTH - PLAYER_WIDTH) {
            playerX = WIDTH - PLAYER_WIDTH;
        }
    }

    public void firePlayerBullet() {
        if (playerBullet == null && lives > 0) {
            int bulletX = playerX + PLAYER_WIDTH / 2 - PLAYER_BULLET_WIDTH / 2;
            int bulletY = PLAYER_Y;
            playerBullet = new Rectangle(bulletX, bulletY, PLAYER_BULLET_WIDTH, PLAYER_BULLET_HEIGHT);
        }
    }

    public void update() {
        if (lives <= 0) {
            return;
        }

        tickCount++;
        movePlayerBullet();
        moveAliens();
        fireAlienBulletRandomly();
        moveAlienBullets();
        checkPlayerBulletShieldCollisions();
        checkAlienBulletShieldCollisions();
        checkPlayerBulletAlienCollisions();
        checkAlienBulletPlayerCollisions();
        checkAliensReachedPlayer();
        checkWaveCleared();
    }

    private void initializeShields() {
        shields = new boolean[SHIELD_COUNT][SHIELD_ROWS][SHIELD_COLS];

        for (int shield = 0; shield < SHIELD_COUNT; shield++) {
            for (int row = 0; row < SHIELD_ROWS; row++) {
                for (int col = 0; col < SHIELD_COLS; col++) {
                    shields[shield][row][col] = true;
                }
            }
        }
    }

    private int getAlienMoveStep() {
        return 10 + (level - 1) * 2;
    }

    private int getAlienFireChance() {
        return Math.min(35 + (level - 1) * 5, 80);
    }

    private void movePlayerBullet() {
        if (playerBullet != null) {
            playerBullet.y -= PLAYER_BULLET_SPEED;
            if (playerBullet.y + playerBullet.height < 0) {
                playerBullet = null;
            }
        }
    }

    private void moveAliens() {
        int moveStep = getAlienMoveStep();

        if (shouldMoveAliensDown(moveStep)) {
            alienOffsetY += ALIEN_DROP_STEP;
            alienDirection *= -1;
        } else {
            alienOffsetX += alienDirection * moveStep;
        }
    }

    private boolean shouldMoveAliensDown(int moveStep) {
        int leftmost = Integer.MAX_VALUE;
        int rightmost = Integer.MIN_VALUE;

        for (int row = 0; row < ALIEN_ROWS; row++) {
            for (int col = 0; col < ALIEN_COLS; col++) {
                if (aliens[row][col]) {
                    int x = getAlienX(col);
                    if (x < leftmost) {
                        leftmost = x;
                    }
                    if (x + ALIEN_WIDTH > rightmost) {
                        rightmost = x + ALIEN_WIDTH;
                    }
                }
            }
        }

        if (leftmost == Integer.MAX_VALUE) {
            return false;
        }

        return (alienDirection > 0 && rightmost + moveStep >= WIDTH)
                || (alienDirection < 0 && leftmost - moveStep <= 0);
    }

    private void fireAlienBulletRandomly() {
        if (tickCount % 20 != 0) {
            return;
        }

        if (random.nextInt(100) >= getAlienFireChance()) {
            return;
        }

        List<Integer> availableColumns = new ArrayList<>();
        for (int col = 0; col < ALIEN_COLS; col++) {
            if (getBottomAliveAlienRow(col) != -1) {
                availableColumns.add(col);
            }
        }

        if (availableColumns.isEmpty()) {
            return;
        }

        int chosenCol = availableColumns.get(random.nextInt(availableColumns.size()));
        int chosenRow = getBottomAliveAlienRow(chosenCol);

        int bulletX = getAlienX(chosenCol) + ALIEN_WIDTH / 2 - ALIEN_BULLET_WIDTH / 2;
        int bulletY = getAlienY(chosenRow) + ALIEN_HEIGHT;

        alienBullets.add(new Rectangle(bulletX, bulletY, ALIEN_BULLET_WIDTH, ALIEN_BULLET_HEIGHT));
    }

    private int getBottomAliveAlienRow(int col) {
        for (int row = ALIEN_ROWS - 1; row >= 0; row--) {
            if (aliens[row][col]) {
                return row;
            }
        }
        return -1;
    }

    private void moveAlienBullets() {
        for (int i = alienBullets.size() - 1; i >= 0; i--) {
            Rectangle bullet = alienBullets.get(i);
            bullet.y += ALIEN_BULLET_SPEED;
            if (bullet.y > HEIGHT) {
                alienBullets.remove(i);
            }
        }
    }

    private void checkPlayerBulletShieldCollisions() {
        if (playerBullet == null) {
            return;
        }

        for (int shield = 0; shield < SHIELD_COUNT; shield++) {
            for (int row = 0; row < SHIELD_ROWS; row++) {
                for (int col = 0; col < SHIELD_COLS; col++) {
                    if (shields[shield][row][col]) {
                        Rectangle block = getShieldBlockBounds(shield, row, col);
                        if (playerBullet.intersects(block)) {
                            shields[shield][row][col] = false;
                            playerBullet = null;
                            return;
                        }
                    }
                }
            }
        }
    }

    private void checkAlienBulletShieldCollisions() {
        for (int i = alienBullets.size() - 1; i >= 0; i--) {
            Rectangle bullet = alienBullets.get(i);

            for (int shield = 0; shield < SHIELD_COUNT; shield++) {
                boolean hit = false;

                for (int row = 0; row < SHIELD_ROWS; row++) {
                    for (int col = 0; col < SHIELD_COLS; col++) {
                        if (shields[shield][row][col]) {
                            Rectangle block = getShieldBlockBounds(shield, row, col);
                            if (bullet.intersects(block)) {
                                shields[shield][row][col] = false;
                                alienBullets.remove(i);
                                hit = true;
                                break;
                            }
                        }
                    }
                    if (hit) {
                        break;
                    }
                }

                if (hit) {
                    break;
                }
            }
        }
    }

    private void checkPlayerBulletAlienCollisions() {
        if (playerBullet == null) {
            return;
        }

        for (int row = 0; row < ALIEN_ROWS; row++) {
            for (int col = 0; col < ALIEN_COLS; col++) {
                if (aliens[row][col]) {
                    Rectangle alienRect = new Rectangle(getAlienX(col), getAlienY(row), ALIEN_WIDTH, ALIEN_HEIGHT);
                    if (playerBullet.intersects(alienRect)) {
                        aliens[row][col] = false;
                        playerBullet = null;
                        score += 10;
                        updateHighScoreIfNeeded();
                        return;
                    }
                }
            }
        }
    }

    private void checkAlienBulletPlayerCollisions() {
        Rectangle playerRect = new Rectangle(playerX, PLAYER_Y, PLAYER_WIDTH, PLAYER_HEIGHT);

        for (int i = alienBullets.size() - 1; i >= 0; i--) {
            Rectangle bullet = alienBullets.get(i);
            if (bullet.intersects(playerRect)) {
                alienBullets.remove(i);
                lives--;
                if (lives < 0) {
                    lives = 0;
                }
            }
        }
    }

    private void checkAliensReachedPlayer() {
        for (int row = 0; row < ALIEN_ROWS; row++) {
            for (int col = 0; col < ALIEN_COLS; col++) {
                if (aliens[row][col]) {
                    int alienBottom = getAlienY(row) + ALIEN_HEIGHT;
                    if (alienBottom >= PLAYER_Y) {
                        lives = 0;
                        return;
                    }
                }
            }
        }
    }

    private void checkWaveCleared() {
        for (int row = 0; row < ALIEN_ROWS; row++) {
            for (int col = 0; col < ALIEN_COLS; col++) {
                if (aliens[row][col]) {
                    return;
                }
            }
        }

        level++;
        resetAliensWave();
    }

    private void resetAliensWave() {
        aliens = new boolean[ALIEN_ROWS][ALIEN_COLS];
        for (int row = 0; row < ALIEN_ROWS; row++) {
            for (int col = 0; col < ALIEN_COLS; col++) {
                aliens[row][col] = true;
            }
        }

        initializeShields();

        alienOffsetX = 0;
        alienOffsetY = 0;
        alienDirection = 1;
        playerBullet = null;
        alienBullets.clear();
    }

    public void updateHighScoreIfNeeded() {
        if (score > highScore) {
            highScore = score;
        }
    }

    public void saveHighScore() {
        updateHighScoreIfNeeded();

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(HIGH_SCORE_FILE))) {
            writer.write(String.valueOf(highScore));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void loadHighScore() {
        try (BufferedReader reader = new BufferedReader(new FileReader(HIGH_SCORE_FILE))) {
            String line = reader.readLine();
            if (line != null) {
                highScore = Integer.parseInt(line.trim());
            } else {
                highScore = 0;
            }
        } catch (IOException | NumberFormatException e) {
            highScore = 0;
        }
    }

    public Rectangle getShieldBlockBounds(int shieldIndex, int row, int col) {
        int totalShieldWidth = SHIELD_COLS * SHIELD_BLOCK_SIZE;
        int spacing = (WIDTH - SHIELD_COUNT * totalShieldWidth) / (SHIELD_COUNT + 1);
        int shieldX = spacing + shieldIndex * (totalShieldWidth + spacing);
        int x = shieldX + col * SHIELD_BLOCK_SIZE;
        int y = SHIELD_START_Y + row * SHIELD_BLOCK_SIZE;

        return new Rectangle(x, y, SHIELD_BLOCK_SIZE, SHIELD_BLOCK_SIZE);
    }

    public int getPlayerX() {
        return playerX;
    }

    public boolean[][] getAliens() {
        return aliens;
    }

    public Rectangle getPlayerBullet() {
        return playerBullet;
    }

    public List<Rectangle> getAlienBullets() {
        return alienBullets;
    }

    public boolean[][][] getShields() {
        return shields;
    }

    public int getScore() {
        return score;
    }

    public int getLives() {
        return lives;
    }

    public int getLevel() {
        return level;
    }

    public int getHighScore() {
        return highScore;
    }

    public boolean isGameOver() {
        return lives <= 0;
    }

    public int getAlienX(int col) {
        return ALIEN_START_X + col * (ALIEN_WIDTH + ALIEN_H_GAP) + alienOffsetX;
    }

    public int getAlienY(int row) {
        return ALIEN_START_Y + row * (ALIEN_HEIGHT + ALIEN_V_GAP) + alienOffsetY;
    }
}