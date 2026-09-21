package admintools;

import admintools.input.FreeCamController;
import admintools.ui.components.ToastManager;
import admintools.ui.theme.LucidTheme;
import admintools.ui.window.FloatingWindow;
import admintools.ui.window.WindowManager;
import admintools.ui.window.WindowSpec;
import arc.Core;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.input.KeyCode;
import arc.math.geom.Rect;
import arc.scene.ui.Label;
import arc.scene.ui.ScrollPane;
import arc.scene.ui.TextButton;
import arc.scene.ui.layout.Table;
import arc.struct.Seq;
import arc.util.Strings;
import mindustry.Vars;
import mindustry.content.Blocks;
import mindustry.gen.Icon;
import mindustry.gen.Iconc;
import mindustry.graphics.Pal;
import mindustry.ui.Styles;

import static arc.Core.camera;
import static mindustry.Vars.renderer;
import static mindustry.Vars.ui;

public class UIController {
    public static final String IP = "159.195.205.212";
    public static final Rect bounds = new Rect();

    public final WindowManager windowManager;
    public final FloatingWindow karmaWindow;
    public final FloatingWindow historyWindow;
    public final FloatingWindow portalWindow;

    public final Seq<HistoryEntry> preview = new Seq<>();

    public static class ServerEntry {
        public final int port;
        public final String name;
        public final int players;
        public final int ping;

        public ServerEntry(int port, String name, int players, int ping) {
            this.port = port;
            this.name = name;
            this.players = players;
            this.ping = ping;
        }
    }

    private final Seq<ServerEntry> onlineServers = new Seq<>();
    private Table serverListTable;
    private Label serverCountLabel;
    private int currentPingSession = 0;

    public static void connect(int port) {
        if (Vars.net.client() || Vars.net.active()) {
            Vars.netClient.disconnectQuietly();
        }
        Vars.player.name(Core.settings.getString("name"));
        ui.join.connect(IP, port);
    }

    public UIController() {
        windowManager = new WindowManager();
        ToastManager.install();

        // 1. Karma Window
        karmaWindow = windowManager.createWindow(
            WindowSpec.of("karma", Core.bundle.get("admintools.window.karma"), Icon.hammer, 540f, 380f),
            win -> win.content(body -> body.add(KarmaDetector.buildView()).grow())
        );

        // 2. History Window
        historyWindow = windowManager.createWindow(
            WindowSpec.of("history", Core.bundle.get("admintools.window.history"), Icon.book, 580f, 380f),
            win -> win.content(body -> body.add(HistoryFrame.buildView()).grow())
        );

        // 3. Portal Window
        portalWindow = windowManager.createWindow(
            WindowSpec.of("portal", Core.bundle.get("admintools.window.portal"), Icon.host, 480f, 400f),
            win -> win.content(this::buildPortalContent)
        );

        // Desktop hotkeys (Ctrl + K, Ctrl + H, Ctrl + P)
        windowManager.bindHotkey(KeyCode.k, "karma");
        windowManager.bindHotkey(KeyCode.h, "history");
        windowManager.bindHotkey(KeyCode.p, "portal");

        // Install into Scene
        windowManager.install(Core.scene);
        FreeCamController.get().installHUD();

        // World tile preview renderer for history
        renderer.addEnvRenderer(0, () -> {
            camera.bounds(bounds);
            preview.each(t -> {
                var tile = Vars.world.tile(t.x, t.y);
                var block = Vars.content.block(t.block);

                if (block == Blocks.air) {
                    if (tile != null && tile.floor() != null) tile.floor().drawBase(tile);
                } else if (block != null && block.region != null) {
                    Draw.rect(block.region, t.x, t.y, t.rotation);
                }
            });
        });
    }

