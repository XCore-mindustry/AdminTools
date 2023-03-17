package admintools;

import arc.Core;
import arc.graphics.Color;
import arc.input.KeyCode;
import arc.scene.ui.ScrollPane;
import arc.scene.ui.TextButton;
import arc.scene.ui.TextField;
import arc.scene.ui.layout.Scl;
import arc.scene.ui.layout.Table;
import arc.util.Align;
import mindustry.ui.Fonts;
import mindustry.ui.Styles;
import mindustry.ui.dialogs.BaseDialog;
import mindustry.ui.dialogs.JoinDialog;

import static mindustry.Vars.*;

public class ConsoleFrameDialog extends BaseDialog {
    public static Table bannedPlayers = new Table();
    public static Table probiv = new Table();

    public static TextField searcher = new TextField();

    public ScrollPane panel1, panel2;

    public ConsoleFrameDialog(String name){
        super(name);

        var style = new TextButton.TextButtonStyle(){{
            over = Styles.flatOver;
            font = Fonts.def;
            fontColor = Color.white;
            disabledFontColor = Color.gray;
            down = Styles.flatOver;
            up = Styles.black5;
        }};

        buttons.defaults().size(210f, 64f);

        buttons.button("Забаненые игроки",()->{panel1.visible = true;panel2.visible=false;updateVisibility();});
        buttons.add();
        buttons.button("Пробиватор",()->{panel1.visible = false;panel2.visible=true;updateVisibility();});
        buttons.row();

        buttons.add().growX().width(-1);

        addCloseButton();

        buttons.add().growX().width(-1);
        bannedPlayers.add("nya");
        bannedPlayers.row();
        bannedPlayers.add("nya");
        bannedPlayers.row();

        probiv.add("ня");
        probiv.row();
        probiv.add("ня");
        probiv.row();

        panel1 = new ScrollPane(bannedPlayers);
        panel1.setFadeScrollBars(false);
        panel1.setScrollingDisabled(true, false);
        cont.add(panel1);

        panel2 = new ScrollPane(probiv);
        panel2.setFadeScrollBars(false);
        panel2.setScrollingDisabled(true, false);
        cont.add(panel2);

        cont.row();
    }
    public static void init(){
        //TODO tests Only
        bannedPlayers.button("Забаненые игроки",()->{});
        probiv.add("Пробиватор");
    }
}
