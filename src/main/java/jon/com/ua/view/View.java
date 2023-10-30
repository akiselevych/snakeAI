package jon.com.ua.view;

import com.codenjoy.dojo.services.Direction;
import jon.com.ua.client.Board;
import jon.com.ua.client.Elements;
import jon.com.ua.client.YourSolver;
import jon.com.ua.view.snake.Snake;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Stream;

import static jon.com.ua.view.BoardElement.realToScr;

/**
 * Created with IntelliJ IDEA.
 * User: al1
 * Date: 1/23/13
 */
public class View extends JPanel {
    public static final int CELL_SIZE = 30;
    public static final Color FIELD_COLOR = Color.LIGHT_GRAY;
    public static final Color WALL_COLOR = Color.GRAY;
    private static final int GAME_OVER_DELAY = 2000;
    public static final int SNAKE_LENGTH = 2;
    public static final int DECREASE_LENGTH = 10;
    public static int DELAY = 1000;

    public static boolean muteSound = true;
    public boolean isEditMode = false;
    public boolean isPaintSprites = true;
    public boolean isPaintPath = false;
    private String manualDirection;
    private BoardExt board;
    private List<Wall> walls = new ArrayList<>();
    private Apple apple;
    private BadApple badApple;
    private Snake snake;
    private final Random rnd = new Random();
    private String textOnCenter;
    private YourSolver solver;
    private int score;
    private boolean isPause;
    private PlaySound playSound;
    private int backFramePosition;
    private int framePosition;
    private int editX;
    private int editY;
    public static volatile int moveCounter;

