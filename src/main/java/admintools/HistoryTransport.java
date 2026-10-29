package admintools;

import arc.Core;
import arc.Events;
import arc.util.Log;
import arc.util.Timer;
import mindustry.Vars;
import mindustry.game.EventType;
import mindustry.gen.Call;
import mindustry.io.JsonIO;

public final class HistoryTransport {
    public static final HistorySession state = new HistorySession();
    private static long nextRequest;
    private static Timer.Task queued;
    private static Timer.Task timeout;
    private HistoryTransport() {}
    public static class Request {
        public long requestId;
        public String type = "tile", uuid = "", cursor = "";
        public int x, y;
    }
    public static void init() {
        Vars.netClient.addPacketHandler("tilelogger_session", text -> {
            try {
                var page = HistoryWire.read(HistoryPage.class, text);
                if (page != null && page.version == 2) {
                    state.session(page.sessionId);
                    HistoryFrame.update(state.entries());
                }
            } catch (RuntimeException invalid) {
                Log.warn("[AdminTools] Invalid history session");
            }
        });
        Vars.netClient.addPacketHandler("tilelogger_history_v2", text -> {
            if (text.length() > 16384)
                return;
            try {
                if (state.accept(HistoryWire.read(HistoryPage.class, text))) {
                    if (timeout != null)
                        timeout.cancel();
                    HistoryFrame.update(state.entries());
                }
            } catch (RuntimeException invalid) {
                Log.warn("[AdminTools] Invalid history page");
            }
        });
        Vars.netClient.addPacketHandler("tilelogger_history_tile", HistoryTransport::legacy);
        Vars.netClient.addPacketHandler("tilelogger_history_player", HistoryTransport::legacy);
        Vars.netClient.addPacketHandler("adm_mod_begin", text -> hello());
        Events.on(EventType.WorldLoadEvent.class, event -> hello());
        Events.on(EventType.ClientServerConnectEvent.class, event -> reset());
        Events.on(EventType.ResetEvent.class, event -> reset());
        Events.on(EventType.TapEvent.class, event -> {
            if (event.player != Vars.player || event.tile == null || !state.ready())
                return;
            var tile = event.tile.build == null ? event.tile : event.tile.build.tile;
            Request request = new Request();
            request.requestId = ++nextRequest;
            request.x = tile.x;
            request.y = tile.y;
            state.begin(request.requestId);
            HistoryFrame.update(state.entries());
            send(request);
        });
    }
    private static void legacy(String text) {
        if (state.ready() || text.length() > 16384)
            return;
        try {
            var entries = JsonIO.read(HistoryEntry[].class, text);
            if (entries != null) {
                for (var entry : entries) {
                    entry.time &= 0xffff;
                    entry.legacy = true;
                }
                HistoryFrame.update(entries);
            }
        } catch (RuntimeException invalid) {
            Log.warn("[AdminTools] Invalid legacy history");
        }
    }
    private static void hello() {
        if (Vars.net.active() && Vars.net.client())
            Call.serverPacketReliable("tilelogger_hello", "2");
    }
    public static void reset() {
        if (queued != null)
            queued.cancel();
        if (timeout != null)
            timeout.cancel();
        state.reset();
        HistoryFrame.update(new HistoryEntry[0]);
    }
    private static void send(Request request) {
        if (queued != null)
            queued.cancel();
        if (timeout != null)
            timeout.cancel();
        String session = state.session();
        queued = Timer.schedule(() -> {
            if (!state.session().equals(session) || state.request() != request.requestId
                    || !Vars.net.active())
                return;
            Call.serverPacketReliable("tilelogger_history_request", HistoryWire.write(request));
            timeout = Timer.schedule(() -> {
                state.timeout(request.requestId);
                HistoryFrame.refreshStatus();
            }, 5f);
        }, 0.12f);
        HistoryFrame.refreshStatus();
    }
    public static void more() {
        if (!state.more())
            return;
        Request request = new Request();
        request.requestId = state.request();
        request.cursor = state.cursor();
        state.loading();
        send(request);
    }
}
