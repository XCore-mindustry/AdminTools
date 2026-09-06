package admintools.ui.theme;

import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Fill;
import arc.graphics.g2d.Lines;
import arc.scene.style.BaseDrawable;
import arc.scene.style.Drawable;
import arc.scene.ui.Button.ButtonStyle;
import arc.scene.ui.ImageButton.ImageButtonStyle;
import arc.scene.ui.TextButton.TextButtonStyle;
import arc.scene.ui.TextField.TextFieldStyle;
import arc.scene.ui.layout.Scl;
import mindustry.gen.Tex;
import mindustry.graphics.Pal;
import mindustry.ui.Styles;

public class LucidTheme {

    // Palette tokens
    public static final Color bgGlass = new Color(0.06f, 0.07f, 0.10f, 0.88f);
    public static final Color bgHeader = new Color(0.11f, 0.13f, 0.18f, 0.96f);
    public static final Color bgCard = new Color(0.12f, 0.14f, 0.20f, 0.75f);
    public static final Color bgHover = new Color(0.20f, 0.24f, 0.32f, 0.85f);
    public static final Color bgActive = new Color(0.24f, 0.30f, 0.42f, 0.90f);

    public static final Color borderIdle = new Color(0.25f, 0.28f, 0.36f, 0.55f);
    public static final Color borderFocus = Pal.accent;
    public static final Color borderSubtle = new Color(0.18f, 0.20f, 0.26f, 0.40f);

    public static final Color accent = Pal.accent;
    public static final Color danger = Pal.remove;
    public static final Color success = Pal.heal;
    public static final Color warning = Color.valueOf("ffa533");
    public static final Color textMuted = Pal.lightishGray;
    public static final Color textDim = Color.valueOf("8a8e9e");

    // Metrics
    public static float borderWidth() {
        return Math.max(1f, Scl.scl(1f));
    }

    public static float touchTarget() {
        return Scl.scl(38f);
    }

    public static float padSmall() {
        return Scl.scl(4f);
    }

    public static float pad() {
        return Scl.scl(8f);
    }

    public static float padLarge() {
        return Scl.scl(14f);
    }

    // Dynamic Glass Drawables
    public static Drawable glass(Color bg, Color border) {
        return new BaseDrawable() {
            @Override
            public void draw(float x, float y, float width, float height) {
                float parentAlpha = Draw.getColor().a;

                // Background
                if (bg != null && bg.a > 0) {
                    Draw.color(bg.r, bg.g, bg.b, bg.a * parentAlpha);
                    Fill.crect(x, y, width, height);
                }

                // Crisp border
                if (border != null && border.a > 0) {
                    Draw.color(border.r, border.g, border.b, border.a * parentAlpha);
                    Lines.stroke(borderWidth());
                    Lines.rect(x, y, width, height);
                }

                Draw.color(); // Reset color
            }
        };
    }

    public static Drawable windowBg(boolean focused) {
        return glass(bgGlass, focused ? borderFocus : borderIdle);
    }

    public static Drawable headerBg(boolean focused) {
        return glass(bgHeader, focused ? borderFocus : borderIdle);
    }

    public static Drawable cardBg() {
        return glass(bgCard, borderSubtle);
    }

    public static Drawable badgeBg(Color color) {
        Color softBg = color.cpy().a(0.20f);
        Color border = color.cpy().a(0.70f);
        return glass(softBg, border);
    }

    // Custom Styles
    public static ButtonStyle flatButtonStyle() {
        var style = new ButtonStyle();
        style.up = glass(bgCard, borderSubtle);
        style.over = glass(bgHover, borderIdle);
        style.down = glass(bgActive, accent);
        return style;
    }

    public static TextButtonStyle flatTextButtonStyle() {
        var style = new TextButtonStyle();
        style.font = Styles.defaultt.font;
        style.fontColor = Color.white;
        style.overFontColor = Pal.accent;
        style.downFontColor = Pal.accent;
        style.up = glass(bgCard, borderSubtle);
        style.over = glass(bgHover, borderIdle);
        style.down = glass(bgActive, accent);
        return style;
    }

    public static ImageButtonStyle glassImageButtonStyle() {
        var style = new ImageButtonStyle();
        style.up = glass(bgCard, borderSubtle);
        style.over = glass(bgHover, borderIdle);
        style.down = glass(bgActive, accent);
        return style;
    }
}
