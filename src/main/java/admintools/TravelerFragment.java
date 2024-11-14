package admintools;

import arc.Core;
import arc.input.KeyCode;
import arc.math.Mathf;
import arc.math.geom.Vec2;
import arc.scene.event.InputEvent;
import arc.scene.event.InputListener;
import arc.scene.ui.ImageButton;
import arc.scene.ui.layout.Table;
import arc.util.Tmp;
import mindustry.gen.Icon;
import mindustry.ui.Styles;

public class TravelerFragment extends Table {

    public static float draggedAlpha = 0.25f;

    public ImageButton dragger;
    public boolean isDragging = false;

    public boolean needSave = false;

    public final String name;

    public TravelerFragment(String name) {
        this.name = name;
        dragger = new ImageButton(Icon.move, Styles.defaulti);
        add(dragger).uniformX().uniformY().fill();

        dragger.addListener(new InputListener() {

            private float dragx, dragy;

            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, KeyCode button) {
                dragx = x;
                dragy = y;
                isDragging = true;
                return true;
            }

            @Override
            public void touchDragged(InputEvent event, float x, float y, int pointer) {
                positionParent(x, y);
            }

            @Override
            public void touchUp(InputEvent e, float x, float y, int pointer, KeyCode button) {
                isDragging = false;
                needSave = true;
            }

        });

        update(() -> {
            if (needSave) {
                needSave = false;
                Core.settings.put(name + "_x", this.x / UIController.container.getWidth());
                Core.settings.put(name + "_y", this.y / UIController.container.getHeight());
                Core.settings.saveValues();
            }
            color.a = isDragging ? draggedAlpha : 1f;

            Vec2 pos = localToParentCoordinates(Tmp.v1.set(0, 0));
            setPosition(
                    Mathf.clamp(pos.x, getPrefWidth() / 2, parent.getWidth() - getPrefWidth() / 2),
                    Mathf.clamp(pos.y, getPrefHeight() / 2, parent.getHeight() - getPrefHeight() / 2)
            );
        });
    }

    public void positionParent(float x, float y) {
        if (parent == null) return;

        Vec2 pos = dragger.localToAscendantCoordinates(parent, Tmp.v1.set(x, y));
        setPosition(
                Mathf.clamp(pos.x, getPrefWidth() / 2, parent.getWidth() - getPrefWidth() / 2),
                Mathf.clamp(pos.y, getPrefHeight() / 2, parent.getHeight() - getPrefHeight() / 2)
        );
    }

}
