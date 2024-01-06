package admintools;

import arc.scene.ui.layout.Table;
import arc.util.Strings;
import mindustry.gen.Iconc;

public class HistoryFrame {
    public static final Table table = new Table();
    static HistoryEntry[] stack;
    static int pageId = 0;

    public static void update(HistoryEntry[] stack) {
        HistoryFrame.stack = stack;
        pageId = 0;

        if (stack.length < 1) table.clear();
        else openPage(0);
    }

    public static boolean openPage(int id) {
        if (id + 1 > Math.ceil(stack.length / 6f) || id < 0) return false;

        table.clear();

        table.add("Tile History [" + stack[0].x + "/" + stack[0].y + "]");
        table.row();

        var t = new Table();

        t.button(Iconc.left + "", () -> changePage(false));

        t.add((id + 1) + "/" + (int) Math.ceil(stack.length / 6f));

        t.button(Iconc.right + "", () -> changePage(true));

        table.add(t);
        table.row();


        int iEnd = Math.max(0, stack.length - (id * 6));
        int iStart = Math.max(0, stack.length - ((id + 1) * 6));

        Table table1 = new Table();

        for (int i = iStart; i < iEnd; i++) {
            table1.add(Strings.stripColors(stack[i].name));
            table1.add("  |  ");
            table1.add(stack[i].blockEmoji());
            table1.add("  |  ");
            table1.add(stack[i].rotationAsString() + "");
            table1.add("  |  ");
            table1.add(stack[i].config.length() > 15 ? stack[i].config.substring(0, 15) : stack[i].config);
            table1.add("  |  ");
            table1.add(stack[i].timeAsString());
            table1.row();
        }

        table.add(table1);
        table.row();

        return true;
    }

    public static void changePage(boolean plus) {
        int tdx = plus ? pageId + 1 : pageId - 1;

        if (openPage(tdx)) pageId = tdx;
    }
}
