package admintools;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

import arc.util.Reflect;
import arc.util.Strings;
import mindustry.Vars;
import mindustry.gen.Iconc;
import mindustry.world.Block;

public class HistoryEntry {
    public short x;
    public short y;
    public String name;
    public String uuid;
    public boolean valid;
    public short time;
    public short block;
    public boolean destroy;
    public short rotation;
    public short config_type;
    public String config;

    public Block block() {
        return Vars.content.block(block);
    }

    public String blockEmoji() {
        try {
            if (block == 0) return destroy ? "[red]X[]" : "[green][]";
            if (block >= 3 && block <= 11) return "[green]" + (block - 2) + "[]";
            return (destroy ? "[red]" : "[]") + Reflect.get(Iconc.class, Strings.kebabToCamel(block().getContentType().name() + "-" + block().name)) + "[]";
        } catch (Exception e) {
            return "[scarlet]" + block + "[]";
        }
    }

    public Object rotationAsString() {
        if (!block().rotate)
            return null;
        return switch (rotation) {
            case 0 -> "";
            case 1 -> "";
            case 2 -> "";
            case 3 -> "";
            default -> "?";
        };
    }

    public String timeAsString() {
        return (valid ? "[white]" : "[gray]") + LocalTime.MIN.plusSeconds(time).format(DateTimeFormatter.ISO_LOCAL_TIME) + "[]";
    }
}
