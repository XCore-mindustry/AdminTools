package admintools;

import arc.Core;
import arc.Events;
import arc.scene.ui.layout.Table;
import arc.struct.ObjectMap;
import arc.struct.Seq;
import mindustry.game.EventType;
import mindustry.gen.Call;
import mindustry.gen.Groups;
import mindustry.gen.Icon;
import mindustry.net.Packets;
import mindustry.ui.Styles;

import static mindustry.Vars.ui;

public class CarmaDetector {
    public static final ObjectMap<String, Integer> blocksBuild = new ObjectMap<>();
    public static final ObjectMap<String, Integer> blocksDestroy = new ObjectMap<>();

    public static Table table = new Table();

    public static void init() {
        Events.on(EventType.PlayerJoin.class, e -> {
            if (e.player == null)
                return;
            blocksBuild.put(e.player.name, 0);
            blocksDestroy.put(e.player.name, 0);
        });

        Events.on(EventType.PlayerLeave.class, e -> {
            if (e.player == null)
                return;
            blocksBuild.remove(e.player.name);
            blocksDestroy.remove(e.player.name);
        });

        Events.on(EventType.BlockBuildEndEvent.class, e -> {
            if (!e.unit.isPlayer()) {
                return;
            }
            String name = e.unit.getPlayer().name;
            if (e.breaking) {
                blocksDestroy.put(name, blocksDestroy.get(name) == null ? 1 : blocksDestroy.get(name) + 1);
            } else {
                blocksBuild.put(name, blocksBuild.get(name) == null ? 1 : blocksBuild.get(name) + 1);
            }
            updateList();
            update();
        });
    }

    public static void update() {
        table.clear();
        Seq<ResultReport> result = new Seq<>();
        for (var nya : blocksDestroy.keys()) {
            result.add(new ResultReport(nya, blocksBuild.containsKey(nya) ? blocksBuild.get(nya) : 0, blocksDestroy.get(nya)));
        }
        result.sort((a, b) -> {
            if (a.value > b.value) return -1;
            if (a.value < b.value) return 1;
            return 0;
        });

        for (var nya : result) {
            table.row();
            table.button(Icon.hammerSmall, Styles.cleari, () -> ui.showConfirm("@confirm", Core.bundle.format("confirmban", nya.name), () -> {
                var user = Groups.player.find(p -> p.name().equals(nya.name));
                if (user != null) {
                    Call.adminRequest(user, Packets.AdminAction.ban, null);
                }
            }));
            table.add(nya.name);
            table.add("|");
            table.add(nya.value+"");
        }
    }

    public static void updateList() {
        Seq<String> toRm = new Seq<>();
        for (var name : blocksDestroy.keys()) {
            var nya = Groups.player.find(p -> {
                return p.name().equals(name);
            });
            if (nya == null) {
                toRm.add(name);
            }
        }
        toRm.each(p -> {
            blocksDestroy.remove(p);
            blocksBuild.remove(p);
        });
    }

    public static class ResultReport {
        String name;
        Float value;

        public ResultReport(String name, int builded, int destroyed) {
            this.name = name;
            if (builded + destroyed > 0) {
                value = destroyed * 1f / (builded + destroyed);
            } else {
                value = 0f;
            }
        }
    }
}

