package tecna.oldwalkinganimation.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import eu.midnightdust.lib.config.MidnightConfig;
//? if fabric
import net.fabricmc.loader.api.FabricLoader;
//? if neoforge
/*import net.neoforged.fml.loading.FMLPaths;*/

import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public final class PresetManager {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Map<String, String> SELECTED = new HashMap<>();
    private static boolean advancedMode = false;
    private static Path path;

    private PresetManager() {}

    public static void init(String modId) {
        //? if fabric {
        path = FabricLoader.getInstance().getConfigDir().resolve(modId + "-presets.json");
        //?} else if neoforge {
        /*path = FMLPaths.CONFIGDIR.get().resolve(modId + "-presets.json");
        *///?}
        load();
        for (FeatureGroup g : FeatureRegistry.all()) {
            SELECTED.putIfAbsent(g.id, g.defaultPresetId);
        }
        for (FeatureGroup g : FeatureRegistry.all()) {
            reconcileSelection(g);
        }
    }

    public static boolean advancedMode() { return advancedMode; }

    public static void setAdvancedMode(boolean v) {
        if (advancedMode == v) return;
        advancedMode = v;
        save();
    }

    public static String selectedIdFor(String groupId) {
        return SELECTED.getOrDefault(groupId, FeatureGroup.CUSTOM_ID);
    }

    public static Preset selectedFor(FeatureGroup g) {
        Preset p = g.presetById(selectedIdFor(g.id));
        return p != null ? p : g.customPreset();
    }

    public static boolean isCustom(FeatureGroup g) {
        return FeatureGroup.CUSTOM_ID.equals(selectedIdFor(g.id));
    }

    public static void selectPreset(FeatureGroup g, Preset p) {
        SELECTED.put(g.id, p.id);
        if (!FeatureGroup.CUSTOM_ID.equals(p.id)) {
            apply(p);
            MidnightConfig.save();
            tecna.oldwalkinganimation.OverlayTextureRefresher.refresh();
        }
        save();
    }

    public static void onFieldChanged(Field f) {
        FeatureGroup g = FeatureRegistry.forField(f.getName());
        if (g == null) return;
        String curId = selectedIdFor(g.id);
        if (FeatureGroup.CUSTOM_ID.equals(curId)) return;
        Preset cur = g.presetById(curId);
        if (cur == null || !valuesMatch(cur)) {
            SELECTED.put(g.id, FeatureGroup.CUSTOM_ID);
            save();
        }
    }

    public static void resetField(Field f) {
        FeatureGroup g = FeatureRegistry.forField(f.getName());
        if (g != null) {
            String curId = selectedIdFor(g.id);
            if (!FeatureGroup.CUSTOM_ID.equals(curId)) {
                Preset p = g.presetById(curId);
                if (p != null) {
                    Object v = p.overrides.get(f.getName());
                    if (v != null) {
                        try {
                            f.setAccessible(true);
                            setField(f, v);
                            return;
                        } catch (ReflectiveOperationException ignored) {}
                    }
                }
            }
        }
        MidnightConfig.resetField(f);
    }

    public static boolean fieldAtPresetValue(Field f) {
        FeatureGroup g = FeatureRegistry.forField(f.getName());
        if (g == null) return MidnightConfig.isDefault(f);
        String curId = selectedIdFor(g.id);
        if (FeatureGroup.CUSTOM_ID.equals(curId)) return MidnightConfig.isDefault(f);
        Preset p = g.presetById(curId);
        if (p == null) return MidnightConfig.isDefault(f);
        Object v = p.overrides.get(f.getName());
        if (v == null) return MidnightConfig.isDefault(f);
        try {
            f.setAccessible(true);
            return valueEquals(f.get(null), v);
        } catch (ReflectiveOperationException e) { return true; }
    }

    // Reset All hook: restore every group to its default preset and re-apply that preset's
    // values. MidnightConfig.resetAll() resets fields but doesn't know about preset selections;
    // without this the cycle buttons would still show the old preset name.
    public static void resetAllToDefaults() {
        for (FeatureGroup g : FeatureRegistry.all()) {
            SELECTED.put(g.id, g.defaultPresetId);
            Preset p = g.presetById(g.defaultPresetId);
            if (p != null && !FeatureGroup.CUSTOM_ID.equals(p.id)) apply(p);
        }
        save();
        MidnightConfig.save();
    }

    private static void reconcileSelection(FeatureGroup g) {
        String cur = SELECTED.get(g.id);
        if (cur == null || FeatureGroup.CUSTOM_ID.equals(cur)) return;
        Preset p = g.presetById(cur);
        if (p == null || !valuesMatch(p)) {
            SELECTED.put(g.id, FeatureGroup.CUSTOM_ID);
        }
    }

    private static boolean valuesMatch(Preset p) {
        Class<?> cls = MidnightConfig.getConfigClass();
        if (cls == null) return false;
        for (Map.Entry<String, Object> e : p.overrides.entrySet()) {
            try {
                Field f = cls.getDeclaredField(e.getKey());
                f.setAccessible(true);
                if (!valueEquals(f.get(null), e.getValue())) return false;
            } catch (ReflectiveOperationException ex) { return false; }
        }
        return true;
    }

    private static void apply(Preset p) {
        Class<?> cls = MidnightConfig.getConfigClass();
        if (cls == null) return;
        for (Map.Entry<String, Object> e : p.overrides.entrySet()) {
            try {
                Field f = cls.getDeclaredField(e.getKey());
                f.setAccessible(true);
                setField(f, e.getValue());
            } catch (ReflectiveOperationException ex) {
                System.err.println("[OWA] Preset apply missed field " + e.getKey() + ": " + ex);
            }
        }
    }

    private static void setField(Field f, Object v) throws ReflectiveOperationException {
        Class<?> t = f.getType();
        if (t == boolean.class) f.setBoolean(null, (Boolean) v);
        else if (t == int.class) f.setInt(null, ((Number) v).intValue());
        else if (t == float.class) f.setFloat(null, ((Number) v).floatValue());
        else if (t == double.class) f.setDouble(null, ((Number) v).doubleValue());
        else f.set(null, v);
    }

    private static boolean valueEquals(Object a, Object b) {
        if (a == null) return b == null;
        if (a instanceof Float fa && b instanceof Number nb) return Math.abs(fa - nb.floatValue()) < 1e-4f;
        if (a instanceof Number na && b instanceof Number nb) return na.doubleValue() == nb.doubleValue();
        return a.equals(b);
    }

    private static void load() {
        SELECTED.clear();
        advancedMode = false;
        if (path == null || !Files.exists(path)) return;
        try {
            JsonObject root = JsonParser.parseString(Files.readString(path)).getAsJsonObject();
            if (root.has("advancedMode")) advancedMode = root.get("advancedMode").getAsBoolean();
            if (root.has("featureSelections")) {
                JsonObject sel = root.getAsJsonObject("featureSelections");
                for (Map.Entry<String, JsonElement> e : sel.entrySet()) {
                    SELECTED.put(e.getKey(), e.getValue().getAsString());
                }
            }
        } catch (IOException e) {
            System.err.println("[OWA] Failed to load presets: " + e);
        }
    }

    private static void save() {
        if (path == null) return;
        try {
            JsonObject root = new JsonObject();
            root.addProperty("advancedMode", advancedMode);
            JsonObject sel = new JsonObject();
            for (Map.Entry<String, String> e : SELECTED.entrySet()) {
                sel.addProperty(e.getKey(), e.getValue());
            }
            root.add("featureSelections", sel);
            Files.createDirectories(path.getParent());
            Files.writeString(path, GSON.toJson(root));
        } catch (IOException e) {
            System.err.println("[OWA] Failed to save presets: " + e);
        }
    }
}
