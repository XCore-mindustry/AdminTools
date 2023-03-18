package admintools;

import arc.graphics.Color;
import arc.scene.ui.ScrollPane;
import arc.scene.ui.TextButton;
import arc.scene.ui.TextField;
import arc.scene.ui.layout.Table;
import mindustry.gen.Tex;
import mindustry.ui.Fonts;
import mindustry.ui.Styles;
import mindustry.ui.dialogs.BaseDialog;

public class ConsoleFrameDialog extends BaseDialog {
    public static Table bannedPlayers = new Table();
    public static Table probiv = new Table();

    public static TextField searcher = new TextField();
    public static TextField searcher2 = new TextField();

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
        bannedPlayers.setHeight(this.getHeight()*0.8f);
        bannedPlayers.add(searcher);
        bannedPlayers.row();
        bannedPlayers.table(Tex.whitePane, t->{
            t.setColor(color);
            t.left();

            t.add("[green]Kowkonya").row();
            t.add("hentaiAAAAAAA==").row();
            t.add("27.73.35.3").row();
            t.add("Заходил: 10").row();
            t.add("Кикнут: 1").row();
        });
        bannedPlayers.row();
        bannedPlayers.table(Tex.whitePane, t->{
            t.setColor(color);
            t.left();

            t.add("[green]Kowkonya").row();
            t.add("hentaiAAAAAAA==").row();
            t.add("27.73.35.3").row();
            t.add("Заходил: 10").row();
            t.add("Кикнут: 1").row();
        });

        probiv.setHeight(this.getHeight()*0.8f);
        probiv.add(searcher2);
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
