package admintools;

import arc.scene.ui.layout.Table;
import arc.util.Reflect;
import arc.util.Strings;
import mindustry.Vars;
import mindustry.ctype.UnlockableContent;
import mindustry.gen.Iconc;

import java.time.Instant;

import static admintools.AdminTools.shortDateFormat;

public class HistoryFrame {
    public static TravelerFragment historyBF;
    public static Table table = new Table();

    public static void update(TransportableHistoryStack stack){
        table.clear();
        table.labelWrap("История тайл ("+stack.x+"; "+stack.y+")");
        table.row();
        for(var nya : stack.data){
            if(nya == null) continue;
            table.add(Strings.stripColors(nya.name));
            table.add("  |  ");
            table.add(emoji(Vars.content.block(nya.blockID))+""); // че за шиза
            table.add("  |  ");
            table.add(nya.type.name());
            table.add("  |  ");
            table.add(shortDateFormat.format(Instant.ofEpochMilli(nya.time))); // я отойду // поесть
            table.row();
        }
    }

    public static class TransportableHistoryStack {
        int x;
        int y;
        HistoryEntry[] data;

        public TransportableHistoryStack() {

        }
    }
    public static char emoji(UnlockableContent content) {
        try {
            return Reflect.get(Iconc.class, Strings.kebabToCamel(content.getContentType().name() + "-" + content.name));
        } catch (Exception e) {
            return '?';
        }
    }
}
