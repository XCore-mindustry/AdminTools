package admintools.ui.auth;

public class AuthStatusPacket {
    public boolean isDiscordLinked;
    public String discordUsername;
    public boolean hasDiscordAdmin;
    public boolean hasPassword;
    public boolean isAdmin;
    public long revision;

    public AuthStatusPacket() {}
}
