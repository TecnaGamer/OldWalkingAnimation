package tecna.oldwalkinganimation.config;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public final class OwaLang {
    private static final String PATH = "/assets/oldwalkinganimation/lang/en_us.json";
    private static volatile Map<String, String> entries;

    private OwaLang() {}

    private static Map<String, String> entries() {
        Map<String, String> e = entries;
        if (e == null) {
            synchronized (OwaLang.class) {
                e = entries;
                if (e == null) {
                    entries = e = Collections.unmodifiableMap(load());
                }
            }
        }
        return e;
    }

    private static Map<String, String> load() {
        Map<String, String> out = new HashMap<>();
        try (InputStream in = OwaLang.class.getResourceAsStream(PATH)) {
            if (in == null) return out;
            try (InputStreamReader r = new InputStreamReader(in, StandardCharsets.UTF_8)) {
                JsonObject obj = JsonParser.parseReader(r).getAsJsonObject();
                for (Map.Entry<String, JsonElement> entry : obj.entrySet()) {
                    if (entry.getValue().isJsonPrimitive()) {
                        out.put(entry.getKey(), entry.getValue().getAsString());
                    }
                }
            }
        } catch (Exception ignored) {}
        return out;
    }

    public static boolean has(String key) {
        return entries().containsKey(key);
    }

    public static String get(String key, String fallback) {
        String v = entries().get(key);
        return v != null ? v : fallback;
    }
}
