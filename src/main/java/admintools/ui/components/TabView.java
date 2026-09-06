package admintools.ui.components;

import admintools.ui.theme.LucidTheme;
import arc.func.Cons;
import arc.graphics.Color;
import arc.scene.style.Drawable;
import arc.scene.ui.TextButton;
import arc.scene.ui.layout.Scl;
import arc.scene.ui.layout.Table;
import arc.struct.Seq;
import mindustry.graphics.Pal;
import mindustry.ui.Styles;

/**
 * A tab container with an active tab indicator and smooth content switching.
 */
public class TabView extends Table {
    private final Table headerTable = new Table();
    private final Table contentTable = new Table();
    private final Seq<TabEntry> tabs = new Seq<>();
    private int activeIndex = -1;

    public static class TabEntry {
        public final String title;
        public final Drawable icon;
        public final Cons<Table> contentBuilder;
        public TextButton button;

        public TabEntry(String title, Drawable icon, Cons<Table> contentBuilder) {
            this.title = title;
            this.icon = icon;
            this.contentBuilder = contentBuilder;
        }
    }

    public TabView() {
        headerTable.left();
        headerTable.background(LucidTheme.glass(LucidTheme.bgHeader, LucidTheme.borderSubtle));
        headerTable.margin(2f, 4f, 2f, 4f);

        contentTable.top().left();

        add(headerTable).growX().row();
        add(contentTable).grow();
    }

    public TabView addTab(String title, Drawable icon, Cons<Table> contentBuilder) {
        int index = tabs.size;
        TabEntry entry = new TabEntry(title, icon, contentBuilder);
        tabs.add(entry);

        TextButton btn = new TextButton(title, Styles.cleart);
        btn.getLabel().setFontScale(0.9f);
        btn.clicked(() -> selectTab(index));
        entry.button = btn;

        headerTable.add(btn).pad(Scl.scl(2f), Scl.scl(6f), Scl.scl(2f), Scl.scl(6f)).height(Scl.scl(30f));

        if (activeIndex == -1) {
            selectTab(0);
        } else {
            updateButtonStyles();
        }

        return this;
    }

    public void selectTab(int index) {
        if (index < 0 || index >= tabs.size || index == activeIndex) return;
        activeIndex = index;

        contentTable.clear();
        tabs.get(index).contentBuilder.get(contentTable);
        updateButtonStyles();
    }

    private void updateButtonStyles() {
        for (int i = 0; i < tabs.size; i++) {
            TabEntry entry = tabs.get(i);
            boolean active = (i == activeIndex);
            if (entry.button != null) {
                entry.button.getLabel().setColor(active ? Pal.accent : LucidTheme.textMuted);
            }
        }
    }
}
