package admintools;

import arc.util.Reflect;
import arc.util.Strings;
import mindustry.Vars;
import mindustry.gen.Iconc;
import mindustry.world.Block;

public class HistoryEntry {
    public int x;
    public int y;
    public String name;
    public String uuid;
    public boolean valid;
    public long time;
    public long sequence;
    public boolean legacy;
    public int action_kind;
    public String action = "";
    public int block;
    public boolean destroy;
    public int rotation;
    public int config_type;
    public String config;

    public Block block() {
        var blocks = Vars.content.blocks();
        return block >= 0 && block < blocks.size ? blocks.get(block) : null;
    }

    public String blockEmoji() {
        try {
            if (block == 0)
                return destroy ? "[red]X[]" : "[green][]";
            if (block >= 3 && block <= 11)
                return "[green]" + (block - 2) + "[]";
            return (destroy ? "[red]" : "[]")
                    + Reflect.get(Iconc.class,
                            Strings.kebabToCamel(block().getContentType().name() + "-" + block().name))
                    + "[]";
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
        long t = legacy ? time & 0xffff : time;
        long hours = t / 3600;
        long minutes = (t % 3600) / 60;
        long seconds = t % 60;
        String formatted = String.format("%02d:%02d:%02d", hours, minutes, seconds);
        return (valid ? "[white]" : "[gray]") + formatted + "[]";
    }
}
