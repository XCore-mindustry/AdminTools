package admintools;

import mindustry.world.Block;

public class Data {
    public String nickname;
    public Block block;
    public boolean destroy;
    public int size;
    public Data(String nickname, Block block, boolean destroy, int size){
        this.nickname = nickname;
        this.block = block;
        this.destroy = destroy;
        this.size = size;
    }
}
