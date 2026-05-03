package tecna.oldwalkinganimation;

//? if >=26.1 || neoforge {
import com.mojang.authlib.GameProfile;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;
import java.util.Random;
import java.util.UUID;

import static tecna.oldwalkinganimation.config.Config.*;

public final class OwaSteveManager {

    private static final Deque<OwaSteve> steves = new ArrayDeque<>();
    private static final Random RNG = new Random();
    private static int nextId = -1000000;
    private static int counter = 0;
    private static KeyMapping spawnKey;
    private static String lastSkinMode = null;

    private OwaSteveManager() {}

    public static void setSpawnKey(KeyMapping key) {
        spawnKey = key;
    }

    public static void tick(Minecraft mc) {
        if (spawnKey == null || mc.level == null || mc.player == null) return;
        scanPlayerInfos(mc);
        refreshSkinsIfModeChanged();
        despawnUnloadedOrVoid(mc.level);
        pruneDead();
        for (OwaSteve s : steves) s.owa$syncNameToCustomName();
        respawnMismatchedNames(mc.level);
        while (spawnKey.consumeClick()) {
            spawn();
        }
    }

    // Suppliers stored on each Steve are captured at spawn time; without this, changing
    // Config.steveSkinMode at runtime leaves already-spawned humans on their old skin/name.
    private static void refreshSkinsIfModeChanged() {
        String mode = tecna.oldwalkinganimation.config.Config.steveSkinMode;
        if (java.util.Objects.equals(mode, lastSkinMode)) return;
        lastSkinMode = mode;
        for (OwaSteve s : steves) {
            OwaSkins.SkinResolution res = OwaSkins.resolveSkin(s.getUUID());
            s.setSkinSupplier(res.skin());
            s.setNameSupplier(res.name());
        }
    }

    private static void despawnUnloadedOrVoid(ClientLevel level) {
        //? if >=26.1 || (neoforge && >=1.21.2) {
        int minY = level.getMinY();
        //?} else {
        /*int minY = level.getMinBuildHeight();
        *///?}
        java.util.Iterator<OwaSteve> it = steves.iterator();
        while (it.hasNext()) {
            OwaSteve s = it.next();
            if (s.getY() < minY - 16) {
                s.discard();
                it.remove();
                continue;
            }
            // isLoaded checks the loaded-chunks set rather than just any cached chunk —
            // matches what vanilla considers "currently active" terrain.
            if (!level.isLoaded(s.blockPosition())) {
                s.discard();
                it.remove();
            }
        }
    }

    // Player.gameProfile is set once at construction and several vanilla render paths
    // (e.g. AvatarRenderer.isPlayerUpsideDown, the nametag text in some versions) read it
    // via the field, bypassing any getter override. Reflecting through to the final field
    // also doesn't propagate to all those reads. The cleanest fix is to recreate the Steve
    // with a fresh GameProfile carrying the resolved canonical name once it's known.
    private static void respawnMismatchedNames(ClientLevel level) {
        // Throttle to one respawn per tick — adding+discarding many entities with the same
        // UUID in a single tick races with ClientLevel's removal pass and can cause some
        // Steves to silently disappear (most visible right after natural spawning, when
        // dozens of "Steve" placeholders all need to be replaced once sources resolve).
        OwaSteve old = null;
        String newName = null;
        for (OwaSteve s : steves) {
            String expected = s.owa$getExpectedName();
            if (expected == null) continue;
            if (expected.equals(s.owa$getSpawnedName())) continue;
            old = s;
            newName = expected;
            break;
        }
        if (old == null) return;
        UUID uuid = old.getUUID();
        GameProfile profile = new GameProfile(uuid, newName);
        OwaSteve fresh = new OwaSteve(level, profile);
        fresh.owa$setSpawnedName(newName);
        fresh.setPos(old.getX(), old.getY(), old.getZ());
        fresh.setYRot(old.getYRot());
        fresh.setXRot(old.getXRot());
        fresh.setYHeadRot(old.getYHeadRot());
        fresh.setYBodyRot(old.yBodyRot);
        fresh.setDeltaMovement(old.getDeltaMovement());
        OwaSkins.SkinResolution res = OwaSkins.resolveSkin(uuid);
        fresh.setSkinSupplier(res.skin());
        fresh.setNameSupplier(res.name());
        fresh.setId(allocateId());
        old.discard();
        steves.remove(old);
        level.addEntity(fresh);
        steves.addLast(fresh);
    }

