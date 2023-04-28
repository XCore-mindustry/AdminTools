package admintools;

import mindustry.Vars;

public class HistoryEntry {
    public short x;
    public short y;

    public String name;
    public String uuid;
    public short time;
    public short block;
    public short rotation;
    public short config_type;
    public String config;

    public HistoryEntry() {
    }

    ;

    public HistoryEntry(short x, short y, String name, String uuid, short time, short block, short rotation, short config_type, String config) {
        this.x = x;
        this.y = y;
        this.name = name;
        this.uuid = uuid;
        this.time = time;
        this.block = block;
        this.rotation = rotation;
        this.config_type = config_type;
        this.config = config;
    }

    public String rotationAsString() {
        if (!Vars.content.block(block).rotate)
            return " ";
        switch (rotation) {
            case 0:
                return "";
            case 1:
                return "";
            case 2:
                return "";
            case 3:
                return "";
            default:
                return "?";
        }
    }
}
