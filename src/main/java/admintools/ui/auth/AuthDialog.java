package admintools.ui.auth;

import admintools.ui.components.ToastManager;
import admintools.ui.theme.LucidTheme;
import arc.Core;
import arc.input.KeyCode;
import arc.scene.ui.CheckBox;
import arc.scene.ui.Dialog;
import arc.scene.ui.ImageButton;
import arc.scene.ui.Label;
import arc.scene.ui.TextButton;
import arc.scene.ui.TextField;
import arc.scene.ui.layout.Scl;
import arc.scene.ui.layout.Table;
import mindustry.gen.Icon;
import mindustry.graphics.Pal;
import mindustry.ui.Styles;

/**
 * A compact, modern modal card for XCore admin authentication.
 * Automatically adapts between Discord Linking flow and Admin Password Login.
 */
public class AuthDialog extends Dialog {
    public static AuthDialog current;

    private TextField passwordField;
    private CheckBox rememberBox;
    private CheckBox autoLoginBox;
    private final Table form = new Table();
    private final Label statusLabel = new Label("");
    private boolean passwordVisible = false;

    public AuthDialog() {
        super("", Styles.defaultDialog);
        current = this;

        // Ensure compact floating dialog, NOT full screen
        setFillParent(false);

        // Dark glass sci-fi background with crisp accent border
        background(LucidTheme.glass(LucidTheme.bgGlass, LucidTheme.accent));
        margin(0f);

        // Remove default title table
        titleTable.clear();
        titleTable.remove();

        // 1. Custom Title Bar
        Table titleBar = new Table();
        titleBar.background(LucidTheme.glass(LucidTheme.bgHeader, LucidTheme.borderSubtle));
        titleBar.margin(8f, 14f, 8f, 10f);

        titleBar.image(Icon.admin).size(22f).color(Pal.accent).padRight(8f);
        titleBar.add("Авторизация XCore").color(Pal.accent).growX().left();

        ImageButton closeBtn = new ImageButton(Icon.cancel, Styles.clearNonei);
        closeBtn.clicked(this::hide);
        titleBar.add(closeBtn).size(24f);

        add(titleBar).growX().row();

        // 2. Form Content Area
        cont.clear();
        cont.margin(14f, 18f, 10f, 18f);
        cont.add(form).width(Scl.scl(340f)).row();

        closeOnBack();

        rebuildUI();

        shown(() -> {
            statusLabel.setText("");
            AuthManager.get().requestAuthStatus();
            if (passwordField != null) {
                Core.scene.setKeyboardFocus(passwordField);
            }
        });

        hidden(() -> {
            if (current == this) current = null;
        });
    }

