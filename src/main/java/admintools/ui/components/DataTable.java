package admintools.ui.components;

import admintools.ui.theme.LucidTheme;
import arc.func.Cons2;
import arc.func.Func;
import arc.scene.event.Touchable;
import arc.scene.ui.Label;
import arc.scene.ui.ScrollPane;
import arc.scene.ui.TextButton;
import arc.scene.ui.layout.Table;
import arc.struct.Seq;
import arc.util.Align;
import mindustry.gen.Iconc;
import mindustry.graphics.Pal;
import mindustry.ui.Styles;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * An interactive data table component with unified grid alignment across all rows and headers.
 */
public class DataTable<T> extends Table {
    public static class Column<T> {
        public final String title;
        public final Cons2<Table, T> cellBuilder;
        public final boolean sortable;
        public final Comparator<T> comparator;
        public int align = Align.left;

        public Column(String title, Cons2<Table, T> cellBuilder, boolean sortable, Comparator<T> comparator) {
            this.title = title;
            this.cellBuilder = cellBuilder;
            this.sortable = sortable;
            this.comparator = comparator;
        }

        public Column<T> align(int a) {
            this.align = a;
            return this;
        }
    }

    private final Seq<Column<T>> columns = new Seq<>();
    private final List<T> rawItems = new ArrayList<>();
    private final List<T> filteredItems = new ArrayList<>();
    private Func<T, Boolean> filter = null;

    private int sortedColumnIndex = -1;
    private boolean sortAsc = true;

    private int page = 0;
    private int pageSize = 8;

    private final Table contentTable = new Table();
    private final Table footerTable = new Table();
    private final Label pageInfoLabel = new Label("");
    private final Label countInfoLabel = new Label("");

    public DataTable() {
        background(LucidTheme.glass(LucidTheme.bgCard, LucidTheme.borderSubtle));
        margin(4f);

        contentTable.top().left();

        // Footer table with clean spacing
        footerTable.margin(6f, 12f, 6f, 12f);
        countInfoLabel.setColor(LucidTheme.textDim);
        countInfoLabel.setFontScale(0.85f);
        footerTable.add(countInfoLabel).left().growX();

        var prevBtn = new TextButton(Iconc.left + "", Styles.cleart);
        prevBtn.clicked(() -> changePage(-1));
        footerTable.add(prevBtn).size(28f);

        pageInfoLabel.setColor(Pal.accent);
        pageInfoLabel.setFontScale(0.85f);
        footerTable.add(pageInfoLabel).pad(0, 10f, 0, 10f);

        var nextBtn = new TextButton(Iconc.right + "", Styles.cleart);
        nextBtn.clicked(() -> changePage(1));
        footerTable.add(nextBtn).size(28f);

        ScrollPane pane = new ScrollPane(contentTable);
        pane.setOverscroll(false, false);
        pane.setScrollingDisabled(true, false);
        add(pane).grow().row();
        add(footerTable).growX();
    }

    public Column<T> column(String title, Func<T, String> textExtractor) {
        return column(title, textExtractor, true);
    }

    public Column<T> column(String title, Func<T, String> textExtractor, boolean sortable) {
        Comparator<T> comp = sortable ? Comparator.comparing(textExtractor::get) : null;
        Column<T> col = new Column<>(title, (cell, item) -> {
            Label lbl = cell.add(textExtractor.get(item)).left().growX().minWidth(0).get();
            lbl.setFontScale(0.9f);
            lbl.setEllipsis(true);
        }, sortable, comp);
        columns.add(col);
        refresh();
        return col;
    }

    public Column<T> customColumn(String title, Cons2<Table, T> builder) {
        Column<T> col = new Column<>(title, builder, false, null);
        columns.add(col);
        refresh();
        return col;
    }

    public DataTable<T> pageSize(int size) {
        this.pageSize = Math.max(1, size);
        refresh();
        return this;
    }

    public void setItems(List<T> items) {
        this.rawItems.clear();
        if (items != null) this.rawItems.addAll(items);
        refresh();
    }

