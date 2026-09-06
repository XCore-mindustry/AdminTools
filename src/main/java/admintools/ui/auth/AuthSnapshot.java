package admintools.ui.auth;

public class AuthSnapshot {
    public final boolean isXCore;
    public final boolean isStatusKnown;
    public final boolean isDiscordLinked;
    public final String discordUsername;
    public final boolean hasDiscordAdmin;
    public final boolean hasPassword;
    public final boolean isRuntimeAdmin;
    public final long revision;

    public AuthSnapshot(boolean isXCore, boolean isStatusKnown, boolean isDiscordLinked, String discordUsername, boolean hasDiscordAdmin, boolean hasPassword, boolean isRuntimeAdmin, long revision) {
        this.isXCore = isXCore;
        this.isStatusKnown = isStatusKnown;
        this.isDiscordLinked = isDiscordLinked;
        this.discordUsername = discordUsername != null ? discordUsername : "";
        this.hasDiscordAdmin = hasDiscordAdmin;
        this.hasPassword = hasPassword;
        this.isRuntimeAdmin = isRuntimeAdmin;
        this.revision = revision;
    }

    public static AuthSnapshot initial() {
        return new AuthSnapshot(false, false, false, "", false, false, false, 0L);
    }
}
