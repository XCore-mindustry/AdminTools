package admintools;

import arc.Core;
import arc.Events;
import arc.scene.event.InputEvent;
import arc.scene.event.InputListener;
import arc.scene.ui.ScrollPane;
import arc.scene.ui.TextField;
import arc.scene.ui.layout.Table;
import arc.struct.Seq;
import mindustry.Vars;
import mindustry.game.EventType;
import mindustry.gen.Call;
import mindustry.gen.Icon;
import mindustry.gen.Tex;
import mindustry.ui.Styles;
import mindustry.ui.dialogs.BaseDialog;

import java.time.Instant;
import java.util.Objects;

import static admintools.AdminTools.longDateFormat;
import static mindustry.Vars.ui;

public class ConsoleFrameDialog extends BaseDialog {
    public static Table bannedPlayersPanel = new Table();
    public static Table bannedPlayersData = new Table();
    public static Table probivPanel = new Table();
    public static Table probivData = new Table();

    public static TextField searcher = new TextField();
    public static TextField searcher2 = new TextField();

    public static Seq<BannedPlayer> banned = new Seq<>();
    public static Seq<InfoPlayer> players = new Seq<>();

    public ScrollPane panel1, panel2;

    public ConsoleFrameDialog() {
        super("Console");

        updateBanned(banned);

        buttons.defaults().size(210f, 64f);

        buttons.add().growX().width(-1);

        addCloseButton();

        buttons.add().growX().width(-1);

        bannedPlayersPanel.clear();
        bannedPlayersPanel.add(searcher);

        bannedPlayersPanel.row();
        bannedPlayersPanel.add(" ");
        bannedPlayersPanel.row();
        bannedPlayersPanel.add(bannedPlayersData);

        probivPanel.clear();
        probivPanel.add(searcher2);
        probivPanel.button(Icon.zoomSmall, () -> {
            if (searcher2.getText().length() > 0 && Vars.net.client())
                updateProbivRequest(searcher2.getText());
        });
        probivPanel.row();
        probivPanel.add(probivData);

        panel1 = new ScrollPane(bannedPlayersPanel);
        panel1.setFadeScrollBars(true);
        panel1.setScrollingDisabled(true, false);
        panel1.setWidth(100f);
        cont.add(panel1);

        panel2 = new ScrollPane(probivPanel);
        panel2.setWidth(100f);
        panel2.setFadeScrollBars(true);
        panel2.setScrollingDisabled(true, false);
        cont.add(panel2);

        cont.row();

        if (Vars.net.client()) {
            Call.serverPacketReliable("bans_request", "");
        }
    }
    public static void init() {

        Events.on(EventType.ClientServerConnectEvent.class, e -> {
            banned.clear();
            players.clear();
            updateBanned(new Seq<>());
            updateProbiv(new Seq<>());
        });

        searcher.addListener(new InputListener() {
            @Override
            public boolean keyTyped(InputEvent event, char character) {
                if (searcher.getText().length() > 0) {
                    updateBanned(banned.copy().filter(p -> p.name.contains(searcher.getText())));
                } else {
                    updateBanned(banned);
                }
                return super.keyTyped(event, character);
            }
        });
    }

    public static void updateBanned(Seq<BannedPlayer> data) {
        bannedPlayersData.clear();
        data.each(p -> {
            bannedPlayersData.table(Tex.whiteui, t -> {
                t.setColor(0.5f, 0.5f, 0.5f, 1);
                t.left();
                t.button(Icon.undo, () -> {
                    Call.serverPacketReliable("unban", p.uuid);
                    banned.remove(p);
                    updateBanned(banned);
                }).height(36f).width(36f);
                t.add();
                t.button(Icon.eye, () -> updateProbivRequest(p.uuid)).height(36f).width(36f);
            }).growX().height(36f).row();
            bannedPlayersData.table(Tex.whitePane, t -> {
                t.setColor(0.5f, 0.5f, 0.5f, 1);
                t.left();

                t.add("Nickname");
                t.add("|");
                t.add(p.name).row();
                t.add("UUID");
                t.add("|");
                t.add(p.uuid).row();
                t.add("Unban date");
                t.add("|");
                t.add(longDateFormat.format(Instant.ofEpochMilli(p.unbanDate))).row();
                t.add("Admin");
                t.add("|");
                t.add(p.adminName).row();
                t.add("Reason");
                t.add("|");
                t.add(p.reason).row();
            });
            bannedPlayersData.row();
            bannedPlayersData.add(" ");
            bannedPlayersData.row();
        });
    }

