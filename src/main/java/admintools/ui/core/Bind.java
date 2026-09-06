package admintools.ui.core;

import arc.graphics.Color;
import arc.scene.Element;
import arc.scene.ui.Button;
import arc.scene.ui.Label;
import arc.scene.ui.layout.Cell;

/**
 * Convenient, safe binding helpers connecting Arc UI Elements with reactive State.
 */
public final class Bind {

    private Bind() {
    }

    public static Subscription text(Label label, ReadState<String> state) {
        return state.subscribe(label::setText);
    }

    public static Subscription visible(Element element, ReadState<Boolean> state) {
        return state.subscribe(b -> element.visible = b);
    }

    public static Subscription visible(Cell<?> cell, ReadState<Boolean> state) {
        return state.subscribe(b -> {
            if (cell.get() != null) cell.get().visible = b;
        });
    }

    public static Subscription disabled(Button button, ReadState<Boolean> state) {
        return state.subscribe(button::setDisabled);
    }

    public static Subscription color(Label label, ReadState<Color> state) {
        return state.subscribe(label::setColor);
    }
}
