package admintools;

import arc.Core;
import arc.input.KeyCode;
import arc.scene.event.InputEvent;
import arc.scene.event.InputListener;
import arc.scene.event.Touchable;
import arc.scene.ui.layout.WidgetGroup;
import arc.util.Align;
import arc.util.Time;
import mindustry.Vars;
import mindustry.gen.Icon;
import mindustry.ui.Styles;

public class UIController {
    boolean hideAll = false;
    boolean showCarma = false;
    boolean showHistory = false;
    boolean showPortalTab = false;

    public static final WidgetGroup container = new WidgetGroup();

    public final TravelerFragment carma = new TravelerFragment("carma");
    public final TravelerFragment history = new TravelerFragment("history");
    public final TravelerFragment portalTab = new TravelerFragment("portal");

    public UIController() {
        container.setFillParent(true);
        container.touchable = Touchable.childrenOnly;
        container.visibility = (() -> !hideAll);

        Core.scene.addListener(new InputListener() {
            @Override
            public boolean keyDown(InputEvent event, KeyCode keycode) {
                if ((Core.input.keyDown(KeyCode.h) || Core.input.keyDown(KeyCode.p) || Core.input.keyDown(KeyCode.c) || Core.input.keyDown(KeyCode.a) || Core.input.keyDown(KeyCode.t)) && (Core.input.keyDown(KeyCode.controlLeft) || Core.input.keyDown(KeyCode.controlRight))) {
                    if (keycode == KeyCode.p) {
                        new ConsoleFrameDialog().show();
                        return true;
                    }
                    if (keycode == (KeyCode.h)) {
                        showHistory = !showHistory;
                        return true;
                    }
                    if (keycode == (KeyCode.c)) {
                        showCarma = !showCarma;
                        return true;
                    }
                    if (keycode == KeyCode.t) {
                        showPortalTab = !showPortalTab;
                        return true;
                    }
                    if (keycode == (KeyCode.a)) {
                        hideAll = !hideAll;
                        return true;
                    }
                    container.updateVisibility();
                    return false;
                }
                return super.keyDown(event, keycode);
            }

        });

        Core.scene.add(container);

        //Generate Portal tab
        portalTab.button(Icon.modePvpSmall, Styles.defaulti, () -> Vars.ui.join.connect("130.61.52.25", 6567)).uniformX().uniformY().fill();
        portalTab.button(Icon.modeSurvivalSmall, Styles.defaulti, () -> Vars.ui.join.connect("130.61.52.25", 6568)).uniformX().uniformY().fill();
        portalTab.row();
        portalTab.button(Icon.planetSmall, Styles.defaulti, () -> Vars.ui.join.connect("130.61.52.25", 6569)).uniformX().uniformY().fill();
        portalTab.button(Icon.modeAttackSmall, Styles.defaulti, () -> Vars.ui.join.connect("130.61.52.25", 6570)).uniformX().uniformY().fill();
        portalTab.button(Icon.starSmall, Styles.defaulti, () -> Vars.ui.join.connect("130.61.52.25", 6572)).uniformX().uniformY().fill();

        //Generate carma info
        carma.row();
        carma.add(CarmaDetector.table.align(Align.left));

        //history
        history.row();
        history.add(HistoryFrame.table.align(Align.left));

        Time.run(10, () -> portalTab.setPosition(container.getWidth() * Core.settings.getFloat("portal_x", 0.5f), container.getHeight() * Core.settings.getFloat("portal_y", 0.2f)));
        Time.run(10, () -> carma.setPosition(container.getWidth() * Core.settings.getFloat("carma_x", 0.5f), container.getHeight() * Core.settings.getFloat("carma_y", 0.5f)));
        Time.run(10, () -> history.setPosition(container.getWidth() * Core.settings.getFloat("history_x", 0.5f), container.getHeight() * Core.settings.getFloat("history_y", 0.5f)));

        portalTab.visibility = (() -> !hideAll && showPortalTab);
        carma.visibility = (() -> !hideAll && showCarma);
        history.visibility = (() -> !hideAll && showHistory);

        container.addChild(portalTab);
        container.addChild(carma);
        container.addChild(history);

        ConsoleFrameDialog.init();//todo remove
    }
}