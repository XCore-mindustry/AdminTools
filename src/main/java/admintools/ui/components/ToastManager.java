package admintools.ui.components;

import admintools.ui.theme.LucidTheme;
import arc.Core;
import arc.graphics.Color;
import arc.scene.actions.Actions;
import arc.scene.event.Touchable;
import arc.scene.ui.Label;
import arc.scene.ui.layout.Scl;
import arc.scene.ui.layout.Table;
import arc.scene.ui.layout.WidgetGroup;

/**
 * Non-blocking floating toast notifications in the corner of the screen.
 */
public class ToastManager {
    private static final WidgetGroup root = new WidgetGroup();
    private static final Table container = new Table();
    private static boolean installed = false;

    public static void install() {
        if (installed || Core.scene == null) return;
        root.setFillParent(true);
        root.touchable = Touchable.childrenOnly;

        container.top().right();
        container.margin(40f, 12f, 12f, 12f);
        root.addChild(container);

        Core.scene.add(root);
        installed = true;
    }

    public static void show(String message, Color accentColor) {
        if (!installed) install();

        Table toast = new Table();
        toast.background(LucidTheme.glass(LucidTheme.bgHeader, accentColor));
        toast.margin(8f, 14f, 8f, 14f);
        toast.touchable = Touchable.enabled;

        Label label = new Label(message);
        label.setColor(Color.white);
        label.setFontScale(0.9f);
        toast.add(label);

        // Click to dismiss early
        toast.clicked(toast::remove);

        // Animation: fade in -> stay -> fade out -> remove
        toast.color.a = 0f;
        toast.actions(
            Actions.fadeIn(0.2f),
            Actions.delay(3.5f),
            Actions.fadeOut(0.3f),
            Actions.remove()
        );

        container.add(toast).padBottom(Scl.scl(6f)).right().row();
    }

    public static void info(String message) {
        show(message, LucidTheme.accent);
    }

    public static void success(String message) {
        show(message, LucidTheme.success);
    }

    public static void warn(String message) {
        show(message, LucidTheme.warning);
    }

    public static void error(String message) {
        show(message, LucidTheme.danger);
    }
}
