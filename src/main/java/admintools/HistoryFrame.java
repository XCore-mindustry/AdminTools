package admintools;

import admintools.ui.components.DataTable;
import admintools.ui.components.SearchField;
import arc.scene.ui.layout.Table;
import arc.struct.Seq;
import arc.util.Align;
import arc.util.Strings;

public class HistoryFrame {
    public static final Seq<HistoryEntry> currentEntries = new Seq<>();
    public static DataTable<HistoryEntry> dataTable;

    public static Table buildView() {
        Table view = new Table();
        view.top().left();

        dataTable = new DataTable<>();
        dataTable.column("Игрок", e -> Strings.stripColors(e.name)); // flexible with ellipsis
        dataTable.column("Блок", HistoryEntry::blockEmoji, false).align(Align.center);
        dataTable.column("Поворот", e -> String.valueOf(e.rotationAsString()), false).align(Align.center);
        dataTable.column("Конфиг", e -> e.config.length() > 14 ? e.config.substring(0, 14) + ".." : e.config);
        dataTable.column("Время", HistoryEntry::timeAsString).align(Align.center);

        dataTable.pageSize(7);

        SearchField search = new SearchField("Поиск по истории...", query -> {
            String q = query.toLowerCase().trim();
            dataTable.setFilter(e -> q.isEmpty() || Strings.stripColors(e.name).toLowerCase().contains(q) || e.config.toLowerCase().contains(q));
        });

        view.add(search).growX().padBottom(10f).row();
        view.add(dataTable).grow();

        return view;
    }

    public static void update(HistoryEntry[] stack) {
        currentEntries.clear();
        if (stack != null) {
            currentEntries.addAll(stack);
        }
        if (dataTable != null) {
            dataTable.setItems(currentEntries);
        }
    }
}
