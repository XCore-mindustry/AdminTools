package admintools;

import arc.Events;
import arc.scene.ui.Label;
import arc.struct.Seq;
import arc.util.Log;
import arc.util.Strings;
import mindustry.game.EventType;
import mindustry.ui.Styles;
import mindustry.world.blocks.ConstructBlock;

public class LastBuildDetector {

    public static final Seq<Data> log = new Seq<>();

    public static TravelerFragment actionsLog;

    public static Label info = new Label("NYA", Styles.outlineLabel);

    public static void append(Data data) {
        if (data.nickname == null){
        }
        var c = log.find(data1 -> data1.destroy == data.destroy && data.block == data1.block && data.nickname.equals(data1.nickname));

        if (c == null) {
            log.add(data);
        } else {
            c.size += data.size;
        }

        while (log.size > 20) {
            log.remove(0);
        }

        updateText();
    }

    public static void init() {
        Events.on(EventType.GameOverEvent.class, e -> {
            log.removeRange(0, log.size);
        });

        Events.on(EventType.BlockBuildEndEvent.class, e -> {
            if (!e.unit.isPlayer()) {
                return;
            }

            var nya = e.tile.block();

            if (e.breaking && e.tile.build instanceof ConstructBlock.ConstructBuild cons) {
                nya = cons.current;
            }

            append(new Data(e.unit.getPlayer().name, nya, e.breaking, 1));
        });
    }

    public static void updateText() {
        StringBuilder b = new StringBuilder();
        for (var line : log) {
            String color = line.destroy ? "[red]" : "[green]";
            b.append(color).append(format(Strings.stripColors(line.nickname), 20)).append("|").append(format(line.block.localizedName, 20)).append("|").append(line.size).append("[]\n");
        }
        info.setText(b.toString());
    }

    public static String format(String s, int offset) {
        if (s.length() > offset) {
            return s.substring(0, offset);
        } else {
            StringBuilder sBuilder = new StringBuilder(s);
            while (sBuilder.length() < offset) {
                sBuilder.append(" ");
            }
            s = sBuilder.toString();
            return s;
        }
    }
}

