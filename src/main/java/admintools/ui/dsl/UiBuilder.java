package admintools.ui.dsl;

import admintools.ui.theme.LucidTheme;
import arc.func.Cons;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Lines;
import arc.scene.Element;
import arc.scene.style.Drawable;
import arc.scene.ui.ImageButton;
import arc.scene.ui.Label;
import arc.scene.ui.TextButton;
import arc.scene.ui.layout.Cell;
import arc.scene.ui.layout.Scl;
import arc.scene.ui.layout.Table;
import mindustry.graphics.Pal;
import mindustry.ui.Styles;

/**
 * Fluent DSL layout helpers for composing clean Arc UI hierarchies.
 */
public final class UiBuilder {

    private UiBuilder() {
    }

    public static Table row(Table parent, Cons<Table> builder) {
        Table r = new Table();
        r.left();
        builder.get(r);
        parent.add(r).growX().row();
        return r;
    }

    public static Table card(Table parent, Cons<Table> builder) {
        Table c = new Table();
        c.background(LucidTheme.cardBg());
        c.margin(8f);
        builder.get(c);
        parent.add(c).growX().padBottom(Scl.scl(6f)).row();
        return c;
    }

    public static Cell<Table> stat(Table parent, String label, String value, Color valueColor) {
        Table t = new Table();
        Label lbl = t.add(label + ": ").color(LucidTheme.textDim).get();
        lbl.setFontScale(0.85f);

        Label val = t.add(value).color(valueColor != null ? valueColor : Pal.accent).get();
        val.setFontScale(0.85f);

        return parent.add(t).pad(Scl.scl(2f), Scl.scl(6f), Scl.scl(2f), Scl.scl(6f));
    }

    public static void separator(Table parent) {
        Element line = new Element() {
            @Override
            public void draw() {
                float parentAlpha = Draw.getColor().a;
                Draw.color(LucidTheme.borderSubtle.r, LucidTheme.borderSubtle.g, LucidTheme.borderSubtle.b, LucidTheme.borderSubtle.a * parentAlpha);
                Lines.stroke(LucidTheme.borderWidth());
                Lines.line(x, y + height / 2f, x + width, y + height / 2f);
                Draw.color();
            }
        };
        parent.add(line).growX().height(Scl.scl(6f)).pad(Scl.scl(4f), 0, Scl.scl(4f), 0).row();
    }

    public static Cell<TextButton> button(Table parent, String text, Runnable onClick) {
        TextButton btn = new TextButton(text, LucidTheme.flatTextButtonStyle());
        btn.clicked(onClick);
        return parent.add(btn).height(Scl.scl(32f));
    }

    public static Cell<ImageButton> iconButton(Table parent, Drawable icon, Runnable onClick) {
        ImageButton btn = new ImageButton(icon, LucidTheme.glassImageButtonStyle());
        btn.clicked(onClick);
        return parent.add(btn).size(Scl.scl(32f));
    }
}
