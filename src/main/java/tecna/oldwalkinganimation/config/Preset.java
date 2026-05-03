package tecna.oldwalkinganimation.config;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public final class Preset {
    public final String id;
    public final String name;
    public final boolean builtin;
    public final Map<String, Object> overrides;

    public Preset(String id, String name, boolean builtin, Map<String, Object> overrides) {
        this.id = id;
        this.name = name;
        this.builtin = builtin;
        this.overrides = Collections.unmodifiableMap(new LinkedHashMap<>(overrides));
    }

    @Override public String toString() { return name; }
}
