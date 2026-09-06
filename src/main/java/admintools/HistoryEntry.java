package admintools;

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

    public String rotationAsString() {
        Block b = block();
        if (b == null || !b.rotate) {
            return "[gray]-[]";
        }
        return switch (rotation) {
            case 0 -> Iconc.right + "";
            case 1 -> Iconc.up + "";
            case 2 -> Iconc.left + "";
            case 3 -> Iconc.down + "";
            default -> "[gray]-[]";
        };
    }

    public String timeAsString() {
        int t = Short.toUnsignedInt(time);
        int hours = t / 3600;
        int minutes = (t % 3600) / 60;
        int seconds = t % 60;
        String formatted = String.format("%02d:%02d:%02d", hours, minutes, seconds);
        return (valid ? "[white]" : "[gray]") + formatted + "[]";
    }
}
