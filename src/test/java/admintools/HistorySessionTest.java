package admintools;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class HistorySessionTest {
    private HistoryPage page(String session, long request, long... sequences) {
        var page = new HistoryPage();
        page.version = 2;
        page.requestId = request;
        page.sessionId = session;
        page.nextCursor = "";
        page.entries = new HistoryEntry[sequences.length];
        for (int i = 0; i < sequences.length; i++) {
            var row = new HistoryEntry();
            row.sequence = sequences[i];
            row.time = 70000;
            page.entries[i] = row;
        }
        return page;
    }
    @Test
    void rejectsStaleRequestsAndOtherSessions() {
        var state = new HistorySession();
        state.session("map-a");
        state.begin(1);
        state.begin(2);
        assertFalse(state.accept(page("map-a", 1, 1)));
        assertFalse(state.accept(page("map-b", 2, 2)));
        assertTrue(state.accept(page("map-a", 2, 3)));
        assertEquals(3, state.entries()[0].sequence);
        state.session("map-b");
        assertEquals(0, state.entries().length);
        assertFalse(state.accept(page("map-a", 2, 3)));
    }
    @Test
    void continuationDeduplicatesAndOrdersByStableSequence() {
        var state = new HistorySession();
        state.session("map");
        state.begin(1);
        var first = page("map", 1, 8, 9);
        first.nextCursor = "cursor";
        first.truncated = true;
        assertTrue(state.accept(first));
        assertTrue(state.more());
        state.loading();
        assertFalse(state.more());
        assertTrue(state.accept(page("map", 1, 6, 7, 8)));
        assertEquals(4, state.entries().length);
        assertEquals(6, state.entries()[0].sequence);
        assertEquals(9, state.entries()[3].sequence);
        assertEquals(70000, state.entries()[0].time);
        assertFalse(state.more());
    }
    @Test
    void newestServerPushSupersedesOlderReplies() {
        var state = new HistorySession();
        state.session("map");
        state.begin(100);
        assertTrue(state.accept(page("map", -2, 2)));
        assertFalse(state.accept(page("map", -1, 1)));
        assertFalse(state.accept(page("map", 100, 3)));
        assertEquals(2, state.entries()[0].sequence);
    }
    @Test
    void malformedPushNeverClearsCurrentData() {
        var state = new HistorySession();
        state.session("map");
        state.begin(1);
        state.accept(page("map", 1, 1));
        assertFalse(state.accept(page("map", -1, -4)));
        assertEquals(1, state.entries()[0].sequence);
        assertEquals(1, state.request());
    }
    @Test
    void resetAndTimeoutReleaseBusyState() {
        var state = new HistorySession();
        state.session("map");
        state.begin(1);
        state.timeout(0);
        assertTrue(state.busy());
        state.timeout(1);
        assertFalse(state.busy());
        state.reset();
        assertFalse(state.ready());
    }
}
