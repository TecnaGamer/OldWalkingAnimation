package tecna.oldwalkinganimation;

//? if >=26.1 || neoforge {
import net.minecraft.client.Minecraft;
import net.minecraft.world.level.Level;
import org.lwjgl.glfw.GLFW;

public final class OwaTimeScale {
    private static double scaledTime = 0.0;
    private static double lastWall = -1.0;

    private OwaTimeScale() {}

    public static float currentTickrate() {
        Minecraft mc = Minecraft.getInstance();
        Level level = mc != null ? mc.level : null;
        if (level == null) return 20.0f;
        //? if >=1.20.4 {
        try { return level.tickRateManager().tickrate(); }
        catch (Throwable t) { return 20.0f; }
        //?} else {
        /*return 20.0f;
        *///?}
    }

    public static double scaledTime() {
        double now = GLFW.glfwGetTime();
        if (lastWall < 0) lastWall = now;
        double wallDelta = now - lastWall;
        lastWall = now;
        if (wallDelta < 0) wallDelta = 0;
        if (wallDelta > 0.25) wallDelta = 0.25;
        scaledTime += wallDelta * (currentTickrate() / 20.0);
        return scaledTime;
    }

    public static float scaleFactor() {
        return currentTickrate() / 20.0f;
    }
}
//?} else {
/*import net.minecraft.client.MinecraftClient;
import net.minecraft.world.World;
import org.lwjgl.glfw.GLFW;

public final class OwaTimeScale {
    private static double scaledTime = 0.0;
    private static double lastWall = -1.0;

    private OwaTimeScale() {}

    public static float currentTickrate() {
        MinecraftClient mc = MinecraftClient.getInstance();
        World world = mc != null ? mc.world : null;
        if (world == null) return 20.0f;
        //? if >=1.20.4 {
        try { return world.getTickManager().getTickRate(); }
        catch (Throwable t) { return 20.0f; }
        //?} else
        /^return 20.0f;^/
    }

    public static double scaledTime() {
        double now = GLFW.glfwGetTime();
        if (lastWall < 0) lastWall = now;
        double wallDelta = now - lastWall;
        lastWall = now;
        if (wallDelta < 0) wallDelta = 0;
        if (wallDelta > 0.25) wallDelta = 0.25;
        scaledTime += wallDelta * (currentTickrate() / 20.0);
        return scaledTime;
    }

    public static float scaleFactor() {
        return currentTickrate() / 20.0f;
    }
}
*///?}
