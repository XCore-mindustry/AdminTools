package admintools.ui.auth;

import admintools.AdminTools;
import admintools.ui.components.ToastManager;
import arc.Core;
import arc.Events;
import arc.util.Log;
import arc.util.Time;
import arc.util.serialization.Json;
import arc.util.serialization.JsonWriter;
import mindustry.Vars;
import mindustry.game.EventType;
import mindustry.gen.Call;
import mindustry.mod.Mods.LoadedMod;

public class AuthManager {
    private static final AuthManager instance = new AuthManager();
    private static final Json strictJson = new Json(JsonWriter.OutputType.json);

    public static final int MIN_PASSWORD_LENGTH = 8;

    public static AuthManager get() {
        return instance;
    }

    public enum Status {
        IDLE,
        AUTHENTICATING,
        AUTHENTICATED,
        ERROR
    }

    // Immutable snapshot representing authoritative state from server
    private AuthSnapshot snapshot = AuthSnapshot.initial();

    // Ephemeral UI / request state
    private boolean isRequestPending = false;
    private int currentRequestId = 0;
    private int pendingRequestId = -1;
    private long pendingRequestEpoch = -1L;
    private String lastError = "";

    // Active Discord link request info
    private String activeLinkCode = null;
    private long activeLinkExpiresAt = 0L;

    // Single-shot auto-login tracking per connection epoch
    private long connectionEpoch = 0L;
    private long autoLoginAttemptedEpoch = -1L;

    // In-flight credentials waiting for server confirmation
    private boolean pendingRemember = false;
    private boolean pendingAutoLogin = true;

    public void init() {
        // Reset connection epoch on disconnect / reset
        Events.on(EventType.ResetEvent.class, e -> resetSession());

        // Handle server handshake identifying XCore
        Vars.netClient.addPacketHandler("adm_mod_begin", content -> {
            connectionEpoch++;
            snapshot = new AuthSnapshot(true, false, false, "", false, false, isRuntimeAdmin(), snapshot.revision);
            isRequestPending = false;
            pendingRequestId = -1;
            pendingRequestEpoch = -1L;

            LoadedMod mod = Vars.mods.getMod(AdminTools.class);
            String version = (mod != null && mod.meta != null && mod.meta.version != null) ? mod.meta.version : "1.8.2";
            Call.serverPacketReliable("adm_mod_end", version);

            // Do not start auto-login here: wait for authoritative adm_auth_status from server!
        });

        // Handle authoritative status updates from server
        Vars.netClient.addPacketHandler("adm_auth_status", content -> {
            try {
                AuthStatusPacket p = strictJson.fromJson(AuthStatusPacket.class, content);
                if (p != null) {
                    Core.app.post(() -> onAuthStatus(p));
                }
            } catch (Exception e) {
                Log.err("Failed to parse adm_auth_status: @", e);
            }
        });

        // Handle Discord link code information
        Vars.netClient.addPacketHandler("adm_discord_link_info", content -> {
            try {
                DiscordLinkInfoPacket p = strictJson.fromJson(DiscordLinkInfoPacket.class, content);
                if (p != null) {
                    Core.app.post(() -> onDiscordLinkInfo(p));
                }
            } catch (Exception e) {
                Log.err("Failed to parse adm_discord_link_info: @", e);
            }
        });

        // Handle auth results
        Vars.netClient.addPacketHandler("adm_auth_result", content -> {
            try {
                AuthResultPacket result = strictJson.fromJson(AuthResultPacket.class, content);
                if (result != null) {
                    Core.app.post(() -> onAuthResult(result));
                }
            } catch (Exception e) {
                Log.err("Failed to parse adm_auth_result: @", e);
            }
        });
    }

    public void resetSession() {
        connectionEpoch++;
        snapshot = AuthSnapshot.initial();
        isRequestPending = false;
        pendingRequestId = -1;
        pendingRequestEpoch = -1L;
        lastError = "";
        activeLinkCode = null;
        activeLinkExpiresAt = 0L;
    }

    public AuthSnapshot getSnapshot() {
        return snapshot;
    }

    public boolean isRuntimeAdmin() {
        return Vars.player != null && Vars.player.admin;
    }

    public boolean isXCore() {
        return snapshot.isXCore;
    }

