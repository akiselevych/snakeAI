package jon.com.ua.view;

import com.codenjoy.dojo.services.Point;
import jon.com.ua.client.Elements;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: al1
 * Date: 1/23/13
 */
public class BoardElement {
    private int x;
    private int y;
    private Color color;
    private String name;
    private static Map<String, BufferedImage> images;

    static {
        images = new HashMap<>();
        for (Elements element : Elements.values()) {
            try {
                String name = element.name().toLowerCase();
                InputStream is = BoardElement.class.getResourceAsStream("/sprite/" + name + ".png");
                BufferedImage image = ImageIO.read(is);
                images.put(name, image);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public BoardElement(Color color, String name, int x, int y) {
        this.name = name;
        this.color = color;
        this.x = x;
        this.y = y;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public static BoardElement of(Point point) {
        return new BoardElement(null, null, point.getX(), point.getY());
    }

    public void paint(Graphics graphics, int cellHeight, int cellWidth, Elements element) {
        if (element != null) {
            paintSprite(graphics, cellHeight, cellWidth, element);
        } else {
            paintColor(graphics, cellHeight, cellWidth, this.color);
        }
    }

    public void paintColor(Graphics g, int cellHeight, int cellWidth, Color color) {
        int x = getX() * cellWidth;
        int y = realToScr(getY()) * cellHeight;

        g.setColor(color);
        g.fill3DRect(x, y, cellWidth, cellHeight, true);
    }

    public void paintSprite(Graphics g, int cellHeight, int cellWidth, Elements element) {
        int x = getX() * cellWidth;
        int y = realToScr(getY()) * cellHeight;

        g.fill3DRect(x, y, cellWidth, cellHeight, true);
        Graphics2D g2d = (Graphics2D) g;
        //g2d.scale(2.0, 2.0);

        g2d.drawImage(images.get(element.name().toLowerCase()), x, y, null);

    }

    public static int realToScr(int y) {
        return BoardExt.SIZE - y - 1;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        BoardElement boardElement = (BoardElement) o;

        if (x != boardElement.x)
            return false;
        if (y != boardElement.y)
            return false;

        return true;
    }

    public boolean itsMe(BoardElement boardElement) {
        return boardElement != null && x == boardElement.getX() && y == boardElement.getY();
    }

    @Override
    public int hashCode() {
        int result = x;
        result = 31 * result + y;
        return result;
    }

    @Override
    public String toString() {
        return
                "x=" + x +
                ", y=" + y;
    }
}
