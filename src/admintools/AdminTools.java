package admintools;

import arc.Core;
import arc.Events;
import arc.scene.style.TextureRegionDrawable;
import mindustry.Vars;
import mindustry.game.EventType;
import mindustry.gen.Call;
import mindustry.gen.Icon;
import mindustry.gen.Iconc;
import mindustry.gen.Sounds;
import mindustry.io.JsonIO;
import mindustry.mod.Mod;

import static mindustry.Vars.netClient;

public class AdminTools extends Mod {
    public static UIController ui;

    @Override
    public void init() {
        netClient.addPacketHandler("give_ban_data", BanDialog::new);
        netClient.addPacketHandler("adm_mod_begin", content ->
                Call.serverPacketReliable("adm_mod_end", Vars.mods.getMod("admintools-mod").meta.version));
        netClient.addPacketHandler("adm_mod_votekick", content -> {
            if (Core.settings.getBool("admintools-notifications")) for (int i = 0; i < 7; i++) Sounds.message.play(40);
        });

        Events.on(EventType.ClientChatEvent.class, e -> {
            String message = e.message;

            if (message.startsWith("/xl") || message.startsWith("/login"))
                Call.sendChatMessage("/login " + Core.settings.getString("xcore-login"));

            if (message.startsWith("/login")) {
                String[] password = message.split(" ");
                if (password.length < 2) {
                    Vars.ui.chatfrag.addMessage(Iconc.warning + "[#f]Your password is too small.");
                    return;
                }

                Core.settings.put("xcore-login", password[1]);
            }
        });

        if (Vars.mobile) return;

        netClient.addPacketHandler("tilelogger_history_tile", content -> {
            HistoryEntry[] stack = JsonIO.read(HistoryEntry[].class, content);
            if (stack == null) return;
            HistoryFrame.update(stack);
        });

        Events.on(EventType.ClientLoadEvent.class, e -> {
            ui = new UIController();

            Vars.ui.settings.addCategory("AdminTools", new TextureRegionDrawable(Icon.admin.getRegion()), t -> {
                t.check("Hide all", ui.hideAll, b -> ui.hideAll = b).left().row();
                t.check("Karma", ui.showKarma, b -> ui.showKarma = b).left().row();
                t.check("History", ui.showHistory, b -> ui.showHistory = b).left().row();
                t.check("Portal", ui.showPortalTab, b -> ui.showPortalTab = b).left().row();
                t.check("Sounds Notifications", Core.settings.getBool("admintools-notifications"),
                    b -> Core.settings.put("admintools-notifications", b)).left().row();
            });
        });

        KarmaDetector.init();
    }
}
