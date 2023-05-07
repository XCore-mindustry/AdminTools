package admintools;

import arc.scene.ui.layout.Table;
import arc.util.Reflect;
import arc.util.Strings;
import mindustry.Vars;
import mindustry.content.Blocks;
import mindustry.ctype.UnlockableContent;
import mindustry.gen.Iconc;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class HistoryFrame {
    public static Table table = new Table();

    static HistoryEntry[] st;
    static int pageId = 0;

    public static void update(HistoryEntry[] stack) {
        st = stack;
        pageId = 0;

        if (stack.length < 1) {
            table.clear();
            return;
        } else {
            openPage(0);
        }
    }

    public static boolean openPage(int id) {
        if (id + 1 > Math.ceil(st.length / 6f) || id < 0) {
            return false;
        }
        table.clear();

        table.add("История тайл (" + st[0].x + "; " + st[0].y + ")");
        table.row();

        var t = new Table();

        t.button(Iconc.left + "", () -> {
            changePage(false);
        });
        t.add((id + 1) + "/" + (int) Math.ceil(st.length / 6f));
        t.button(Iconc.right + "", () -> {
            changePage(true);
        });
        table.add(t);

        table.row();


        int iEnd = Math.max(0, st.length - (id * 6));
        int iStart = Math.max(0, st.length - ((id + 1) * 6));

        Table table1 = new Table();
        for (int i = iStart; i < iEnd; i++) {
            var nya = st[i];
            table1.add(Strings.stripColors(nya.name));
            table1.add("  |  ");
            table1.add((!nya.valid ? "[red]" : "") + emoji(Vars.content.block(nya.block)));
            table1.add("  |  ");
            table1.add((!nya.valid ? "[red]" : "") + nya.rotationAsString());
            table1.add("  |  ");
            table1.add(nya.config.length() > 15 ? nya.config.substring(0, 15) : nya.config);
            table1.add("  |  ");
            table1.add((!nya.valid ? "[red]" : "") + LocalTime.MIN.plusSeconds(nya.time).format(DateTimeFormatter.ISO_LOCAL_TIME)); // я отойду // поесть
            table1.row();
        }
        table.add(table1);
        table.row();

        return true;
    }

    public static void changePage(boolean plus) {
        int tdx = plus ? pageId + 1 : pageId - 1;
        if (openPage(tdx)) {
            pageId = tdx;
        }
    }

    public static char emoji(UnlockableContent content) {
        try {
            return Reflect.get(Iconc.class, Strings.kebabToCamel(content.getContentType().name() + "-" + content.name));
        } catch (Exception e) {
            if (content == Blocks.air) {
                return 'X';
            }
            return '?';
        }
    }
}