    public void rebuildUI() {
        form.clear();
        buttons.clear();

        boolean isLinked = AuthManager.get().isDiscordLinked();
        String activeCode = AuthManager.get().getActiveLinkCode();

        if (!isLinked) {
            if (activeCode != null && !activeCode.isEmpty()) {
                // Seamless Linking Card: Active code received
                form.add("[accent]Привязка Discord[]").left().padBottom(6f).row();

                Label instruct = new Label("Введите в канале Discord сервера команду:");
                instruct.setWrap(true);
                instruct.setFontScale(0.85f);
                instruct.setColor(LucidTheme.textDim);
                form.add(instruct).growX().padBottom(10f).row();

                Table codeBox = new Table();
                codeBox.background(LucidTheme.glass(LucidTheme.bgCard, LucidTheme.accent));
                codeBox.margin(8f, 12f, 8f, 12f);

                Label cmdLbl = new Label("/link " + activeCode);
                cmdLbl.setColor(Pal.accent);
                cmdLbl.setFontScale(1.05f);
                codeBox.add(cmdLbl).growX().left().padRight(8f);

                TextButton copyBtn = new TextButton("Копировать", LucidTheme.flatTextButtonStyle());
                copyBtn.clicked(() -> {
                    Core.app.setClipboardText("/link " + activeCode);
                    ToastManager.info("Команда скопирована в буфер!");
                });
                codeBox.add(copyBtn).size(Scl.scl(100f), Scl.scl(32f));

                form.add(codeBox).growX().padBottom(8f).row();

                // Countdown timer label
                Label timerLbl = new Label("");
                timerLbl.setFontScale(0.85f);
                timerLbl.update(() -> {
                    long rem = (AuthManager.get().getActiveLinkExpiresAt() - System.currentTimeMillis()) / 1000L;
                    if (rem <= 0) {
                        timerLbl.setText("[scarlet]Срок действия кода истёк[]");
                    } else {
                        long mins = rem / 60;
                        long secs = rem % 60;
                        timerLbl.setText(String.format("[gray]Действителен ещё %02d:%02d[]", mins, secs));
                    }
                });
                form.add(timerLbl).center().padBottom(4f).row();

                Label waitingLbl = new Label("[gray]Ожидание подтверждения от бота...[]");
                waitingLbl.setFontScale(0.85f);
                form.add(waitingLbl).center().padBottom(8f).row();

                buttons.margin(6f, 16f, 14f, 16f);
                buttons.defaults().size(Scl.scl(120f), Scl.scl(38f)).pad(Scl.scl(6f));
                buttons.button("Отмена", Styles.defaultt, () -> {
                    AuthManager.get().clearActiveLinkCode();
                    rebuildUI();
                });
            } else {
                // Link request prompt
                form.add("[scarlet]⚠ Discord аккаунт не привязан[]").left().padBottom(8f).row();

                Label desc = new Label("Для получения прав администратора необходимо привязать ваш Discord к аккаунту Mindustry.");
                desc.setWrap(true);
                desc.setFontScale(0.85f);
                desc.setColor(LucidTheme.textDim);
                form.add(desc).growX().padBottom(16f).row();

                // Request Link Code Button
                TextButton linkBtn = new TextButton("Получить код привязки", LucidTheme.flatTextButtonStyle());
                linkBtn.clicked(() -> {
                    statusLabel.setColor(Pal.accent);
                    statusLabel.setText("Генерация кода привязки...");
                    AuthManager.get().requestDiscordLink();
                });
                form.add(linkBtn).size(Scl.scl(220f), Scl.scl(38f)).center().padBottom(8f).row();

                form.add(statusLabel).growX().minHeight(20f).padBottom(4f).row();

                buttons.margin(6f, 16f, 14f, 16f);
                buttons.defaults().size(Scl.scl(120f), Scl.scl(38f)).pad(Scl.scl(6f));
                buttons.button("Закрыть", Styles.defaultt, this::hide);
            }
        } else {
            // Discord IS linked: render authentication form
            String discUser = AuthManager.get().getDiscordUsername();
            String subtitle = discUser.isEmpty() ? "Сеть серверов XCore" : "Привязан: [accent]@" + discUser + "[]";
            form.add(subtitle).color(LucidTheme.textDim).left().padBottom(8f).row();

            if (!AuthManager.get().hasDiscordAdmin()) {
                Label noRole = new Label("[goldenrod]⚠ У привязанного Discord аккаунта нет роли администратора[]");
                noRole.setWrap(true);
                noRole.setFontScale(0.8f);
                form.add(noRole).growX().padBottom(8f).row();
            }

            // Password input row with inline eye toggle
            Table passRow = new Table();
            passRow.background(LucidTheme.glass(LucidTheme.bgCard, LucidTheme.borderSubtle));
            passRow.margin(4f, 8f, 4f, 8f);

            passwordField = new TextField("", Styles.defaultField);
            passwordField.setPasswordMode(!passwordVisible);
            passwordField.setPasswordCharacter('*');
            passwordField.setMessageText(AuthManager.get().hasPassword() ? "Пароль администратора..." : "Придумайте пароль (от " + AuthManager.MIN_PASSWORD_LENGTH + " символов)...");

            ImageButton eyeBtn = new ImageButton(passwordVisible ? Icon.eyeOff : Icon.eye, Styles.clearNonei);
            eyeBtn.clicked(() -> {
                passwordVisible = !passwordVisible;
                passwordField.setPasswordMode(!passwordVisible);
                eyeBtn.getStyle().imageUp = passwordVisible ? Icon.eyeOff : Icon.eye;
            });

            passRow.add(passwordField).growX().height(32f);
            passRow.add(eyeBtn).size(26f).padLeft(6f);

            form.add(passRow).growX().padBottom(12f).row();

            // Checkboxes with compact spacing
            Table checkTable = new Table();
            checkTable.left();

            rememberBox = new CheckBox("Запомнить устройство (безопасный токен)");
            rememberBox.setChecked(AuthManager.get().hasSavedCredentials());

            autoLoginBox = new CheckBox("Входить автоматически при подключении");
            autoLoginBox.setChecked(AuthManager.get().isAutoLoginEnabled());

            checkTable.add(rememberBox).left().row();
            checkTable.add(autoLoginBox).left().padTop(6f).row();

            form.add(checkTable).left().padBottom(10f).row();

            // Status label
            statusLabel.setFontScale(0.85f);
            statusLabel.setColor(Pal.remove);
            statusLabel.setWrap(true);
            form.add(statusLabel).growX().minHeight(20f).padBottom(4f).row();

            // Action Buttons
            buttons.margin(6f, 16f, 14f, 16f);
            buttons.defaults().size(Scl.scl(120f), Scl.scl(38f)).pad(Scl.scl(6f));
            buttons.button("Отмена", Styles.defaultt, this::hide);
            buttons.button(AuthManager.get().hasPassword() ? "Войти" : "Создать", LucidTheme.flatTextButtonStyle(), this::submit);

            passwordField.keyDown(key -> {
                if (key == KeyCode.enter) {
                    submit();
                }
            });

            Core.app.post(() -> {
                if (passwordField != null && isShown()) {
                    Core.scene.setKeyboardFocus(passwordField);
                }
            });
        }
        pack();
    }

    private void submit() {
        if (passwordField == null) return;
        if (AuthManager.get().getStatus() == AuthManager.Status.AUTHENTICATING) return;

        String pass = passwordField.getText().trim();
        if (pass.length() < AuthManager.MIN_PASSWORD_LENGTH) {
            statusLabel.setColor(Pal.remove);
            statusLabel.setText("Пароль должен быть не короче " + AuthManager.MIN_PASSWORD_LENGTH + " символов");
            return;
        }

        statusLabel.setColor(Pal.accent);
        statusLabel.setText("Авторизация...");

        boolean rem = rememberBox != null && rememberBox.isChecked();
        boolean auto = autoLoginBox != null && autoLoginBox.isChecked();
        AuthManager.get().loginWithPassword(pass, rem, auto);
    }

    public void setError(String error) {
        statusLabel.setColor(Pal.remove);
        statusLabel.setText(error);
    }

    public static void showDialog() {
        if (current == null) {
            new AuthDialog().show();
        } else {
            current.rebuildUI();
            current.show();
        }
    }
}
