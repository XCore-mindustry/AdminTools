package admintools.ui.window;

import arc.scene.style.Drawable;

/**
 * Immutable specification for an AdminTools window.
 */
public record WindowSpec(
    String id,
    String title,
    Drawable icon,
    float defaultWidth,
    float defaultHeight,
    float minWidth,
    float minHeight,
    boolean resizable,
    boolean collapsible
) {
    public static WindowSpec of(String id, String title, Drawable icon) {
        return new WindowSpec(id, title, icon, 460f, 360f, 260f, 180f, true, true);
    }

    public static WindowSpec of(String id, String title, Drawable icon, float width, float height) {
        return new WindowSpec(id, title, icon, width, height, Math.min(width, 240f), Math.min(height, 160f), true, true);
    }
}
