package admintools.ui.components;

import admintools.ui.theme.LucidTheme;
import arc.func.Cons;
import arc.scene.ui.Image;
import arc.scene.ui.ImageButton;
import arc.scene.ui.TextField;
import arc.scene.ui.layout.Table;
import arc.util.Timer;
import arc.util.Timer.Task;
import mindustry.gen.Icon;
import mindustry.ui.Styles;

/**
 * A modern search input with balanced padding, search icon, clear button, and debounced callback.
 */
public class SearchField extends Table {
    private final TextField field;
    private final ImageButton clearBtn;
    private Task debounceTask;
    private float debounceDelay = 0.2f;

    public SearchField(String placeholder, Cons<String> onSearch) {
        background(LucidTheme.glass(LucidTheme.bgCard, LucidTheme.borderSubtle));
        margin(5f, 12f, 5f, 12f);

        // 1. Search Icon with clear gap
        Image icon = new Image(Icon.zoom);
        icon.setColor(LucidTheme.textDim);
        add(icon).size(18f).padRight(10f);

        // 2. Text input
        field = new TextField("");
        field.setMessageText(placeholder);
        field.setStyle(Styles.defaultField);
        add(field).growX().height(28f);

        // 3. Clear button
        clearBtn = new ImageButton(Icon.cancel, Styles.clearNonei);
        clearBtn.visible = false;
        clearBtn.clicked(() -> {
            field.setText("");
            clearBtn.visible = false;
            if (debounceTask != null) debounceTask.cancel();
            onSearch.get("");
        });
        add(clearBtn).size(20f).padLeft(6f);

        // Typing handler with debounce
        field.changed(() -> {
            String text = field.getText();
            clearBtn.visible = !text.isEmpty();

            if (debounceTask != null) debounceTask.cancel();
            debounceTask = Timer.schedule(() -> onSearch.get(text), debounceDelay);
        });
    }

    public void setText(String text) {
        field.setText(text);
        clearBtn.visible = !text.isEmpty();
    }

    public String getText() {
        return field.getText();
    }

    public TextField getField() {
        return field;
    }

    public void setDebounceDelay(float delaySeconds) {
        this.debounceDelay = delaySeconds;
    }
}
