package jon.com.ua.view.snake;

import com.codenjoy.dojo.services.Direction;
import com.codenjoy.dojo.services.Point;
import jon.com.ua.client.Elements;
import jon.com.ua.view.Apple;
import jon.com.ua.view.BadApple;
import jon.com.ua.view.BoardElement;
import jon.com.ua.view.BoardExt;
import jon.com.ua.view.View;
import jon.com.ua.view.Wall;

import java.awt.*;
import java.util.LinkedList;
import java.util.List;

import static com.codenjoy.dojo.services.Direction.DOWN;
import static com.codenjoy.dojo.services.Direction.LEFT;
import static com.codenjoy.dojo.services.Direction.RIGHT;
import static com.codenjoy.dojo.services.Direction.UP;
import static jon.com.ua.client.Elements.HEAD_DOWN;
import static jon.com.ua.client.Elements.HEAD_LEFT;
import static jon.com.ua.client.Elements.HEAD_RIGHT;
import static jon.com.ua.client.Elements.HEAD_UP;
import static jon.com.ua.client.Elements.NONE;
import static jon.com.ua.client.Elements.TAIL_END_DOWN;
import static jon.com.ua.client.Elements.TAIL_END_LEFT;
import static jon.com.ua.client.Elements.TAIL_END_RIGHT;
import static jon.com.ua.client.Elements.TAIL_END_UP;
import static jon.com.ua.client.Elements.TAIL_HORIZONTAL;
import static jon.com.ua.client.Elements.TAIL_LEFT_DOWN;
import static jon.com.ua.client.Elements.TAIL_LEFT_UP;
import static jon.com.ua.client.Elements.TAIL_RIGHT_DOWN;
import static jon.com.ua.client.Elements.TAIL_RIGHT_UP;
import static jon.com.ua.client.Elements.TAIL_VERTICAL;
import static jon.com.ua.view.snake.BodyDirection.HORIZONTAL;
import static jon.com.ua.view.snake.BodyDirection.TURNED_LEFT_DOWN;
import static jon.com.ua.view.snake.BodyDirection.TURNED_LEFT_UP;
import static jon.com.ua.view.snake.BodyDirection.TURNED_RIGHT_DOWN;
import static jon.com.ua.view.snake.BodyDirection.TURNED_RIGHT_UP;
import static jon.com.ua.view.snake.BodyDirection.VERTICAL;

/**
 * Created with IntelliJ IDEA.
 * User: al1
 * Date: 1/23/13
 */
public class Snake {
    private Direction direction;
    private LinkedList<BoardElement> heads;
    private Color bodyColor = Color.CYAN;
    private boolean isGrow;
    private List<Wall> walls;
    private BadApple badApple;
    private Apple apple;
    private BoardExt board;
    private List<Point> path;

    public Snake(int length, List<Wall> walls, Apple apple, BadApple badApple, BoardExt board) {
        this.board = board;
        this.walls = walls;
        this.apple = apple;
        this.badApple = badApple;
        this.direction = Direction.RIGHT;
        create(length);
    }

    public void create(int length) {
        heads = new LinkedList<>();
        int x = BoardExt.SIZE / 2 - length;
        int y = BoardExt.SIZE / 2;
        for (int i = 0; i < length; i++) {
            heads.addFirst(new BoardElement(bodyColor, "", x++, y));
        }
        direction = Direction.RIGHT;
    }

    public Direction getDirection() {
        return direction;
    }

    public void setDirection(Direction direction) {
        this.direction = direction;
    }

    public LinkedList<BoardElement> getHeads() {
        return heads;
    }

    public void paint(Graphics g, int cellHeight, int cellWidth, boolean isPaintSprites) {
        for (int i = 0; i < heads.size(); i++) {
            BoardElement head = heads.get(i);
            if (isPaintSprites) {
                head.paintSprite(g, cellHeight, cellWidth, board.getAt(head.getX(), head.getY()));
            } else {
                head.paintColor(g, cellHeight, cellWidth, bodyColor);
            }
        }
    }

