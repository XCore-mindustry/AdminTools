package admintools.ui.window;

import admintools.input.FreeCamController;
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
import arc.scene.ui.layout.Scl;
import arc.scene.ui.layout.Table;
import arc.struct.ObjectMap;
import mindustry.gen.Icon;
import mindustry.graphics.Pal;
import mindustry.ui.Styles;

/**
 * A sleek floating taskbar/dock for quickly toggling AdminTools windows on both Android and Desktop.
 * Truly collapses into a compact 34x34 pill button, and expands to full dock with drag handle.
 */
public class QuickDock extends Table {
    private final WindowManager manager;
    private final Table itemsTable = new Table();
    private final ObjectMap<String, ImageButton> dockButtons = new ObjectMap<>();
    private final ObjectMap<String, Label> badgeLabels = new ObjectMap<>();
    private boolean isCollapsed = false;
    private boolean dockRestored = false;
    private boolean wasDragged = false;

    private final InputListener dragListener = new InputListener() {
        private float lastStageX, lastStageY;

        @Override
        public boolean touchDown(InputEvent event, float x, float y, int pointer, KeyCode button) {
            lastStageX = event.stageX;
            lastStageY = event.stageY;
            wasDragged = false;
            return true;
        }

        @Override
        public void touchDragged(InputEvent event, float x, float y, int pointer) {
            float dx = event.stageX - lastStageX;
            float dy = event.stageY - lastStageY;
            if (Math.abs(dx) > 3f || Math.abs(dy) > 3f) {
                wasDragged = true;
            }
            moveBy(dx, dy);
            lastStageX = event.stageX;
            lastStageY = event.stageY;
            clampToBounds();
        }

        @Override
        public void touchUp(InputEvent event, float x, float y, int pointer, KeyCode button) {
            clampToBounds();
            saveState();
        }
    };

    public QuickDock(WindowManager manager) {
        this.manager = manager;

        touchable = Touchable.enabled;
        background(LucidTheme.glass(LucidTheme.bgHeader, LucidTheme.borderIdle));

        // Setup item buttons in container
        itemsTable.left();
        setupAuthButton();
        setupFreeCamButton();

        rebuildLayout();

        // Auto-restore state when parent is laid out
        update(() -> {
            if (!dockRestored && parent != null && parent.getWidth() > 0) {
                ensureRestored();
            }
        });
    }

    public boolean isCollapsed() {
        return isCollapsed;
    }

    public void toggleCollapse() {
        isCollapsed = !isCollapsed;
        rebuildLayout();
        saveState();
    }

    public void rebuildLayout() {
        clear();

        if (isCollapsed) {
            margin(4f, 8f, 4f, 8f);

            ImageButton dragHandle = new ImageButton(Icon.move, Styles.clearNonei);
            dragHandle.getStyle().imageUpColor = LucidTheme.textDim;
            dragHandle.addListener(dragListener);

            ImageButton expandBtn = new ImageButton(Icon.rightOpen, LucidTheme.glassImageButtonStyle());
            expandBtn.getStyle().imageUpColor = Pal.accent;
            expandBtn.clicked(this::toggleCollapse);

            Table slot = new Table();
            slot.stack(expandBtn, new Table(t -> {
                t.top().right();
                Label badge = new Label("");
                badge.setColor(Pal.remove);
                badge.setFontScale(0.7f);
                badge.update(() -> {
                    boolean hasBadge = false;
                    for (Label l : badgeLabels.values()) {
                        if (l.visible) {
                            hasBadge = true;
                            break;
                        }
                    }
                    badge.setText(hasBadge ? "●" : "");
                    badge.visible = hasBadge;
                });
                t.add(badge).padRight(-2f).padTop(-2f);
            })).size(28f);

            add(dragHandle).size(28f).padRight(6f);
            add(slot).size(28f);
        } else {
            margin(4f, 8f, 4f, 8f);

            ImageButton dragHandle = new ImageButton(Icon.move, Styles.clearNonei);
            dragHandle.getStyle().imageUpColor = LucidTheme.textDim;
            dragHandle.addListener(dragListener);

            ImageButton collapseBtn = new ImageButton(Icon.leftOpen, LucidTheme.glassImageButtonStyle());
            collapseBtn.getStyle().imageUpColor = LucidTheme.textDim;
            collapseBtn.clicked(this::toggleCollapse);

            add(dragHandle).size(28f).padRight(6f);
            add(itemsTable);
            add(collapseBtn).size(28f).padLeft(6f);
        }

        pack();
        invalidateHierarchy();
        clampToBounds();
    }

    private void setupAuthButton() {
        ImageButton authBtn = new ImageButton(Icon.lock, LucidTheme.glassImageButtonStyle());
        authBtn.clicked(() -> {
            if (AuthManager.get().getStatus() == AuthManager.Status.AUTHENTICATED) {
                ConfirmDialog.show("@admintools.auth.title", "@admintools.auth.already_authed", () -> {
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

        var cell = itemsTable.add(authBtn).size(30f).pad(0f, 3f, 0f, 3f);
        cell.visible(() -> AuthManager.get().isXCore() || AuthManager.get().getStatus() == AuthManager.Status.AUTHENTICATED);
    }

    private void setupFreeCamButton() {
        ImageButton camBtn = new ImageButton(Icon.eye, LucidTheme.glassImageButtonStyle());
        camBtn.clicked(() -> FreeCamController.get().toggle());

        camBtn.update(() -> {
            boolean active = FreeCamController.get().isActive();
            camBtn.getStyle().imageUpColor = active ? Pal.accent : LucidTheme.textDim;
            camBtn.getStyle().up = active ? LucidTheme.glass(LucidTheme.bgActive, LucidTheme.accent) : LucidTheme.glass(LucidTheme.bgCard, LucidTheme.borderSubtle);
        });

        var cell = itemsTable.add(camBtn).size(30f).pad(0f, 3f, 0f, 3f);
        cell.visible(() -> FreeCamController.get().isSupported());
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
        })).size(30f);

        dockButtons.put(spec.id(), btn);
        itemsTable.add(slot).size(30f).pad(0f, 3f, 0f, 3f);
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
        float defaultY = parent.getHeight() - getPrefHeight() - Scl.scl(12f);

        float normX = Core.settings.getFloat("lucid_dock_x", defaultX / parent.getWidth());
        float normY = Core.settings.getFloat("lucid_dock_y", defaultY / parent.getHeight());

        setPosition(normX * parent.getWidth(), normY * parent.getHeight());
        clampToBounds();

        if (Core.settings.getBool("lucid_dock_collapsed", false) && !isCollapsed) {
            isCollapsed = true;
            rebuildLayout();
        }
    }

    public void clampToBounds() {
        if (parent == null || parent.getWidth() <= 0) return;
        float safeMargin = Scl.scl(8f);
        float maxX = Math.max(safeMargin, parent.getWidth() - width - safeMargin);
        float maxY = Math.max(safeMargin, parent.getHeight() - height - safeMargin);
        setPosition(
            Mathf.clamp(x, safeMargin, maxX),
            Mathf.clamp(y, safeMargin, maxY)
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
