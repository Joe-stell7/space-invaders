import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

public class GameController implements KeyListener {
    private GameModel model;
    private GameView view;
    private JFrame frame;
    private Timer timer;

    public GameController() {
        model = new GameModel();
        view = new GameView(model);
    }

    private void startGame() {
        frame = new JFrame("Space Invaders");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(view);
        frame.setSize(GameModel.WIDTH, GameModel.HEIGHT);
        frame.setLocationRelativeTo(null);
        frame.addKeyListener(this);
        frame.setVisible(true);

        timer = new Timer(16, e -> {
            if (model.getLives() > 0) {
                model.update();
                view.repaint();
            } else {
                ((Timer) e.getSource()).stop();
                view.repaint();
            }
        });

        timer.start();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            GameController controller = new GameController();
            controller.startGame();
        });
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (model.getLives() <= 0) {
            return;
        }

        int key = e.getKeyCode();

        if (key == KeyEvent.VK_LEFT) {
            model.movePlayerLeft();
        } else if (key == KeyEvent.VK_RIGHT) {
            model.movePlayerRight();
        } else if (key == KeyEvent.VK_SPACE) {
            model.firePlayerBullet();
        }

        view.repaint();
    }

    @Override
    public void keyReleased(KeyEvent e) {
    }

    @Override
    public void keyTyped(KeyEvent e) {
    }
}