package admintools;

import admintools.ui.auth.AuthDialog;
import admintools.ui.auth.AuthManager;
import admintools.ui.components.ToastManager;
import arc.Core;
import arc.Events;
import arc.input.KeyCode;
import arc.scene.event.InputEvent;
import arc.scene.event.InputListener;
import arc.scene.style.TextureRegionDrawable;
import arc.scene.ui.TextField;
import arc.util.Log;
import arc.util.Reflect;
import mindustry.Vars;
import mindustry.game.EventType;
import mindustry.gen.Icon;
import mindustry.io.JsonIO;
import mindustry.mod.Mod;

import static mindustry.Vars.netClient;

public class AdminTools extends Mod {
    public static UIController ui;

    @Override
    public void init() {
        netClient.addPacketHandler("give_ban_data", BanDialog::new);
        netClient.addPacketHandler("tilelogger_history_tile", content -> {
            HistoryEntry[] stack = JsonIO.read(HistoryEntry[].class, content);
            if (stack == null) return;
            HistoryFrame.update(stack);
        });

        // Initialize unified AuthManager (handles adm_mod_begin, adm_auth_result, auto-login)
        AuthManager.get().init();

        Events.on(EventType.ClientLoadEvent.class, e -> {
            ui = new UIController();

            // Anti-leak hook on chatfield to intercept /login and prevent accidental password leakage (Desktop + Mobile)
            try {
                TextField chatfield = Reflect.get(Vars.ui.chatfrag, "chatfield");
                if (chatfield != null) {
                    chatfield.setProgrammaticChangeEvents(true);

                    Runnable checkLeak = () -> {
                        String raw = chatfield.getText().trim();
                        String text = raw;
                        if (text.startsWith("/t ") || text.startsWith("/a ")) {
                            text = text.substring(3).trim();
                        }
                        String lower = text.toLowerCase();
                        if (lower.startsWith("/login") || lower.startsWith("/xl")) {
                            chatfield.setText("");
                            Core.app.post(AuthDialog::showDialog);
                        } else if (lower.startsWith("login ") || lower.startsWith("xl ")) {
                            chatfield.setText("");
                            Core.app.post(() -> {
                                ToastManager.error("Отправка пароля в общий чат заблокирована!");
                                AuthDialog.showDialog();
                            });
                        }
                    };

                    chatfield.addListener(new InputListener() {
                        @Override
                        public boolean keyDown(InputEvent event, KeyCode keycode) {
                            if (keycode == KeyCode.enter) {
                                checkLeak.run();
                            }
                            return false;
                        }
                    });

                    chatfield.changed(checkLeak::run);
                }
            } catch (Throwable t) {
                Log.err("Failed to hook chatfield for anti-leak", t);
            }

            Vars.ui.settings.addCategory("AdminTools (Xcore)", new TextureRegionDrawable(Icon.admin.getRegion()), t -> {
                t.button("Авторизация XCore", Icon.lock, AuthDialog::showDialog).size(260f, 44f).pad(6f).left().row();
                t.check("Автологин при входе", AuthManager.get().isAutoLoginEnabled(), b -> {
                    Core.settings.put("admintools-auth-autologin", b);
                    Core.settings.saveValues();
                }).left().row();
                t.check("Hide all", ui.isHideAll(), ui::setHideAll).left().row();
                t.check("Karma", ui.karmaWindow.visible, b -> {
                    if (b) ui.karmaWindow.show(); else ui.karmaWindow.hide();
                }).left().row();
                t.check("History", ui.historyWindow.visible, b -> {
                    if (b) ui.historyWindow.show(); else ui.historyWindow.hide();
                }).left().row();
                t.check("Portal", ui.portalWindow.visible, b -> {
                    if (b) ui.portalWindow.show(); else ui.portalWindow.hide();
                }).left().row();
                t.check("Sounds Notifications", Core.settings.getBool("admintools-notifications"),
                    b -> Core.settings.put("admintools-notifications", b)).left().row();
            });
        });

        KarmaDetector.init();
    }
}
