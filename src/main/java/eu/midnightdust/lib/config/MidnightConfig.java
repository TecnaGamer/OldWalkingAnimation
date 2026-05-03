package eu.midnightdust.lib.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
//? if fabric
import net.fabricmc.loader.api.FabricLoader;
//? if neoforge
/*import net.neoforged.fml.loading.FMLPaths;*/

import java.io.IOException;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class MidnightConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static Path configPath;
    private static Class<? extends MidnightConfig> configClass;
    private static final Map<String, Object> DEFAULTS = new HashMap<>();

    public static void init(String modId, Class<? extends MidnightConfig> cls) {
        configClass = cls;
        //? if fabric {
        configPath = FabricLoader.getInstance().getConfigDir().resolve(modId + ".json");
        //?} else if neoforge {
        /*configPath = FMLPaths.CONFIGDIR.get().resolve(modId + ".json");
        *///?}
        captureDefaults();
        load();
    }

    private static void captureDefaults() {
        DEFAULTS.clear();
        try {
            for (Field f : configClass.getDeclaredFields()) {
                if (!Modifier.isStatic(f.getModifiers())) continue;
                if (f.getAnnotation(Entry.class) == null) continue;
                f.setAccessible(true);
                DEFAULTS.put(f.getName(), f.get(null));
            }
        } catch (ReflectiveOperationException e) {
            System.err.println("[OWA] Failed to capture defaults: " + e);
        }
    }

    public static void resetField(Field f) {
        Object def = DEFAULTS.get(f.getName());
        if (def == null) return;
        try {
            f.setAccessible(true);
            f.set(null, def);
        } catch (ReflectiveOperationException e) {
            System.err.println("[OWA] Failed to reset " + f.getName() + ": " + e);
        }
    }

    public static void resetAll() {
        if (configClass == null) return;
        for (Field f : configClass.getDeclaredFields()) {
            if (!Modifier.isStatic(f.getModifiers())) continue;
            if (f.getAnnotation(Entry.class) == null) continue;
            resetField(f);
        }
    }

    public static boolean isDefault(Field f) {
        Object def = DEFAULTS.get(f.getName());
        if (def == null) return true;
        try {
            f.setAccessible(true);
            Object cur = f.get(null);
            return def.equals(cur);
        } catch (ReflectiveOperationException e) {
            return true;
        }
    }

    public static boolean anyNonDefault() {
        if (configClass == null) return false;
        for (Field f : configClass.getDeclaredFields()) {
            if (!Modifier.isStatic(f.getModifiers())) continue;
            if (f.getAnnotation(Entry.class) == null) continue;
            if (!isDefault(f)) return true;
        }
        return false;
    }

    public static void load() {
        if (configPath == null || !Files.exists(configPath)) {
            save();
            return;
        }
        try {
            JsonObject obj = JsonParser.parseString(Files.readString(configPath)).getAsJsonObject();
            for (Field f : configClass.getDeclaredFields()) {
                if (!Modifier.isStatic(f.getModifiers())) continue;
                if (f.getAnnotation(Entry.class) == null) continue;
                if (!obj.has(f.getName())) continue;
                f.setAccessible(true);
                Class<?> t = f.getType();
                if (t == boolean.class) f.setBoolean(null, obj.get(f.getName()).getAsBoolean());
                else if (t == int.class) f.setInt(null, obj.get(f.getName()).getAsInt());
                else if (t == float.class) f.setFloat(null, obj.get(f.getName()).getAsFloat());
                else if (t == double.class) f.setDouble(null, obj.get(f.getName()).getAsDouble());
                else if (t == String.class) f.set(null, obj.get(f.getName()).getAsString());
            }
        } catch (IOException | ReflectiveOperationException e) {
            System.err.println("[OWA] Failed to load config: " + e);
        }
    }

    public static void save() {
        if (configPath == null) return;
        try {
            JsonObject obj = new JsonObject();
            for (Field f : configClass.getDeclaredFields()) {
                if (!Modifier.isStatic(f.getModifiers())) continue;
                if (f.getAnnotation(Entry.class) == null) continue;
                f.setAccessible(true);
                Object v = f.get(null);
                if (v instanceof Boolean b) obj.addProperty(f.getName(), b);
                else if (v instanceof Number n) obj.addProperty(f.getName(), n);
                else if (v instanceof String s) obj.addProperty(f.getName(), s);
            }
            Files.createDirectories(configPath.getParent());
            Files.writeString(configPath, GSON.toJson(obj));
        } catch (IOException | ReflectiveOperationException e) {
            System.err.println("[OWA] Failed to save config: " + e);
        }
    }

    public static Class<? extends MidnightConfig> getConfigClass() {
        return configClass;
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    public @interface Entry {
        String category() default "default";
        String name() default "";
        boolean isSlider() default false;
        float min() default 0f;
        float max() default 0f;
        int precision() default 1;
        String dependsOn() default "";
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    public @interface Comment {
        String category() default "default";
        boolean centered() default false;
    }
}
