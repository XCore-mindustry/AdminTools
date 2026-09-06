package admintools;

import admintools.ui.theme.LucidTheme;
import arc.Core;
import arc.graphics.Color;
import arc.scene.ui.CheckBox;
import arc.scene.ui.Dialog;
import arc.scene.ui.ImageButton;
import arc.scene.ui.Label;
import arc.scene.ui.TextButton;
import arc.scene.ui.TextField;
import arc.scene.ui.layout.Scl;
import arc.scene.ui.layout.Table;
import arc.util.Log;
import arc.util.Strings;
import arc.util.serialization.JsonReader;
import arc.util.serialization.JsonValue;
import arc.util.serialization.JsonWriter;
import mindustry.Vars;
import mindustry.gen.Call;
import mindustry.gen.Icon;
import mindustry.graphics.Pal;
import mindustry.ui.Styles;

/**
 * Modern, compact modal card for ban execution and rollback requests.
 */
public class BanDialog extends Dialog {
    private static final JsonReader jsonReader = new JsonReader();
    public JsonValue json;

    private TextField reasonField;
    private TextField durationField;
    private CheckBox rollbackBox;
    private boolean submitted = false;

    public BanDialog(String content) {
        super("", Styles.defaultDialog);

        try {
            json = jsonReader.parse(content);
        } catch (Exception e) {
            Log.err(e);
            Vars.ui.showException(Core.bundle.get("admintools.ban.parse_error"), e);
            return;
        }

        String nickname = json.getString("name", "Unknown");
        int pid = json.getInt("pid", 0);

        // Compact floating modal, not fullscreen
        setFillParent(false);
        background(LucidTheme.glass(LucidTheme.bgGlass, LucidTheme.accent));
        margin(0f);

        titleTable.clear();
        titleTable.remove();

        // 1. Custom Title Bar
        Table titleBar = new Table();
        titleBar.background(LucidTheme.glass(LucidTheme.bgHeader, LucidTheme.borderSubtle));
        titleBar.margin(8f, 14f, 8f, 10f);

        titleBar.image(Icon.hammer).size(22f).color(Pal.accent).padRight(8f);
        titleBar.add("@admintools.dialog.ban_title").color(Pal.accent).growX().left();

        ImageButton closeBtn = new ImageButton(Icon.cancel, Styles.clearNonei);
        closeBtn.clicked(this::cancel);
        titleBar.add(closeBtn).size(24f);

        add(titleBar).growX().row();

        // 2. Main Form Content Area
        cont.clear();
        cont.margin(14f, 18f, 10f, 18f);

        Table form = new Table();
        form.top().left();

        // Player Target Card
        Table playerCard = new Table();
        playerCard.background(LucidTheme.glass(LucidTheme.bgCard, LucidTheme.borderSubtle));
        playerCard.margin(8f, 12f, 8f, 12f);

        Table infoTable = new Table();
        infoTable.left();
        infoTable.add(nickname).color(Color.white).left().growX().row();
        infoTable.add("[gray]PID: [accent]" + pid + "[]").left();

        playerCard.image(Icon.admin).size(24f).color(Pal.accent).padRight(10f);
        playerCard.add(infoTable).growX().left();

        form.add(playerCard).growX().padBottom(10f).row();

        // Reason Label
        form.add("@admintools.ban.reason").color(LucidTheme.textDim).left().padBottom(4f).row();

        // Quick Reason Presets (Chips)
        Table reasonChips = new Table();
        reasonChips.left();

        String[] chipKeys = {
            "admintools.ban.chip.1",
            "admintools.ban.chip.2",
            "admintools.ban.chip.3",
            "admintools.ban.chip.4",
            "admintools.ban.chip.5",
            "admintools.ban.chip.6"
        };

        String[] presetKeys = {
            "admintools.ban.reason.1",
            "admintools.ban.reason.2",
            "admintools.ban.reason.3",
            "admintools.ban.reason.4",
            "admintools.ban.reason.5",
            "admintools.ban.reason.6"
        };

        Table row1 = new Table(); row1.left();
        Table row2 = new Table(); row2.left();

        for (int i = 0; i < chipKeys.length; i++) {
            String chipText = Core.bundle.get(chipKeys[i]);
            String fullReason = Core.bundle.get(presetKeys[i]);
            Table targetRow = (i < 3) ? row1 : row2;

            TextButton chip = new TextButton(chipText, LucidTheme.flatTextButtonStyle());
            chip.getLabel().setFontScale(0.82f);
            chip.clicked(() -> {
                if (reasonField != null) {
                    reasonField.setText(fullReason);
                }
            });
            targetRow.add(chip).height(28f).padRight(4f).padBottom(4f);
        }

        reasonChips.add(row1).left().row();
        reasonChips.add(row2).left().row();
        form.add(reasonChips).growX().padBottom(6f).row();

        // Custom Reason Input Field
        Table reasonFieldBox = new Table();
        reasonFieldBox.background(LucidTheme.glass(LucidTheme.bgCard, LucidTheme.borderSubtle));
        reasonFieldBox.margin(4f, 8f, 4f, 8f);

        reasonField = new TextField(Core.bundle.get(presetKeys[0]), Styles.defaultField);
        reasonField.setMessageText(Core.bundle.get("admintools.ban.reason"));
        reasonFieldBox.add(reasonField).growX().height(30f);
        form.add(reasonFieldBox).growX().padBottom(10f).row();

        // Duration Label
        form.add("@admintools.ban.duration").color(LucidTheme.textDim).left().padBottom(4f).row();

        // Quick Duration Chips
        Table durationChips = new Table();
        durationChips.left();

        String[] durationPresets = {"1h", "1d", "3d", "7d", "30d", "0"};
        for (String dur : durationPresets) {
            String label = "0".equals(dur) ? "perm (0)" : dur;
            TextButton dChip = new TextButton(label, LucidTheme.flatTextButtonStyle());
            dChip.getLabel().setFontScale(0.82f);
            dChip.clicked(() -> {
                if (durationField != null) {
                    durationField.setText(dur);
                }
            });
            durationChips.add(dChip).height(28f).padRight(4f);
        }
        form.add(durationChips).left().padBottom(6f).row();

        // Custom Duration Input Field
        Table durationFieldBox = new Table();
        durationFieldBox.background(LucidTheme.glass(LucidTheme.bgCard, LucidTheme.borderSubtle));
        durationFieldBox.margin(4f, 8f, 4f, 8f);

        durationField = new TextField("1d", Styles.defaultField);
        durationFieldBox.add(durationField).growX().height(30f);
        form.add(durationFieldBox).growX().padBottom(10f).row();

        // Rollback Checkbox
        rollbackBox = new CheckBox("@admintools.ban.rollback");
        rollbackBox.setChecked(false);
        form.add(rollbackBox).left().padBottom(6f).row();

        cont.add(form).width(Scl.scl(380f)).row();

        // 3. Action Buttons
        buttons.clear();
        buttons.margin(6f, 16f, 14f, 16f);
        buttons.defaults().size(Scl.scl(120f), Scl.scl(38f)).pad(Scl.scl(6f));

        buttons.button("@cancel", Styles.defaultt, this::cancel);
        buttons.button("@admintools.action.ban", LucidTheme.flatTextButtonStyle(), this::submit);

        closeOnBack(this::cancel);

        hidden(() -> {
            if (!submitted) {
                cancel();
            }
        });

        pack();
        show();
    }

    private void submit() {
        submitted = true;
        String reason = reasonField != null ? reasonField.getText().trim() : "";
        if (reason.isEmpty()) {
            reason = Core.bundle.get("admintools.ban.reason.1");
        }

        String banTime = durationField != null ? durationField.getText().trim() : "0";
        if (banTime.isEmpty()) {
            banTime = "0";
        }

        boolean rollback = rollbackBox != null && rollbackBox.isChecked();

        json.get("reason").set(reason);
        json.get("duration").set(banTime);

        if (rollback) {
            Call.sendChatMessage("/rollback " + json.getInt("pid", 0));
        }

        Call.serverPacketReliable("take_ban_data", json.toJson(JsonWriter.OutputType.json));
        hide();
    }

    private void cancel() {
        if (!submitted) {
            submitted = true;
            Call.serverPacketReliable("cancel_ban_data", json.toJson(JsonWriter.OutputType.json));
        }
        hide();
    }
}
