package admintools;

import java.util.TreeMap;

/** Pure request/session ordering, independent of the game UI and transport. */
public final class HistorySession {
    public static final int MAX_ENTRIES = 4096;
    private String session = "", cursor = "";
    private long activeRequest, latestPush;
    private boolean busy, truncated, failed;
    private final TreeMap<Long, HistoryEntry> entries = new TreeMap<>();
    public void reset() {
        session = "";
        activeRequest = 0;
        latestPush = 0;
        cursor = "";
        busy = false;
        truncated = false;
        failed = false;
        entries.clear();
    }
    public void session(String value) {
        if (value == null || value.isEmpty())
            return;
        if (!value.equals(session)) {
            reset();
            session = value;
        }
    }
    public boolean ready() {
        return !session.isEmpty();
    }
    public String session() {
        return session;
    }
    public long request() {
        return activeRequest;
    }
    public void begin(long id) {
        activeRequest = id;
        cursor = "";
        busy = true;
        truncated = false;
        failed = false;
        entries.clear();
    }
    public boolean more() {
        return !busy && !cursor.isEmpty() && entries.size() < MAX_ENTRIES;
    }
    public boolean busy() {
        return busy;
    }
    public boolean failed() {
        return failed;
    }
    public boolean truncated() {
        return truncated;
    }
    public String cursor() {
        return cursor;
    }
    public void loading() {
        busy = true;
        failed = false;
    }
    public void timeout(long id) {
        if (activeRequest == id) {
            busy = false;
            failed = true;
        }
    }
    public boolean accept(HistoryPage page) {
        if (!ready() || page == null || page.version != 2 || !session.equals(page.sessionId)
                || page.entries == null || page.entries.length > 32)
            return false;
        for (HistoryEntry entry : page.entries) {
            if (entry == null || entry.sequence <= 0 || entry.time < 0 || entry.x < 0 || entry.y < 0
                    || entry.x > 65535 || entry.y > 65535)
                return false;
        }
        if (page.nextCursor != null && page.nextCursor.length() > 64)
            return false;
        if (page.requestId < 0 && page.requestId < latestPush) {
            latestPush = page.requestId;
            begin(page.requestId);
        }
        if (page.requestId == 0 || page.requestId != activeRequest)
            return false;
        boolean discarded = false;
        for (HistoryEntry entry : page.entries) {
            if (entries.size() < MAX_ENTRIES || entries.containsKey(entry.sequence))
                entries.put(entry.sequence, entry);
            else
                discarded = true;
        }
        cursor = page.nextCursor == null ? "" : page.nextCursor;
        truncated = page.truncated || discarded;
        busy = false;
        failed = false;
        if (entries.size() >= MAX_ENTRIES) {
            truncated |= !cursor.isEmpty();
            cursor = "";
        }
        return true;
    }
    public HistoryEntry[] entries() {
        return entries.values().toArray(new HistoryEntry[0]);
    }
}
