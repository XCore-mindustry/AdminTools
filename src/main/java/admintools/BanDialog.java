package admintools;

import admintools.ui.theme.LucidTheme;
import arc.Core;
import arc.graphics.Color;
import arc.scene.event.SceneResizeEvent;
import arc.scene.ui.CheckBox;
import arc.scene.ui.Dialog;
import arc.scene.ui.ImageButton;
import arc.scene.ui.Label;
import arc.scene.ui.ScrollPane;
import arc.scene.ui.TextButton;
import arc.scene.ui.TextField;
import arc.scene.ui.layout.Cell;
import arc.scene.ui.layout.Scl;
import arc.scene.ui.layout.Table;
import arc.struct.Seq;
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
 * Fully responsive across desktop and mobile screens.
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

        // 1. Custom Title Bar using Dialog's native titleTable (Row 0)
        titleTable.clear();
        titleTable.background(LucidTheme.glass(LucidTheme.bgHeader, LucidTheme.borderSubtle));
        titleTable.margin(7f, 14f, 7f, 10f);

        titleTable.image(Icon.hammer).size(20f).color(Pal.accent).padRight(8f);
        titleTable.add("@admintools.dialog.ban_title").color(Pal.accent).growX().left();

        ImageButton closeBtn = new ImageButton(Icon.cancel, Styles.clearNonei);
        closeBtn.getStyle().imageUpColor = LucidTheme.textDim;
        closeBtn.clicked(this::cancel);
        titleTable.add(closeBtn).size(24f);

        // 2. Main Form Content Area
        float screenW = (Core.scene.getWidth() - Core.scene.marginLeft - Core.scene.marginRight) / Scl.scl(1f);
        float formWidth = Math.max(340f, Math.min(screenW - 36f, 440f));
        float maxH = Math.max(160f, (Core.scene.getHeight() - Core.scene.marginTop - Core.scene.marginBottom) / Scl.scl(1f) - 130f);

        cont.clear();
        cont.margin(12f, 14f, 10f, 14f);

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

        Seq<TextButton> allReasonButtons = new Seq<>();
        boolean isMobileNarrow = formWidth < 360f;

        if (isMobileNarrow) {
            int[][] narrowRows = {{0, 1}, {3, 2}, {4, 5}};
            for (int r = 0; r < narrowRows.length; r++) {
                Table row = new Table(); row.left();
                for (int idx : narrowRows[r]) {
                    String chipText = Core.bundle.get(chipKeys[idx]);
                    String fullReason = Core.bundle.get(presetKeys[idx]);
                    TextButton chip = new TextButton(chipText, LucidTheme.chipButtonStyle(idx == 0));
                    chip.getLabel().setFontScale(0.82f);
                    chip.getLabel().setWrap(false);
                    allReasonButtons.add(chip);
                    chip.clicked(() -> {
                        if (reasonField != null) reasonField.setText(fullReason);
                        for (int b = 0; b < allReasonButtons.size; b++) {
                            TextButton btn = allReasonButtons.get(b);
                            btn.setStyle(LucidTheme.chipButtonStyle(btn == chip));
                        }
                    });
                    row.add(chip).height(28f).padRight(5f);
                }
                reasonChips.add(row).left().padBottom(r < narrowRows.length - 1 ? 4f : 0f).row();
            }
        } else {
            int[] row1Indices = {0, 1, 3}; // Griefing, Toxicity, Bug Abuse
            int[] row2Indices = {2, 4, 5}; // Vote-kick, Ban Evasion, NSFW
            Table row1 = new Table(); row1.left();
            Table row2 = new Table(); row2.left();

            for (int idx : row1Indices) {
                String chipText = Core.bundle.get(chipKeys[idx]);
                String fullReason = Core.bundle.get(presetKeys[idx]);
                TextButton chip = new TextButton(chipText, LucidTheme.chipButtonStyle(idx == 0));
                chip.getLabel().setFontScale(0.84f);
                chip.getLabel().setWrap(false);
                allReasonButtons.add(chip);
                chip.clicked(() -> {
                    if (reasonField != null) reasonField.setText(fullReason);
                    for (int b = 0; b < allReasonButtons.size; b++) {
                        TextButton btn = allReasonButtons.get(b);
                        btn.setStyle(LucidTheme.chipButtonStyle(btn == chip));
                    }
                });
                row1.add(chip).height(28f).padRight(5f);
            }

            for (int idx : row2Indices) {
                String chipText = Core.bundle.get(chipKeys[idx]);
                String fullReason = Core.bundle.get(presetKeys[idx]);
                TextButton chip = new TextButton(chipText, LucidTheme.chipButtonStyle(false));
                chip.getLabel().setFontScale(0.84f);
                chip.getLabel().setWrap(false);
                allReasonButtons.add(chip);
                chip.clicked(() -> {
                    if (reasonField != null) reasonField.setText(fullReason);
                    for (int b = 0; b < allReasonButtons.size; b++) {
                        TextButton btn = allReasonButtons.get(b);
                        btn.setStyle(LucidTheme.chipButtonStyle(btn == chip));
                    }
                });
                row2.add(chip).height(28f).padRight(5f);
            }

            reasonChips.add(row1).left().padBottom(4f).row();
            reasonChips.add(row2).left().row();
        }
        form.add(reasonChips).growX().padBottom(8f).row();

        // Custom Reason Input Field
        Table reasonFieldBox = new Table();
        reasonFieldBox.background(LucidTheme.glass(LucidTheme.bgCard, LucidTheme.borderSubtle));
        reasonFieldBox.margin(4f, 10f, 4f, 10f);

        reasonField = new TextField(Core.bundle.get(presetKeys[0]), LucidTheme.transparentFieldStyle());
        reasonField.setMessageText(Core.bundle.get("admintools.ban.reason"));
        reasonFieldBox.add(reasonField).growX().height(28f);
        form.add(reasonFieldBox).growX().padBottom(10f).row();

        // Duration Label
        form.add("@admintools.ban.duration").color(LucidTheme.textDim).left().padBottom(4f).row();

        // Quick Duration Chips
        Table durationChips = new Table();
        durationChips.left();

        String[] durationPresets = {"1h", "1d", "3d", "7d", "30d", "0"};
        String[] durationLabels = {"1h", "1d", "3d", "7d", "30d", "perm"};
        Seq<TextButton> allDurationButtons = new Seq<>();

        if (isMobileNarrow) {
            Table dRow1 = new Table(); dRow1.left();
            Table dRow2 = new Table(); dRow2.left();
            for (int i = 0; i < durationPresets.length; i++) {
                String dur = durationPresets[i];
                String label = durationLabels[i];
                TextButton dChip = new TextButton(label, LucidTheme.chipButtonStyle(dur.equals("1d")));
                dChip.getLabel().setFontScale(0.84f);
                dChip.getLabel().setWrap(false);
                allDurationButtons.add(dChip);
                dChip.clicked(() -> {
                    if (durationField != null) durationField.setText(dur);
                    for (int b = 0; b < allDurationButtons.size; b++) {
                        TextButton btn = allDurationButtons.get(b);
                        btn.setStyle(LucidTheme.chipButtonStyle(btn == dChip));
                    }
                });
                (i < 3 ? dRow1 : dRow2).add(dChip).height(28f).padRight(5f);
            }
            durationChips.add(dRow1).left().padBottom(4f).row();
            durationChips.add(dRow2).left().row();
        } else {
            for (int i = 0; i < durationPresets.length; i++) {
                String dur = durationPresets[i];
                String label = durationLabels[i];
                TextButton dChip = new TextButton(label, LucidTheme.chipButtonStyle(dur.equals("1d")));
                dChip.getLabel().setFontScale(0.84f);
                dChip.getLabel().setWrap(false);
                allDurationButtons.add(dChip);
                dChip.clicked(() -> {
                    if (durationField != null) durationField.setText(dur);
                    for (int b = 0; b < allDurationButtons.size; b++) {
                        TextButton btn = allDurationButtons.get(b);
                        btn.setStyle(LucidTheme.chipButtonStyle(btn == dChip));
                    }
                });
                durationChips.add(dChip).height(28f).padRight(5f);
            }
        }
        form.add(durationChips).left().padBottom(8f).row();

        // Custom Duration Input Field
        Table durationFieldBox = new Table();
        durationFieldBox.background(LucidTheme.glass(LucidTheme.bgCard, LucidTheme.borderSubtle));
        durationFieldBox.margin(4f, 10f, 4f, 10f);

        durationField = new TextField("1d", LucidTheme.transparentFieldStyle());
        durationFieldBox.add(durationField).growX().height(28f);
        form.add(durationFieldBox).growX().padBottom(10f).row();

        // Rollback Checkbox
        rollbackBox = new CheckBox("@admintools.ban.rollback");
        rollbackBox.getLabel().setColor(Color.white);
        rollbackBox.getLabel().setFontScale(0.9f);
        rollbackBox.setChecked(false);
        form.add(rollbackBox).left().padTop(2f).padBottom(8f).row();

        Cell<ScrollPane> paneCell = cont.pane(form);
        if (paneCell.get() != null) {
            paneCell.get().setOverscroll(false, false);
            paneCell.get().setScrollingDisabled(true, false);
        }
        paneCell.width(formWidth).maxHeight(maxH).scrollX(false).row();

        // 3. Action Buttons
        buttons.clear();
        buttons.margin(8f, 14f, 14f, 14f);
        float btnWidth = Math.min((formWidth - 16f) / 2f, 130f);
        buttons.defaults().size(btnWidth, 38f).pad(4f);

        buttons.button("@cancel", LucidTheme.secondaryTextButtonStyle(), this::cancel);
        buttons.button("@admintools.action.ban", LucidTheme.dangerTextButtonStyle(), this::submit);

        closeOnBack(this::cancel);

        hidden(() -> {
            if (!submitted) {
                cancel();
            }
        });

        addListener(event -> {
            if (event instanceof SceneResizeEvent && isShown()) {
                float w = (Core.scene.getWidth() - Core.scene.marginLeft - Core.scene.marginRight) / Scl.scl(1f);
                float fw = Math.max(280f, Math.min(w - 36f, 380f));
                float mh = Math.max(160f, (Core.scene.getHeight() - Core.scene.marginTop - Core.scene.marginBottom) / Scl.scl(1f) - 130f);
                paneCell.width(fw).maxHeight(mh);
                pack();
            }
            return false;
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
