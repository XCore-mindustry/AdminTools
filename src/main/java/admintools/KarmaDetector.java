package admintools;

import admintools.ui.components.Badge;
import admintools.ui.components.ConfirmDialog;
import admintools.ui.components.DataTable;
import admintools.ui.components.SearchField;
import admintools.ui.dsl.UiBuilder;
import admintools.ui.theme.LucidTheme;
import arc.Core;
import arc.Events;
import arc.scene.ui.layout.Table;
import arc.struct.ObjectMap;
import arc.struct.Seq;
import arc.util.Align;
import mindustry.game.EventType;
import mindustry.gen.Call;
import mindustry.gen.Groups;
import mindustry.gen.Icon;
import mindustry.net.Packets;

public class KarmaDetector {
    public static final ObjectMap<String, Integer> blocksBuild = new ObjectMap<>();
    public static final ObjectMap<String, Integer> blocksDestroy = new ObjectMap<>();
    public static final Seq<ResultReport> reports = new Seq<>();
    public static DataTable<ResultReport> dataTable;

    public static void init() {
        Events.on(EventType.PlayerJoin.class, e -> {
            if (e.player == null) return;
            blocksBuild.put(e.player.name, 0);
            blocksDestroy.put(e.player.name, 0);
        });

        Events.on(EventType.PlayerLeave.class, e -> {
            if (e.player == null) return;
            blocksBuild.remove(e.player.name);
            blocksDestroy.remove(e.player.name);
            updateList();
            update();
        });

        Events.on(EventType.BlockBuildEndEvent.class, e -> {
            if (e.unit == null || !e.unit.isPlayer() || e.unit.getPlayer() == null) return;
            String name = e.unit.getPlayer().name;
            if (e.breaking) {
                blocksDestroy.put(name, blocksDestroy.get(name, 0) + 1);
            } else {
                blocksBuild.put(name, blocksBuild.get(name, 0) + 1);
            }
            updateList();
            update();
        });
    }

    public static Table buildView() {
        Table view = new Table();
        view.top().left();

        dataTable = new DataTable<>();

        // 1. Nickname column - flexible with ellipsis
        dataTable.column("Игрок", r -> r.name);

        // 2. Karma column - center
        dataTable.customColumn("Карма", (cell, r) -> {
            if (r.value >= 0.7f) {
                cell.add(Badge.danger((int)(r.value * 100) + "% GRIEF"));
            } else if (r.value >= 0.35f) {
                cell.add(Badge.warning((int)(r.value * 100) + "% WARN"));
            } else {
                cell.add(Badge.success((int)(r.value * 100) + "% OK"));
            }
        }).align(Align.center);

        // 3. Stats column - center
        dataTable.customColumn("Сломано / Всего", (cell, r) -> {
            var lbl = cell.add(r.destroyed + " / " + (r.builded + r.destroyed)).get();
            lbl.setFontScale(0.85f);
            lbl.setColor(LucidTheme.textDim);
        }).align(Align.center);

        // 4. Action column - center
        dataTable.customColumn("Бан", (cell, r) -> {
            UiBuilder.iconButton(cell, Icon.hammerSmall, () -> {
                ConfirmDialog.show("Бан игрока", Core.bundle.format("confirmban", r.name), () -> {
                    var user = Groups.player.find(p -> p.name().equals(r.name));
                    if (user != null) {
                        Call.adminRequest(user, Packets.AdminAction.ban, null);
                    }
                });
            });
        }).align(Align.center);

        dataTable.pageSize(7);

        // Search bar on top with clean margin
        SearchField search = new SearchField("Поиск по нику...", query -> {
            String lower = query.toLowerCase().trim();
            dataTable.setFilter(r -> lower.isEmpty() || r.name.toLowerCase().contains(lower));
        });

        view.add(search).growX().padBottom(10f).row();
        view.add(dataTable).grow();

        update();
        return view;
    }

    public static void update() {
        reports.clear();
        for (var name : blocksDestroy.keys()) {
            reports.add(new ResultReport(name, blocksBuild.get(name, 0), blocksDestroy.get(name, 0)));
        }
        reports.sort((a, b) -> b.value.compareTo(a.value));

        if (dataTable != null) {
            dataTable.setItems(reports);
        }
    }

    public static void updateList() {
        Seq<String> toRm = new Seq<>();
        for (var name : blocksDestroy.keys()) {
            if (Groups.player.find(p -> p.name().equals(name)) == null) {
                toRm.add(name);
            }
        }
        toRm.each(p -> {
            blocksDestroy.remove(p);
            blocksBuild.remove(p);
        });
    }

    public static class ResultReport {
        public final String name;
        public final int builded;
        public final int destroyed;
        public final Float value;

        public ResultReport(String name, int builded, int destroyed) {
            this.name = name;
            this.builded = builded;
            this.destroyed = destroyed;
            if (builded + destroyed > 0) {
                this.value = destroyed * 1f / (builded + destroyed);
            } else {
                this.value = 0f;
            }
        }
    }
}
