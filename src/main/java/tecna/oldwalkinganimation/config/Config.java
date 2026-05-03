package tecna.oldwalkinganimation.config;


import eu.midnightdust.lib.config.MidnightConfig;

public class Config extends MidnightConfig {

/** Every option in a MidnightConfig class has to be public and static, so we can access it from other classes.
 * The config class also has to extend MidnightConfig*/



    //Features
    @Comment(//? if >=1.20
            category = "features",
            centered = true) public static Comment mainInfo;
    @Entry(//? if >=1.20
            category = "features"
    ) public static boolean enableMod = true;
    @Entry(//? if >=1.20
            category = "features"
    ) public static boolean enableMobs = true;

    // Toggle for the "OWA Config" button on the title and pause screens. Hidden from the regular
    // options list (rendered as a small button in the screen's footer instead). Default is OFF
    // when ModMenu (Fabric) or NeoForge's mod-list is available, since users can already reach
    // the config through those — and ON otherwise.
    @Entry(//? if >=1.20
            category = "features"
    ) public static boolean showQuickAccess = owa$computeQuickAccessDefault();

    private static boolean owa$computeQuickAccessDefault() {
        //? if neoforge {
        /*return false;
        *///?} else {
        try {
            return !net.fabricmc.loader.api.FabricLoader.getInstance().isModLoaded("modmenu");
        } catch (Throwable t) {
            return true;
        }
        //?}
    }


    @Entry(//? if >=1.20
            category = "animation"
    ) public static boolean bodyRot = true;

    //Bounce
    @Comment(//? if >=1.20
            category = "animation"
    ) public static Comment bouncespacer;
    @Comment(//? if >=1.20
            category = "animation",
            centered = true) public static Comment bounceInfo;

    @Entry(//? if >=1.20
            category = "animation"
    ) public static boolean bounce = true;
    @Entry(//? if >=1.20
            category = "animation",
            isSlider = true, min = 0f, max = 1f, precision = 100, dependsOn = "bounce") public static float bounceHeight = 1F;
    @Entry(//? if >=1.20
            category = "animation",
            dependsOn = "bounce") public static boolean bounceInverted = false;



    //Animation Settings
    @Comment(//? if >=1.20
            category = "animation"
    ) public static Comment animationspacer;
    @Comment(//? if >=1.20
            category = "animation",
            centered = true) public static Comment animationInfo;


    @Entry(//? if >=1.20
            category = "animation",
            isSlider = true, min = 0f, max = 2f, precision = 100) public static float speed = 1.0f;

    @Entry(//? if >=1.20
            category = "animation",
            name = "Set Max Speed", isSlider = true, min = 0f, max = 1f, precision = 100) public static float maxSpeed = 1.0f;
    @Entry(//? if >=1.20
            category = "animation",
            name = "Animation Speed Trigger", isSlider = true, min = 0f, max = 1f, precision = 100) public static float speedTrigger = 0.05f;


    //Sneak 0.2
    //Walk 0.65
    //Run 0.85
    //Speed 2 run 1.18
    //Fly 1.65
    //Fly Run 3.3


    @Entry(//? if >=1.20
            category = "animation"
    ) public static boolean vanillaSpeed = false;


    @Entry(//? if >=1.20
            category = "animation"
    ) public static boolean speedLimbAngle = false;


    @Entry(//? if >=1.20
            category = "animation",
            name = "Smoothing"
    ) public static boolean smoothing = true;

    @Entry(//? if >=1.20
            category = "animation",
            name = "Decay Factor", isSlider = true, min = 0f, max = 2, precision = 100, dependsOn = "smoothing") public static float decayFactor = 0.3F;



    //Limb Settings
    //? if <1.20
    /*@Comment public static Comment limbsspacer;*/

    //Arms
    @Comment(//? if >=1.20
            category = "animation",
            centered = true) public static Comment armInfo;

