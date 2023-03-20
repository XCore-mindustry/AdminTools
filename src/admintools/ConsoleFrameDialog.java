package admintools;

import arc.scene.ui.ScrollPane;
import arc.scene.ui.TextField;
import arc.scene.ui.layout.Table;
import arc.struct.Seq;
import arc.util.Log;
import mindustry.gen.Icon;
import mindustry.gen.Tex;
import mindustry.ui.dialogs.BaseDialog;

public class ConsoleFrameDialog extends BaseDialog {
    public static Table bannedPlayersPanel = new Table();
    public static Table bannedPlayersData = new Table();
    public static Table probiv = new Table();

    public static TextField searcher = new TextField();
    public static TextField searcher2 = new TextField();

    public static Seq<BannedPlayer> banned = new Seq<>();

    public ScrollPane panel1, panel2;

    public ConsoleFrameDialog(String name){
        super(name);

        updateBanned();

        buttons.defaults().size(210f, 64f);

        buttons.add().growX().width(-1);

        addCloseButton();

        buttons.add().growX().width(-1);
        bannedPlayersPanel.clear();
        bannedPlayersPanel.setHeight(this.getHeight()*0.8f);
        bannedPlayersPanel.add(searcher);
        bannedPlayersPanel.row();
        bannedPlayersPanel.add(" ");
        bannedPlayersPanel.row();
        bannedPlayersPanel.add(bannedPlayersData);

        probiv.setHeight(this.getHeight()*0.8f);
        probiv.add(searcher2);
        probiv.row();
        probiv.add("ня");
        probiv.row();
        probiv.add("ня");
        probiv.row();

        panel1 = new ScrollPane(bannedPlayersPanel);
        panel1.setFadeScrollBars(false);
        panel1.setScrollingDisabled(true, false);
        panel1.setWidth(50f);
        cont.add(panel1);

        panel2 = new ScrollPane(probiv);
        panel2.setFadeScrollBars(false);
        panel2.setScrollingDisabled(true, false);
//        cont.add(panel2);

        cont.row();
    }
    public static void init(){
        //TODO tests Only
        bannedPlayersPanel.button("Забаненые игроки",()->{});
        probiv.add("Пробиватор");
    }

    //TODO redo
    public void updateBanned(){
        banned.add(new BannedPlayer("[green] kowkonya","4YGwSr3sUNQAAAAAeFW8Vw==","127.0.0.1","console nya",123432224L,"HentaiPOst"));
        bannedPlayersData.clear();
        banned.each(p->{
            bannedPlayersData.table(Tex.whiteui, t -> {
                t.setColor(0.5f,0.5f,0.5f,1);
                t.left();
                t.button(Icon.undo,()->{Log.info(p.uuid);}).height(36f).width(36f);
                t.add();
                t.button(Icon.eye,()->{Log.info(p.uuid);}).height(36f).width(36f);
            }).growX().height(36f).row();
            bannedPlayersData.table(Tex.whitePane, t->{
                t.setColor(0.5f,0.5f,0.5f,1);
                t.left();

                t.add("Nickname");t.add("|");t.add(p.name).row();
                t.add("UUID");t.add("|");t.add(p.uuid).row();
                t.add("IP");t.add("|");t.add(p.ip).row();
                t.add("Unban date");t.add("|");t.add(p.unBanDate+"").row();
                t.add("Admin");t.add("|");t.add(p.adminName).row();
                t.add("Reason");t.add("|");t.add(p.reason).row();
            });
            bannedPlayersData.row();
            bannedPlayersData.add(" ");
            bannedPlayersData.row();
        });
    }

    public static class BannedPlayer{
        public String name;
        public String uuid;
        public String ip;
        public String adminName;
        public Long unBanDate;
        public String reason;

        public BannedPlayer(String name, String uuid, String ip,String adminName, Long unBanDate, String reason) {
            this.name = name;
            this.uuid = uuid;
            this.adminName = adminName;
            this.ip = ip;
            this.unBanDate = unBanDate;
            this.reason = reason;
        }
    }
}
