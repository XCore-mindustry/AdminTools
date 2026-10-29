package admintools;

import admintools.ui.components.DataTable;
import admintools.ui.components.SearchField;
import arc.scene.ui.layout.Table;
import arc.struct.Seq;
import arc.util.Strings;

public class HistoryFrame {
    public static final Seq<HistoryEntry> currentEntries = new Seq<>();
    public static DataTable<HistoryEntry> dataTable;
    private static arc.scene.ui.Label status;
    private static arc.scene.ui.TextButton more;

    public static Table buildView() {
        Table view = new Table();
        view.top().left();

        dataTable = new DataTable<>();
        // Bound flex-column measurement so long action/config previews cannot push the clock off-screen.
        dataTable.text(arc.Core.bundle.get("admintools.table.player"), e -> Strings.stripColors(e.name))
                .flex(1.2f).minWidth(90f).measure(() -> arc.scene.ui.layout.Scl.scl(96f));
        dataTable.text(arc.Core.bundle.get("admintools.history.block"),
                e -> e.blockEmoji() + " " + e.rotationAsString(), false).auto().minWidth(54f).align(arc.util.Align.center);
        dataTable.time(arc.Core.bundle.get("admintools.history.time"), HistoryEntry::timeAsString).minWidth(90f);
        dataTable.text(arc.Core.bundle.get("admintools.history.config"),
                e -> (e.action == null || e.action.isBlank() ? "" : e.action + ": ") + (e.config == null ? "" : e.config))
                .flex(0.8f).minWidth(60f).measure(() -> arc.scene.ui.layout.Scl.scl(80f));

        dataTable.pageSize(7);

        SearchField search = new SearchField(arc.Core.bundle.get("admintools.search.history"), query -> {
            String q = query.toLowerCase().trim();
            dataTable.setFilter(e
                    -> q.isEmpty()
                            || (e.name != null && Strings.stripColors(e.name).toLowerCase().contains(q))
                            || (e.config != null && e.config.toLowerCase().contains(q)));
        });

        view.add(search).growX().padBottom(8f).row();
        view.add(dataTable).grow().row();
        status = new arc.scene.ui.Label("");
        status.setWrap(true);
        view.add(status).growX().padTop(6f).row();
        more = new arc.scene.ui.TextButton(arc.Core.bundle.get("admintools.history.more"));
        more.clicked(HistoryTransport::more);
        view.add(more).growX().height(40f).padTop(4f);
        refreshStatus();

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
        refreshStatus();
    }
    public static void refreshStatus() {
        if (more == null || status == null)
            return;
        more.setDisabled(!HistoryTransport.state.more());
        status.setText(HistoryTransport.state.failed() ? arc.Core.bundle.get("admintools.history.failed")
                        : HistoryTransport.state.busy()
                        ? arc.Core.bundle.get("admintools.history.loading")
                        : arc.Core.bundle.format("admintools.history.loaded", currentEntries.size)
                                + (HistoryTransport.state.truncated()
                                                ? " · " + arc.Core.bundle.get("admintools.history.partial")
                                                : ""));
    }
}
