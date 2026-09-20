package admintools.ui.components;

import admintools.input.FreeCamController;
import admintools.ui.theme.LucidTheme;
import arc.Core;
import arc.scene.event.Touchable;
import arc.scene.ui.ImageButton;
import arc.scene.ui.Label;
import arc.scene.ui.layout.Table;
import mindustry.gen.Icon;
import mindustry.graphics.Pal;
import mindustry.ui.Styles;

/**
 * Compact floating HUD indicator showing FreeCam status with quick Recenter and Exit controls.
 */
public class FreeCamHUD extends Table {

    public FreeCamHUD() {
        touchable = Touchable.childrenOnly;

        Table pill = new Table();
        pill.background(LucidTheme.glass(LucidTheme.bgHeader, LucidTheme.accent));
        pill.margin(4f, 10f, 4f, 10f);

        // Icon + Label
        pill.image(Icon.eye).size(18f).color(Pal.accent).padRight(6f);
        Label title = pill.label(() -> Core.bundle.get("admintools.freecam.hud_title")).color(Pal.accent).padRight(10f).get();
        title.setFontScale(0.85f);

        // Recenter on player unit
        ImageButton centerBtn = new ImageButton(Icon.home, Styles.clearNonei);
        centerBtn.clicked(() -> FreeCamController.get().centerOnPlayer());
        pill.add(centerBtn).size(24f).padRight(6f);

        // Exit FreeCam
        ImageButton closeBtn = new ImageButton(Icon.cancel, Styles.clearNonei);
        closeBtn.clicked(() -> FreeCamController.get().setActive(false));
        pill.add(closeBtn).size(24f);

        add(pill);
        pack();
    }
}
