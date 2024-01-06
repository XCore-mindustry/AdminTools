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

public class DraggableFragment extends Table {

    public static final float draggedAlpha = 0.25f;
    public final ImageButton dragButton;
    public final String name;
    public boolean isDragging = false;
    public boolean needSave = false;

    public DraggableFragment(String name) {
        this.name = name;

        dragButton = new ImageButton(Icon.move, Styles.defaulti){{
            addListener(new InputListener() {
                @Override
                public boolean touchDown(InputEvent event, float x, float y, int pointer, KeyCode button) {
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
        }};

        add(dragButton).uniformX().uniformY().fill();

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

    private void positionParent(float x, float y) {
        if (parent == null) return;

        Vec2 pos = dragButton.localToAscendantCoordinates(parent, Tmp.v1.set(x, y));
        setPosition(
                Mathf.clamp(pos.x, getPrefWidth() / 2, parent.getWidth() - getPrefWidth() / 2),
                Mathf.clamp(pos.y, getPrefHeight() / 2, parent.getHeight() - getPrefHeight() / 2)
        );
    }
}