    public Status getStatus() {
        if (isRuntimeAdmin()) {
            return Status.AUTHENTICATED;
        }
        if (isRequestPending) {
            return Status.AUTHENTICATING;
        }
        if (!lastError.isEmpty()) {
            return Status.ERROR;
        }
        return Status.IDLE;
    }

    public boolean isStatusKnown() {
        return snapshot.isStatusKnown;
    }

    public boolean isDiscordLinked() {
        return snapshot.isDiscordLinked;
    }

    public String getDiscordUsername() {
        return snapshot.discordUsername;
    }

    public boolean hasDiscordAdmin() {
        return snapshot.hasDiscordAdmin;
    }

    public boolean hasPassword() {
        return snapshot.hasPassword;
    }

    public String getLastError() {
        return lastError;
    }

    public String getActiveLinkCode() {
        return activeLinkCode;
    }

    public long getActiveLinkExpiresAt() {
        return activeLinkExpiresAt;
    }

    public void clearActiveLinkCode() {
        activeLinkCode = null;
        activeLinkExpiresAt = 0L;
        if (AuthDialog.current != null) {
            AuthDialog.current.rebuildUI();
        }
    }

    public String getSavedToken() {
        return Core.settings.getString("admintools-auth-token", "");
    }

    public String getSavedPassword() {
        return Core.settings.getString("admintools-auth-password", "");
    }

    public boolean hasSavedCredentials() {
        return !getSavedToken().isEmpty() || !getSavedPassword().isEmpty();
    }

    public boolean isAutoLoginEnabled() {
        return Core.settings.getBool("admintools-auth-autologin", true);
    }

    public boolean canAutoLogin() {
        return snapshot.isXCore
                && snapshot.isStatusKnown
                && snapshot.isDiscordLinked
                && snapshot.hasDiscordAdmin
                && isAutoLoginEnabled()
                && hasSavedCredentials()
                && !isRuntimeAdmin()
                && !isRequestPending;
    }

    public void tryAutoLogin() {
        if (autoLoginAttemptedEpoch == connectionEpoch) return;
        if (canAutoLogin()) {
            autoLoginAttemptedEpoch = connectionEpoch;
            performAutoLogin();
        }
    }

    public void requestAuthStatus() {
        Call.serverPacketReliable("adm_auth_status_req", "");
    }

    public void requestDiscordLink() {
        Call.serverPacketReliable("adm_discord_link_req", "");
    }

    public void performAutoLogin() {
        String token = getSavedToken();
        if (!token.isEmpty()) {
            loginWithToken(token);
            return;
        }

        String pass = getSavedPassword();
        if (!pass.isEmpty()) {
            loginWithPassword(pass, true, true);
        }
    }

    public void loginWithPassword(String password, boolean rememberDevice, boolean autoLogin) {
        if (isRequestPending) return;

        if (password == null || password.length() < MIN_PASSWORD_LENGTH) {
            lastError = arc.Core.bundle.format("admintools.auth.pass_too_short", MIN_PASSWORD_LENGTH);
            if (AuthDialog.current != null) {
                AuthDialog.current.setError(lastError);
            }
            return;
        }

        isRequestPending = true;
        lastError = "";
        pendingRemember = rememberDevice;
        pendingAutoLogin = autoLogin;

        currentRequestId++;
        int reqId = currentRequestId;
        pendingRequestId = reqId;
        pendingRequestEpoch = connectionEpoch;

        AuthLoginPacket packet = new AuthLoginPacket(reqId, password, rememberDevice);
        Call.serverPacketReliable("adm_auth_login", strictJson.toJson(packet));

        setupTimeoutGuard(reqId, connectionEpoch);
    }

    public void loginWithToken(String token) {
        if (isRequestPending) return;

        isRequestPending = true;
        lastError = "";
        pendingRemember = true;
        pendingAutoLogin = isAutoLoginEnabled();

        currentRequestId++;
        int reqId = currentRequestId;
        pendingRequestId = reqId;
        pendingRequestEpoch = connectionEpoch;

        AuthTokenLoginPacket packet = new AuthTokenLoginPacket(reqId, token);
        Call.serverPacketReliable("adm_auth_token_login", strictJson.toJson(packet));

        setupTimeoutGuard(reqId, connectionEpoch);
    }

