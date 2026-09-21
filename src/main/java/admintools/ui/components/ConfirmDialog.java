package admintools.ui.components;

import admintools.ui.theme.LucidTheme;
import arc.Core;
import arc.scene.ui.Dialog;
import arc.scene.ui.layout.Scl;
import mindustry.ui.Styles;

/**
 * A sleek, compact confirmation modal dialog for critical actions (logout, bans, rollbacks).
 * Fully responsive across desktop and mobile screens.
 */
public class ConfirmDialog extends Dialog {

    public ConfirmDialog(String title, String message, Runnable onConfirm) {
        super(title, Styles.defaultDialog);

        setFillParent(false);
        background(LucidTheme.glass(LucidTheme.bgGlass, LucidTheme.accent));
        closeOnBack();

        float screenW = (Core.scene.getWidth() - Core.scene.marginLeft - Core.scene.marginRight) / Scl.scl(1f);
        float dialogWidth = Math.max(260f, Math.min(screenW - 36f, 320f));

        cont.margin(16f, 16f, 12f, 16f);
        cont.add(message).wrap().width(dialogWidth).pad(8f).center().row();

        buttons.margin(6f, 14f, 14f, 14f);
        float btnWidth = Math.min((dialogWidth - 16f) / 2f, 120f);
        buttons.defaults().size(btnWidth, 38f).pad(4f);
        buttons.button("@cancel", LucidTheme.secondaryTextButtonStyle(), this::hide);
        buttons.button("@ok", LucidTheme.accentTextButtonStyle(), () -> {
            hide();
            if (onConfirm != null) onConfirm.run();
        });
    }

    public static void show(String title, String message, Runnable onConfirm) {
        new ConfirmDialog(title, message, onConfirm).show();
    }
}
