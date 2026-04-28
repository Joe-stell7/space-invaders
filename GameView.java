import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.util.List;

public class GameView extends JPanel {
    private GameModel model;
    private boolean paused;

    public GameView(GameModel model) {
        this.model = model;
        this.paused = false;
    }

    public void setPaused(boolean paused) {
        this.paused = paused;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        g.setColor(Color.BLACK);
        g.fillRect(0, 0, getWidth(), getHeight());

        drawPlayer(g);
        drawAliens(g);
        drawShields(g);
        drawPlayerBullet(g);
        drawAlienBullets(g);
        drawHud(g);

        if (paused && !model.isGameOver()) {
            drawPaused(g);
        }

        if (model.isGameOver()) {
            drawGameOver(g);
        }
    }

    private void drawPlayer(Graphics g) {
        g.setColor(Color.GREEN);
        g.fillRect(
                model.getPlayerX(),
                GameModel.PLAYER_Y,
                GameModel.PLAYER_WIDTH,
                GameModel.PLAYER_HEIGHT
        );
    }

    private void drawAliens(Graphics g) {
        boolean[][] aliens = model.getAliens();
        g.setColor(Color.RED);

        for (int row = 0; row < GameModel.ALIEN_ROWS; row++) {
            for (int col = 0; col < GameModel.ALIEN_COLS; col++) {
                if (aliens[row][col]) {
                    g.fillRect(
                            model.getAlienX(col),
                            model.getAlienY(row),
                            GameModel.ALIEN_WIDTH,
                            GameModel.ALIEN_HEIGHT
                    );
                }
            }
        }
    }

    private void drawShields(Graphics g) {
        boolean[][][] shields = model.getShields();
        g.setColor(Color.CYAN);

        for (int shield = 0; shield < GameModel.SHIELD_COUNT; shield++) {
            for (int row = 0; row < GameModel.SHIELD_ROWS; row++) {
                for (int col = 0; col < GameModel.SHIELD_COLS; col++) {
                    if (shields[shield][row][col]) {
                        Rectangle block = model.getShieldBlockBounds(shield, row, col);
                        g.fillRect(block.x, block.y, block.width, block.height);
                    }
                }
            }
        }
    }

    private void drawPlayerBullet(Graphics g) {
        Rectangle bullet = model.getPlayerBullet();
        if (bullet != null) {
            g.setColor(Color.WHITE);
            g.fillRect(bullet.x, bullet.y, bullet.width, bullet.height);
        }
    }

    private void drawAlienBullets(Graphics g) {
        List<Rectangle> bullets = model.getAlienBullets();
        g.setColor(Color.YELLOW);

        for (Rectangle bullet : bullets) {
            g.fillRect(bullet.x, bullet.y, bullet.width, bullet.height);
        }
    }

    private void drawHud(Graphics g) {
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 20));
        g.drawString("Score: " + model.getScore(), 20, 30);
        g.drawString("Lives: " + model.getLives(), 680, 30);
    }

    private void drawPaused(Graphics g) {
        String message = "PAUSED";
        Font font = new Font("Arial", Font.BOLD, 40);

        g.setColor(Color.WHITE);
        g.setFont(font);

        FontMetrics metrics = g.getFontMetrics(font);
        int x = (getWidth() - metrics.stringWidth(message)) / 2;
        int y = getHeight() / 2;

        g.drawString(message, x, y);
    }

    private void drawGameOver(Graphics g) {
        String message1 = "GAME OVER";
        String message2 = "Press R to Restart";

        Font font1 = new Font("Arial", Font.BOLD, 40);
        Font font2 = new Font("Arial", Font.PLAIN, 24);

        g.setColor(Color.WHITE);

        g.setFont(font1);
        FontMetrics metrics1 = g.getFontMetrics(font1);
        int x1 = (getWidth() - metrics1.stringWidth(message1)) / 2;
        int y1 = getHeight() / 2 - 20;
        g.drawString(message1, x1, y1);

        g.setFont(font2);
        FontMetrics metrics2 = g.getFontMetrics(font2);
        int x2 = (getWidth() - metrics2.stringWidth(message2)) / 2;
        int y2 = y1 + 40;
        g.drawString(message2, x2, y2);
    }
}