package admintools;

/** TileLogger v2 wire response; old packet names remain supported independently. */
public class HistoryPage {
    public int version;
    public long requestId;
    public String sessionId, nextCursor, query;
    public boolean truncated;
    public HistoryEntry[] entries;
}
