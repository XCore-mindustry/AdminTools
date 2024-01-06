package admintools;

import mindustry.world.Block;

//TODO make record? why is it unused?
public class Data {
    public final String nickname;
    public final Block block;
    public final boolean destroy;
    public final int size;
    public Data(String nickname, Block block, boolean destroy, int size){
        this.nickname = nickname;
        this.block = block;
        this.destroy = destroy;
        this.size = size;
    }
}
