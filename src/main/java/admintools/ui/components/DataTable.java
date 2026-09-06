package admintools.ui.components;

import admintools.ui.dsl.UiBuilder;
import admintools.ui.theme.LucidTheme;
import arc.func.Cons;
import arc.func.Cons2;
import arc.func.Func;
import arc.func.Prov;
import arc.scene.event.Touchable;
import arc.scene.style.Drawable;
import arc.scene.ui.Label;
import arc.scene.ui.ScrollPane;
import arc.scene.ui.TextButton;
import arc.scene.ui.layout.Scl;
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
 * An intelligent reactive data table component with:
 * <ul>
 *     <li>Intrinsic auto-measurement for content and headers (zero magic numbers).</li>
 *     <li>Proportional flex space distribution for flexible columns.</li>
 *     <li>Centralized data normalization (safe null / empty handling).</li>
 *     <li>Semantic column roles (text, icon, time, action, custom).</li>
 *     <li>Responsive real-time relayout on window resize.</li>
 *     <li>Unified grid alignment across all rows and headers.</li>
 * </ul>
 */
public class DataTable<T> extends Table {

    public enum ColumnRole {
        TEXT,
        ICON,
        TIME,
        NUMBER,
        ACTION,
        CUSTOM
    }

    public enum WidthPolicy {
        AUTO,   // Fits widest content & header
        FLEX,   // Expands to fill available leftover space proportionally
        FIXED   // Explicit fixed width in dips
    }

    public static class Column<T> {
        public final String title;
        public Cons2<Table, T> cellBuilder;
        public Func<T, String> textExtractor;
        public Prov<Float> customMeasurer;
        public boolean sortable;
        public Comparator<T> comparator;

        public ColumnRole role = ColumnRole.TEXT;
        public WidthPolicy policy = WidthPolicy.AUTO;
        public int align = Align.left;
        public float weight = 1.0f;
        public float fixedWidth = -1f;
        public float minWidth = -1f;
        public boolean ellipsis = false;

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

        public Column<T> flex() {
            return flex(1.0f);
        }

        public Column<T> flex(float w) {
            this.policy = WidthPolicy.FLEX;
            this.weight = Math.max(0.1f, w);
            this.ellipsis = true;
            return this;
        }

        public Column<T> auto() {
            this.policy = WidthPolicy.AUTO;
            return this;
        }

        public Column<T> fixed(float width) {
            this.policy = WidthPolicy.FIXED;
            this.fixedWidth = width;
            return this;
        }

        public Column<T> minWidth(float mw) {
            this.minWidth = mw;
            return this;
        }

        public Column<T> ellipsis(boolean e) {
            this.ellipsis = e;
            return this;
        }

        public Column<T> role(ColumnRole r) {
            this.role = r;
            return this;
        }

