package admintools.ui.components;

import admintools.ui.theme.LucidTheme;
import arc.scene.ui.layout.Scl;
import mindustry.ui.Styles;
import mindustry.ui.dialogs.BaseDialog;

/**
 * A sleek confirmation dialog for dangerous or critical actions (bans, rollbacks, resets).
 */
public class ConfirmDialog extends BaseDialog {

    public ConfirmDialog(String title, String message, Runnable onConfirm) {
        super(title);

        cont.background(LucidTheme.glass(LucidTheme.bgGlass, LucidTheme.borderIdle));
        cont.margin(16f);

        cont.add(message).wrap().width(Scl.scl(380f)).pad(Scl.scl(12f)).center().row();

        buttons.defaults().size(Scl.scl(140f), Scl.scl(44f)).pad(Scl.scl(6f));
        buttons.button("@cancel", this::hide).style(Styles.defaultt);
        buttons.button("@ok", () -> {
            hide();
            if (onConfirm != null) onConfirm.run();
        }).style(LucidTheme.flatTextButtonStyle());
    }

    public static void show(String title, String message, Runnable onConfirm) {
        new ConfirmDialog(title, message, onConfirm).show();
    }
}
