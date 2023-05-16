package admintools;

import arc.scene.ui.layout.Table;
import arc.util.Log;
import arc.util.serialization.JsonReader;
import arc.util.serialization.JsonValue;
import arc.util.serialization.JsonWriter;
import mindustry.gen.Call;
import mindustry.ui.dialogs.BaseDialog;

public class BanDialog extends BaseDialog {
    public String reason;
    public String banTime = "0";

    public BanDialog(String content) {
        super("ban");
        Log.info(content);
        JsonValue json = new JsonReader().parse(content);

        String name = json.get("name").asString();

        closeOnBack();
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
        buttons.button("@cancel", this::hide);
        buttons.button("@ok", () -> {
            json.get("reason").set(reason);
            json.get("duration").set(banTime);
            Call.serverPacketReliable("take_ban_data", json.toJson(JsonWriter.OutputType.json));
            hide();
        });
    }
}