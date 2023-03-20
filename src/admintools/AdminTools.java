package admintools;

import arc.Core;
import arc.Events;
import arc.scene.Element;
import arc.scene.event.Touchable;
import arc.scene.ui.Button;
import arc.scene.ui.ImageButton;
import arc.scene.ui.layout.Cell;
import arc.scene.ui.layout.WidgetGroup;
import arc.struct.Seq;
import arc.util.Align;
import arc.util.Time;
import mindustry.Vars;
import mindustry.game.EventType;
import mindustry.gen.Icon;
import mindustry.io.JsonIO;
import mindustry.mod.*;
import mindustry.ui.Styles;

import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

import static admintools.CarmaDetector.carmaInfo;
import static admintools.HistoryFrame.historyBF;
import static mindustry.Vars.netClient;

public class AdminTools extends Mod {

    public static TravelerFragment travelerFragment;
    public static final DateTimeFormatter shortDateFormat = DateTimeFormatter.ofPattern("HH:mm:ss").withZone(ZoneOffset.UTC);

    public static final Seq<Cell<ImageButton>> buttons = new Seq<>();

    public static boolean visible = true;

    @Override
    public void init() {
        netClient.addPacketHandler("give_ban_data", content -> new BanDialog(content).show());

        netClient.addPacketHandler("take_history_info", content -> {
            if(Vars.mobile) return;
            HistoryFrame.TransportableHistoryStack nya = JsonIO.read(HistoryFrame.TransportableHistoryStack.class, content);
            if (nya == null) return;
            HistoryFrame.update(nya);
            // nya
        });

        if(Vars.mobile) return;

        Events.on(EventType.ClientLoadEvent.class, e -> {
            WidgetGroup group = new WidgetGroup();
            group.setFillParent(true);
            group.touchable = Touchable.childrenOnly;
            group.visible(() -> true);
            Core.scene.add(group);

            travelerFragment = new TravelerFragment();
            travelerFragment.button(Icon.eyeOffSmall, Styles.defaulti,()->{
                visible = !visible;
                buttons.each(b->b.visible(visible));
                travelerFragment.updateVisibility();
            });
            buttons.add(travelerFragment.button(Icon.eyeSmall, Styles.defaulti, () -> {
                carmaInfo.visible = !carmaInfo.visible;
                carmaInfo.updateVisibility();
            }).uniformX().uniformY().fill());
            buttons.add(travelerFragment.button(Icon.settingsSmall, Styles.defaulti, () -> {
                Core.settings.put("travelerx", travelerFragment.x / group.getWidth());
                Core.settings.put("carmax", carmaInfo.x / group.getWidth());
                Core.settings.put("historyx", historyBF.x / group.getWidth());
                Core.settings.put("travelery", travelerFragment.y / group.getHeight());
                Core.settings.put("carmay", carmaInfo.y / group.getHeight());
                Core.settings.put("historyy", historyBF.y / group.getHeight());
                Core.settings.saveValues();
            }).uniformX().uniformY().fill());
            buttons.add(travelerFragment.button(Icon.zoomSmall, Styles.defaulti, () -> {
                historyBF.visible = !historyBF.visible;
                historyBF.updateVisibility();
            }).uniformX().uniformY().fill());
            buttons.add(travelerFragment.button(Icon.adminSmall, Styles.defaulti, () -> new ConsoleFrameDialog("Пробиватор v277353HENTAI").show()));
            travelerFragment.row();
            buttons.add(travelerFragment.button(Icon.modePvpSmall, Styles.defaulti, () -> Vars.ui.join.connect("130.61.52.25", 6567)).uniformX().uniformY().fill());
            buttons.add(travelerFragment.button(Icon.modeSurvivalSmall, Styles.defaulti, () -> Vars.ui.join.connect("130.61.52.25", 6568)).uniformX().uniformY().fill());
            buttons.add(travelerFragment.button(Icon.planetSmall, Styles.defaulti, () -> Vars.ui.join.connect("130.61.52.25", 6569)).uniformX().uniformY().fill());
            buttons.add(travelerFragment.button(Icon.modeAttackSmall, Styles.defaulti, () -> Vars.ui.join.connect("130.61.52.25", 6570)).uniformX().uniformY().fill());
            buttons.add(travelerFragment.button(Icon.starSmall, Styles.defaulti, () -> Vars.ui.join.connect("130.61.52.25", 6572)).uniformX().uniformY().fill());

            carmaInfo = new TravelerFragment();
            carmaInfo.row();
            carmaInfo.add(CarmaDetector.table.align(Align.left));

            historyBF = new TravelerFragment();
            historyBF.row();
            historyBF.add(HistoryFrame.table.align(Align.left));

            group.addChild(travelerFragment);
            group.addChild(carmaInfo);
            group.addChild(historyBF);

            Time.run(10, () -> travelerFragment.setPosition(group.getWidth() * Core.settings.getFloat("travelerx", 0.5f), group.getHeight() * Core.settings.getFloat("travelery", 0.2f)));
            Time.run(10, () -> carmaInfo.setPosition(group.getWidth() * Core.settings.getFloat("carmax", 0.5f), group.getHeight() * Core.settings.getFloat("carmay", 0.5f)));
            Time.run(10, () -> historyBF.setPosition(group.getWidth() * Core.settings.getFloat("historyx", 0.5f), group.getHeight() * Core.settings.getFloat("historyy", 0.5f)));
        });

        CarmaDetector.init();
    }
}