    public static void updateProbiv(Seq<InfoPlayer> data) {
        probivData.clear();
        data.each(p -> {
            probivData.table(Tex.whiteui, t -> {
                t.setColor(0.5f, 0.5f, 0.5f, 1);
                t.left();
                if (p.admin) {
                    t.button(Icon.adminSmall, () -> {
                    });
                } else {
                    t.button(Icon.hammerSmall, Styles.cleari, () ->
                            ui.showConfirm("@confirm", Core.bundle.format("confirmban", p.lastName), () -> {
                        //todo добавить кода
                    }));
                    t.button(Icon.cancelSmall, Styles.cleari, () -> {
                        data.remove(p);
                        updateProbiv(data);
                    });
                }
            }).growX().height(36f).row();
            probivData.table(Tex.whitePane, t -> {
                t.setColor(0.5f, 0.5f, 0.5f, 1);
                t.left();

                t.add("Nickname");
                t.add("|");
                t.add(p.lastName);
                t.button(Icon.copySmall, () -> Core.app.setClipboardText(p.lastName)).size(10f);
                t.row();
                t.add("UUID");
                t.add("|");
                t.add(p.uuid);
                t.button(Icon.copySmall, () -> Core.app.setClipboardText(p.uuid)).size(10f);
                t.row();
                t.add("IP");
                t.add("|");
                t.add(p.lastIP);
                t.button(Icon.copySmall, () -> Core.app.setClipboardText(p.lastIP)).size(10f);
                t.row();
                t.row();
                String n = p.names.first();
                p.names.remove(0);
                t.add("Names");
                t.add(">");
                t.add(n);
                String finalN = n;
                t.button(Icon.copySmall, () -> Core.app.setClipboardText(finalN)).size(10f);
                t.row();
                for (String name : p.names) {
                    t.add(" ");
                    t.add(">");
                    t.add(name);
                    t.button(Icon.copySmall, () -> Core.app.setClipboardText(name)).size(10f);
                    t.row();
                }
                n = p.ips.first();
                p.ips.remove(0);
                t.add("IPs");
                t.add(">");
                t.add(n);
                String finalN1 = n;
                t.button(Icon.copySmall, () -> Core.app.setClipboardText(finalN1)).size(10f);
                t.row();
                for (String name : p.ips) {
                    t.add(" ");
                    t.add(">");
                    t.add(name);
                    t.button(Icon.copySmall, () -> Core.app.setClipboardText(name)).size(10f);
                    t.row();
                }
            });
            probivData.row();
            probivData.add(" ");
            probivData.row();
        });
    }

    public static void addBanPlayer(BannedPlayer p) {
        if (banned.find(pl -> Objects.equals(p.uuid, pl.uuid)) != null) return;
        banned.add(p);
        updateBanned(banned);
    }

    public static void addProbived(InfoPlayer p) {
        if (players.find(pl -> Objects.equals(p.uuid, pl.uuid)) != null) return;
        players.add(p);
        players = players.filter(pl -> {
            for (String name : pl.names) {
                if (name.contains(searcher2.getText())) return true;
            }
            return pl.uuid.contains(searcher2.getText()) || pl.lastName.contains(searcher2.getText());
        });
        updateProbiv(players);
    }

    public static void updateProbivRequest(String key) {
        players.clear();
        updateProbiv(players);
        searcher2.setText(key);
        Call.serverPacketReliable("info_request", key);
    }

    public static class BannedPlayer {
        public String name, uuid, adminName, reason;
        public long unbanDate;

        public BannedPlayer() {}
    }

    public static class InfoPlayer {
        public String lastName, lastIP, uuid;
        boolean admin;
        Seq<String> names, ips;

        public InfoPlayer() {}
    }
}