        public Column<T> measure(Prov<Float> measurer) {
            this.customMeasurer = measurer;
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
    private final ScrollPane scrollPane;
    private final Label pageInfoLabel = new Label("");
    private final Label countInfoLabel = new Label("");

    private float lastSolvedWidth = -1f;
    private boolean isSolvingLayout = false;
    private static Label measureLabel;

    public DataTable() {
        background(LucidTheme.glass(LucidTheme.bgCard, LucidTheme.borderSubtle));
        margin(4f);

        contentTable.top().left();

        // Footer table
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

        scrollPane = new ScrollPane(contentTable);
        scrollPane.setOverscroll(false, false);
        scrollPane.setScrollingDisabled(false, false); // horizontal scroll enabled for small screens
        add(scrollPane).grow().row();
        add(footerTable).growX();
    }

    // --- High-level Smart API ---

    /** Adds a smart text column. Automatically normalizes null/empty values to a dimmed dash. */
    public Column<T> text(String title, Func<T, String> textExtractor) {
        return text(title, textExtractor, true);
    }

    public Column<T> text(String title, Func<T, String> textExtractor, boolean sortable) {
        Comparator<T> comp = sortable ? Comparator.comparing(e -> normalizeSort(textExtractor.get(e))) : null;
        Column<T> col = new Column<>(title, null, sortable, comp);
        col.textExtractor = textExtractor;
        col.role = ColumnRole.TEXT;
        col.policy = columns.isEmpty() ? WidthPolicy.FLEX : WidthPolicy.AUTO;
        col.ellipsis = (col.policy == WidthPolicy.FLEX);

        col.cellBuilder = (cell, item) -> {
            String val = normalize(textExtractor.get(item));
            Label lbl = new Label(val);
            lbl.setFontScale(0.85f);
            if (col.ellipsis) lbl.setEllipsis(true);

            var cellDef = cell.add(lbl);
            if (col.align == Align.center) cellDef.center();
            else if (col.align == Align.right) cellDef.right();
            else cellDef.left();

            if (col.policy == WidthPolicy.FLEX) {
                cellDef.growX().minWidth(0);
            }
        };

        columns.add(col);
        refresh();
        return col;
    }

    /** Adds an auto-centered, auto-sized icon/symbol column. */
    public Column<T> icon(String title, Func<T, String> iconExtractor) {
        Column<T> col = text(title, iconExtractor, false);
        col.role = ColumnRole.ICON;
        col.policy = WidthPolicy.AUTO;
        col.align = Align.center;
        col.ellipsis = false;
        return col;
    }

    /** Adds an auto-sized, non-truncated time column (e.g. HH:mm:ss). */
    public Column<T> time(String title, Func<T, String> timeExtractor) {
        Column<T> col = text(title, timeExtractor, true);
        col.role = ColumnRole.TIME;
        col.policy = WidthPolicy.AUTO;
        col.align = Align.center;
        col.ellipsis = false;
        return col;
    }

    /** Adds an action button column (e.g. Ban, View, Delete). */
    public Column<T> action(String title, Drawable icon, Cons<T> onClick) {
        Column<T> col = new Column<>(title, (cell, item) -> {
            UiBuilder.iconButton(cell, icon, () -> onClick.get(item)).size(28f);
        }, false, null);
        col.role = ColumnRole.ACTION;
        col.policy = WidthPolicy.AUTO;
        col.align = Align.center;
        columns.add(col);
        refresh();
        return col;
    }

    /** Adds a custom rendered column. */
    public Column<T> custom(String title, Cons2<Table, T> builder) {
        Column<T> col = new Column<>(title, builder, false, null);
        col.role = ColumnRole.CUSTOM;
        col.policy = columns.isEmpty() ? WidthPolicy.FLEX : WidthPolicy.AUTO;
        columns.add(col);
        refresh();
        return col;
    }

    // --- Backward Compatible API ---

    public Column<T> column(String title, Func<T, String> textExtractor) {
        return text(title, textExtractor);
    }

    public Column<T> column(String title, Func<T, String> textExtractor, boolean sortable) {
        return text(title, textExtractor, sortable);
    }

    public Column<T> customColumn(String title, Cons2<Table, T> builder) {
        return custom(title, builder);
    }

    // --- Data and Pagination ---

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

    // --- Layout Solving & Measurement Engine ---

    @Override
    public void layout() {
        super.layout();
        if (isSolvingLayout) return;

        float currentWidth = getWidth();
        if (currentWidth > 0 && Math.abs(currentWidth - lastSolvedWidth) > 3f) {
            lastSolvedWidth = currentWidth;
            isSolvingLayout = true;
            try {
                rebuildRows();
                super.layout();
            } finally {
                isSolvingLayout = false;
            }
        }
    }

    private static float measureText(String text, float fontScale) {
        if (text == null || text.isEmpty()) return 0f;
        if (measureLabel == null) {
            measureLabel = new Label("", Styles.defaultLabel);
        }
        measureLabel.setFontScale(fontScale);
        measureLabel.setText(text);
        return measureLabel.getPrefWidth();
    }

    public static String normalize(String text) {
        if (text == null) return "[gray]-[]";
        String trimmed = text.trim();
        if (trimmed.isEmpty() || trimmed.equalsIgnoreCase("null")) return "[gray]-[]";
        return text;
    }

    private static String normalizeSort(String text) {
        if (text == null) return "";
        return text.trim();
    }

    private float[] solveColumnWidths() {
        int count = columns.size;
        float[] result = new float[count];
        if (count == 0) return result;

        float tableW = getWidth();
        // Reserve horizontal margin for scrollbar + padding
        float availableWidth = Math.max(0, tableW - Scl.scl(28f));

        float totalFlexWeight = 0f;
        float reservedWidth = 0f;
        float[] intrinsicWidths = new float[count];

        for (int i = 0; i < count; i++) {
            Column<T> col = columns.get(i);

            if (col.policy == WidthPolicy.FIXED && col.fixedWidth > 0) {
                intrinsicWidths[i] = Scl.scl(col.fixedWidth);
                reservedWidth += intrinsicWidths[i];
                continue;
            }

            // 1. Measure header text with padding
            float headerW = measureText(col.title, 0.85f) + Scl.scl(20f);
            float maxContentW = 0f;

            // 2. Measure content intrinsic width
            if (col.role == ColumnRole.ACTION) {
                maxContentW = Scl.scl(32f);
            } else if (col.role == ColumnRole.ICON) {
                maxContentW = Scl.scl(28f);
            } else if (col.customMeasurer != null) {
                maxContentW = col.customMeasurer.get();
            } else if (col.textExtractor != null) {
                int sampleSize = Math.min(filteredItems.size(), 40);
                for (int s = 0; s < sampleSize; s++) {
                    String str = col.textExtractor.get(filteredItems.get(s));
                    if (str != null && !str.isEmpty()) {
                        float w = measureText(str, 0.85f);
                        if (w > maxContentW) maxContentW = w;
                    }
                }
            }

            float contentPadding = Scl.scl(20f);
            float requiredW = Math.max(headerW, maxContentW + contentPadding);

            if (col.minWidth > 0) {
                requiredW = Math.max(requiredW, Scl.scl(col.minWidth));
            }

            intrinsicWidths[i] = Math.max(requiredW, Scl.scl(36f));

            if (col.policy == WidthPolicy.FLEX) {
                totalFlexWeight += col.weight;
            } else {
                reservedWidth += intrinsicWidths[i];
            }
        }

        // 3. Distribute remaining space among flex columns
        float remaining = availableWidth - reservedWidth;

        if (totalFlexWeight > 0 && remaining > 0) {
            for (int i = 0; i < count; i++) {
                Column<T> col = columns.get(i);
                if (col.policy == WidthPolicy.FLEX) {
                    float flexShare = remaining * (col.weight / totalFlexWeight);
                    result[i] = Math.max(intrinsicWidths[i], flexShare);
                } else {
                    result[i] = intrinsicWidths[i];
                }
            }
        } else {
            // Container narrow: every column gets at least its intrinsic width
            for (int i = 0; i < count; i++) {
                result[i] = intrinsicWidths[i];
            }
        }

        return result;
    }

    private void rebuildRows() {
        contentTable.clear();

        int maxPages = Math.max(1, (int) Math.ceil(filteredItems.size() / (float) pageSize));
        int startIndex = page * pageSize;
        int endIndex = Math.min(filteredItems.size(), startIndex + pageSize);

        pageInfoLabel.setText((page + 1) + " / " + maxPages);
        countInfoLabel.setText(admintools.I18N.format("admintools.table.total", filteredItems.size()));

        if (filteredItems.isEmpty()) {
            contentTable.add(admintools.I18N.get("admintools.table.empty")).colspan(Math.max(1, columns.size)).pad(24f).center().row();
            return;
        }

        float[] colWidths = solveColumnWidths();

        // 1. Unified Header row
        for (int i = 0; i < columns.size; i++) {
            int colIndex = i;
            Column<T> col = columns.get(i);

            Table colCell = new Table();
            colCell.background(LucidTheme.glass(LucidTheme.bgHeader, LucidTheme.borderSubtle));
            colCell.margin(6f, 8f, 6f, 8f);
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
            cell.width(colWidths[i] / Scl.scl());
        }
        contentTable.row();

        // 2. Unified Data rows
        for (int rowIdx = startIndex; rowIdx < endIndex; rowIdx++) {
            T item = filteredItems.get(rowIdx);
            boolean isAlt = (rowIdx % 2 == 1);

            for (int c = 0; c < columns.size; c++) {
                Column<T> col = columns.get(c);

                Table cellContent = new Table();
                if (isAlt) {
                    cellContent.background(LucidTheme.glass(LucidTheme.bgCard.cpy().a(0.40f), null));
                }
                cellContent.margin(5f, 8f, 5f, 8f);
                cellContent.align(col.align);

                if (col.cellBuilder != null) {
                    col.cellBuilder.get(cellContent, item);
                }

                var cell = contentTable.add(cellContent).pad(1f).fill();
                cell.width(colWidths[c] / Scl.scl());
            }
            contentTable.row();
        }
    }
}
