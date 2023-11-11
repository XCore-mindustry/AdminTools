package admintools;

import arc.Core;
import arc.Events;
import mindustry.Vars;
import mindustry.game.EventType;
import mindustry.gen.Call;
import mindustry.io.JsonIO;
import mindustry.mod.Mod;

import static mindustry.Vars.netClient;

public class AdminTools extends Mod {
    public static UIController ui;

    @Override
    public void init() {
        netClient.addPacketHandler("give_ban_data", content -> new BanDialog(content).show());
        netClient.addPacketHandler("adm_mod_begin", content ->
                Call.serverPacketReliable("adm_mod_end", Vars.mods.getMod("admintools-mod").meta.version));

        if (Vars.mobile) return;

        netClient.addPacketHandler("tilelogger_history_tile", content -> {
            HistoryEntry[] stack = JsonIO.read(HistoryEntry[].class, content);
            if (stack == null) return;
            HistoryFrame.update(stack);
        });

//        netClient.addPacketHandler("tilelogger_rollback_preview", content -> {
//            HistoryEntry[] stack = JsonIO.read(HistoryEntry[].class, content);
//            if (stack == null) return;
//            ui.updatePreview(stack);
//        });

        Events.on(EventType.ClientChatEvent.class, e -> {
            String message = e.message;

            if (message.startsWith("/xl")) {
                Call.sendChatMessage("/login " + Core.settings.getString("xcore-login"));
            }
            if (message.startsWith("/login")) {
                Core.settings.put("xcore-login", message.substring(7));
            }
        });
        Events.on(EventType.ClientLoadEvent.class, e -> ui = new UIController());

        CarmaDetector.init();
    }
}
