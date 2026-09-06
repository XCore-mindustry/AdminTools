package admintools;

import arc.Core;
import arc.files.Fi;
import arc.util.I18NBundle;
import arc.util.Log;
import arc.util.Strings;
import arc.util.io.PropertiesUtils;
import mindustry.Vars;
import mindustry.mod.Mods.LoadedMod;

/**
 * Localization helper for AdminTools.
 * Seamlessly integrates with Mindustry's {@link Core#bundle} and supports en, ru, and uk locales.
 */
public class I18N {

    /**
     * Ensures AdminTools bundle keys are registered into Core.bundle.
     * Mindustry loads mod bundles automatically if present in mod.root/bundles,
     * but this acts as a foolproof safeguard.
     */
    public static void init() {
        if (Core.bundle == null) return;
        if (Core.bundle.has("admintools.category")) return;

        try {
            LoadedMod mod = Vars.mods != null ? Vars.mods.getMod(AdminTools.class) : null;
            if (mod != null && mod.root != null) {
                Fi folder = mod.root.child("bundles");
                if (folder.exists()) {
                    loadBundlesInto(Core.bundle, folder);
                }
            }
        } catch (Throwable t) {
            Log.err("Failed to load AdminTools fallback bundles", t);
        }
    }

    private static void loadBundlesInto(I18NBundle targetBundle, Fi folder) {
        I18NBundle current = targetBundle;
        while (current != null) {
            String str = current.getLocale().toString();
            String localeSuffix = "bundle" + (str.isEmpty() ? "" : "_" + str);
            Fi file = folder.child(localeSuffix + ".properties");
            if (file.exists()) {
                try {
                    PropertiesUtils.load(current.getProperties(), file.reader());
                } catch (Throwable t) {
                    Log.err("Error reading bundle file: @", file, t);
                }
            }
            current = current.getParent();
        }
    }

    public static String get(String key) {
        if (Core.bundle != null && Core.bundle.has(key)) {
            return Core.bundle.get(key);
        }
        return key;
    }

    public static String get(String key, String def) {
        if (Core.bundle != null && Core.bundle.has(key)) {
            return Core.bundle.get(key);
        }
        return def;
    }

    public static String format(String key, Object... args) {
        if (Core.bundle != null && Core.bundle.has(key)) {
            try {
                return Core.bundle.format(key, args);
            } catch (Exception e) {
                return Strings.format(Core.bundle.get(key, key), args);
            }
        }
        return Strings.format(key, args);
    }
}