    private void setupTimeoutGuard(int reqId, long epoch) {
        Time.runTask(600f, () -> {
            if (isRequestPending && pendingRequestId == reqId && pendingRequestEpoch == epoch) {
                isRequestPending = false;
                pendingRequestId = -1;
                pendingRequestEpoch = -1L;
                lastError = arc.Core.bundle.get("admintools.auth.timeout");
                if (AuthDialog.current != null) {
                    AuthDialog.current.setError(lastError);
                }
            }
        });
    }

    public void logout() {
        String token = getSavedToken();
        AuthLogoutPacket packet = new AuthLogoutPacket(token);
        Call.serverPacketReliable("adm_auth_logout", strictJson.toJson(packet));

        // Purge stored credentials on explicit logout
        Core.settings.remove("admintools-auth-token");
        Core.settings.remove("admintools-auth-password");
        Core.settings.saveValues();

        isRequestPending = false;
        pendingRequestId = -1;
        pendingRequestEpoch = -1L;
        lastError = "";
        ToastManager.info(arc.Core.bundle.get("admintools.auth.logged_out"));
    }

    private void onAuthStatus(AuthStatusPacket p) {
        // Monotonic check: ignore outdated packets
        if (p.revision < snapshot.revision) return;

        snapshot = new AuthSnapshot(snapshot.isXCore, true, p.isDiscordLinked, p.discordUsername, p.hasDiscordAdmin, p.hasPassword, p.isAdmin, p.revision);

        if (p.isDiscordLinked) {
            activeLinkCode = null;
            activeLinkExpiresAt = 0L;
        }

        if (AuthDialog.current != null) {
            AuthDialog.current.rebuildUI();
        }

        tryAutoLogin();
    }

    private void onDiscordLinkInfo(DiscordLinkInfoPacket p) {
        if (p.success && p.code != null && !p.code.isEmpty()) {
            this.activeLinkCode = p.code;
            this.activeLinkExpiresAt = p.expiresAt;
            if (AuthDialog.current != null) {
                AuthDialog.current.rebuildUI();
            }
        } else if (p.error != null && !p.error.isEmpty()) {
            ToastManager.error(arc.Core.bundle.format("admintools.auth.link_error", p.error));
        }
    }

    private void onAuthResult(AuthResultPacket result) {
        // Epoch-bound guard: Reject stale packets from prior connection epochs
        if (!isRequestPending || result.requestId != pendingRequestId || pendingRequestEpoch != connectionEpoch) {
            return;
        }
        isRequestPending = false;
        pendingRequestId = -1;
        pendingRequestEpoch = -1L;

        String resStatus = result.status != null ? result.status : "UNKNOWN";

        if ("SUCCESS".equals(resStatus) || "PASSWORD_CREATED".equals(resStatus)) {
            lastError = "";

            if (result.token != null && !result.token.isEmpty()) {
                Core.settings.put("admintools-auth-token", result.token);
                // Purge legacy plaintext password
                Core.settings.remove("admintools-auth-password");
            } else if (!pendingRemember) {
                Core.settings.remove("admintools-auth-token");
                Core.settings.remove("admintools-auth-password");
            }
            Core.settings.put("admintools-auth-autologin", pendingAutoLogin);
            Core.settings.saveValues();

            ToastManager.success(arc.Core.bundle.get("admintools.auth.success"));
            if (AuthDialog.current != null) {
                AuthDialog.current.hide();
            }
        } else if ("TOKEN_INVALID".equals(resStatus)) {
            // Saved token is invalid or was revoked on server
            Core.settings.remove("admintools-auth-token");
            Core.settings.saveValues();

            lastError = arc.Core.bundle.get("admintools.auth.token_invalid");
            ToastManager.error(lastError);
            if (AuthDialog.current != null) {
                AuthDialog.current.setError(lastError);
            }
        } else {
            lastError = switch (resStatus) {
                case "WRONG_PASSWORD" -> arc.Core.bundle.get("admintools.auth.wrong_password");
                case "PASSWORD_TOO_SHORT" -> arc.Core.bundle.format("admintools.auth.pass_too_short", MIN_PASSWORD_LENGTH);
                case "DISCORD_APPROVAL_REQUIRED" -> arc.Core.bundle.get("admintools.auth.no_discord_role");
                case "RATE_LIMITED" -> arc.Core.bundle.get("admintools.auth.rate_limited");
                default -> arc.Core.bundle.format("admintools.auth.general_error", resStatus);
            };

            ToastManager.error(lastError);
            if (AuthDialog.current != null) {
                AuthDialog.current.setError(lastError);
            }
        }
    }
}