    public void setItems(Seq<T> items) {
        this.rawItems.clear();
        if (items != null) {
            for (int i = 0; i < items.size; i++) {
                this.rawItems.add(items.get(i));
            }
        }
        refresh();
    }

    public void setFilter(Func<T, Boolean> filter) {
        this.filter = filter;
        this.page = 0;
        refresh();
    }

    private void changePage(int delta) {
        int maxPages = Math.max(1, (int) Math.ceil(filteredItems.size() / (float) pageSize));
        int newPage = page + delta;
        if (newPage >= 0 && newPage < maxPages) {
            page = newPage;
            rebuildRows();
        }
    }

    private void toggleSort(int colIndex) {
        if (sortedColumnIndex == colIndex) {
            sortAsc = !sortAsc;
        } else {
            sortedColumnIndex = colIndex;
            sortAsc = true;
        }
        refresh();
    }

    public void refresh() {
        filteredItems.clear();
        for (T item : rawItems) {
            if (filter == null || filter.get(item)) {
                filteredItems.add(item);
            }
        }

        if (sortedColumnIndex >= 0 && sortedColumnIndex < columns.size) {
            Column<T> col = columns.get(sortedColumnIndex);
            if (col.comparator != null) {
                filteredItems.sort(sortAsc ? col.comparator : col.comparator.reversed());
            }
        }

        int maxPages = Math.max(1, (int) Math.ceil(filteredItems.size() / (float) pageSize));
        if (page >= maxPages) page = maxPages - 1;

        rebuildRows();
    }

    private void rebuildRows() {
        contentTable.clear();

        int maxPages = Math.max(1, (int) Math.ceil(filteredItems.size() / (float) pageSize));
        int startIndex = page * pageSize;
        int endIndex = Math.min(filteredItems.size(), startIndex + pageSize);

        pageInfoLabel.setText((page + 1) + " / " + maxPages);
        countInfoLabel.setText("Всего: " + filteredItems.size());

        if (filteredItems.isEmpty()) {
            contentTable.add("[lightgray]Нет данных для отображения[]").pad(24f).center().row();
            return;
        }

        // 1. Unified Header row - exact same table columns!
        for (int i = 0; i < columns.size; i++) {
            int colIndex = i;
            Column<T> col = columns.get(i);

            Table colCell = new Table();
            colCell.background(LucidTheme.glass(LucidTheme.bgHeader, LucidTheme.borderSubtle));
            colCell.margin(6f, 10f, 6f, 10f);
            colCell.align(col.align);

            String sortIndicator = "";
            if (sortedColumnIndex == colIndex) {
                sortIndicator = sortAsc ? " ^" : " v";
            }

            Label lbl = new Label(col.title + sortIndicator);
            lbl.setColor(sortedColumnIndex == colIndex ? Pal.accent : LucidTheme.textMuted);
            lbl.setFontScale(0.85f);
            colCell.add(lbl);

            if (col.sortable) {
                colCell.touchable = Touchable.enabled;
                colCell.clicked(() -> toggleSort(colIndex));
            }

            var cell = contentTable.add(colCell).pad(1f).fill();
            if (i == 0) cell.growX().minWidth(0);
        }
        contentTable.row();

        // 2. Unified Data rows - exact same table columns!
        for (int rowIdx = startIndex; rowIdx < endIndex; rowIdx++) {
            T item = filteredItems.get(rowIdx);
            boolean isAlt = (rowIdx % 2 == 1);

            for (int c = 0; c < columns.size; c++) {
                Column<T> col = columns.get(c);

                Table cellContent = new Table();
                if (isAlt) {
                    cellContent.background(LucidTheme.glass(LucidTheme.bgCard.cpy().a(0.40f), null));
                }
                cellContent.margin(5f, 10f, 5f, 10f);
                cellContent.align(col.align);

                col.cellBuilder.get(cellContent, item);

                var cell = contentTable.add(cellContent).pad(1f).fill();
                if (c == 0) cell.growX().minWidth(0);
            }
            contentTable.row();
        }
    }
}
