package admintools;

import arc.Events;
import mindustry.Vars;
import mindustry.game.EventType;
import mindustry.gen.Call;
import mindustry.io.JsonIO;
import mindustry.mod.Mod;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

import static mindustry.Vars.netClient;

public class AdminTools extends Mod {

    public static final DateTimeFormatter shortDateFormat = DateTimeFormatter.ofPattern("HH:mm:ss").withZone(ZoneId.systemDefault());
    public static final DateTimeFormatter longDateFormat = DateTimeFormatter.ofPattern("HH:mm:ss dd.MM.uu").withZone(ZoneId.systemDefault());

    @Override
    public void init() {
        netClient.addPacketHandler("give_ban_data", content -> new BanDialog(content).show());
        netClient.addPacketHandler("adm_mod_begin", content -> Call.serverPacketReliable("adm_mod_end", ""));

        if (Vars.mobile) return;

        netClient.addPacketHandler("take_history_info", content -> {
            HistoryFrame.TransportableHistoryStack stack = JsonIO.read(HistoryFrame.TransportableHistoryStack.class, content);
            if (stack == null) return;
            HistoryFrame.update(stack);
        });

        netClient.addPacketHandler("ban_data", content -> {
            ConsoleFrameDialog.BannedPlayer ban = JsonIO.read(ConsoleFrameDialog.BannedPlayer.class, content);
            if (ban == null) return;
            ConsoleFrameDialog.addBanPlayer(ban);
        });

        netClient.addPacketHandler("info_data", content -> {
            ConsoleFrameDialog.InfoPlayer info = JsonIO.read(ConsoleFrameDialog.InfoPlayer.class, content);
            if (info == null) return;
            ConsoleFrameDialog.addProbived(info);
        });

        Events.on(EventType.ClientLoadEvent.class, e -> new UIController());

        CarmaDetector.init();
    }
}
