package jon.com.ua.view;

import java.awt.*;

/**
 * Created with IntelliJ IDEA.
 * User: al1
 * Date: 7/10/22
 */

public class Wall extends BoardElement {
    private static Color color = Color.GRAY;
    private String text;

    public Wall(int x, int y, String text) {
        super(color, "", x, y);
        this.text = text;
    }

    public String getText() {
        return text;
    }
}
