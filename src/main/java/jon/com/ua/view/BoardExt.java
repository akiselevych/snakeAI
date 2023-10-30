package jon.com.ua.view;

import jon.com.ua.client.Board;
import jon.com.ua.client.Elements;
import jon.com.ua.view.snake.Snake;

import java.util.List;

public class BoardExt extends Board implements Cloneable {
    public static final int SIZE = 15;

    public BoardExt(int size) {
        super(size);
        clear();
    }

    public BoardExt() {
        super(SIZE);
        clear();
    }

    public void clear() {
        this.field = new char[1][this.size][this.size];
        for (int i = 0; i < this.field[0].length; i++) {
            for (int j = 0; j < this.field[0][i].length; j++) {
                this.field[0][i][j] = Elements.NONE.ch();
            }
        }
    }

    private static boolean isXOutOfBounds(int x, int width) {
        return x < 0 || x > width;
    }

    private static boolean isYOutOfBounds(int y, int height) {
        return y < 0 || y > height;
    }

    public static boolean isInBounds(BoardElement boardElement, int width, int height) {
        boolean isXInBounds = !isXOutOfBounds(boardElement.getX(), width);
        boolean isYInBounds = !isYOutOfBounds(boardElement.getY(), height);
        return isXInBounds && isYInBounds;
    }

    public void render(Snake snake, Apple apple, BadApple badApple, List<Wall> walls) {
        clearHigh();
        putWalls(walls);
        putApple(apple);
        putBadApple(badApple);
        putSnake(snake);
    }

    public void clearHigh() {
        for (int x = 0; x < size(); x++) {
            for (int y = 0; y < size(); y++) {
                set(x, y, Elements.NONE.ch());
            }
        }
    }

    public void putWalls(List<Wall> walls) {
        walls.forEach(w -> set(w.getX(), w.getY(), Elements.BREAK.ch()));
    }

    public void putApple(Apple apple) {
        set(apple.getX(), apple.getY(), Elements.GOOD_APPLE.ch());
    }

    public void putBadApple(BadApple badApple) {
        set(badApple.getX(), badApple.getY(), Elements.BAD_APPLE.ch());
    }

    public void putSnake(Snake snake) {
          for (BoardElement head : snake.getHeads()) {
            if ((snake.isHead(head))) {
                Elements headElement = snake.getHeadElement();
                set(head.getX(), head.getY(), headElement.ch());
            } else if (snake.isBodyWithoutHeadAndTail(head)) {
                Elements bodyElement = snake.getBody(head);
                set(head.getX(), head.getY(), bodyElement.ch());
            } else {
                Elements tail = snake.getTailLastElement();
                set(head.getX(), head.getY(), tail.ch());
            }
        }
    }

    @Override
    protected Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
}