    @Entry(//? if >=1.20
            category = "animation"
    ) public static boolean arms = true;
    @Entry(//? if >=1.20
            category = "animation"
    ) public static boolean ridingMobAnimation = false;
    @Entry(//? if >=1.20
            category = "animation"
    ) public static boolean tpose = false;
    @Entry(//? if >=1.20
            category = "animation",
            name = "T-pose Angle", isSlider = true, min = 0f, max = 3f, precision = 100, dependsOn = "tpose") public static float tposeAngle = 0.9F;

    //Legacy toggles (pre-1.8 / Classic)
    @Comment(//? if >=1.20
            category = "animation"
    ) public static Comment legacyspacer;
    @Comment(//? if >=1.20
            category = "animation",
            centered = true) public static Comment legacyInfo;

    @Entry(//? if >=1.20
            category = "animation",
            name = "Pre-1.8 Sneak Pose"
    ) public static boolean sneakPose17 = false;
    @Entry(//? if >=1.20
            category = "animation",
            name = "Classic Always-Run"
    ) public static boolean classicRun = false;


    //Head
    @Comment(//? if >=1.20
            category = "animation"
    ) public static Comment headspacer;
    @Comment(//? if >=1.20
            category = "animation",
            centered = true) public static Comment headInfo;

    @Entry(//? if >=1.20
            category = "animation"
    ) public static boolean headBob = false;
    @Entry(//? if >=1.20
            category = "animation",
            name = "Head Bob Speed", isSlider = true, min = 0f, max = 2f, precision = 100, dependsOn = "headBob") public static float headSpeed = 0.5F;
    @Entry(//? if >=1.20
            category = "animation",
            name = "Head Bob Intensity", isSlider = true, min = 0f, max = 2f, precision = 100, dependsOn = "headBob") public static float headAmount = 0.5F;


    //? if <1.20
    /*@Comment public static Comment damagespacer;*/

    //Damage
    @Comment(//? if >=1.20
            category = "damage",
            centered = true) public static Comment damageInfo;

    @Entry(//? if >=1.20
            category = "damage"
    ) public static boolean damage = true;
    @Entry(//? if >=1.20
            category = "damage",
            dependsOn = "damage"
    ) public static boolean velocity = false;
    @Entry(//? if >=1.20
            category = "damage",
            dependsOn = "damage"
    ) public static boolean fallback = true;
    @Entry(//? if >=1.20
            category = "damage",
            name = "Damage Intensity", isSlider = true, min = 0f, max = 90f, precision = 10, dependsOn = "damage") public static float damageIntensity = 14.0F;
    @Entry(//? if >=1.20
            category = "damage",
            dependsOn = "damage"
    ) public static boolean lockRot = true;

    //@Entry(category = "damage", name = "Rot", isSlider = true, min = 0f, max = 360, precision = 1) public static float damrot = 0;

    //Damage Flash
    @Comment(//? if >=1.20
            category = "damage"
    ) public static Comment damageFlashspacer;
    @Comment(//? if >=1.20
            category = "damage",
            centered = true) public static Comment damageFlashInfo;

    @Entry(//? if >=1.20
            category = "damage"
    ) public static boolean damageFlash = true;

    @Entry(//? if >=1.20
            category = "damage",
            name = "Damage Flash Alpha", isSlider = true, min = 0, max = 255, precision = 1, dependsOn = "damageFlash") public static int alpha = 178;
    @Entry(//? if >=1.20
            category = "damage",
            name = "red", isSlider = true, min = 0f, max = 255, precision = 1, dependsOn = "damageFlash") public static int red = 255;
    @Entry(//? if >=1.20
            category = "damage",
            name = "blue", isSlider = true, min = 0f, max = 255, precision = 1, dependsOn = "damageFlash") public static int blue = 255;
    @Entry(//? if >=1.20
            category = "damage",
            name = "green", isSlider = true, min = 0f, max = 255, precision = 1, dependsOn = "damageFlash") public static int green = 255;

