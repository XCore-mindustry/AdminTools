package admintools.ui.window;

import arc.Core;
import arc.Events;
import arc.func.Cons;
import arc.input.KeyCode;
import arc.scene.Element;
import arc.scene.Scene;
import arc.scene.event.InputEvent;
import arc.scene.event.InputListener;
import arc.scene.event.Touchable;
import arc.scene.ui.TextField;
import arc.scene.ui.layout.WidgetGroup;
import arc.struct.ObjectMap;
import arc.struct.Seq;
import mindustry.game.EventType;

/**
 * Root manager for floating windows, docking taskbar, hotkeys, and scene integration.
 */
public class WindowManager extends InputListener {
    private final WidgetGroup root = new WidgetGroup();
    private final Seq<FloatingWindow> windows = new Seq<>();
    private final ObjectMap<String, FloatingWindow> windowMap = new ObjectMap<>();
    private final ObjectMap<KeyCode, String> hotkeys = new ObjectMap<>();
    private FloatingWindow activeWindow = null;
    private QuickDock dock;
    private boolean hideAll = false;

    public WindowManager() {
        root.setFillParent(true);
        root.touchable = Touchable.childrenOnly;
        root.visibility = () -> !hideAll;

        // Auto-release scroll focus when cursor leaves UI windows back to the game world
        root.update(() -> {
            if (Core.scene == null) return;
            Element focus = Core.scene.getScrollFocus();
            if (focus != null && focus.isDescendantOf(root)) {
                Element hover = Core.scene.getHoverElement();
                if (hover == null || !hover.isDescendantOf(root)) {
                    Core.scene.setScrollFocus(null);
                }
            }
        });

        dock = new QuickDock(this);
        root.addChild(dock);

        Events.on(EventType.ResizeEvent.class, e -> {
            for (int i = 0; i < windows.size; i++) {
                windows.get(i).clampToBounds();
            }
            if (dock != null) dock.clampToBounds();
        });
    }

    public void install(Scene scene) {
        if (scene == null) return;
        scene.addListener(this);
        scene.add(root);
        dock.clampToBounds();
    }

    public void uninstall() {
        if (Core.scene != null) {
            Core.scene.removeListener(this);
        }
        root.remove();
    }

    public FloatingWindow createWindow(WindowSpec spec, Cons<FloatingWindow> builder) {
        FloatingWindow win = new FloatingWindow(spec, this);
        builder.get(win);
        win.visible = false;

        windows.add(win);
        windowMap.put(spec.id(), win);
        root.addChild(win);
        dock.registerWindow(spec);

        // Schedule position restoration once scene layout is ready
        Core.app.post(win::restoreState);

        return win;
    }

    public FloatingWindow getWindow(String id) {
        return windowMap.get(id);
    }

    public void open(String id) {
        FloatingWindow win = windowMap.get(id);
        if (win != null) {
            win.show();
            bringToFront(win);
        }
    }

    public void close(String id) {
        FloatingWindow win = windowMap.get(id);
        if (win != null) {
            win.hide();
        }
    }

    public void toggle(String id) {
        FloatingWindow win = windowMap.get(id);
        if (win != null) {
            win.toggle();
            if (win.visible) bringToFront(win);
        }
    }

    public void bringToFront(FloatingWindow win) {
        if (activeWindow != null && activeWindow != win) {
            activeWindow.setFocused(false);
        }
        activeWindow = win;
        win.toFront();
        win.setFocused(true);
        // Ensure dock stays accessible
        if (dock != null) dock.toFront();
    }

    public void bindHotkey(KeyCode key, String windowId) {
        hotkeys.put(key, windowId);
    }

    public QuickDock getDock() {
        return dock;
    }

    public void setHideAll(boolean hide) {
        this.hideAll = hide;
    }

    public boolean isHideAll() {
        return hideAll;
    }

    @Override
    public boolean touchDown(InputEvent event, float x, float y, int pointer, KeyCode button) {
        // When clicking outside of AdminTools windows (into the game world), release UI scroll & keyboard focus
        if (Core.scene != null) {
            Element target = event.targetActor;
            if (target == null || !target.isDescendantOf(root)) {
                Element focus = Core.scene.getScrollFocus();
                if (focus != null && focus.isDescendantOf(root)) {
                    Core.scene.setScrollFocus(null);
                }
                Element kb = Core.scene.getKeyboardFocus();
                if (kb != null && kb.isDescendantOf(root)) {
                    Core.scene.setKeyboardFocus(null);
                }
            }
        }

        return super.touchDown(event, x, y, pointer, button);
    }

    @Override
    public boolean keyDown(InputEvent event, KeyCode keyCode) {
        // Ignore hotkeys when typing into an active text input field
        if (Core.scene != null && Core.scene.getKeyboardFocus() instanceof TextField) {
            return false;
        }

        // Require Ctrl / Command on Desktop for window hotkeys
        boolean ctrlDown = Core.input.keyDown(KeyCode.controlLeft) || Core.input.keyDown(KeyCode.controlRight);
        if (!ctrlDown) return false;

        String targetWindow = hotkeys.get(keyCode);
        if (targetWindow != null) {
            toggle(targetWindow);
            return true;
        }

        return super.keyDown(event, keyCode);
    }
}
