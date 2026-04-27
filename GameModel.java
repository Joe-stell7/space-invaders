import java.awt.Rectangle;
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
    public static final int ALIEN_MOVE_STEP = 10;
    public static final int ALIEN_DROP_STEP = 20;

    public static final int PLAYER_BULLET_WIDTH = 4;
    public static final int PLAYER_BULLET_HEIGHT = 12;
    public static final int PLAYER_BULLET_SPEED = 12;

    public static final int ALIEN_BULLET_WIDTH = 4;
    public static final int ALIEN_BULLET_HEIGHT = 12;
    public static final int ALIEN_BULLET_SPEED = 6;

    private int playerX;
    private boolean[][] aliens;
    private int alienOffsetX;
    private int alienOffsetY;
    private int alienDirection;

    private Rectangle playerBullet;
    private List<Rectangle> alienBullets;

    private int score;
    private int lives;

    private Random random;
    private int tickCount;

    public GameModel() {
        random = new Random();
        alienBullets = new ArrayList<>();
        resetGame();
    }

    public void resetGame() {
        playerX = (WIDTH - PLAYER_WIDTH) / 2;

        aliens = new boolean[ALIEN_ROWS][ALIEN_COLS];
        for (int row = 0; row < ALIEN_ROWS; row++) {
            for (int col = 0; col < ALIEN_COLS; col++) {
                aliens[row][col] = true;
            }
        }

        alienOffsetX = 0;
        alienOffsetY = 0;
        alienDirection = 1;

        playerBullet = null;
        alienBullets.clear();

        score = 0;
        lives = 3;
        tickCount = 0;
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
        checkPlayerBulletAlienCollisions();
        checkAlienBulletPlayerCollisions();
        checkAliensReachedPlayer();
        checkWaveCleared();
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
        if (shouldMoveAliensDown()) {
            alienOffsetY += ALIEN_DROP_STEP;
            alienDirection *= -1;
        } else {
            alienOffsetX += alienDirection * ALIEN_MOVE_STEP;
        }
    }

    private boolean shouldMoveAliensDown() {
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

        return (alienDirection > 0 && rightmost + ALIEN_MOVE_STEP >= WIDTH)
                || (alienDirection < 0 && leftmost - ALIEN_MOVE_STEP <= 0);
    }

    private void fireAlienBulletRandomly() {
        if (tickCount % 20 != 0) {
            return;
        }

        if (random.nextInt(100) >= 35) {
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

        resetAliensWave();
    }

    private void resetAliensWave() {
        for (int row = 0; row < ALIEN_ROWS; row++) {
            for (int col = 0; col < ALIEN_COLS; col++) {
                aliens[row][col] = true;
            }
        }

        alienOffsetX = 0;
        alienOffsetY = 0;
        alienDirection = 1;
        playerBullet = null;
        alienBullets.clear();
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

    public int getScore() {
        return score;
    }

    public int getLives() {
        return lives;
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