package admintools.ui.window;

import admintools.ui.auth.AuthDialog;
import admintools.ui.auth.AuthManager;
import admintools.ui.components.ConfirmDialog;
import admintools.ui.theme.LucidTheme;
import arc.Core;
import arc.input.KeyCode;
import arc.math.Mathf;
import arc.scene.event.InputEvent;
import arc.scene.event.InputListener;
import arc.scene.event.Touchable;
import arc.scene.ui.ImageButton;
import arc.scene.ui.Label;
import arc.scene.ui.layout.Table;
import arc.struct.ObjectMap;
import mindustry.gen.Icon;
import mindustry.graphics.Pal;
import mindustry.ui.Styles;

/**
 * A sleek floating taskbar/dock for quickly toggling AdminTools windows on both Android and Desktop.
 */
public class QuickDock extends Table {
    private final WindowManager manager;
    private final Table itemsTable = new Table();
    private final ObjectMap<String, ImageButton> dockButtons = new ObjectMap<>();
    private final ObjectMap<String, Label> badgeLabels = new ObjectMap<>();
    private boolean isCollapsed = false;
    private boolean dockRestored = false;

    public QuickDock(WindowManager manager) {
        this.manager = manager;

        touchable = Touchable.enabled;
        background(LucidTheme.glass(LucidTheme.bgHeader, LucidTheme.borderIdle));
        margin(5f, 10f, 5f, 10f);

        // Drag handle for moving the dock
        ImageButton dragHandle = new ImageButton(Icon.move, Styles.clearNonei);
        dragHandle.addListener(new InputListener() {
            private float lastStageX, lastStageY;

            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, KeyCode button) {
                lastStageX = event.stageX;
                lastStageY = event.stageY;
                return true;
            }

            @Override
            public void touchDragged(InputEvent event, float x, float y, int pointer) {
                moveBy(event.stageX - lastStageX, event.stageY - lastStageY);
                lastStageX = event.stageX;
                lastStageY = event.stageY;
                clampToBounds();
            }

            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, KeyCode button) {
                clampToBounds();
                saveState();
            }
        });

        // Collapse / minimize toggle
        ImageButton collapseBtn = new ImageButton(Icon.leftOpen, Styles.clearNonei);
        collapseBtn.clicked(() -> {
            isCollapsed = !isCollapsed;
            itemsTable.visible = !isCollapsed;
            collapseBtn.getStyle().imageUp = isCollapsed ? Icon.rightOpen : Icon.leftOpen;
            pack();
            clampToBounds();
            saveState();
        });

        add(dragHandle).size(22f).padRight(8f);
        setupAuthButton();
        add(itemsTable).grow();
        add(collapseBtn).size(22f).padLeft(8f);

        // Auto-restore state when parent is laid out
        update(() -> {
            if (!dockRestored && parent != null && parent.getWidth() > 0) {
                ensureRestored();
            }
        });
    }

    private void setupAuthButton() {
        ImageButton authBtn = new ImageButton(Icon.lock, LucidTheme.glassImageButtonStyle());
        authBtn.clicked(() -> {
            if (AuthManager.get().getStatus() == AuthManager.Status.AUTHENTICATED) {
                ConfirmDialog.show("Авторизация XCore", "Вы уже авторизованы как администратор.\nХотите выйти?", () -> {
                    AuthManager.get().logout();
                });
            } else {
                AuthDialog.showDialog();
            }
        });

        authBtn.update(() -> {
            AuthManager.Status s = AuthManager.get().getStatus();
            if (s == AuthManager.Status.AUTHENTICATED) {
                authBtn.getStyle().imageUp = Icon.admin;
                authBtn.getStyle().imageUpColor = Pal.accent;
            } else if (s == AuthManager.Status.AUTHENTICATING) {
                authBtn.getStyle().imageUp = Icon.lock;
                authBtn.getStyle().imageUpColor = Pal.accent;
            } else if (s == AuthManager.Status.ERROR) {
                authBtn.getStyle().imageUp = Icon.lock;
                authBtn.getStyle().imageUpColor = Pal.remove;
            } else {
                authBtn.getStyle().imageUp = Icon.lock;
                authBtn.getStyle().imageUpColor = LucidTheme.textDim;
            }
        });

        var cell = itemsTable.add(authBtn).size(34f).pad(0f, 4f, 0f, 4f);
        cell.visible(() -> AuthManager.get().isXCore() || AuthManager.get().getStatus() == AuthManager.Status.AUTHENTICATED);
    }

    public void registerWindow(WindowSpec spec) {
        ImageButton btn = new ImageButton(spec.icon() != null ? spec.icon() : Icon.admin, LucidTheme.glassImageButtonStyle());
        btn.clicked(() -> manager.toggle(spec.id()));

        Table slot = new Table();
        slot.stack(btn, new Table(t -> {
            t.top().right();
            Label badge = new Label("");
            badge.setColor(Pal.remove);
            badge.setFontScale(0.75f);
            badge.visible = false;
            t.add(badge).padRight(-2f).padTop(-2f);
            badgeLabels.put(spec.id(), badge);
        })).size(32f);

        dockButtons.put(spec.id(), btn);
        itemsTable.add(slot).size(34f).pad(0f, 4f, 0f, 4f);
        pack();
        clampToBounds();
    }

    public void setBadge(String windowId, String text, boolean isVisible) {
        Label label = badgeLabels.get(windowId);
        if (label != null) {
            label.setText(text);
            label.visible = isVisible;
        }
    }

    public void ensureRestored() {
        if (dockRestored || parent == null || parent.getWidth() <= 0) return;
        dockRestored = true;
        pack();

        float defaultX = (parent.getWidth() - getPrefWidth()) / 2f;
        float defaultY = parent.getHeight() - getPrefHeight() - 10f;

        float normX = Core.settings.getFloat("lucid_dock_x", defaultX / parent.getWidth());
        float normY = Core.settings.getFloat("lucid_dock_y", defaultY / parent.getHeight());

        setPosition(normX * parent.getWidth(), normY * parent.getHeight());
        clampToBounds();

        if (Core.settings.getBool("lucid_dock_collapsed", false) && !isCollapsed) {
            isCollapsed = true;
            itemsTable.visible = false;
            pack();
            clampToBounds();
        }
    }

    public void clampToBounds() {
        if (parent == null || parent.getWidth() <= 0) return;
        float maxX = Math.max(0, parent.getWidth() - width);
        float maxY = Math.max(0, parent.getHeight() - height);
        setPosition(
            Mathf.clamp(x, 0, maxX),
            Mathf.clamp(y, 0, maxY)
        );
    }

    public void saveState() {
        if (parent == null || parent.getWidth() <= 0 || parent.getHeight() <= 0) return;
        Core.settings.put("lucid_dock_x", x / parent.getWidth());
        Core.settings.put("lucid_dock_y", y / parent.getHeight());
        Core.settings.put("lucid_dock_collapsed", isCollapsed);
        Core.settings.saveValues();
    }
}
