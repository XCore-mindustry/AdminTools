package admintools;

import arc.scene.ui.layout.Table;
import arc.util.Log;
import arc.util.serialization.JsonReader;
import arc.util.serialization.JsonValue;
import arc.util.serialization.JsonWriter;
import mindustry.Vars;
import mindustry.gen.Call;
import mindustry.ui.dialogs.BaseDialog;

public class BanDialog extends BaseDialog {
    public String reason;
    public String banTime = "0";
    public JsonValue json;

    public BanDialog(String content) {
        super("ban");
        try {
            json = new JsonReader().parse(content);
        } catch (Exception e) {
            Log.err(e);
            Vars.ui.showException("An error occurred while parsing the ban data:", e);
            return;
        }

        shown(() -> {
            cont.clear();
            Table table = new Table();
            table.add(name);
            table.row();
            table.add("Reason: ").padRight(8f);
            table.defaults().height(60f).padTop(8);
            table.field(null, value -> reason = value);
            table.row();
            table.add("Ban duration(1d, 1h30m, etc): ").padRight(8f);
            table.field(null, value -> banTime = value);
            table.row();
            cont.row();
            cont.add(table);
        });
        buttons.defaults().size(200f, 50f);

        closeOnBack(this::cancel);
        buttons.button("@cancel", () -> {
            hide();
            cancel();
        });

        buttons.button("@ok", () -> {
            reason = switch (reason) {
                case "1" -> "grief";
                case "2" -> "inadequate behavior";
                case "3" -> "vote kick without reason";
                case "4" -> "exploiting";
                case "5" -> "ban bypassing";
                case "6" -> "adult content";
                default -> reason;
            };
            json.get("reason").set(reason);
            json.get("duration").set(banTime);
            Call.serverPacketReliable("take_ban_data", json.toJson(JsonWriter.OutputType.json));
            hide();
        });
    }
    private void cancel() {
        Call.serverPacketReliable("cancel_ban_data", json.toJson(JsonWriter.OutputType.json));
    }
}