    //? if <1.20
    /*@Comment public static Comment armspacer;*/

    // First person arm
    @Comment(//? if >=1.20
            category = "firstperson",
            centered = true) public static Comment firstPersonArmInfo;
    @Entry(//? if >=1.20
            category = "firstperson"
    ) public static boolean enableArm = true;

    @Comment(//? if >=1.20
            category = "firstperson"
    ) public static Comment armLocationspacer;
    @Comment(//? if >=1.20
            category = "firstperson",
            centered = true) public static Comment armLocationInfo;
    @Entry(//? if >=1.20
            category = "firstperson",
            name = "Y trans", isSlider = true, min = -1f, max = 1f, precision = 100, dependsOn = "enableArm") public static float Ytrans = 0.08f;
    @Entry(//? if >=1.20
            category = "firstperson",
            name = "X trans", isSlider = true, min = -1f, max = 1f, precision = 100, dependsOn = "enableArm") public static float Xtrans = 0.08f;
    @Entry(//? if >=1.20
            category = "firstperson",
            name = "Z trans", isSlider = true, min = -1f, max = 1f, precision = 100, dependsOn = "enableArm") public static float Ztrans = -0.09f;

    @Comment(//? if >=1.20
            category = "firstperson"
    ) public static Comment armRotationspacer;
    @Comment(//? if >=1.20
            category = "firstperson",
            centered = true) public static Comment armRotationInfo;
    @Entry(//? if >=1.20
            category = "firstperson",
            name = "Pitch", isSlider = true, min = -2f, max = 2f, precision = 100, dependsOn = "enableArm") public static float Pitch = 0.28f;
    @Entry(//? if >=1.20
            category = "firstperson",
            name = "Roll", isSlider = true, min = -2f, max = 2f, precision = 100, dependsOn = "enableArm") public static float Roll = 0.08f;
    @Entry(//? if >=1.20
            category = "firstperson",
            name = "Yaw", isSlider = true, min = -2f, max = 2f, precision = 100, dependsOn = "enableArm") public static float Yaw = -0.76f;


    //? if >=1.20 {

    // Steves
    @Comment(category = "steves", centered = true) public static Comment steveInfo;

    @Entry(category = "steves", name = "Enable Classic Human Spawner") public static boolean enableSteve = true;
    @Entry(category = "steves", name = "Authentic Pre-Classic Physics") public static boolean authenticPhysics = true;
    @Entry(category = "steves", name = "rd-132328 Slow Physics", dependsOn = "authenticPhysics") public static boolean rdEarlyPhysics = false;
    @Entry(category = "steves", name = "Force Classic Animation") public static boolean steveClassicAnim = true;
    @Entry(category = "steves", name = "rd-20090515 Head Bob") public static boolean rdHeadBob = true;
    @Entry(category = "steves", name = "rd-Style Limb Animation (sin)") public static boolean rdStyleLimbs = true;

    @Entry(category = "steves", name = "Skin Mode") public static String steveSkinMode = "steve";
    // Comma-separated list of player names and/or skin URLs. Each Steve picks one entry by its
    // UUID hash. Entries starting with http(s):// are treated as direct skin texture URLs;
    // others are looked up via Mojang's name->UUID->profile API. Refreshed periodically so the
    // latest skin shows up if the source is updated.
    @Entry(category = "steves", name = "Custom Skins")
    public static String steveCustomSkinSources = "";
    @Entry(category = "steves", name = "Show Name Tags") public static boolean steveShowNames = false;

    @Entry(category = "steves", name = "Max Classic Humans", isSlider = true, min = 0, max = 1000, precision = 1) public static int maxHumans = 0;
    @Entry(category = "steves", name = "Natural Spawning") public static boolean naturalSpawning = false;
    @Entry(category = "steves", name = "Spawn Chance Per Chunk (%)", isSlider = true, min = 0, max = 100, precision = 1, dependsOn = "naturalSpawning") public static int naturalSpawnChance = 40;

    //?}

}