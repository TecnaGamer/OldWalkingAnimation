package tecna.oldwalkinganimation.config;

import java.util.Collections;
import java.util.List;

public final class FeatureGroup {
    public static final String CUSTOM_ID = "custom";

    public final String id;
    public final String name;
    public final String category;
    public final List<String> fields;
    public final List<Preset> presets;
    public final String defaultPresetId;

    public FeatureGroup(String id, String name, String category,
                        List<String> fields, List<Preset> presets, String defaultPresetId) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.fields = Collections.unmodifiableList(fields);
        this.presets = Collections.unmodifiableList(presets);
        this.defaultPresetId = defaultPresetId;
    }

    public Preset presetById(String pid) {
        for (Preset p : presets) if (p.id.equals(pid)) return p;
        return null;
    }

    public Preset customPreset() {
        return presetById(CUSTOM_ID);
    }
}
