package admintools.input;

import admintools.ui.components.FreeCamHUD;
import admintools.ui.components.ToastManager;
import arc.Core;
import arc.Events;
import arc.scene.event.Touchable;
import arc.util.Log;
import mindustry.Vars;
import mindustry.game.EventType;
import mindustry.input.MobileInput;

/**
 * Controller managing FreeCam lifecycle, input swapping, HUD overlays, and user preferences.
 */
public class FreeCamController {
    private static final String SETTING_KEY = "admintools-freecam";
    private static final FreeCamController instance = new FreeCamController();

    private FreeCamInputHandler activeHandler;
    private FreeCamHUD hud;
    private boolean hudInstalled = false;

    public static FreeCamController get() {
        return instance;
    }

    private FreeCamController() {
    }

    public void init() {
        if (isEnabled() && isSupported()) {
            ensureInstalled();
        }

        Events.on(EventType.WorldLoadEvent.class, e -> onWorldLoaded());
        Events.on(EventType.ResetEvent.class, e -> onReset());
    }

    public boolean isSupported() {
        return Vars.mobile || Core.settings.getBool("touchscreen") || (Vars.control != null && Vars.control.input instanceof MobileInput);
    }

    public boolean isEnabled() {
        return Core.settings.getBool(SETTING_KEY, true);
    }

    public void setEnabled(boolean enabled) {
        Core.settings.put(SETTING_KEY, enabled);
        Core.settings.saveValues();

        if (enabled && isSupported()) {
            ensureInstalled();
        } else {
            restoreVanilla();
        }
    }

    public boolean isActive() {
        return activeHandler != null && activeHandler.isFreeCam();
    }

    public void setActive(boolean active) {
        if (active) {
            ensureInstalled();
        }
        if (activeHandler != null) {
            activeHandler.setFreeCam(active);
            if (active) {
                ToastManager.info(Core.bundle.get("admintools.freecam.active"));
            } else {
                ToastManager.info(Core.bundle.get("admintools.freecam.inactive"));
            }
        }
    }

    public void toggle() {
        setActive(!isActive());
    }

    public void centerOnPlayer() {
        if (activeHandler != null) {
            activeHandler.centerOnPlayer();
        } else if (Vars.player != null) {
            if (Vars.player.unit() != null) {
                Core.camera.position.set(Vars.player.unit().x, Vars.player.unit().y);
            } else {
                Core.camera.position.set(Vars.player.x, Vars.player.y);
            }
        }
    }

    public void ensureInstalled() {
        if (!isEnabled()) return;
        if (Vars.control == null || Vars.control.input == null) return;

        if (Vars.control.input instanceof FreeCamInputHandler handler) {
            this.activeHandler = handler;
            return;
        }

        if (Vars.control.input instanceof MobileInput mobileInput) {
            FreeCamInputHandler handler = new FreeCamInputHandler(mobileInput);
            Vars.control.setInput(handler);
            this.activeHandler = handler;
            Log.info("[AdminTools] FreeCamInputHandler installed via control.setInput.");
        }
    }

    public void restoreVanilla() {
        if (Vars.control == null || Vars.control.input == null) return;
        if (Vars.control.input instanceof FreeCamInputHandler handler) {
            handler.setFreeCam(false);
            MobileInput vanilla = new MobileInput();
            vanilla.block = handler.block;
            vanilla.mode = handler.mode;
            vanilla.rotation = handler.rotation;
            Vars.control.setInput(vanilla);
            this.activeHandler = null;
            Log.info("[AdminTools] Restored vanilla MobileInput.");
        }
    }

    public void installHUD() {
        if (hudInstalled || Vars.ui == null || Vars.ui.hudGroup == null) return;
        hudInstalled = true;

        hud = new FreeCamHUD();
        Vars.ui.hudGroup.fill(t -> {
            t.name = "admintools-freecam-hud";
            t.top();
            t.margin(38f);
            t.touchable = Touchable.childrenOnly;
            t.visible(() -> isActive() && Vars.state.isGame() && Vars.ui.hudfrag.shown);
            t.add(hud);
        });
    }

    public void onWorldLoaded() {
        if (isEnabled() && isSupported()) {
            ensureInstalled();
            if (activeHandler != null) {
                activeHandler.setFreeCam(false);
            }
        }
    }

    public void onReset() {
        if (activeHandler != null) {
            activeHandler.setFreeCam(false);
        }
    }
}
