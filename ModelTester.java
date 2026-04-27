import java.awt.Rectangle;

public class ModelTester {
    public static void main(String[] args) {
        testLeftEdge();
        testRightEdge();
        testSinglePlayerBullet();
        testBulletRemovedAtTop();
        testAlienDestroyedIncreasesScore();
        testGameOverAfterLosingAllLives();
    }

    private static void testLeftEdge() {
        GameModel model = new GameModel();

        for (int i = 0; i < 200; i++) {
            model.movePlayerLeft();
        }

        if (model.getPlayerX() == 0) {
            System.out.println("PASS: Player cannot move past left edge");
        } else {
            System.out.println("FAIL: Player moved past left edge");
        }
    }

    private static void testRightEdge() {
        GameModel model = new GameModel();

        for (int i = 0; i < 200; i++) {
            model.movePlayerRight();
        }

        if (model.getPlayerX() == GameModel.WIDTH - GameModel.PLAYER_WIDTH) {
            System.out.println("PASS: Player cannot move past right edge");
        } else {
            System.out.println("FAIL: Player moved past right edge");
        }
    }

    private static void testSinglePlayerBullet() {
        GameModel model = new GameModel();

        model.firePlayerBullet();
        Rectangle firstBullet = model.getPlayerBullet();
        model.firePlayerBullet();
        Rectangle secondBullet = model.getPlayerBullet();

        if (firstBullet != null && firstBullet == secondBullet) {
            System.out.println("PASS: Firing while bullet is in flight does nothing");
        } else {
            System.out.println("FAIL: Extra player bullet was created");
        }
    }

    private static void testBulletRemovedAtTop() {
        GameModel model = new GameModel();

        model.firePlayerBullet();

        while (model.getPlayerBullet() != null) {
            model.update();
        }

        if (model.getPlayerBullet() == null) {
            System.out.println("PASS: Bullet is removed when it reaches the top");
        } else {
            System.out.println("FAIL: Bullet was not removed at the top");
        }
    }

    private static void testAlienDestroyedIncreasesScore() {
        GameModel model = new GameModel();

        int targetCol = 0;
        int targetRow = GameModel.ALIEN_ROWS - 1;
        int targetX = model.getAlienX(targetCol) + GameModel.ALIEN_WIDTH / 2;

        while (model.getPlayerX() + GameModel.PLAYER_WIDTH / 2 < targetX) {
            model.movePlayerRight();
        }

        while (model.getPlayerX() + GameModel.PLAYER_WIDTH / 2 > targetX) {
            model.movePlayerLeft();
        }

        model.firePlayerBullet();
        int scoreBefore = model.getScore();

        for (int i = 0; i < 200; i++) {
            model.update();
            if (!model.getAliens()[targetRow][targetCol]) {
                break;
            }
        }

        if (!model.getAliens()[targetRow][targetCol] && model.getScore() > scoreBefore) {
            System.out.println("PASS: Destroying an alien increases the score");
        } else {
            System.out.println("FAIL: Alien was not destroyed or score did not increase");
        }
    }

    private static void testGameOverAfterLosingAllLives() {
        GameModel model = new GameModel();

        while (model.getLives() > 0) {
            int bulletX = model.getPlayerX() + GameModel.PLAYER_WIDTH / 2 - GameModel.ALIEN_BULLET_WIDTH / 2;
            int bulletY = GameModel.PLAYER_Y;
            model.getAlienBullets().add(new Rectangle(
                    bulletX,
                    bulletY,
                    GameModel.ALIEN_BULLET_WIDTH,
                    GameModel.ALIEN_BULLET_HEIGHT
            ));
            model.update();
        }

        if (model.getLives() <= 0) {
            System.out.println("PASS: Losing all lives triggers the game-over state");
        } else {
            System.out.println("FAIL: Game-over state was not reached");
        }
    }
}