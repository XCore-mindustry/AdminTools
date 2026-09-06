package admintools;

import admintools.ui.components.DataTable;
import admintools.ui.components.SearchField;
import arc.scene.ui.layout.Table;
import arc.struct.Seq;
import arc.util.Strings;

public class HistoryFrame {
    public static final Seq<HistoryEntry> currentEntries = new Seq<>();
    public static DataTable<HistoryEntry> dataTable;

    public static Table buildView() {
        Table view = new Table();
        view.top().left();

        dataTable = new DataTable<>();
        // Smart declarative columns:
        // - "Player": flex column taking available space with graceful ellipsis
        // - "Block" and "Rotation": auto-measured & auto-centered icon columns
        // - "Config": flex column with 0.6 weight
        // - "Time": auto-measured time column fitting full HH:mm:ss without truncation
        dataTable.text(arc.Core.bundle.get("admintools.table.player"), e -> Strings.stripColors(e.name)).flex();
        dataTable.icon(arc.Core.bundle.get("admintools.history.block"), HistoryEntry::blockEmoji);
        dataTable.icon(arc.Core.bundle.get("admintools.history.rotation"), HistoryEntry::rotationAsString);
        dataTable.text(arc.Core.bundle.get("admintools.history.config"), e -> e.config).flex(0.6f);
        dataTable.time(arc.Core.bundle.get("admintools.history.time"), HistoryEntry::timeAsString);

        dataTable.pageSize(7);

        SearchField search = new SearchField(arc.Core.bundle.get("admintools.search.history"), query -> {
            String q = query.toLowerCase().trim();
            dataTable.setFilter(e -> q.isEmpty() ||
                (e.name != null && Strings.stripColors(e.name).toLowerCase().contains(q)) ||
                (e.config != null && e.config.toLowerCase().contains(q)));
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