    public static void onChunkLoad(ClientLevel level, LevelChunk chunk) {
        if (!naturalSpawning || !enableSteve) return;
        if (atCapacity()) return;
        if (naturalSpawnChance <= 0) return;
        if (RNG.nextInt(100) >= naturalSpawnChance) return;

        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null) return;

        ChunkPos pos = chunk.getPos();
        int blockX = pos.getMinBlockX() + RNG.nextInt(16);
        int blockZ = pos.getMinBlockZ() + RNG.nextInt(16);
        // Spawn at the surface for the chosen (x, z), not relative to the player — otherwise
        // a player deep underground spawns Steves embedded in the surrounding terrain. Use
        // MOTION_BLOCKING (Purpose.CLIENT on every supported version); MOTION_BLOCKING_NO_LEAVES
        // is server-only on <=1.21.x and the client samples 0 → Steves spawn at bedrock and fall.
        int surfaceY = chunk.getHeight(net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING, blockX, blockZ);
        double x = blockX + 0.5;
        double z = blockZ + 0.5;
        double y = surfaceY + 1.0;
        spawnAt(level, x, y, z);
    }

    private static void pruneDead() {
        Iterator<OwaSteve> it = steves.iterator();
        while (it.hasNext()) if (!it.next().isAlive()) it.remove();
    }

    private static int scanCooldown = 0;
    private static void scanPlayerInfos(Minecraft mc) {
        if (--scanCooldown > 0) return;
        scanCooldown = 40;
        var conn = mc.getConnection();
        if (conn == null) return;
        for (var info : conn.getOnlinePlayers()) {
            OwaSkins.onProfileSeen(info.getProfile());
        }
    }

    private static boolean atCapacity() {
        return maxHumans > 0 && steves.size() >= maxHumans;
    }

    public static void spawn() {
        if (!enableSteve || atCapacity()) return;

        Minecraft mc = Minecraft.getInstance();
        ClientLevel level = mc.level;
        LocalPlayer player = mc.player;
        if (level == null || player == null) return;

        double yaw = player.getYRot() * Math.PI / 180.0;
        double spawnX = player.getX() - Math.sin(yaw) * 2.0;
        double spawnZ = player.getZ() + Math.cos(yaw) * 2.0;
        spawnAt(level, spawnX, player.getY(), spawnZ);
    }

    private static void spawnAt(ClientLevel level, double x, double y, double z) {
        UUID uuid = UUID.nameUUIDFromBytes(("owa_steve_" + (counter++)).getBytes());
        OwaSkins.SkinResolution res = OwaSkins.resolveSkin(uuid);
        String resolvedName;
        try {
            String n = res.name().get();
            resolvedName = (n == null || n.isEmpty() || "Custom".equals(n)) ? "Steve" : n;
        } catch (Throwable t) {
            resolvedName = "Steve";
        }
        GameProfile profile = new GameProfile(uuid, resolvedName);
        OwaSteve steve = new OwaSteve(level, profile);
        steve.owa$setSpawnedName(resolvedName);
        steve.setSkinSupplier(res.skin());
        steve.setNameSupplier(res.name());
        steve.setPos(x, y, z);
        steve.setId(allocateId());
        level.addEntity(steve);
        steves.addLast(steve);
    }

    public static void clear() {
        for (OwaSteve s : steves) {
            s.discard();
        }
        steves.clear();
        OwaSkins.resetUsed();
    }

    public static void applyExplosion(Vec3 center, float power) {
        if (power <= 0.0f || steves.isEmpty()) return;
        double effectRadius = power * 2.0;
        for (OwaSteve s : steves) {
            if (!s.isAlive()) continue;
            Vec3 eye = s.getEyePosition();
            double dist = Math.sqrt(s.distanceToSqr(center)) / effectRadius;
            if (dist > 1.0) continue;
            Vec3 dir = eye.subtract(center);
            if (dir.lengthSqr() < 1.0e-6) continue;
            dir = dir.normalize();
            double impulse = 1.0 - dist;
            Vec3 v = s.getDeltaMovement();
            s.setDeltaMovement(v.x + dir.x * impulse, v.y + dir.y * impulse, v.z + dir.z * impulse);
        }
    }

    private static int allocateId() {
        int id = nextId--;
        if (nextId > -1000) nextId = -1000000;
        return id;
    }
}
//?} else {
/*import com.mojang.authlib.GameProfile;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.chunk.WorldChunk;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;
import java.util.Random;
import java.util.UUID;

import static tecna.oldwalkinganimation.config.Config.*;

public final class OwaSteveManager {

    private static final Deque<OwaSteve> steves = new ArrayDeque<>();
    private static final Random RNG = new Random();
    private static int nextId = -1000000;
    private static int counter = 0;
    private static KeyBinding spawnKey;
    private static String lastSkinMode = null;

    private OwaSteveManager() {}

    public static void setSpawnKey(KeyBinding key) {
        spawnKey = key;
    }

    public static void tick(MinecraftClient mc) {
        if (spawnKey == null || mc.world == null || mc.player == null) return;
        scanPlayerInfos(mc);
        refreshSkinsIfModeChanged();
        despawnUnloadedOrVoid(mc.world);
        pruneDead();
        for (OwaSteve s : steves) s.owa$syncNameToCustomName();
        respawnMismatchedNames(mc.world);
        while (spawnKey.wasPressed()) {
            spawn();
        }
    }

    // Suppliers stored on each Steve are captured at spawn time; without this, changing
    // Config.steveSkinMode at runtime leaves already-spawned humans on their old skin/name.
    private static void refreshSkinsIfModeChanged() {
        String mode = tecna.oldwalkinganimation.config.Config.steveSkinMode;
        if (java.util.Objects.equals(mode, lastSkinMode)) return;
        lastSkinMode = mode;
        for (OwaSteve s : steves) {
            OwaSkins.SkinResolution res = OwaSkins.resolveSkin(s.getUuid());
            s.setSkinSupplier(res.skin());
            s.setNameSupplier(res.name());
        }
    }

    private static void despawnUnloadedOrVoid(ClientWorld world) {
        int minY = world.getBottomY();
        java.util.Iterator<OwaSteve> it = steves.iterator();
        while (it.hasNext()) {
            OwaSteve s = it.next();
            if (s.getY() < minY - 16) {
                s.discard();
                it.remove();
                continue;
            }
            if (!world.isPosLoaded(s.getBlockX(), s.getBlockZ())) {
                s.discard();
                it.remove();
            }
        }
    }

    private static void respawnMismatchedNames(ClientWorld world) {
        OwaSteve old = null;
        String newName = null;
        for (OwaSteve s : steves) {
            String expected = s.owa$getExpectedName();
            if (expected == null) continue;
            if (expected.equals(s.owa$getSpawnedName())) continue;
            old = s;
            newName = expected;
            break;
        }
        if (old == null) return;
        UUID uuid = old.getUuid();
        GameProfile profile = new GameProfile(uuid, newName);
        OwaSteve fresh = new OwaSteve(world, profile);
        fresh.owa$setSpawnedName(newName);
        fresh.setPosition(old.getX(), old.getY(), old.getZ());
        fresh.setYaw(old.getYaw());
        fresh.setPitch(old.getPitch());
        fresh.setHeadYaw(old.getHeadYaw());
        fresh.setBodyYaw(old.bodyYaw);
        fresh.setVelocity(old.getVelocity());
        OwaSkins.SkinResolution res = OwaSkins.resolveSkin(uuid);
        fresh.setSkinSupplier(res.skin());
        fresh.setNameSupplier(res.name());
        fresh.setId(allocateId());
        old.discard();
        steves.remove(old);
        world.addEntity(fresh);
        steves.addLast(fresh);
    }

    public static void onChunkLoad(ClientWorld world, WorldChunk chunk) {
        if (!naturalSpawning || !enableSteve) return;
        if (atCapacity()) return;
        if (naturalSpawnChance <= 0) return;
        if (RNG.nextInt(100) >= naturalSpawnChance) return;

        MinecraftClient mc = MinecraftClient.getInstance();
        ClientPlayerEntity player = mc.player;
        if (player == null) return;

        ChunkPos pos = chunk.getPos();
        int blockX = pos.getStartX() + RNG.nextInt(16);
        int blockZ = pos.getStartZ() + RNG.nextInt(16);
        // Spawn at the surface for the chosen (x, z) so Steves don't embed in terrain when
        // the player is deep underground. MOTION_BLOCKING is the only motion-blocking heightmap
        // sent to the client on yarn <=1.21.x (MOTION_BLOCKING_NO_LEAVES has Purpose.LIVE_WORLD
        // there → server-only → client samples 0 and Steves fall through bedrock into the void).
        int surfaceY = chunk.sampleHeightmap(net.minecraft.world.Heightmap.Type.MOTION_BLOCKING, blockX, blockZ);
        double x = blockX + 0.5;
        double z = blockZ + 0.5;
        double y = surfaceY + 1.0;
        spawnAt(world, x, y, z);
    }

    private static void pruneDead() {
        Iterator<OwaSteve> it = steves.iterator();
        while (it.hasNext()) if (!it.next().isAlive()) it.remove();
    }

    private static int scanCooldown = 0;
    private static void scanPlayerInfos(MinecraftClient mc) {
        if (--scanCooldown > 0) return;
        scanCooldown = 40;
        var conn = mc.getNetworkHandler();
        if (conn == null) return;
        for (var info : conn.getPlayerList()) {
            OwaSkins.onProfileSeen(info.getProfile());
        }
    }

    private static boolean atCapacity() {
        return maxHumans > 0 && steves.size() >= maxHumans;
    }

    public static void spawn() {
        if (!enableSteve || atCapacity()) return;

        MinecraftClient mc = MinecraftClient.getInstance();
        ClientWorld world = mc.world;
        ClientPlayerEntity player = mc.player;
        if (world == null || player == null) return;

        double yaw = player.getYaw() * Math.PI / 180.0;
        double spawnX = player.getX() - Math.sin(yaw) * 2.0;
        double spawnZ = player.getZ() + Math.cos(yaw) * 2.0;
        spawnAt(world, spawnX, player.getY(), spawnZ);
    }

    private static void spawnAt(ClientWorld world, double x, double y, double z) {
        UUID uuid = UUID.nameUUIDFromBytes(("owa_steve_" + (counter++)).getBytes());
        OwaSkins.SkinResolution res = OwaSkins.resolveSkin(uuid);
        String resolvedName;
        try {
            String n = res.name().get();
            resolvedName = (n == null || n.isEmpty() || "Custom".equals(n)) ? "Steve" : n;
        } catch (Throwable t) {
            resolvedName = "Steve";
        }
        GameProfile profile = new GameProfile(uuid, resolvedName);
        OwaSteve steve = new OwaSteve(world, profile);
        steve.owa$setSpawnedName(resolvedName);
        steve.setSkinSupplier(res.skin());
        steve.setNameSupplier(res.name());
        steve.setPosition(x, y, z);
        steve.setId(allocateId());
        world.addEntity(steve);
        steves.addLast(steve);
    }

    public static void clear() {
        for (OwaSteve s : steves) {
            s.discard();
        }
        steves.clear();
        OwaSkins.resetUsed();
    }

    public static void applyExplosion(Vec3d center, float power) {
        if (power <= 0.0f || steves.isEmpty()) return;
        double effectRadius = power * 2.0;
        for (OwaSteve s : steves) {
            if (!s.isAlive()) continue;
            Vec3d eye = s.getEyePos();
            double dist = Math.sqrt(s.squaredDistanceTo(center)) / effectRadius;
            if (dist > 1.0) continue;
            Vec3d dir = eye.subtract(center);
            if (dir.lengthSquared() < 1.0e-6) continue;
            dir = dir.normalize();
            double impulse = 1.0 - dist;
            Vec3d v = s.getVelocity();
            s.setVelocity(v.x + dir.x * impulse, v.y + dir.y * impulse, v.z + dir.z * impulse);
        }
    }

    private static int allocateId() {
        int id = nextId--;
        if (nextId > -1000) nextId = -1000000;
        return id;
    }
}
*///?}