    public View(JFrame main, YourSolver solver) {
        this.solver = solver;
        this.board = new BoardExt();
        createWalls();
        createApple();
        createBadApple();
        snake = new Snake(SNAKE_LENGTH, this.walls, this.apple, this.badApple, this.board);
        //new PlayBackground().start();
        if (PlayBackground.clip != null) {
            if (!muteSound) {
                startSound();
            } else {
                stopSound();
            }
        }
        main.addKeyListener(new KeyAdapter() {

            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_SPACE) {
                    isPause = !isPause;
                    if (!muteSound) {
                        if (PlayBackground.clip == null) {
                            return;
                        }
                        if (isPause) {
                            stopSound();
                        } else {
                            startSound();
                        }
                    }
                }
                if (DELAY > 50 && (e.getKeyCode() == KeyEvent.VK_PLUS || e.getKeyCode() == KeyEvent.VK_EQUALS)) {
                    DELAY -= 50;
                }
                if (DELAY <= 50 && DELAY > 5 && (e.getKeyCode() == KeyEvent.VK_PLUS || e.getKeyCode() == KeyEvent.VK_EQUALS)) {
                    DELAY -= 5;
                }
                if (DELAY < 6 && DELAY != 1 && (e.getKeyCode() == KeyEvent.VK_PLUS || e.getKeyCode() == KeyEvent.VK_EQUALS)) {
                    DELAY -= 1;
                }

                if (DELAY >= 50 && e.getKeyCode() == KeyEvent.VK_MINUS) {
                    DELAY += 50;
                }
                if (DELAY < 50 && DELAY >= 5 && e.getKeyCode() == KeyEvent.VK_MINUS) {
                    DELAY += 5;
                }
                if (DELAY < 5 && e.getKeyCode() == KeyEvent.VK_MINUS) {
                    DELAY += 1;
                }
                if (e.getKeyCode() == KeyEvent.VK_E) {
                    isEditMode = !isEditMode;
                    isPause = isEditMode;
                    if (!isEditMode) {
                        createApple();
                    }
                }
                if (e.getKeyCode() == KeyEvent.VK_S) {
                    isPaintSprites = !isPaintSprites;
                }
                if (e.getKeyCode() == KeyEvent.VK_P) {
                    isPaintPath = !isPaintPath;
                }
                if (e.getKeyCode() == KeyEvent.VK_M) {
                    muteSound = !muteSound;
                    if (!muteSound) {
                        startSound();
                    } else {
                        stopSound();
                    }
                }
                manualDirection = switch (e.getKeyCode()) {
                    case KeyEvent.VK_UP -> Direction.UP.toString();
                    case KeyEvent.VK_DOWN -> Direction.DOWN.toString();
                    case KeyEvent.VK_LEFT -> Direction.LEFT.toString();
                    case KeyEvent.VK_RIGHT -> Direction.RIGHT.toString();
                    default -> null;
                };
                if (e.getKeyCode() == KeyEvent.VK_UP) {
                    manualDirection = Direction.UP.toString();
                }
            }


        });
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                editX = e.getX();
                editY = e.getY();
                int y = realToScr((editY) / CELL_SIZE) - 1;
                int x = (editX) / CELL_SIZE;
                x = editX > 368 && editX < 392 ? 13 : x;
                y = editY > 12 && editY < 51 ? 13 : y;
                x = editX > 337 && editX < 367 ? 12 : x;
                y = editY > 52 && editY < 79 ? 12 : y;
                x = editX > 309 && editX < 336 ? 11 : x;
                y = editY > 80 && editY < 107 ? 11 : y;
                x = editX > 281 && editX < 308 ? 10 : x;
                y = editY > 108 && editY < 133 ? 10 : y;

                BoardElement boardElement = new BoardElement(null, null, x, y);
                if (!isWall(boardElement) && !snake.isBody(boardElement) && !badApple.itsMe(boardElement)) {
                    createApple(x, y);
                    isPause = false;
                }
            }
        });
        new Thread(() -> {
            while (!Thread.interrupted()) {
                if (isPause) {
                    continue;
                }

                setSnakeDirection();
                setSnakePathToTarget();
                boolean eated = snake.move();
                handleEatedApple(eated);
//                checkSnakeEatApple();
                checkSnakeEatBadApple();
                repaint();
                this.board.render(this.snake, this.apple, this.badApple, this.walls);
                System.out.println(this.board);
                View.this.sleep(DELAY);
                if (snake.isBittenItselfOrWall()) {
                    gameOver();
                }
                checkSnakeDead();
            }
        }).start();
    }

    private void handleEatedApple(boolean eated) {
        if (eated) {
            if (isEditMode) {
                isPause = true;
            } else {
                createApple();
            }
            increaseScore(this.snake.size());
            moveCounter = 0;
            // For disable event sound
            if (!muteSound) {
                playSound = new PlaySound();
                playSound.start();
            }
        }
    }

    private void startSound() {
        PlayBackground.clip.setFramePosition(backFramePosition);
        PlayBackground.clip.start();
        if (PlaySound.clip != null) {
            PlaySound.clip.setFramePosition(framePosition);
            PlaySound.clip.start();
        }
    }

    private void stopSound() {
        backFramePosition = PlayBackground.clip.getFramePosition();
        PlayBackground.clip.stop();
        if (PlaySound.clip != null) {
            framePosition = PlaySound.clip.getFramePosition();
            PlaySound.clip.stop();
        }
    }

    private void createWalls() {
        walls = new ArrayList<>();
        // Left
        for (int i = 0; i < BoardExt.SIZE; i++) {
            walls.add(new Wall(0, i, String.valueOf(i)));
        }
        // Right
        for (int i = 0; i < BoardExt.SIZE; i++) {
            walls.add(new Wall(BoardExt.SIZE - 1, i, ""));
        }
        // Down
        for (int i = 0; i < BoardExt.SIZE; i++) {
            walls.add(new Wall(i, BoardExt.SIZE - 1, String.valueOf(i)));
        }
        // Up
        for (int i = 0; i < BoardExt.SIZE; i++) {
            walls.add(new Wall(i, 0, ""));
        }
    }

    private void setSnakeDirection() {
        try {
            clearPrevPath();
            String directionName = Optional.ofNullable(
                    manualDirection == null ? solver.get((Board) board.clone()) : manualDirection
            ).orElse(Direction.LEFT.toString());
            manualDirection = null;
            Direction direction = Direction.valueOf(directionName);
            snake.setDirection(direction);
        } catch (IllegalArgumentException | CloneNotSupportedException e) {
            e.printStackTrace();
        }
    }

    private void clearPrevPath() {
        try {
            Field path = solver.getClass().getDeclaredField("path");
            path.setAccessible(true);
            path.set(this.solver, new ArrayList<>());
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    private void setSnakePathToTarget() {
        snake.setPath(solver.getPath());
    }

/*    private BoardElement checkSnakeEatApple() {
        BoardElement eatedApple = tryToEatAndGet(this.apple);
        if (eatedApple != null) {
            if (isEditMode) {
                isPause = true;
            } else {
                createApple();
            }
            increaseScore(this.snake.size());
            snake.grow();
            // For disable event sound
            if (!muteSound) {
                playSound = new PlaySound();
                playSound.start();
            }
        }
        return eatedApple;
    }*/

    private BoardElement checkSnakeEatBadApple() {
        BoardElement eatBadApple = tryToEatAndGet(this.badApple);
        if (eatBadApple != null) {
            createBadApple();
            if (this.snake.size() <= DECREASE_LENGTH) {
                gameOver();
                return eatBadApple;
            }
            snake.decrease(DECREASE_LENGTH);
            // For disable event sound
            if (!muteSound) {
                playSound = new PlaySound();
                playSound.start();
            }
        }
        return null;
    }

    private void increaseScore(int score) {
        this.score += score;
    }

    private BoardElement tryToEatAndGet(BoardElement target) {
        for (BoardElement head : snake.getHeads()) {
            if (target.getX() == head.getX() && target.getY() == head.getY()) {
                return target;
            }
        }
        return null;
    }

    private void createApple(int x, int y) {
        this.apple = new Apple(x, y);
    }

    private void createApple() {
        // TODO redevelop to place to only empty place
        do {
            int rndX = rnd.nextInt(board.size());
            int rndY = rnd.nextInt(board.size());
            if (this.apple == null) {
                this.apple = new Apple(rndX, rndY);
            }
            this.apple.setX(rndX);
            this.apple.setY(rndY);
//            this.apple = new Apple(rndX, rndY);
        } while ((snake != null && snake.isBody(this.apple)) || this.apple.itsMe(badApple) || isWall(this.apple));
    }

    private void createBadApple() {
        do {
            int rndX = rnd.nextInt(board.size());
            int rndY = rnd.nextInt(board.size());
            badApple = new BadApple(rndX, rndY);
        } while ((snake != null && snake.isBody(badApple)) || badApple.itsMe(apple) || isWall(badApple));
    }

    private boolean isWall(BoardElement boardElement) {
        return walls.stream().anyMatch(w -> w.itsMe(boardElement));
    }

    private void checkSnakeDead() {
        textOnCenter = null;
        BoardElement head = snake.getHeads().peekFirst();
        if (!BoardExt.isInBounds(head, board.size(), board.size())) {
            gameOver();
        }
    }

    private void gameOver() {
        textOnCenter = "Game over";
        repaint();
        sleep(GAME_OVER_DELAY);
        snake.create(SNAKE_LENGTH);
        createApple();
        createBadApple();
        manualDirection = null;
        board.set(board.getHead().getX(), board.getHead().getY(), Elements.NONE.ch());
    }

    private void sleep(int delay) {
        try {
            Thread.sleep(delay);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> initWindow(new YourSolver(null), null));
    }

    private static void initWindow(YourSolver solver, Board board) {
        JFrame main = new JFrame("Intellectual snake");
        main.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        final View view = new View(main, solver);

        main.setContentPane(view);
        int fieldSize = CELL_SIZE * BoardExt.SIZE;
        main.setBounds(10, 10, fieldSize - 13, fieldSize - 5);
        main.setVisible(true);
    }

    public static void runClient(YourSolver solver, Board board) {
        SwingUtilities.invokeLater(() -> initWindow(solver, board));
    }

    @Override
    public void paintComponent(Graphics graphics) {
        synchronized (View.class) {
            if (isPaintSprites) {
                paintSprites(graphics);
            } else {
                paintSimple(graphics);
            }
        }
    }

    private void paintSimple(Graphics graphics) {
        int cellHeight = getHeight() / board.size();
        int cellWidth = getWidth() / board.size();
        clearScreen(graphics, null);
        paintGrid(graphics, cellHeight, cellWidth);
        paintWalls(graphics, cellHeight, cellWidth, null);
        snake.paint(graphics, cellHeight, cellWidth, false);
        apple.paint(graphics, cellHeight, cellWidth, null);
        if (isPaintPath) {
            snake.paintPath(graphics, cellHeight, cellWidth, null);
        }
//        paintNewApplePlace(graphics, cellHeight, cellWidth);
        badApple.paint(graphics, cellHeight, cellWidth, null);
        paintCenterText(graphics);
        paintScore(graphics);
        paintDelay(graphics);
        paintHelp(graphics);
        paintMove(graphics);
        paintSize(graphics);
        if (snake.isGrow()) {
            paintPlusScore(graphics);
            snake.setGrow(false);
        }
    }

    private void paintSprites(Graphics graphics) {
        int cellHeight = getHeight() / board.size();
        int cellWidth = getWidth() / board.size();
        clearScreen(graphics, Elements.NONE);
//        paintGrid(graphics, cellHeight, cellWidth);
        paintWalls(graphics, cellHeight, cellWidth, Elements.BREAK);

        apple.paint(graphics, cellHeight, cellWidth, board.getAt(apple.getX(), apple.getY()));
        if (isPaintPath) {
            snake.paintPath(graphics, cellHeight, cellWidth, null);
        }
//        paintNewApplePlace(graphics, cellHeight, cellWidth);
        badApple.paint(graphics, cellHeight, cellWidth, board.getAt(badApple.getX(), badApple.getY()));
        paintScore(graphics);
        paintDelay(graphics);
        paintHelp(graphics);
        paintMove(graphics);
        paintSize(graphics);
        snake.paint(graphics, cellHeight, cellWidth, true);
        paintCenterText(graphics);
        if (snake.isGrow()) {
            paintPlusScore(graphics);
            snake.setGrow(false);
        }

    }

/*    private void paintNewApplePlace(Graphics graphics, int cellHeight, int cellWidth) {
        int x = editX;
        int y = editY;
        graphics.setColor(Color.BLACK);
        graphics.drawRect(x, y, 2, 2);

        Rectangle clipBounds = graphics.getClipBounds();
        int fontSize = 20;
        graphics.setFont(new Font("Arial", Font.PLAIN, fontSize));
        Color tmpColor = graphics.getColor();
        graphics.setColor(Color.BLACK);
//        graphics.drawString(x + ", " + y , 0, 200);
        int yCell = realToScr((editY) / CELL_SIZE) - 1;
        int xCell = (editX) / CELL_SIZE;
//        graphics.drawString(xCell + ", " + yCell , 0, 250);
        graphics.setColor(tmpColor);
    }*/

    private void paintWalls(Graphics graphics, int cellHeight, int cellWidth, Elements element) {
        for (Wall wall : this.walls) {
            wall.paint(graphics, cellHeight, cellWidth, element);
        }
    }

    private void paintScore(Graphics graphics) {
        Color tmpColor = graphics.getColor();
        int fontSize = 14;
        graphics.setFont(new Font("Arial", Font.BOLD, fontSize));
        graphics.setColor(Color.WHITE);
        graphics.drawString("Score: " + score, 10, 12);
        graphics.setColor(tmpColor);
    }

    private void paintDelay(Graphics graphics) {
        Color tmpColor = graphics.getColor();
        int fontSize = 14;
        graphics.setFont(new Font("Arial", Font.PLAIN, fontSize));
        graphics.setColor(Color.WHITE);
        graphics.drawString("Delay: " + DELAY, 10, 26);
        graphics.setColor(tmpColor);
    }

    private void paintSize(Graphics graphics) {
        Color tmpColor = graphics.getColor();
        int fontSize = 14;
        graphics.setFont(new Font("Arial", Font.PLAIN, fontSize));
        graphics.setColor(Color.WHITE);
        graphics.drawString("S:" + snake.size(), 80, 26);
        graphics.setColor(tmpColor);
    }

    private void paintHelp(Graphics graphics) {
        Color tmpColor = graphics.getColor();
        int fontSize = 14;
        graphics.setFont(new Font("Arial", Font.PLAIN, fontSize));
        graphics.setColor(Color.WHITE);
        graphics.drawString("Pause: space  |  Edit: e  |  Speed: +/-", 190, 12);
        graphics.drawString("Path: p | Graph: s | Control: arrows | Apple: click", 120, 26);
        graphics.setColor(tmpColor);
    }

    private void paintMove(Graphics graphics) {
        Color tmpColor = graphics.getColor();
        int fontSize = 14;
        graphics.setFont(new Font("Arial", Font.PLAIN, fontSize));
        graphics.setColor(Color.WHITE);
        graphics.drawString("Move:" + moveCounter, 120, 12);
        graphics.setColor(tmpColor);
    }

    private void paintCenterText(Graphics graphics) {
        if (this.textOnCenter == null) {
            return;
        }
        Rectangle clipBounds = graphics.getClipBounds();
        int fontSize = 30;
        graphics.setFont(new Font("Arial", Font.PLAIN, fontSize));
        Color tmpColor = graphics.getColor();
        graphics.setColor(Color.WHITE);
        graphics.drawString(textOnCenter, (int) (clipBounds.getWidth() / 2) - textOnCenter.length() / 2 * 20, (int) (clipBounds.getHeight() / 2));
        graphics.setColor(tmpColor);
    }

    private void paintPlusScore(Graphics graphics) {
        Rectangle clipBounds = graphics.getClipBounds();
        int fontSize = 50;
        graphics.setFont(new Font("Arial", Font.BOLD, fontSize));
        Color tmpColor = graphics.getColor();
        graphics.setColor(Color.RED);
        graphics.drawString("+" + snake.size(), (int) (clipBounds.getWidth() / 2), (int) (clipBounds.getHeight() / 2));
        graphics.setColor(tmpColor);
    }

    private void paintGrid(Graphics graphics, int cellHeight, int cellWidth) {
        int x = 0;
        int y = 0;
        graphics.setColor(Color.BLACK);
        for (int i = 0; i < board.size(); i++) {
            graphics.drawLine(0, y, getWidth(), y);
            y += cellHeight;
        }
        for (int i = 0; i < board.size(); i++) {
            graphics.drawLine(x, 0, x, getHeight());
            x += cellWidth;
        }
    }

    private void clearScreen(Graphics graphics, Elements elements) {
        if (elements != null) {
            Stream.concat(
                            board.getApples().stream(),
                            board.getBarriers().stream()
                    )
                    .map(BoardElement::of)
                    .forEach(e -> e.paintSprite(graphics, getWidth(), getHeight(), elements));
        } else {
            graphics.setColor(FIELD_COLOR);
            graphics.fill3DRect(0, 0, getWidth(), getHeight(), true);
        }
    }
}
