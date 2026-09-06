package admintools.ui.window;

import admintools.ui.core.UiScope;
import admintools.ui.theme.LucidTheme;
import arc.Core;
import arc.func.Cons;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Lines;
import arc.input.KeyCode;
import arc.math.Mathf;
import arc.scene.Element;
import arc.scene.event.InputEvent;
import arc.scene.event.InputListener;
import arc.scene.event.Touchable;
import arc.scene.ui.ImageButton;
import arc.scene.ui.Label;
import arc.scene.ui.layout.Scl;
import arc.scene.ui.layout.Table;
import arc.util.Time;
import mindustry.gen.Icon;
import mindustry.graphics.Pal;
import mindustry.ui.Styles;

/**
 * A modern, draggable, resizable floating window for Arc UI with automatic position & size persistence.
 */
public class FloatingWindow extends Table implements AutoCloseable {
    public final WindowSpec spec;
    public final UiScope scope = new UiScope();

    protected final Table titleBar = new Table();
    protected final Table contentArea = new Table();
    protected final Table bottomBar = new Table();
    protected final Label titleLabel;
    protected final ImageButton collapseBtn;
    protected final ImageButton closeBtn;
    protected final Element resizeGrip;

    protected boolean collapsed = false;
    protected boolean focused = false;
    protected boolean stateRestored = false;
    protected float uncollapsedHeight;
    protected WindowManager manager;
    private long lastTitleClickTime = 0;

    public FloatingWindow(WindowSpec spec, WindowManager manager) {
        this.spec = spec;
        this.manager = manager;
        this.uncollapsedHeight = Scl.scl(spec.defaultHeight());

        // Base table settings
        touchable = Touchable.enabled;
        background(LucidTheme.windowBg(false));

        // 1. Title bar setup - spacious and clear
        titleBar.background(LucidTheme.headerBg(false));
        titleBar.margin(6f, 12f, 6f, 10f);

        if (spec.icon() != null) {
            titleBar.image(spec.icon()).size(20f).padRight(12f);
        }

        titleLabel = titleBar.add(spec.title()).color(Pal.accent).growX().left().get();

        if (spec.collapsible()) {
            collapseBtn = new ImageButton(Icon.upOpen, Styles.clearNonei);
            collapseBtn.clicked(this::toggleCollapse);
            titleBar.add(collapseBtn).size(26f).padRight(4f);
        } else {
            collapseBtn = null;
        }

        closeBtn = new ImageButton(Icon.cancel, Styles.clearNonei);
        closeBtn.clicked(this::hide);
        titleBar.add(closeBtn).size(26f);

        // 2. Content area setup with clean padding
        contentArea.margin(10f);
        contentArea.top().left();

        // 3. Bottom bar & Resize grip
        resizeGrip = new Element() {
            @Override
            public void draw() {
                if (!spec.resizable() || collapsed) return;
                float parentAlpha = Draw.getColor().a;
                Draw.color(Pal.accent.r, Pal.accent.g, Pal.accent.b, 0.6f * parentAlpha);
                Lines.stroke(Scl.scl(1.5f));
                float right = x + width;
                float bottom = y;
                Lines.line(right - Scl.scl(10f), bottom, right, bottom + Scl.scl(10f));
                Lines.line(right - Scl.scl(6f), bottom, right, bottom + Scl.scl(6f));
                Draw.color();
            }
        };
        resizeGrip.setSize(Scl.scl(14f), Scl.scl(14f));

        bottomBar.right().bottom();
        bottomBar.margin(0f, 4f, 2f, 4f);
        bottomBar.add(resizeGrip).size(14f);

        // 4. Assemble layout
        add(titleBar).growX().height(36f).row();
        add(contentArea).grow().row();
        add(bottomBar).growX().height(10f);

        setSize(Scl.scl(spec.defaultWidth()), uncollapsedHeight);

        // Auto-restore state when parent layout is ready
        update(() -> {
            if (!stateRestored && parent != null && parent.getWidth() > 0) {
                ensureRestored();
            }
        });

        // 5. Input Listeners
        initInput();
    }

