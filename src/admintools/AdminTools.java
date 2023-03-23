package admintools;

import arc.Events;
import mindustry.Vars;
import mindustry.game.EventType;
import mindustry.io.JsonIO;
import mindustry.mod.Mod;

import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

import static mindustry.Vars.netClient;

public class AdminTools extends Mod {

    public static final DateTimeFormatter shortDateFormat = DateTimeFormatter.ofPattern("HH:mm:ss").withZone(ZoneOffset.UTC);

    @Override
    public void init() {
        netClient.addPacketHandler("give_ban_data", content -> new BanDialog(content).show());

        netClient.addPacketHandler("take_history_info", content -> {
            if (Vars.mobile) return;
            HistoryFrame.TransportableHistoryStack nya = JsonIO.read(HistoryFrame.TransportableHistoryStack.class, content);
            if (nya == null) return;
            HistoryFrame.update(nya);
            // nya
        });

        if (Vars.mobile) return;
        Events.on(EventType.ClientLoadEvent.class, e -> {
            new UIController();
        });

        CarmaDetector.init();
    }
}
