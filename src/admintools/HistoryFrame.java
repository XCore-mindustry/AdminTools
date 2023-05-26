package admintools;

import arc.scene.ui.layout.Table;
import arc.util.Strings;
import mindustry.gen.Iconc;

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
            var state = st[i];
            table1.add(Strings.stripColors(state.name));
            table1.add("  |  ");
            table1.add(state.blockEmoji());
            table1.add("  |  ");
            table1.add(state.rotationAsString() + "");
            table1.add("  |  ");
            table1.add(state.config.length() > 15 ? state.config.substring(0, 15) : state.config);
            table1.add("  |  ");
            table1.add(state.timeAsString());
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
}
