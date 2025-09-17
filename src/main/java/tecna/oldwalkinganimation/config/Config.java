package tecna.oldwalkinganimation.config;


import eu.midnightdust.lib.config.MidnightConfig;

public class Config extends MidnightConfig {

/** Every option in a MidnightConfig class has to be public and static, so we can access it from other classes.
 * The config class also has to extend MidnightConfig*/



    //Main Settings
    @Comment(//? if >=1.20
            category = "main",
            centered = true) public static Comment mainInfo;
    @Entry(//? if >=1.20
            category = "main"
    ) public static boolean enableMod = true;
    @Entry(//? if >=1.20
            category = "main"
    ) public static boolean enableMobs = true;


    //@Comment(category = "main") public static Comment bodyRotspacer;

    @Entry(//? if >=1.20
            category = "main"
    ) public static boolean bodyRot = true;

    //Bounce
    @Comment(//? if >=1.20
            category = "main"
    ) public static Comment bouncespacer;
    @Comment(//? if >=1.20
            category = "main",
            centered = true) public static Comment bounceInfo;

    @Entry(//? if >=1.20
            category = "main"
    ) public static boolean bounce = true;
    @Entry(//? if >=1.20
            category = "main",
            isSlider = true, min = 0f, max = 1f, precision = 100) public static float bounceHeight = 1F;



    //Animation Settings
    @Comment(//? if >=1.20
            category = "main"
    ) public static Comment animationspacer;
    @Comment(//? if >=1.20
            category = "main",
            centered = true) public static Comment animationInfo;


    @Entry(//? if >=1.20
            category = "main",
            isSlider = true, min = 0f, max = 2f, precision = 100) public static float speed = 1.0f;

    @Entry(//? if >=1.20
            category = "main",
            name = "Set Max Speed", isSlider = true, min = 0f, max = 1f, precision = 100) public static float maxSpeed = 1.0f;
    @Entry(//? if >=1.20
            category = "main",
            name = "Animation Speed Trigger", isSlider = true, min = 0f, max = 1f, precision = 100) public static float speedTrigger = 0.05f;


    //Sneak 0.2
    //Walk 0.65
    //Run 0.85
    //Speed 2 run 1.18
    //Fly 1.65
    //Fly Run 3.3


    @Entry(//? if >=1.20
            category = "main"
    ) public static boolean vanillaSpeed = false;


    @Entry(//? if >=1.20
            category = "main"
    ) public static boolean speedLimbAngle = false;


    @Entry(//? if >=1.20
            category = "main",
            name = "Decay Factor", isSlider = true, min = 0f, max = 2, precision = 100) public static float decayFactor = 0.3F;



    //Limb Settings
    //? if <1.20
    /*@Comment public static Comment limbsspacer;*/

    //Arms
    @Comment(//? if >=1.20
            category = "limbs",
            centered = true) public static Comment armInfo;

    @Entry(//? if >=1.20
            category = "limbs"
    ) public static boolean arms = true;
    @Entry(//? if >=1.20
            category = "limbs"
    ) public static boolean ridingMobAnimation = false;
    @Entry(//? if >=1.20
            category = "limbs"
    ) public static boolean tpose = false;
    @Entry(//? if >=1.20
            category = "limbs",
            name = "T-pose Angle", isSlider = true, min = 0f, max = 3f, precision = 100) public static float tposeAngle = 0.9F;


    //Head
    @Comment(//? if >=1.20
            category = "limbs"
    ) public static Comment headspacer;
    @Comment(//? if >=1.20
            category = "limbs",
            centered = true) public static Comment headInfo;

    @Entry(//? if >=1.20
            category = "limbs"
    ) public static boolean headBob = false;
    @Entry(//? if >=1.20
            category = "limbs",
            name = "Head Bob Speed", isSlider = true, min = 0f, max = 2f, precision = 100) public static float headSpeed = 0.5F;
    @Entry(//? if >=1.20
            category = "limbs",
            name = "Head Bob Intensity", isSlider = true, min = 0f, max = 2f, precision = 100) public static float headAmount = 0.5F;


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
            category = "damage"
    ) public static boolean velocity = false;
    @Entry(//? if >=1.20
            category = "damage"
    ) public static boolean fallback = true;
    @Entry(//? if >=1.20
            category = "damage",
            name = "Damage Intensity", isSlider = true, min = 0f, max = 90f, precision = 10) public static float damageIntensity = 14.0F;
    @Entry(//? if >=1.20
            category = "damage"
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
            category = "damage"
    ) public static boolean damageGlow = true;

    @Entry(//? if >=1.20
            category = "damage",
            name = "Damage Flash Alpha", isSlider = true, min = 0, max = 255, precision = 1) public static int alpha = 178;
    @Entry(//? if >=1.20
            category = "damage",
            name = "red", isSlider = true, min = 0f, max = 255, precision = 1) public static int red = 255;
    @Entry(//? if >=1.20
            category = "damage",
            name = "blue", isSlider = true, min = 0f, max = 255, precision = 1) public static int blue = 255;
    @Entry(//? if >=1.20
            category = "damage",
            name = "green", isSlider = true, min = 0f, max = 255, precision = 1) public static int green = 255;

    //? if <1.20
    /*@Comment public static Comment armspacer;*/

    // First person arm
    @Comment(//? if >=1.20
            category = "arm",
            centered = true) public static Comment firstPersonArmInfo;
    @Entry(//? if >=1.20
            category = "arm"
    ) public static boolean enableArm = true;

    @Comment(//? if >=1.20
            category = "arm"
    ) public static Comment armLocationspacer;
    @Comment(//? if >=1.20
            category = "arm",
            centered = true) public static Comment armLocationInfo;
    @Entry(//? if >=1.20
            category = "arm",
            name = "Y trans", isSlider = true, min = -1f, max = 1f, precision = 100) public static float Ytrans = 0.08f;
    @Entry(//? if >=1.20
            category = "arm",
            name = "X trans", isSlider = true, min = -1f, max = 1f, precision = 100) public static float Xtrans = 0.08f;
    @Entry(//? if >=1.20
            category = "arm",
            name = "Z trans", isSlider = true, min = -1f, max = 1f, precision = 100) public static float Ztrans = -0.09f;

    @Comment(//? if >=1.20
            category = "arm"
    ) public static Comment armRotationspacer;
    @Comment(//? if >=1.20
            category = "arm",
            centered = true) public static Comment armRotationInfo;
    @Entry(//? if >=1.20
            category = "arm",
            name = "Pitch", isSlider = true, min = -2f, max = 2f, precision = 100) public static float Pitch = 0.28f;
    @Entry(//? if >=1.20
            category = "arm",
            name = "Roll", isSlider = true, min = -2f, max = 2f, precision = 100) public static float Roll = 0.08f;
    @Entry(//? if >=1.20
            category = "arm",
            name = "Yaw", isSlider = true, min = -2f, max = 2f, precision = 100) public static float Yaw = -0.76f;



}