import javax.swing.JFrame;
import javax.swing.SwingUtilities;

public class GameController {
    private GameModel model;
    private GameView view;

    public GameController() {
        model = new GameModel();
        view = new GameView();
    }

    private void startGame() {
        JFrame frame = new JFrame("Space Invaders");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(view);
        frame.setSize(800, 600);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            GameController controller = new GameController();
            controller.startGame();
        });
    }
}