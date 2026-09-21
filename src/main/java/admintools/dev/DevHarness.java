package admintools.dev;

import admintools.BanDialog;
import admintools.HistoryEntry;
import admintools.HistoryFrame;
import admintools.KarmaDetector;
import admintools.UIController;
import admintools.input.FreeCamController;
import admintools.ui.auth.AuthDialog;
import admintools.ui.theme.LucidTheme;
import arc.Core;
import arc.Events;
import arc.files.Fi;
import arc.input.KeyCode;
import arc.scene.event.InputEvent;
import arc.scene.event.InputListener;
import arc.scene.ui.Dialog;
import arc.util.Log;
import arc.util.ScreenUtils;
import arc.util.Time;
import mindustry.Vars;
import mindustry.game.EventType;
import mindustry.gen.Icon;

/**
 * Universal developer and automated test harness for AdminTools.
 * <p>
 * Zero-overhead in production: this class is dynamically loaded via reflection
 * only when explicit dev system properties (admintools.screen, admintools.dev, admintools.screenshot)
 * are supplied, and is stripped from the release JAR when building with -Prelease.
 */
public class DevHarness {

    private static boolean initialized = false;
    private static UIController ui;

    public static void init(UIController controller) {
        if (initialized) return;
        initialized = true;
        ui = controller;

        Log.info("[DevHarness] Initializing AdminTools developer harness...");

        configureDisplay();
        setupKeyListeners();

        String screen = System.getProperty("admintools.screen");
        String screenshotPath = System.getProperty("admintools.screenshot");

        if (screen != null && !screen.isEmpty()) {
            Time.runTask(20f, () -> {
                applyScreen(screen);

                if (screenshotPath != null && !screenshotPath.isEmpty()) {
                    int delay = Integer.getInteger("admintools.delay", 30);
                    boolean autoExit = !"false".equalsIgnoreCase(System.getProperty("admintools.exit", "true"));
                    scheduleScreenshot(screenshotPath, delay, autoExit);
                }
            });
        } else if (screenshotPath != null && !screenshotPath.isEmpty()) {
            int delay = Integer.getInteger("admintools.delay", 30);
            boolean autoExit = !"false".equalsIgnoreCase(System.getProperty("admintools.exit", "true"));
            scheduleScreenshot(screenshotPath, delay, autoExit);
        }
    }

    private static void configureDisplay() {
        String mobileProp = System.getProperty("admintools.mobile");
        if (mobileProp != null) {
            boolean isLandscape = "landscape".equalsIgnoreCase(mobileProp) || Boolean.getBoolean("admintools.mobile.landscape");
            boolean isMobile = "true".equalsIgnoreCase(mobileProp) || "portrait".equalsIgnoreCase(mobileProp) || isLandscape;

            if (isMobile) {
                Vars.mobile = true;
                Core.app.post(() -> {
                    if (isLandscape) {
                        Core.graphics.setWindowSize(800, 420);
                    } else {
                        Core.graphics.setWindowSize(480, 800);
                    }
                });
            }
        }

        int customWidth = Integer.getInteger("admintools.width", 0);
        int customHeight = Integer.getInteger("admintools.height", 0);
        if (customWidth > 0 && customHeight > 0) {
            Core.app.post(() -> Core.graphics.setWindowSize(customWidth, customHeight));
        }
    }

    private static void setupKeyListeners() {
        Core.scene.addListener(new InputListener() {
            @Override
            public boolean keyDown(InputEvent event, KeyCode keycode) {
                if (keycode == KeyCode.f8) {
                    showDevMenu();
                    return true;
                }
                return false;
            }
        });
    }

    public static void applyScreen(String screen) {
        Log.info("[DevHarness] Applying mock screen: @", screen);
        switch (screen.toLowerCase()) {
            case "portal" -> {
                ui.portalWindow.show();
                ui.portalWindow.setPosition(40f, 100f);
            }
            case "karma" -> {
                mockKarmaData();
                ui.karmaWindow.show();
                ui.karmaWindow.setPosition(40f, 120f);
            }
            case "history" -> {
                mockHistoryData();
                ui.historyWindow.show();
                ui.historyWindow.setPosition(40f, 120f);
            }
            case "ban" -> {
                new BanDialog("{\"name\":\"Griefer_Target\",\"pid\":42,\"ip\":\"127.0.0.1\",\"id\":\"abc12345\"}");
            }
            case "auth" -> {
                AuthDialog.showDialog();
            }
            case "dock_collapsed" -> {
                if (!ui.windowManager.getDock().isCollapsed()) {
                    ui.windowManager.getDock().toggleCollapse();
                }
            }
            case "dock_expanded" -> {
                if (ui.windowManager.getDock().isCollapsed()) {
                    ui.windowManager.getDock().toggleCollapse();
                }
            }
            case "freecam" -> {
                FreeCamController.get().toggle();
            }
            case "all" -> {
                mockKarmaData();
                mockHistoryData();
                ui.portalWindow.show();
                ui.portalWindow.setPosition(20f, 40f);
                ui.karmaWindow.show();
                ui.karmaWindow.setPosition(380f, 40f);
                ui.historyWindow.show();
                ui.historyWindow.setPosition(740f, 40f);
            }
            default -> Log.warn("[DevHarness] Unknown screen: @", screen);
        }
    }

