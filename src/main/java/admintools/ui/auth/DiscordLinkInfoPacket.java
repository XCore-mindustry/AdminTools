package admintools.ui.auth;

public class DiscordLinkInfoPacket {
    public boolean success;
    public String code;
    public long expiresAt;
    public String error;

    public DiscordLinkInfoPacket() {}
}
