package admintools.ui.components;

import admintools.ui.theme.LucidTheme;
import arc.graphics.Color;
import arc.scene.ui.Label;
import arc.scene.ui.layout.Table;

/**
 * A compact, visually appealing status badge / chip pill.
 */
public class Badge extends Table {
    private final Label label;

    public Badge(String text, Color color) {
        background(LucidTheme.badgeBg(color));
        margin(2f, 6f, 2f, 6f);

        label = new Label(text);
        label.setColor(color);
        label.setFontScale(0.85f);
        add(label);
    }

    public static Badge danger(String text) {
        return new Badge(text, LucidTheme.danger);
    }

    public static Badge warning(String text) {
        return new Badge(text, LucidTheme.warning);
    }

    public static Badge success(String text) {
        return new Badge(text, LucidTheme.success);
    }

    public static Badge neutral(String text) {
        return new Badge(text, LucidTheme.textMuted);
    }

    public static Badge accent(String text) {
        return new Badge(text, LucidTheme.accent);
    }

    public void setText(String text) {
        label.setText(text);
    }
}