    public static void mockKarmaData() {
        KarmaDetector.blocksBuild.clear();
        KarmaDetector.blocksDestroy.clear();

        KarmaDetector.blocksBuild.put("MegaBuilder2026", 450);
        KarmaDetector.blocksDestroy.put("MegaBuilder2026", 4);

        KarmaDetector.blocksBuild.put("Vova_Griefer", 12);
        KarmaDetector.blocksDestroy.put("Vova_Griefer", 148);

        KarmaDetector.blocksBuild.put("SuspiciousGuy", 30);
        KarmaDetector.blocksDestroy.put("SuspiciousGuy", 25);

        KarmaDetector.blocksBuild.put("NoobPlayer", 80);
        KarmaDetector.blocksDestroy.put("NoobPlayer", 0);

        KarmaDetector.update();
    }

    public static void mockHistoryData() {
        HistoryEntry h1 = new HistoryEntry();
        h1.name = "Vova_Griefer";
        h1.block = 1;
        h1.destroy = true;
        h1.time = 3665;
        h1.rotation = 1;
        h1.valid = true;
        h1.config = "copper";

        HistoryEntry h2 = new HistoryEntry();
        h2.name = "MegaBuilder2026";
        h2.block = 4;
        h2.destroy = false;
        h2.time = 3600;
        h2.rotation = 0;
        h2.valid = true;
        h2.config = null;

        HistoryEntry h3 = new HistoryEntry();
        h3.name = "SuspiciousGuy";
        h3.block = 2;
        h3.destroy = true;
        h3.time = 620;
        h3.rotation = 3;
        h3.valid = false;
        h3.config = null;

        HistoryFrame.update(new HistoryEntry[]{h1, h2, h3});
    }

    public static void scheduleScreenshot(String outputPath, int delayTicks, boolean autoExit) {
        Log.info("[DevHarness] Scheduling automated screenshot in @ ticks to: @", delayTicks, outputPath);

        boolean[] captured = {false};
        boolean[] ready = {false};

        Events.run(EventType.Trigger.uiDrawEnd, () -> {
            if (!ready[0] || captured[0]) return;
            captured[0] = true;

            try {
                Fi file = Fi.get(outputPath);
                file.parent().mkdirs();
                ScreenUtils.saveScreenshot(file);
                Log.info("[DevHarness] Screenshot successfully captured to: @", file.absolutePath());
            } catch (Throwable t) {
                Log.err("[DevHarness] Failed to capture screenshot", t);
            } finally {
                if (autoExit) {
                    Log.info("[DevHarness] Auto-exit requested. Terminating game client.");
                    Time.runTask(5f, () -> Core.app.exit());
                }
            }
        });

        Time.runTask(delayTicks, () -> ready[0] = true);
    }

    public static void showDevMenu() {
        Dialog dialog = new Dialog("AdminTools Dev Menu");
        dialog.title.setColor(LucidTheme.accent);

        dialog.cont.table(t -> {
            t.defaults().size(220f, 40f).pad(4f);

            t.button("Portal Window", Icon.host, () -> applyScreen("portal")).row();
            t.button("Karma Detector", Icon.hammer, () -> applyScreen("karma")).row();
            t.button("Tile History", Icon.book, () -> applyScreen("history")).row();
            t.button("Ban Dialog", Icon.cancel, () -> applyScreen("ban")).row();
            t.button("Auth Dialog", Icon.lock, () -> applyScreen("auth")).row();
            t.button("Toggle Dock Collapse", Icon.move, () -> ui.windowManager.getDock().toggleCollapse()).row();
            t.button("Toggle FreeCam HUD", Icon.eye, () -> FreeCamController.get().toggle()).row();

            t.button("Capture Screenshot", Icon.save, () -> {
                String path = "/tmp/admintools_dev_" + System.currentTimeMillis() + ".png";
                scheduleScreenshot(path, 2, false);
            }).row();
        });

        dialog.addCloseButton();
        dialog.show();
    }
}
