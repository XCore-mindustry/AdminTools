package admintools.ui.components;

import admintools.ui.theme.LucidTheme;
import arc.scene.ui.Dialog;
import arc.scene.ui.layout.Scl;
import mindustry.graphics.Pal;
import mindustry.ui.Styles;

/**
 * A sleek, compact confirmation modal dialog for critical actions (logout, bans, rollbacks).
 */
public class ConfirmDialog extends Dialog {

    public ConfirmDialog(String title, String message, Runnable onConfirm) {
        super(title, Styles.defaultDialog);

        setFillParent(false);
        background(LucidTheme.glass(LucidTheme.bgGlass, LucidTheme.accent));
        closeOnBack();

        cont.margin(16f, 20f, 12f, 20f);
        cont.add(message).wrap().width(Scl.scl(320f)).pad(Scl.scl(8f)).center().row();

        buttons.margin(6f, 16f, 14f, 16f);
        buttons.defaults().size(Scl.scl(120f), Scl.scl(38f)).pad(Scl.scl(6f));
        buttons.button("@cancel", Styles.defaultt, this::hide);
        buttons.button("@ok", LucidTheme.flatTextButtonStyle(), () -> {
            hide();
            if (onConfirm != null) onConfirm.run();
        });
    }

    public static void show(String title, String message, Runnable onConfirm) {
        new ConfirmDialog(title, message, onConfirm).show();
    }
}