    public void paintPath(Graphics g, int cellHeight, int cellWidth, List<Point> path) {
        if (path != null) {
            this.path = path;
        }
        if (this.path == null) {
            return;
        }

        Point previous = null;
        int headX = 0;
        int headY = 0;

        for (Point current : this.path) {
            if (previous == null) {
                previous = current;
                continue;
            }
            int prevX = previous.getX() * cellWidth + cellWidth / 2;
            int prevY = BoardElement.realToScr(previous.getY()) * cellHeight + cellHeight / 2;
            headX = current.getX() * cellWidth + cellWidth / 2;
            headY = BoardElement.realToScr(current.getY()) * cellHeight + cellHeight / 2;
            g.setColor(Color.ORANGE);
            drawDashedLine(g, prevX, prevY, headX, headY);

            previous = current;
        }
        g.setColor(Color.RED);
        g.drawRect(headX, headY, 2, 2);
    }

    public void drawDashedLine(Graphics g, int x1, int y1, int x2, int y2) {
        Graphics2D g2d = (Graphics2D) g.create();
        Stroke dashed = new BasicStroke(3, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[] { 9 }, 0);
        g2d.setStroke(dashed);
        g2d.drawLine(x1, y1, x2, y2);
        g2d.dispose();
    }

    public boolean move() {
        BoardElement lead = heads.peekFirst();
        BoardElement newHead = getNewHead(lead);
        heads.addFirst(newHead);
        View.moveCounter++;
        boolean justEat = checkSnakeEatApple() != null;
        if (!justEat) {
            heads.removeLast();
        } else {
            grow();
        }

        return justEat;
    }

    private BoardElement checkSnakeEatApple() {
        return tryToEatAndGet(this.apple);
    }


    private BoardElement tryToEatAndGet(BoardElement target) {
        for (BoardElement head : heads) {
            if (target.getX() == head.getX() && target.getY() == head.getY()) {
                return target;
            }
        }
        return null;
    }

    private BoardElement getNewHead(BoardElement lead) {
        return switch (direction) {
            case RIGHT -> new BoardElement(bodyColor, "", lead.getX() + 1, lead.getY());
            case LEFT -> new BoardElement(bodyColor, "", lead.getX() - 1, lead.getY());
            case UP -> new BoardElement(bodyColor, "", lead.getX(), lead.getY() + 1);
            case DOWN -> new BoardElement(bodyColor, "", lead.getX(), lead.getY() - 1);
            default -> null;
        };
    }

    public int size() {
        return this.heads.size();
    }

    public void hide() {
        for (BoardElement head : heads) {
            head.setX(Integer.MAX_VALUE);
            head.setY(Integer.MAX_VALUE);
        }
    }

    public void setGrow(boolean grow) {
        this.isGrow = grow;
    }

    public void grow() {
        this.isGrow = true;
    }

    public boolean isGrow() {
        return isGrow;
    }

    public boolean isBittenItselfOrWall() {
        BoardElement lead = heads.getFirst();
        boolean isMe = heads.stream().skip(1).anyMatch(h -> h.itsMe(lead));
        boolean isWall = walls.stream().anyMatch(w -> w.itsMe(lead));
        return isMe || isWall;
    }

    // TODO refactor to itsMe on Elements
    public boolean isCollision(BoardElement lead, BoardElement head) {
        return lead.getX() == head.getX() && lead.getY() == head.getY();
    }

    public boolean isBody(BoardElement boardElement) {
        if (this.heads == null) {
            return false;
        }
        for (BoardElement head : heads) {
            if (isCollision(head, boardElement)) {
                return true;
            }
        }
        return false;
    }

    public boolean isBodyWithoutHeadAndTail(BoardElement boardElement) {
        for (BoardElement head : heads) {
            if (!isHead(boardElement) && isCollision(head, boardElement) && !isCollision(head, getTail())) {
                return true;
            }
        }
        return false;
    }

