import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.util.List;

public class GameView extends JPanel {
    private GameModel model;

    public GameView(GameModel model) {
        this.model = model;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        g.setColor(Color.BLACK);
        g.fillRect(0, 0, getWidth(), getHeight());

        drawPlayer(g);
        drawAliens(g);
        drawPlayerBullet(g);
        drawAlienBullets(g);
        drawHud(g);

        if (model.getLives() <= 0) {
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

    private void drawGameOver(Graphics g) {
        String message = "GAME OVER";
        Font font = new Font("Arial", Font.BOLD, 40);
        g.setFont(font);
        g.setColor(Color.WHITE);

        FontMetrics metrics = g.getFontMetrics(font);
        int x = (getWidth() - metrics.stringWidth(message)) / 2;
        int y = (getHeight() - metrics.getHeight()) / 2 + metrics.getAscent();

        g.drawString(message, x, y);
    }
}