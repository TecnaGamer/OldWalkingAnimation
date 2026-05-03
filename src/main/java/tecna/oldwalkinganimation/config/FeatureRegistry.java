package tecna.oldwalkinganimation.config;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class FeatureRegistry {
    private static final List<FeatureGroup> ALL = buildAll();
    private static final Map<String, FeatureGroup> BY_FIELD = buildByField();
    private static final Map<String, FeatureGroup> BY_ID = buildById();

    private FeatureRegistry() {}

    public static List<FeatureGroup> all() { return ALL; }
    public static FeatureGroup byId(String id) { return BY_ID.get(id); }
    public static FeatureGroup forField(String fieldName) { return BY_FIELD.get(fieldName); }

    public static List<FeatureGroup> forCategory(String category) {
        List<FeatureGroup> out = new ArrayList<>();
        for (FeatureGroup g : ALL) if (g.category.equals(category)) out.add(g);
        return out;
    }

    private static Map<String, FeatureGroup> buildByField() {
        Map<String, FeatureGroup> m = new HashMap<>();
        for (FeatureGroup g : ALL) for (String f : g.fields) m.put(f, g);
        return m;
    }

    private static Map<String, FeatureGroup> buildById() {
        Map<String, FeatureGroup> m = new LinkedHashMap<>();
        for (FeatureGroup g : ALL) m.put(g.id, g);
        return m;
    }

    private static Preset custom() {
        return new Preset(FeatureGroup.CUSTOM_ID, "Custom", true, Collections.emptyMap());
    }

    private static Map<String, Object> map(Object... kv) {
        Map<String, Object> m = new LinkedHashMap<>();
        for (int i = 0; i < kv.length; i += 2) m.put((String) kv[i], kv[i + 1]);
        return m;
    }

    private static List<FeatureGroup> buildAll() {
        List<FeatureGroup> out = new ArrayList<>();

        // --- main: Bounce ---
        out.add(new FeatureGroup("bounce", "Bounce", "animation",
                List.of("bounce", "bounceHeight", "bounceInverted"),
                List.of(
                        new Preset("off", "Off", true, map("bounce", false)),
                        new Preset("classic", "Classic (Synced)", true, map(
                                "bounce", true, "bounceHeight", 1.0f, "bounceInverted", false)),
                        new Preset("inverted", "c0.0.23a (Inverted)", true, map(
                                "bounce", true, "bounceHeight", 1.0f, "bounceInverted", true)),
                        new Preset("subtle", "Subtle", true, map(
                                "bounce", true, "bounceHeight", 0.5f, "bounceInverted", false)),
                        custom()
                ),
                "classic"));

        // --- main: Animation Speed ---
        out.add(new FeatureGroup("animSpeed", "Animation Speed", "animation",
                List.of("speed", "maxSpeed", "speedTrigger", "vanillaSpeed", "speedLimbAngle",
                        "smoothing", "decayFactor"),
                List.of(
                        new Preset("classic", "Classic", true, map(
                                "speed", 1.0f, "maxSpeed", 1.0f, "speedTrigger", 0.05f,
                                "vanillaSpeed", false, "speedLimbAngle", true,
                                "smoothing", true, "decayFactor", 0.3f)),
                        new Preset("indev", "Indev / Alpha", true, map(
                                "speed", 1.0f, "maxSpeed", 1.0f, "speedTrigger", 0.05f,
                                "vanillaSpeed", false, "speedLimbAngle", false,
                                "smoothing", true, "decayFactor", 0.3f)),
                        new Preset("vanilla", "Vanilla", true, map(
                                "speed", 1.0f, "maxSpeed", 1.0f, "speedTrigger", 0.05f,
                                "vanillaSpeed", true, "speedLimbAngle", false,
                                "smoothing", true, "decayFactor", 0.3f)),
                        custom()
                ),
                "indev"));

        // --- limbs: Arm Flailing ---
        out.add(new FeatureGroup("armFlail", "Arm Flailing", "animation",
                List.of("arms", "ridingMobAnimation", "classicRun"),
                List.of(
                        new Preset("off", "Off", true, map("arms", false)),
                        new Preset("classic", "Classic", true, map(
                                "arms", true, "ridingMobAnimation", false,
                                "classicRun", false)),
                        new Preset("preclassic", "Pre-Classic", true, map(
                                "arms", true, "ridingMobAnimation", false,
                                "classicRun", true)),
                        custom()
                ),
                "classic"));

        // --- limbs: Head Bob ---
        out.add(new FeatureGroup("headBobGroup", "Head Bob", "animation",
                List.of("headBob", "headSpeed", "headAmount"),
                List.of(
                        new Preset("off", "Off", true, map("headBob", false)),
                        new Preset("classic", "Classic (rd)", true, map(
                                "headBob", true, "headSpeed", 0.83f, "headAmount", 1.0f)),
                        new Preset("mild", "Mild", true, map(
                                "headBob", true, "headSpeed", 0.5f, "headAmount", 0.5f)),
                        custom()
                ),
                "off"));

        // --- damage: Damage Animation ---
        out.add(new FeatureGroup("damageAnim", "Damage Animation", "damage",
                List.of("damage", "velocity", "fallback", "damageIntensity", "lockRot"),
                List.of(
                        new Preset("off", "Off", true, map("damage", false)),
                        new Preset("classic", "Classic (Source)", true, map(
                                "damage", true, "velocity", false, "fallback", true,
                                "damageIntensity", 14.0f, "lockRot", true)),
                        new Preset("velocity", "Velocity-Based", true, map(
                                "damage", true, "velocity", true, "fallback", true,
                                "damageIntensity", 14.0f, "lockRot", true)),
                        custom()
                ),
                "classic"));

        // --- damage: Damage Flash ---
        out.add(new FeatureGroup("damageFlashGroup", "Damage Flash", "damage",
                List.of("damageFlash", "alpha", "red", "green", "blue"),
                List.of(
                        new Preset("off", "Off", true, map("damageFlash", false)),
                        new Preset("red", "Red (Vanilla-Style)", true, map(
                                "damageFlash", true,
                                "alpha", 178, "red", 255, "green", 0, "blue", 0)),
                        new Preset("white", "White", true, map(
                                "damageFlash", true,
                                "alpha", 178, "red", 255, "green", 255, "blue", 255)),
                        custom()
                ),
                "red"));

        // --- steves: Physics ---
        out.add(new FeatureGroup("stevePhysics", "Classic Human Physics", "steves",
                List.of("authenticPhysics", "rdEarlyPhysics"),
                List.of(
                        new Preset("modern", "Modern-Feel", true, map(
                                "authenticPhysics", false, "rdEarlyPhysics", false)),
                        new Preset("classic", "pc-152252+ (Fast)", true, map(
                                "authenticPhysics", true, "rdEarlyPhysics", false)),
                        new Preset("rd132328", "rd-132328 (Slow)", true, map(
                                "authenticPhysics", true, "rdEarlyPhysics", true)),
                        custom()
                ),
                "classic"));

        // --- steves: Animation ---
        out.add(new FeatureGroup("steveAnim", "Classic Human Animation", "steves",
                List.of("rdHeadBob", "rdStyleLimbs"),
                List.of(
                        new Preset("rd20090515", "rd-20090515", true, map(
                                "rdHeadBob", true, "rdStyleLimbs", true)),
                        new Preset("c0015a", "c0.0.15a", true, map(
                                "rdHeadBob", false, "rdStyleLimbs", true)),
                        new Preset("c0023a", "c0.0.23a", true, map(
                                "rdHeadBob", false, "rdStyleLimbs", false)),
                        custom()
                ),
                "rd20090515"));

        // --- steves: Skin ---
        out.add(new FeatureGroup("steveSkin", "Classic Human Skin", "steves",
                List.of("steveSkinMode", "steveCustomSkinSources"),
                List.of(
                        new Preset("random_default", "Random Default", true, map(
                                "steveSkinMode", "random_default")),
                        new Preset("steve", "Classic Steve", true, map(
                                "steveSkinMode", "steve")),
                        new Preset("player", "Your Skin", true, map(
                                "steveSkinMode", "player")),
                        new Preset("random_cached", "Random Seen Player", true, map(
                                "steveSkinMode", "random_cached")),
                        new Preset("custom_file", "Custom Skin", true, map(
                                "steveSkinMode", "custom"))
                ),
                "steve"));

        return Collections.unmodifiableList(out);
    }
}