    private void buildPortalContent(Table body) {
        body.top().left();

        // Header toolbar: server count label + refresh button
        Table topBar = new Table();
        topBar.left();
        topBar.margin(0f, 2f, 8f, 2f);

        serverCountLabel = topBar.add(Core.bundle.get("admintools.portal.searching")).color(LucidTheme.textDim).left().growX().get();
        serverCountLabel.setFontScale(0.85f);

        TextButton refreshBtn = new TextButton(Iconc.refresh + " " + Core.bundle.get("admintools.portal.refresh"), LucidTheme.flatTextButtonStyle());
        refreshBtn.getLabel().setFontScale(0.85f);
        refreshBtn.clicked(this::refreshServers);
        topBar.add(refreshBtn).size(100f, 28f);

        body.add(topBar).growX().row();

        serverListTable = new Table();
        serverListTable.top().left();

        ScrollPane pane = new ScrollPane(serverListTable);
        pane.setOverscroll(false, false);
        pane.setScrollingDisabled(true, false);

        body.add(pane).grow();

        refreshServers();
    }

    private void refreshServers() {
        final int session = ++currentPingSession;
        onlineServers.clear();
        rebuildServerList();
        if (serverCountLabel != null) serverCountLabel.setText(Core.bundle.get("admintools.portal.pinging"));

        for (int port = 7000; port <= 7025; port++) {
            int currentPort = port;
            Vars.net.pingHost(IP, currentPort,
                host -> {
                    // Ignore stale responses from previous refresh sessions
                    if (session != currentPingSession) return;

                    String name = host.name;
                    name = Strings.stripColors(name);
                    name = name.replaceAll("(?i)x?core", "");
                    name = name.replaceAll("[\uE000-\uF8FF]", "");
                    name = name.replaceAll("^[\\s>›»—–\\-:xX]+", "").trim();

                    ServerEntry entry = new ServerEntry(currentPort, name.trim(), host.players, host.ping);

                    // Strictly deduplicate by port so duplicate entries can never occur
                    onlineServers.removeAll(s -> s.port == currentPort);
                    onlineServers.add(entry);
                    onlineServers.sort(s -> s.port);

                    if (serverCountLabel != null) {
                        serverCountLabel.setText(Core.bundle.format("admintools.portal.online", onlineServers.size));
                    }
                    rebuildServerList();
                },
                exception -> {
                    // Offline port - simply not added, ZERO ghost cells!
                }
            );
        }
    }

    private void rebuildServerList() {
        if (serverListTable == null) return;
        serverListTable.clear();

        if (onlineServers.isEmpty()) {
            serverListTable.add(Core.bundle.get("admintools.portal.empty")).pad(24f).center().row();
            return;
        }

        for (int i = 0; i < onlineServers.size; i++) {
            ServerEntry s = onlineServers.get(i);

            Table row = new Table();
            row.background(LucidTheme.glass(LucidTheme.bgCard, LucidTheme.borderSubtle));
            row.margin(6f, 10f, 6f, 10f);
            row.left();

            // Port badge (pill style)
            Table portBadge = new Table();
            portBadge.background(LucidTheme.badgeBg(Pal.accent));
            portBadge.margin(2f, 6f, 2f, 6f);
            Label pLbl = portBadge.add(String.valueOf(s.port)).color(Pal.accent).get();
            pLbl.setFontScale(0.85f);
            row.add(portBadge).padRight(8f);

            // Server name
            Label nameLabel = row.add(s.name).left().growX().get();
            nameLabel.setFontScale(0.9f);
            nameLabel.setColor(Color.white);

            // Ping indicator
            Label pingLbl = row.add("[gray]" + s.ping + "ms[]").padRight(8f).get();
            pingLbl.setFontScale(0.80f);

            // Players indicator
            Label playersLbl = row.add(Core.bundle.format("admintools.portal.players", s.players)).color(s.players > 0 ? LucidTheme.textMuted : LucidTheme.textDim).padRight(4f).get();
            playersLbl.setFontScale(0.85f);

            var listener = row.clicked(() -> connect(s.port));
            row.update(() -> row.background(listener.isOver() ? LucidTheme.glass(LucidTheme.bgHover, LucidTheme.accent) : LucidTheme.glass(LucidTheme.bgCard, LucidTheme.borderSubtle)));

            serverListTable.add(row).growX().padBottom(5f).row();
        }
    }

    public void updatePreview(HistoryEntry[] p) {
        preview.clear();
        if (p != null) preview.addAll(p);
    }

    public void setHideAll(boolean hide) {
        windowManager.setHideAll(hide);
    }

    public boolean isHideAll() {
        return windowManager.isHideAll();
    }
}