    private void initInput() {
        // Window click brings it to front without blocking children
        addListener(new InputListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, KeyCode button) {
                if (manager != null) manager.bringToFront(FloatingWindow.this);
                return false;
            }
        });

        // Title bar dragging & double-click collapse
        titleBar.addListener(new InputListener() {
            private float lastStageX, lastStageY;

            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, KeyCode button) {
                if (manager != null) manager.bringToFront(FloatingWindow.this);

                long now = Time.millis();
                if (now - lastTitleClickTime < 300) {
                    toggleCollapse();
                    lastTitleClickTime = 0;
                    return true;
                }
                lastTitleClickTime = now;

                lastStageX = event.stageX;
                lastStageY = event.stageY;
                return true;
            }

            @Override
            public void touchDragged(InputEvent event, float x, float y, int pointer) {
                float dx = event.stageX - lastStageX;
                float dy = event.stageY - lastStageY;
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
        });

        // Corner resizing
        if (spec.resizable()) {
            resizeGrip.addListener(new InputListener() {
                private float lastStageX, lastStageY;

                @Override
                public boolean touchDown(InputEvent event, float x, float y, int pointer, KeyCode button) {
                    if (collapsed) return false;
                    lastStageX = event.stageX;
                    lastStageY = event.stageY;
                    return true;
                }

                @Override
                public void touchDragged(InputEvent event, float x, float y, int pointer) {
                    float dx = event.stageX - lastStageX;
                    float dy = event.stageY - lastStageY;
                    lastStageX = event.stageX;
                    lastStageY = event.stageY;

                    float newW = Math.max(Scl.scl(spec.minWidth()), width + dx);
                    float newH = Math.max(Scl.scl(spec.minHeight()), height - dy);

                    float actualDy = height - newH;
                    y += actualDy;
                    setSize(newW, newH);
                    uncollapsedHeight = newH;
                    invalidateHierarchy();
                    clampToBounds();
                }

                @Override
                public void touchUp(InputEvent event, float x, float y, int pointer, KeyCode button) {
                    clampToBounds();
                    saveState();
                }
            });
        }
    }

    public Table getContent() {
        return contentArea;
    }

    public FloatingWindow content(Cons<Table> builder) {
        contentArea.clear();
        builder.get(contentArea);
        return this;
    }

    public void setFocused(boolean focused) {
        this.focused = focused;
        background(LucidTheme.windowBg(focused));
        titleBar.background(LucidTheme.headerBg(focused));
    }

    public void toggleCollapse() {
        if (!spec.collapsible()) return;

        float topY = y + height;
        collapsed = !collapsed;
        contentArea.visible = !collapsed;
        bottomBar.visible = !collapsed;

        float titleHeight = Scl.scl(36f);

        if (collapsed) {
            uncollapsedHeight = height;
            y = topY - titleHeight;
            setHeight(titleHeight);
            if (collapseBtn != null) collapseBtn.getStyle().imageUp = Icon.downOpen;
        } else {
            y = topY - uncollapsedHeight;
            setHeight(uncollapsedHeight);
            if (collapseBtn != null) collapseBtn.getStyle().imageUp = Icon.upOpen;
        }

        invalidateHierarchy();
        clampToBounds();
        saveState();
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

    public void ensureRestored() {
        if (stateRestored || parent == null || parent.getWidth() <= 0) return;
        stateRestored = true;
        restoreState();
    }

    public void restoreState() {
        if (parent == null || parent.getWidth() <= 0 || parent.getHeight() <= 0) return;

        float savedW = Core.settings.getFloat("lucid_win_" + spec.id() + "_w", spec.defaultWidth());
        float savedH = Core.settings.getFloat("lucid_win_" + spec.id() + "_h", spec.defaultHeight());

        float w = Math.max(Scl.scl(spec.minWidth()), Math.min(Scl.scl(savedW), parent.getWidth()));
        float h = Math.max(Scl.scl(spec.minHeight()), Math.min(Scl.scl(savedH), parent.getHeight()));

        setSize(w, h);
        uncollapsedHeight = h;

        float defaultX = (parent.getWidth() - w) / 2f;
        float defaultY = (parent.getHeight() - h) / 2f;

        float normX = Core.settings.getFloat("lucid_win_" + spec.id() + "_x", defaultX / parent.getWidth());
        float normY = Core.settings.getFloat("lucid_win_" + spec.id() + "_y", defaultY / parent.getHeight());

        setPosition(normX * parent.getWidth(), normY * parent.getHeight());
        clampToBounds();

        boolean wasCollapsed = Core.settings.getBool("lucid_win_" + spec.id() + "_collapsed", false);
        if (wasCollapsed && !collapsed) {
            toggleCollapse();
        }

        boolean wasOpen = Core.settings.getBool("lucid_win_" + spec.id() + "_open", false);
        if (wasOpen) {
            visible = true;
            if (manager != null) manager.bringToFront(this);
            clampToBounds();
        }
    }

    public void saveState() {
        if (parent == null || parent.getWidth() <= 0 || parent.getHeight() <= 0) return;

        float fullY = collapsed ? (y + height - uncollapsedHeight) : y;

        Core.settings.put("lucid_win_" + spec.id() + "_x", x / parent.getWidth());
        Core.settings.put("lucid_win_" + spec.id() + "_y", fullY / parent.getHeight());
        Core.settings.put("lucid_win_" + spec.id() + "_w", width / Scl.scl(1f));
        Core.settings.put("lucid_win_" + spec.id() + "_h", uncollapsedHeight / Scl.scl(1f));
        Core.settings.put("lucid_win_" + spec.id() + "_collapsed", collapsed);
        Core.settings.put("lucid_win_" + spec.id() + "_open", visible);
        Core.settings.saveValues();
    }

    public void hide() {
        visible = false;
        if (Core.scene != null) {
            Element focus = Core.scene.getScrollFocus();
            if (focus != null && focus.isDescendantOf(this)) {
                Core.scene.setScrollFocus(null);
            }
            Element kb = Core.scene.getKeyboardFocus();
            if (kb != null && kb.isDescendantOf(this)) {
                Core.scene.setKeyboardFocus(null);
            }
        }
        saveState();
    }

    public void show() {
        ensureRestored();
        visible = true;
        if (manager != null) manager.bringToFront(this);
        clampToBounds();
        saveState();
    }

    public void toggle() {
        if (visible) hide();
        else show();
    }

    @Override
    public void close() {
        hide();
        scope.close();
        remove();
    }
}
