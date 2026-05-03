package tecna.oldwalkinganimation.config;

//? if fabric
import net.fabricmc.loader.api.FabricLoader;
//? if neoforge
/*import net.neoforged.fml.loading.FMLPaths;*/
//? if >=26.1 || neoforge {
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.network.chat.Component;
//?} else {
/*import net.minecraft.client.MinecraftClient;
import net.minecraft.client.toast.SystemToast;
import net.minecraft.text.Text;
*///?}

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

public class KillStats {
    private static final long[] THRESHOLDS   = {    10,    50,   100,   500,  1000,  5000, 10000, 50000 };
    private static final String[] TITLES     = {
        "Violent Tendencies",
        "Repeat Offender",
        "Centurion",
        "Obsessed",
        "Digital Psychopath",
        "The Reaper",
        "Harbinger",
        "God of Death"
    };
    private static final String[] DESCRIPTIONS = {
        "Killed the preview 10 times",
        "50 kills and counting...",
        "100 kills \u2014 a feat of violence",
        "500 kills \u2014 you should seek help",
        "1,000 kills \u2014 true psychopath",
        "5,000 kills \u2014 nothing can stop you",
        "10,000 kills \u2014 a legend is born",
        "50,000 kills \u2014 the dummy dreams of you"
    };

    private static long kills = -1L;
    private static Path file;
    private static boolean hoverHintShown = false;

    public static void tryShowHoverHint() {
        if (hoverHintShown) return;
        hoverHintShown = true;
        showToast("Old Walking Animation", "Click to damage");
    }

    private static void ensureLoaded() {
        if (kills >= 0) return;
        //? if fabric {
        file = FabricLoader.getInstance().getConfigDir().resolve("owa-killstats.properties");
        //?} else if neoforge {
        /*file = FMLPaths.CONFIGDIR.get().resolve("owa-killstats.properties");
        *///?}
        Properties p = new Properties();
        if (Files.exists(file)) {
            try (InputStream in = Files.newInputStream(file)) { p.load(in); } catch (IOException ignored) {}
        }
        try {
            kills = Long.parseLong(p.getProperty("kills", "0"));
        } catch (NumberFormatException e) {
            kills = 0;
        }
        if (kills < 0) kills = 0;
    }

    private static void save() {
        if (file == null) return;
        Properties p = new Properties();
        p.setProperty("kills", String.valueOf(kills));
        try (OutputStream out = Files.newOutputStream(file)) {
            p.store(out, "OWA secret kill stats \u2014 don't touch");
        } catch (IOException ignored) {}
    }

    public static long getKills() {
        ensureLoaded();
        return kills;
    }

    public static void onKill() {
        ensureLoaded();
        long before = kills;
        kills++;
        save();
        for (int i = 0; i < THRESHOLDS.length; i++) {
            if (before < THRESHOLDS[i] && kills >= THRESHOLDS[i]) {
                showToast(TITLES[i], DESCRIPTIONS[i]);
                return;
            }
        }
    }

    public static void showToast(String title, String desc) {
        //? if >=26.1 || neoforge {
        Minecraft mc = Minecraft.getInstance();
        if (mc == null) return;
        //? if >=1.20.4 {
        SystemToast.add(
            //? if >=26.1 || (neoforge && >=1.21.2)
            mc.getToastManager(),
            //? if neoforge && <1.21.2
            /*mc.getToasts(),*/
            SystemToast.SystemToastId.PERIODIC_NOTIFICATION,
            Component.literal("\u00A7e\u2605 " + title),
            Component.literal(desc)
        );
        //?}
        //?} else {
        /*MinecraftClient mc = MinecraftClient.getInstance();
        if (mc == null) return;
        SystemToast.add(
            mc.getToastManager(),
            SystemToast.Type.PERIODIC_NOTIFICATION,
            Text.literal("\u00A7e\u2605 " + title),
            Text.literal(desc)
        );
        *///?}
    }
}
