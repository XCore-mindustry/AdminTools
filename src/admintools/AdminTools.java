package admintools;

import arc.Core;
import arc.Events;
import arc.scene.event.Touchable;
import arc.scene.ui.layout.WidgetGroup;
import arc.util.Time;
import mindustry.Vars;
import mindustry.game.EventType;
import mindustry.gen.Icon;
import mindustry.mod.*;
import mindustry.ui.Styles;

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

            group.addChild(travelerFragment);

            Time.run(10, () -> travelerFragment.setPosition(group.getWidth() / 2, group.getHeight() / 1.5f));
        });
    }
}
