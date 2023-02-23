package admintools;

import mindustry.mod.*;

import static mindustry.Vars.netClient;

public class AdminTools extends Mod {
    @Override
    public void init() {
        netClient.addPacketHandler("give_ban_data", content -> new BanDialog(content).show());
    }
}
