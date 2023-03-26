package admintools;

import arc.Events;
import arc.util.Log;
import mindustry.Vars;
import mindustry.game.EventType;
import mindustry.io.JsonIO;
import mindustry.mod.Mod;

import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

import static mindustry.Vars.netClient;

public class AdminTools extends Mod {

    public static final DateTimeFormatter shortDateFormat = DateTimeFormatter.ofPattern("HH:mm:ss").withZone(ZoneOffset.UTC);
    public static final DateTimeFormatter longDateFormat = DateTimeFormatter.ofPattern("HH:mm:ss dd.MM.uu").withZone(ZoneOffset.UTC);

    @Override
    public void init() {
        netClient.addPacketHandler("give_ban_data", content -> new BanDialog(content).show());

        if (Vars.mobile) return;

        netClient.addPacketHandler("take_history_info", content -> {
            HistoryFrame.TransportableHistoryStack nya = JsonIO.read(HistoryFrame.TransportableHistoryStack.class, content);
            if (nya == null) return;
            HistoryFrame.update(nya);
            // nya
        });

        netClient.addPacketHandler("ban_data", content -> {
            Log.info(content);
            ConsoleFrameDialog.BannedPlayer nya = ConsoleFrameDialog.BannedPlayer.parse(content);
            if (nya == null) return;
            ConsoleFrameDialog.addBanPlayer(nya);
        });

        netClient.addPacketHandler("info_data", content -> {
            Log.info(content);
            ConsoleFrameDialog.InfoPlayer nya = ConsoleFrameDialog.InfoPlayer.parse(content);
            if (nya == null) return;
            ConsoleFrameDialog.addProbived(nya);
        });

        Events.on(EventType.ClientLoadEvent.class, e -> {
            new UIController();
        });

        CarmaDetector.init();
    }
}
