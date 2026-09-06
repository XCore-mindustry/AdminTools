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
    private static final JsonReader jsonReader = new JsonReader();
    public String reason;
    public String banTime = "0";
    public boolean rollback = false;
    public JsonValue json;

    public BanDialog(String content) {
        super("ban");
        String nickname;
        try {
            json = jsonReader.parse(content);
            nickname = json.getString("name");
        } catch (Exception e) {
            Log.err(e);
            Vars.ui.showException(I18N.get("admintools.ban.parse_error"), e);
            return;
        }

        shown(() -> {
            cont.clear();
            Table table = new Table();
            table.add(nickname);
            table.row();
            table.add(I18N.get("admintools.ban.reason")).padRight(8f);
            table.defaults().height(60f).padTop(8);
            table.field(null, value -> reason = value);
            table.row();
            table.add(I18N.get("admintools.ban.duration")).padRight(8f);
            table.field(null, value -> banTime = value);
            table.row();
            table.check(I18N.get("admintools.ban.rollback"), rollback, (value) -> rollback = value);
            cont.row();
            cont.add(table);
        });

        buttons.defaults().size(200f, 50f);

        closeOnBack(this::cancel);

        buttons.button("@cancel", this::cancel);

        buttons.button("@ok", () -> {
            reason = switch (reason) {
                case "1" -> I18N.get("admintools.ban.reason.1");
                case "2" -> I18N.get("admintools.ban.reason.2");
                case "3" -> I18N.get("admintools.ban.reason.3");
                case "4" -> I18N.get("admintools.ban.reason.4");
                case "5" -> I18N.get("admintools.ban.reason.5");
                case "6" -> I18N.get("admintools.ban.reason.6");
                default -> reason;
            };

            json.get("reason").set(reason);
            json.get("duration").set(banTime);

            if (rollback) {
                Call.sendChatMessage("/rollback " + json.getInt("pid"));
            }

            Call.serverPacketReliable("take_ban_data", json.toJson(JsonWriter.OutputType.json));
            hide();
        });

        show();
    }
    private void cancel() {
        Call.serverPacketReliable("cancel_ban_data", json.toJson(JsonWriter.OutputType.json));
        hide();
    }
}