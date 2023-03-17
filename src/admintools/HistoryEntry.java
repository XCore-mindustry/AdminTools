package admintools;

public class HistoryEntry {
    public String name;
    public Type type;
    public short blockID;
    public long time;

    public enum Type {
        built, broke, configured
    }
}