    public boolean isHead(BoardElement boardElement) {
        BoardElement lead = heads.getFirst();
        return isCollision(lead, boardElement);
    }

    public void decrease(int length) {
        if (length < size()) {
            for (int i = 0; i < length; i++) {
                this.heads.removeLast();
            }
        }
    }

    public void setPath(List<Point> path) {
        this.path = path;
    }

    public BodyDirection getBodyDirection(BoardElement curr) {
        int currIndex = heads.indexOf(curr);
        BoardElement prev = heads.get(currIndex - 1);
        BoardElement next = heads.get(currIndex + 1);

        BodyDirection nextPrev = orientation(next, prev);
        if (nextPrev != null) {
            return nextPrev;
        }

        if (orientation(prev, curr) == HORIZONTAL) {
            boolean clockwise = curr.getY() < next.getY() ^ curr.getX() > prev.getX();
            if (curr.getY() < next.getY()) {
                return (clockwise) ? TURNED_RIGHT_UP : TURNED_LEFT_UP;
            } else {
                return (clockwise) ? TURNED_LEFT_DOWN : TURNED_RIGHT_DOWN;
            }
        } else {
            boolean clockwise = curr.getX() < next.getX() ^ curr.getY() < prev.getY();
            if (curr.getX() < next.getX()) {
                return (clockwise) ? TURNED_RIGHT_DOWN : TURNED_RIGHT_UP;
            } else {
                return (clockwise) ? TURNED_LEFT_UP : TURNED_LEFT_DOWN;
            }
        }
    }

    public BodyDirection orientation(BoardElement curr, BoardElement next) {
        if (curr.getX() == next.getX()) {
            return VERTICAL;
        } else if (curr.getY() == next.getY()) {
            return HORIZONTAL;
        } else {
            return null;
        }
    }

    public BoardElement getTail() {
        return heads.getLast();
    }

    public Direction getTailDirection() {
        BoardElement prev = heads.get(size() - 2);
        BoardElement tail = getTail();

        if (prev.getX() == tail.getX()) {
            return (prev.getY() < tail.getY()) ? UP : DOWN;
        } else {
            return (prev.getX() < tail.getX()) ? RIGHT : LEFT;
        }
    }

    public Elements getTailLastElement() {
        return getTailLastElement(getTailDirection());
    }

    public Elements getTailLastElement(Direction direction) {
        return switch (direction) {
            case DOWN -> TAIL_END_DOWN;
            case UP -> TAIL_END_UP;
            case LEFT -> TAIL_END_LEFT;
            case RIGHT -> TAIL_END_RIGHT;
            default -> NONE;
        };
    }

    public Elements getHeadElement() {
        return getHeadElement(getDirection());
    }

    public Elements getHeadElement(Direction direction) {
        return switch (direction) {
            case DOWN -> HEAD_DOWN;
            case UP -> HEAD_UP;
            case LEFT -> HEAD_LEFT;
            case RIGHT -> HEAD_RIGHT;
            default -> NONE;
        };
    }

    public Elements getBody(BoardElement boardElement) {
        return getBody(getBodyDirection(boardElement));
    }

    public Elements getBody(BodyDirection bodyDirection) {
        return switch (bodyDirection) {
            case HORIZONTAL -> TAIL_HORIZONTAL;
            case VERTICAL -> TAIL_VERTICAL;
            case TURNED_LEFT_DOWN -> TAIL_LEFT_DOWN;
            case TURNED_LEFT_UP -> TAIL_LEFT_UP;
            case TURNED_RIGHT_DOWN -> TAIL_RIGHT_DOWN;
            case TURNED_RIGHT_UP -> TAIL_RIGHT_UP;
            default -> NONE;
        };
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (BoardElement head : heads) {
            sb.append("[" + head.getX() + "," + head.getY() + "]");
        }
        return sb.toString();
    }

}
