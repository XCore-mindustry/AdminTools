package admintools;

import arc.Core;
import arc.graphics.g2d.Draw;
import arc.input.KeyCode;
import arc.math.geom.Rect;
import arc.scene.event.InputEvent;
import arc.scene.event.InputListener;
import arc.scene.event.Touchable;
import arc.scene.ui.layout.WidgetGroup;
import arc.struct.Seq;
import arc.util.Align;
import arc.util.Time;
import mindustry.Vars;
import mindustry.content.Blocks;
import mindustry.gen.Icon;
import mindustry.ui.Styles;

import static arc.Core.camera;
import static mindustry.Vars.renderer;
import static mindustry.Vars.ui;

public class UIController extends InputListener {

    public static final WidgetGroup container = new WidgetGroup();
    public static final Rect bounds = new Rect();
    public final DraggableFragment karma = new DraggableFragment("carma");
    public final DraggableFragment history = new DraggableFragment("history");
    public final DraggableFragment portalTab = new DraggableFragment("portal");
    public final Seq<HistoryEntry> preview = new Seq<>();
    public boolean hideAll = false;
    public boolean showKarma = false;
    public boolean showHistory = false;
    public boolean showPortalTab = false;

    public static void connect(int port) {
        Vars.player.name(Core.settings.getString("name"));
        ui.join.connect("128.140.88.66", port);
    }

    public UIController() {
        container.setFillParent(true);
        container.touchable = Touchable.childrenOnly;
        container.visibility = (() -> !hideAll);

        Core.scene.addListener(this);

        Core.scene.add(container);

        //Generate Portal tab
        portalTab.button(Icon.modePvpSmall, Styles.defaulti, () -> connect(6567)).uniformX().uniformY().fill();
        portalTab.button(Icon.modeSurvivalSmall, Styles.defaulti, () ->connect(6568)).uniformX().uniformY().fill();
        portalTab.row();
        portalTab.button(Icon.planetSmall, Styles.defaulti, () ->connect(6569)).uniformX().uniformY().fill();
        portalTab.button(Icon.modeAttackSmall, Styles.defaulti, () ->connect(6570)).uniformX().uniformY().fill();
        portalTab.button(Icon.modeAttackSmall, Styles.defaulti, () ->connect(6575)).uniformX().uniformY().fill();
        portalTab.row();
        portalTab.button(Icon.starSmall, Styles.defaulti, () ->connect(6572)).uniformX().uniformY().fill();
        portalTab.button(Icon.commandAttackSmall, Styles.defaulti, () ->connect(6571)).uniformX().uniformY().fill();
        portalTab.button(Icon.wavesSmall, Styles.defaulti, () ->connect(6572)).uniformX().uniformY().fill();
        portalTab.row();
        portalTab.button(Icon.turretSmall, Styles.defaulti, () ->connect(6573)).uniformX().uniformY().fill();
        portalTab.button(Icon.redditAlienSmall, Styles.defaulti, () ->connect(6574)).uniformX().uniformY().fill();

        portalTab.visibility = (() -> !hideAll && showPortalTab);

        //Generate karma info
        karma.row();
        karma.add(KarmaDetector.table.align(Align.left));
        karma.visibility = (() -> !hideAll && showKarma);

        //history
        history.row();
        history.add(HistoryFrame.table.align(Align.left));
        history.visibility = (() -> !hideAll && showHistory);

        container.addChild(portalTab);
        container.addChild(karma);
        container.addChild(history);

        renderer.addEnvRenderer(0, () -> {
            camera.bounds(bounds); // do NOT use Tmp.r1

            preview.each(t -> {
                var tile = Vars.world.tile(t.x, t.y);
                var block = Vars.content.block(t.block);

                if (block == Blocks.air) tile.floor().drawBase(tile);
                else Draw.rect(block.region, t.x, t.y, t.rotation);
            });
        });

        //set positions
        Time.run(10, () -> portalTab.setPosition(container.getWidth() * Core.settings.getFloat("portal_x", 0.5f), container.getHeight() * Core.settings.getFloat("portal_y", 0.2f)));
        Time.run(10, () -> karma.setPosition(container.getWidth() * Core.settings.getFloat("carma_x", 0.5f), container.getHeight() * Core.settings.getFloat("carma_y", 0.5f)));
        Time.run(10, () -> history.setPosition(container.getWidth() * Core.settings.getFloat("history_x", 0.5f), container.getHeight() * Core.settings.getFloat("history_y", 0.5f)));
    }

    public void updatePreview(HistoryEntry[] p) {
        preview.clear();
        preview.addAll(p);
    }

    @Override
    public boolean keyDown(InputEvent event, KeyCode keyCode) {
        if(!Core.input.keyDown(KeyCode.controlLeft) && !Core.input.keyDown(KeyCode.controlRight)) return false;

        container.updateVisibility();

        return switch (keyCode){
            case h -> {
                showHistory = !showHistory;
                yield true;
            }
            case c -> {
                showKarma = !showKarma;
                yield true;
            }
            case t -> {
                showPortalTab = !showPortalTab;
                yield true;
            }
            default -> super.keyDown(event, keyCode);
        };
    }
}

