package admintools;

import arc.Core;
import arc.Events;
import arc.scene.event.Touchable;
import arc.scene.ui.layout.WidgetGroup;
import arc.util.Align;
import arc.util.Time;
import mindustry.Vars;
import mindustry.game.EventType;
import mindustry.gen.Icon;
import mindustry.mod.*;
import mindustry.ui.Styles;

import static admintools.CarmaDetector.carmaInfo;
import static admintools.LastBuildDetector.actionsLog;
import static mindustry.Vars.netClient;

public class AdminTools extends Mod {
    @Override
    public void init() {
        netClient.addPacketHandler("give_ban_data", content -> new BanDialog(content).show());

        Events.on(EventType.ClientLoadEvent.class, e -> {
            WidgetGroup group = new WidgetGroup();
            group.setFillParent(true);
            group.touchable = Touchable.childrenOnly;
            group.visible(() -> true);
            Core.scene.add(group);

            TravelerFragment travelerFragment = new TravelerFragment();
            travelerFragment.button(Icon.modePvp, Styles.defaulti, () -> Vars.ui.join.connect("130.61.52.25", 6567)).uniformX().uniformY().fill();
            travelerFragment.button(Icon.modeSurvival, Styles.defaulti, () -> Vars.ui.join.connect("130.61.52.25", 6568)).uniformX().uniformY().fill();
            travelerFragment.button(Icon.planet, Styles.defaulti, () -> Vars.ui.join.connect("130.61.52.25", 6569)).uniformX().uniformY().fill();
            travelerFragment.button(Icon.modeAttack, Styles.defaulti, () -> Vars.ui.join.connect("130.61.52.25", 6570)).uniformX().uniformY().fill();
            travelerFragment.button(Icon.star, Styles.defaulti, () -> Vars.ui.join.connect("130.61.52.25", 6572)).uniformX().uniformY().fill();
            travelerFragment.button(Icon.eye, Styles.defaulti, () -> {actionsLog.visible = !actionsLog.visible;carmaInfo.visible = actionsLog.visible; carmaInfo.updateVisibility(); actionsLog.updateVisibility();}).uniformX().uniformY().fill();

            actionsLog = new TravelerFragment();
            actionsLog.row();
            actionsLog.add(LastBuildDetector.info);

            carmaInfo = new TravelerFragment();
            carmaInfo.row();
            carmaInfo.add(CarmaDetector.table.align(Align.left));

            group.addChild(travelerFragment);
            group.addChild(actionsLog);
            group.addChild(carmaInfo);

            Time.run(10, () -> travelerFragment.setPosition(group.getWidth() / 2f, group.getHeight() / 5f));
            Time.run(10, () -> actionsLog.setPosition(group.getWidth() / 15f, group.getHeight() / 1.5f));
            Time.run(10, () -> carmaInfo.setPosition(group.getWidth() / 2f, group.getHeight() / 1.5f));
        });

        LastBuildDetector.init();
        CarmaDetector.init();
    }
}
