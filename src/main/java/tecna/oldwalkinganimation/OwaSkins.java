package tecna.oldwalkinganimation;

//? if >=26.1 || (neoforge && >=1.21.9) {
import com.google.common.collect.ImmutableMultimap;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.mojang.authlib.properties.PropertyMap;
import com.mojang.blaze3d.platform.NativeImage;
//? if fabric
import net.fabricmc.loader.api.FabricLoader;
//? if neoforge
/*import net.neoforged.fml.loading.FMLPaths;*/
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.core.ClientAsset;
//? if >=1.21.11 {
import net.minecraft.resources.Identifier;
//?} else {
/*import net.minecraft.resources.ResourceLocation;
*///?}
import net.minecraft.world.entity.player.PlayerModelType;
import net.minecraft.world.entity.player.PlayerSkin;
import tecna.oldwalkinganimation.config.Config;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.function.Supplier;

public final class OwaSkins {
    public static final String MODE_RANDOM_DEFAULT = "random_default";
    public static final String MODE_STEVE = "steve";
    public static final String MODE_PLAYER = "player";
    public static final String MODE_RANDOM_CACHED = "random_cached";
    public static final String MODE_CUSTOM = "custom";

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Executor EXEC = Executors.newFixedThreadPool(2, r -> {
        Thread t = new Thread(r, "OWA-Skins");
        t.setDaemon(true);
        return t;
    });
    private static final Random RNG = new Random();

    private record CachedProfile(String name, String textures, String signature) {}

    private static final Map<UUID, CachedProfile> CACHE = new ConcurrentHashMap<>();
    private static final java.util.Set<UUID> USED_CACHED = java.util.concurrent.ConcurrentHashMap.newKeySet();

    //? if >=1.21.11 {
    private static Identifier customSkinId;
    //?} else {
    /*private static ResourceLocation customSkinId;
    *///?}
    private static long customSkinMtime = -1L;

    private static Path cachePath;
    private static Path customSkinPath;

    private OwaSkins() {}

    public static void init(String modId) {
        //? if fabric {
        Path cfg = FabricLoader.getInstance().getConfigDir();
        //?} else if neoforge {
        /*Path cfg = FMLPaths.CONFIGDIR.get();
        *///?}
        cachePath = cfg.resolve(modId + "-skin-cache.json");
        customSkinPath = cfg.resolve(modId + "-custom-skin.png");
        OwaCustomSkinSources.init(cfg);
        load();
    }

    public static Path customSkinFile() { return customSkinPath; }

    public static void onProfileSeen(GameProfile profile) {
        if (profile == null) return;
        UUID id = profile.id();
        if (id == null) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc != null && mc.player != null && id.equals(mc.player.getUUID())) return;
        var texturesProps = profile.properties().get("textures");
        if (texturesProps.isEmpty()) return;
        Property prop = texturesProps.iterator().next();
        String value = prop.value();
        String sig = prop.signature();
        CachedProfile existing = CACHE.get(id);
        if (existing != null && Objects.equals(existing.textures, value)) return;
        CACHE.put(id, new CachedProfile(profile.name(), value, sig));
        saveAsync();
    }

    public record SkinResolution(Supplier<PlayerSkin> skin, Supplier<String> name) {}

    public static SkinResolution resolveSkin(UUID steveUuid) {
        String mode = Config.steveSkinMode == null ? MODE_RANDOM_DEFAULT : Config.steveSkinMode;
        switch (mode) {
            case MODE_STEVE: return steveSkinSupplier();
            case MODE_PLAYER: return playerSkinSupplier(steveUuid);
            case MODE_RANDOM_CACHED: return cachedSkinSupplier(steveUuid);
            case MODE_CUSTOM: return customSkinSupplier(steveUuid);
            case MODE_RANDOM_DEFAULT:
            default: return defaultSkinSupplier(steveUuid);
        }
    }

    private static SkinResolution defaultSkinSupplier(UUID steveUuid) {
        PlayerSkin skin = DefaultPlayerSkin.get(steveUuid);
        return new SkinResolution(() -> skin, () -> skinDisplayName(skin));
    }

    //? if >=1.21.11 {
    private static Identifier classicSteveId;
    //?} else {
    /*private static ResourceLocation classicSteveId;
    *///?}
    private static boolean classicSteveTried;

    private static SkinResolution steveSkinSupplier() {
        PlayerSkin skin = loadClassicSteveSkin();
        if (skin == null) {
            PlayerSkin fallback = new PlayerSkin(
                    new ClientAsset.ResourceTexture(
                            //? if >=1.21.11 {
                            Identifier.withDefaultNamespace("entity/player/wide/steve")),
                            //?} else {
                            /*ResourceLocation.withDefaultNamespace("entity/player/wide/steve")),
                            *///?}
                    null, null, PlayerModelType.WIDE, true);
            return new SkinResolution(() -> fallback, () -> "Steve");
        }
        return new SkinResolution(() -> skin, () -> "Steve");
    }

    private static synchronized PlayerSkin loadClassicSteveSkin() {
        if (classicSteveId != null) {
            return new PlayerSkin(
                    new ClientAsset.ResourceTexture(classicSteveId, classicSteveId),
                    null, null, PlayerModelType.WIDE, true);
        }
        if (classicSteveTried) return null;
        classicSteveTried = true;
        Minecraft mc = Minecraft.getInstance();
        if (mc == null) return null;
        //? if >=1.21.11 {
        Identifier src = Identifier.fromNamespaceAndPath("oldwalkinganimation", "textures/entity/classic_steve.png");
        //?} else {
        /*ResourceLocation src = ResourceLocation.fromNamespaceAndPath("oldwalkinganimation", "textures/entity/classic_steve.png");
        *///?}
        var opt = mc.getResourceManager().getResource(src);
        if (opt.isEmpty()) return null;
        try (InputStream in = opt.get().open()) {
            NativeImage img = NativeImage.read(in);
            img = processLegacySkin(img);
            DynamicTexture tex = new DynamicTexture(() -> "owa_classic_steve", img);
            //? if >=1.21.11 {
            Identifier id = Identifier.fromNamespaceAndPath("oldwalkinganimation", "classic_steve_processed");
            //?} else {
            /*ResourceLocation id = ResourceLocation.fromNamespaceAndPath("oldwalkinganimation", "classic_steve_processed");
            *///?}
            mc.getTextureManager().register(id, tex);
            classicSteveId = id;
            return new PlayerSkin(
                    new ClientAsset.ResourceTexture(id, id),
                    null, null, PlayerModelType.WIDE, true);
        } catch (IOException e) {
            System.err.println("[OWA] Failed to load classic Steve skin: " + e);
            return null;
        }
    }

    private static NativeImage processLegacySkin(NativeImage img) {
        int w = img.getWidth();
        int h = img.getHeight();
        if (w != 64 || (h != 32 && h != 64)) return img;
        boolean legacy = h == 32;
        if (!legacy) return img;

        NativeImage out = new NativeImage(64, 64, true);
        out.copyFrom(img);
        img.close();
        out.fillRect(0, 32, 64, 32, 0);
        out.copyRect(4, 16, 16, 32, 4, 4, true, false);
        out.copyRect(8, 16, 16, 32, 4, 4, true, false);
        out.copyRect(0, 20, 24, 32, 4, 12, true, false);
        out.copyRect(4, 20, 16, 32, 4, 12, true, false);
        out.copyRect(8, 20, 8, 32, 4, 12, true, false);
        out.copyRect(12, 20, 16, 32, 4, 12, true, false);
        out.copyRect(44, 16, -8, 32, 4, 4, true, false);
        out.copyRect(48, 16, -8, 32, 4, 4, true, false);
        out.copyRect(40, 20, 0, 32, 4, 12, true, false);
        out.copyRect(44, 20, -8, 32, 4, 12, true, false);
        out.copyRect(48, 20, -16, 32, 4, 12, true, false);
        out.copyRect(52, 20, -8, 32, 4, 12, true, false);
        return out;
    }

    private static SkinResolution playerSkinSupplier(UUID fallback) {
        Supplier<PlayerSkin> skin = () -> {
            Minecraft mc = Minecraft.getInstance();
            if (mc != null && mc.player != null) return mc.player.getSkin();
            return DefaultPlayerSkin.get(fallback);
        };
        Supplier<String> name = () -> {
            Minecraft mc = Minecraft.getInstance();
            if (mc != null && mc.player != null) return mc.player.getGameProfile().name();
            return "Player";
        };
        return new SkinResolution(skin, name);
    }

    private static String skinDisplayName(PlayerSkin skin) {
        try {
            String path = skin.body().texturePath().getPath();
            int slash = path.lastIndexOf('/');
            String last = slash >= 0 ? path.substring(slash + 1) : path;
            if (last.endsWith(".png")) last = last.substring(0, last.length() - 4);
            if (last.isEmpty()) return "Steve";
            return Character.toUpperCase(last.charAt(0)) + last.substring(1);
        } catch (Throwable t) {
            return "Steve";
        }
    }

    private static SkinResolution cachedSkinSupplier(UUID fallback) {
        List<UUID> keys = new ArrayList<>(CACHE.keySet());
        keys.removeAll(USED_CACHED);
        if (keys.isEmpty()) return new SkinResolution(
                () -> DefaultPlayerSkin.get(fallback), () -> "Player");
        UUID pick = keys.get(RNG.nextInt(keys.size()));
        USED_CACHED.add(pick);
        CachedProfile c = CACHE.get(pick);
        if (c == null) return new SkinResolution(
                () -> DefaultPlayerSkin.get(fallback), () -> "Player");
        PropertyMap props = new PropertyMap(ImmutableMultimap.of(
                "textures", new Property("textures", c.textures, c.signature)));
        GameProfile profile = new GameProfile(pick, c.name, props);
        Minecraft mc = Minecraft.getInstance();
        if (mc == null) return new SkinResolution(
                () -> DefaultPlayerSkin.get(fallback), () -> c.name);
        Supplier<PlayerSkin> inner = mc.getSkinManager().createLookup(profile, false);
        Supplier<PlayerSkin> skin = () -> {
            try { return inner.get(); } catch (Throwable t) { return DefaultPlayerSkin.get(fallback); }
        };
        return new SkinResolution(skin, () -> c.name);
    }

    private static SkinResolution customSkinSupplier(UUID fallback) {
        // Dynamic supplier: resolve each call so async-fetched name/URL skins (and the cape +
        // canonical name pulled from the Mojang profile for name entries) appear as soon as
        // they're cached, instead of locking in the default at spawn time.
        return new SkinResolution(() -> {
            OwaCustomSkinSources.tick(Config.steveCustomSkinSources);
            java.util.List<Path> sources = OwaCustomSkinSources.resolved();
            if (!sources.isEmpty()) {
                int idx = Math.floorMod(fallback.hashCode(), sources.size());
                Path picked = sources.get(idx);
                Path capePath = OwaCustomSkinSources.capeForPath(picked);
                PlayerSkin s = loadCustomSkinFromPath(picked, capePath);
                if (s != null) return s;
            }
            PlayerSkin s = loadCustomSkin();
            if (s != null) return s;
            return DefaultPlayerSkin.get(fallback);
        }, () -> {
            java.util.List<Path> sources = OwaCustomSkinSources.resolved();
            if (!sources.isEmpty()) {
                int idx = Math.floorMod(fallback.hashCode(), sources.size());
                String name = OwaCustomSkinSources.nameForPath(sources.get(idx));
                if (name != null) return name;
            }
            return "Custom";
        });
    }

    //? if >=1.21.11 {
    private static final java.util.Map<Path, Identifier> CUSTOM_TEX_ID = new java.util.concurrent.ConcurrentHashMap<>();
    //?} else {
    /*private static final java.util.Map<Path, ResourceLocation> CUSTOM_TEX_ID = new java.util.concurrent.ConcurrentHashMap<>();
    *///?}
    private static final java.util.Map<Path, Long> CUSTOM_TEX_MTIME = new java.util.concurrent.ConcurrentHashMap<>();

    //? if >=1.21.11 {
    private static synchronized Identifier owa$ensureTexture(Path path, String namePrefix, boolean processSkin) {
    //?} else {
    /*private static synchronized ResourceLocation owa$ensureTexture(Path path, String namePrefix, boolean processSkin) {
    *///?}
        if (path == null || !Files.exists(path)) return null;
        Minecraft mc = Minecraft.getInstance();
        if (mc == null) return null;
        try {
            long mtime = Files.getLastModifiedTime(path).toMillis();
            Long known = CUSTOM_TEX_MTIME.get(path);
            //? if >=1.21.11 {
            Identifier cachedId = CUSTOM_TEX_ID.get(path);
            //?} else {
            /*ResourceLocation cachedId = CUSTOM_TEX_ID.get(path);
            *///?}
            if (cachedId != null && known != null && known == mtime) return cachedId;
            byte[] bytes = Files.readAllBytes(path);
            NativeImage img = NativeImage.read(bytes);
            if (processSkin) img = processLegacySkin(img);
            DynamicTexture tex = new DynamicTexture(() -> namePrefix, img);
            String pathHash = Integer.toHexString(path.toString().hashCode());
            //? if >=1.21.11 {
            Identifier id = Identifier.fromNamespaceAndPath("oldwalkinganimation", namePrefix + "_" + pathHash);
            Identifier oldId = cachedId;
            //?} else {
            /*ResourceLocation id = ResourceLocation.fromNamespaceAndPath("oldwalkinganimation", namePrefix + "_" + pathHash);
            ResourceLocation oldId = cachedId;
            *///?}
            if (oldId != null) mc.getTextureManager().release(oldId);
            mc.getTextureManager().register(id, tex);
            CUSTOM_TEX_MTIME.put(path, mtime);
            CUSTOM_TEX_ID.put(path, id);
            return id;
        } catch (IOException e) {
            System.err.println("[OWA] Failed to load texture " + path + ": " + e);
            return null;
        }
    }

    private static PlayerSkin loadCustomSkinFromPath(Path skinPath, Path capePath) {
        //? if >=1.21.11 {
        Identifier skinId = owa$ensureTexture(skinPath, "owa_custom_skin", true);
        if (skinId == null) return null;
        Identifier capeId = capePath == null ? null : owa$ensureTexture(capePath, "owa_custom_cape", false);
        ClientAsset.ResourceTexture capeAsset = capeId == null ? null : new ClientAsset.ResourceTexture(capeId, capeId);
        return new PlayerSkin(
                new ClientAsset.ResourceTexture(skinId, skinId),
                capeAsset, null, PlayerModelType.WIDE, true);
        //?} else {
        /*ResourceLocation skinId = owa$ensureTexture(skinPath, "owa_custom_skin", true);
        if (skinId == null) return null;
        ResourceLocation capeId = capePath == null ? null : owa$ensureTexture(capePath, "owa_custom_cape", false);
        ClientAsset.ResourceTexture capeAsset = capeId == null ? null : new ClientAsset.ResourceTexture(capeId, capeId);
        return new PlayerSkin(
                new ClientAsset.ResourceTexture(skinId, skinId),
                capeAsset, null, PlayerModelType.WIDE, true);
        *///?}
    }

    private static PlayerSkin loadCustomSkin() {
        return loadCustomSkinFromPath(customSkinPath, null);
    }

    private static void load() {
        CACHE.clear();
        if (cachePath == null || !Files.exists(cachePath)) return;
        try {
            JsonObject root = JsonParser.parseString(Files.readString(cachePath)).getAsJsonObject();
            if (!root.has("profiles")) return;
            JsonObject profiles = root.getAsJsonObject("profiles");
            for (Map.Entry<String, JsonElement> e : profiles.entrySet()) {
                JsonObject p = e.getValue().getAsJsonObject();
                String name = p.has("name") ? p.get("name").getAsString() : "?";
                String textures = p.get("textures").getAsString();
                String sig = p.has("signature") ? p.get("signature").getAsString() : null;
                try {
                    CACHE.put(UUID.fromString(e.getKey()), new CachedProfile(name, textures, sig));
                } catch (IllegalArgumentException ignored) {}
            }
        } catch (IOException e) {
            System.err.println("[OWA] Failed to load skin cache: " + e);
        }
    }

    private static final Object SAVE_LOCK = new Object();
    private static volatile boolean savePending = false;

    private static void saveAsync() {
        synchronized (SAVE_LOCK) {
            if (savePending) return;
            savePending = true;
        }
        EXEC.execute(() -> {
            synchronized (SAVE_LOCK) { savePending = false; }
            save();
        });
    }

    private static void save() {
        if (cachePath == null) return;
        try {
            JsonObject root = new JsonObject();
            JsonObject profiles = new JsonObject();
            for (Map.Entry<UUID, CachedProfile> e : CACHE.entrySet()) {
                JsonObject p = new JsonObject();
                p.addProperty("name", e.getValue().name);
                p.addProperty("textures", e.getValue().textures);
                if (e.getValue().signature != null) p.addProperty("signature", e.getValue().signature);
                profiles.add(e.getKey().toString(), p);
            }
            root.add("profiles", profiles);
            Files.createDirectories(cachePath.getParent());
            Files.writeString(cachePath, GSON.toJson(root));
        } catch (IOException e) {
            System.err.println("[OWA] Failed to save skin cache: " + e);
        }
    }

    public static int cachedProfileCount() {
        return CACHE.size();
    }

    public static void resetUsed() {
        USED_CACHED.clear();
    }

    @SuppressWarnings("unused")
    private static List<UUID> unused() { return Collections.emptyList(); }
}
//?} else if >=1.21.9 {
/*import com.google.common.collect.ImmutableMultimap;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.mojang.authlib.properties.PropertyMap;
import net.minecraft.client.texture.NativeImage;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.client.util.DefaultSkinHelper;
import net.minecraft.entity.player.PlayerSkinType;
import net.minecraft.entity.player.SkinTextures;
import net.minecraft.util.AssetInfo;
import net.minecraft.util.Identifier;
import tecna.oldwalkinganimation.config.Config;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.function.Supplier;

public final class OwaSkins {
    public static final String MODE_RANDOM_DEFAULT = "random_default";
    public static final String MODE_STEVE = "steve";
    public static final String MODE_PLAYER = "player";
    public static final String MODE_RANDOM_CACHED = "random_cached";
    public static final String MODE_CUSTOM = "custom";

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Executor EXEC = Executors.newFixedThreadPool(2, r -> {
        Thread t = new Thread(r, "OWA-Skins");
        t.setDaemon(true);
        return t;
    });
    private static final Random RNG = new Random();

    private record CachedProfile(String name, String textures, String signature) {}

    private static final Map<UUID, CachedProfile> CACHE = new ConcurrentHashMap<>();
    private static final java.util.Set<UUID> USED_CACHED = java.util.concurrent.ConcurrentHashMap.newKeySet();

    private static Identifier customSkinId;
    private static long customSkinMtime = -1L;

    private static Path cachePath;
    private static Path customSkinPath;

    private OwaSkins() {}

    public static void init(String modId) {
        Path cfg = FabricLoader.getInstance().getConfigDir();
        cachePath = cfg.resolve(modId + "-skin-cache.json");
        customSkinPath = cfg.resolve(modId + "-custom-skin.png");
        OwaCustomSkinSources.init(cfg);
        load();
    }

    public static Path customSkinFile() { return customSkinPath; }

    public static void onProfileSeen(GameProfile profile) {
        if (profile == null) return;
        UUID id = profile.id();
        if (id == null) return;
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc != null && mc.player != null && id.equals(mc.player.getUuid())) return;
        var texturesProps = profile.properties().get("textures");
        if (texturesProps.isEmpty()) return;
        Property prop = texturesProps.iterator().next();
        String value = prop.value();
        String sig = prop.signature();
        CachedProfile existing = CACHE.get(id);
        if (existing != null && Objects.equals(existing.textures, value)) return;
        CACHE.put(id, new CachedProfile(profile.name(), value, sig));
        saveAsync();
    }

    public record SkinResolution(Supplier<SkinTextures> skin, Supplier<String> name) {}

    public static SkinResolution resolveSkin(UUID steveUuid) {
        String mode = Config.steveSkinMode == null ? MODE_RANDOM_DEFAULT : Config.steveSkinMode;
        switch (mode) {
            case MODE_STEVE: return steveSkinSupplier();
            case MODE_PLAYER: return playerSkinSupplier(steveUuid);
            case MODE_RANDOM_CACHED: return cachedSkinSupplier(steveUuid);
            case MODE_CUSTOM: return customSkinSupplier(steveUuid);
            case MODE_RANDOM_DEFAULT:
            default: return defaultSkinSupplier(steveUuid);
        }
    }

    private static SkinResolution defaultSkinSupplier(UUID steveUuid) {
        SkinTextures skin = DefaultSkinHelper.getSkinTextures(steveUuid);
        return new SkinResolution(() -> skin, () -> skinDisplayName(skin));
    }

    private static Identifier classicSteveId;
    private static boolean classicSteveTried;

    private static SkinResolution steveSkinSupplier() {
        SkinTextures skin = loadClassicSteveSkin();
        if (skin == null) {
            Identifier wide = Identifier.ofVanilla("textures/entity/player/wide/steve.png");
            SkinTextures fallback = new SkinTextures(
                    new AssetInfo.TextureAssetInfo(wide, wide),
                    null, null, PlayerSkinType.WIDE, true);
            return new SkinResolution(() -> fallback, () -> "Steve");
        }
        return new SkinResolution(() -> skin, () -> "Steve");
    }

    private static synchronized SkinTextures loadClassicSteveSkin() {
        if (classicSteveId != null) {
            return new SkinTextures(
                    new AssetInfo.TextureAssetInfo(classicSteveId, classicSteveId),
                    null, null, PlayerSkinType.WIDE, true);
        }
        if (classicSteveTried) return null;
        classicSteveTried = true;
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc == null) return null;
        Identifier src = Identifier.of("oldwalkinganimation", "textures/entity/classic_steve.png");
        var opt = mc.getResourceManager().getResource(src);
        if (opt.isEmpty()) return null;
        try (InputStream in = opt.get().getInputStream()) {
            NativeImage img = NativeImage.read(in);
            img = processLegacySkin(img);
            NativeImageBackedTexture tex = new NativeImageBackedTexture(() -> "owa_classic_steve", img);
            Identifier id = Identifier.of("oldwalkinganimation", "classic_steve_processed");
            mc.getTextureManager().registerTexture(id, tex);
            classicSteveId = id;
            return new SkinTextures(
                    new AssetInfo.TextureAssetInfo(id, id),
                    null, null, PlayerSkinType.WIDE, true);
        } catch (IOException e) {
            System.err.println("[OWA] Failed to load classic Steve skin: " + e);
            return null;
        }
    }

    private static NativeImage processLegacySkin(NativeImage img) {
        int w = img.getWidth();
        int h = img.getHeight();
        if (w != 64 || (h != 32 && h != 64)) return img;
        boolean legacy = h == 32;
        if (!legacy) return img;

        NativeImage out = new NativeImage(64, 64, true);
        out.copyFrom(img);
        img.close();
        out.fillRect(0, 32, 64, 32, 0);
        out.copyRect(4, 16, 16, 32, 4, 4, true, false);
        out.copyRect(8, 16, 16, 32, 4, 4, true, false);
        out.copyRect(0, 20, 24, 32, 4, 12, true, false);
        out.copyRect(4, 20, 16, 32, 4, 12, true, false);
        out.copyRect(8, 20, 8, 32, 4, 12, true, false);
        out.copyRect(12, 20, 16, 32, 4, 12, true, false);
        out.copyRect(44, 16, -8, 32, 4, 4, true, false);
        out.copyRect(48, 16, -8, 32, 4, 4, true, false);
        out.copyRect(40, 20, 0, 32, 4, 12, true, false);
        out.copyRect(44, 20, -8, 32, 4, 12, true, false);
        out.copyRect(48, 20, -16, 32, 4, 12, true, false);
        out.copyRect(52, 20, -8, 32, 4, 12, true, false);
        return out;
    }

    private static SkinResolution playerSkinSupplier(UUID fallback) {
        Supplier<SkinTextures> skin = () -> {
            MinecraftClient mc = MinecraftClient.getInstance();
            if (mc != null && mc.player != null) return mc.player.getSkin();
            return DefaultSkinHelper.getSkinTextures(fallback);
        };
        Supplier<String> name = () -> {
            MinecraftClient mc = MinecraftClient.getInstance();
            if (mc != null && mc.player != null) return mc.player.getGameProfile().name();
            return "Player";
        };
        return new SkinResolution(skin, name);
    }

    private static String skinDisplayName(SkinTextures skin) {
        try {
            String path = skin.body().texturePath().getPath();
            int slash = path.lastIndexOf('/');
            String last = slash >= 0 ? path.substring(slash + 1) : path;
            if (last.endsWith(".png")) last = last.substring(0, last.length() - 4);
            if (last.isEmpty()) return "Steve";
            return Character.toUpperCase(last.charAt(0)) + last.substring(1);
        } catch (Throwable t) {
            return "Steve";
        }
    }

    private static SkinResolution cachedSkinSupplier(UUID fallback) {
        List<UUID> keys = new ArrayList<>(CACHE.keySet());
        keys.removeAll(USED_CACHED);
        if (keys.isEmpty()) return new SkinResolution(
                () -> DefaultSkinHelper.getSkinTextures(fallback), () -> "Player");
        UUID pick = keys.get(RNG.nextInt(keys.size()));
        USED_CACHED.add(pick);
        CachedProfile c = CACHE.get(pick);
        if (c == null) return new SkinResolution(
                () -> DefaultSkinHelper.getSkinTextures(fallback), () -> "Player");
        PropertyMap props = new PropertyMap(ImmutableMultimap.of(
                "textures", new Property("textures", c.textures, c.signature)));
        GameProfile profile = new GameProfile(pick, c.name, props);
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc == null) return new SkinResolution(
                () -> DefaultSkinHelper.getSkinTextures(fallback), () -> c.name);
        CompletableFuture<java.util.Optional<SkinTextures>> future = mc.getSkinProvider().fetchSkinTextures(profile);
        Supplier<SkinTextures> skin = () -> {
            if (future.isDone()) {
                try {
                    var s = future.getNow(null);
                    if (s != null && s.isPresent()) return s.get();
                } catch (Throwable ignored) {}
            }
            return DefaultSkinHelper.getSkinTextures(fallback);
        };
        return new SkinResolution(skin, () -> c.name);
    }

    private static SkinResolution customSkinSupplier(UUID fallback) {
        return new SkinResolution(() -> {
            OwaCustomSkinSources.tick(Config.steveCustomSkinSources);
            java.util.List<Path> sources = OwaCustomSkinSources.resolved();
            if (!sources.isEmpty()) {
                int idx = Math.floorMod(fallback.hashCode(), sources.size());
                Path picked = sources.get(idx);
                Path capePath = OwaCustomSkinSources.capeForPath(picked);
                SkinTextures s = loadCustomSkinFromPath(picked, capePath);
                if (s != null) return s;
            }
            SkinTextures s = loadCustomSkin();
            if (s != null) return s;
            return DefaultSkinHelper.getSkinTextures(fallback);
        }, () -> {
            java.util.List<Path> sources = OwaCustomSkinSources.resolved();
            if (!sources.isEmpty()) {
                int idx = Math.floorMod(fallback.hashCode(), sources.size());
                String name = OwaCustomSkinSources.nameForPath(sources.get(idx));
                if (name != null) return name;
            }
            return "Custom";
        });
    }

    private static final java.util.Map<Path, Long> CUSTOM_TEX_MTIME = new java.util.concurrent.ConcurrentHashMap<>();
    private static final java.util.Map<Path, Identifier> CUSTOM_TEX_ID = new java.util.concurrent.ConcurrentHashMap<>();

    private static synchronized Identifier owa$ensureTexture(Path path, String namePrefix, boolean processSkin) {
        if (path == null || !Files.exists(path)) return null;
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc == null) return null;
        try {
            long mtime = Files.getLastModifiedTime(path).toMillis();
            Long known = CUSTOM_TEX_MTIME.get(path);
            Identifier cachedId = CUSTOM_TEX_ID.get(path);
            if (cachedId != null && known != null && known == mtime) return cachedId;
            byte[] bytes = Files.readAllBytes(path);
            NativeImage img = NativeImage.read(bytes);
            if (processSkin) img = processLegacySkin(img);
            NativeImageBackedTexture tex = new NativeImageBackedTexture(() -> namePrefix, img);
            String pathHash = Integer.toHexString(path.toString().hashCode());
            Identifier id = Identifier.of("oldwalkinganimation", namePrefix + "_" + pathHash);
            if (cachedId != null) mc.getTextureManager().destroyTexture(cachedId);
            mc.getTextureManager().registerTexture(id, tex);
            CUSTOM_TEX_MTIME.put(path, mtime);
            CUSTOM_TEX_ID.put(path, id);
            return id;
        } catch (IOException e) {
            System.err.println("[OWA] Failed to load texture " + path + ": " + e);
            return null;
        }
    }

    private static SkinTextures loadCustomSkinFromPath(Path skinPath, Path capePath) {
        Identifier skinId = owa$ensureTexture(skinPath, "owa_custom_skin", true);
        if (skinId == null) return null;
        Identifier capeId = capePath == null ? null : owa$ensureTexture(capePath, "owa_custom_cape", false);
        AssetInfo.TextureAssetInfo capeAsset = capeId == null ? null : new AssetInfo.TextureAssetInfo(capeId, capeId);
        return new SkinTextures(
                new AssetInfo.TextureAssetInfo(skinId, skinId),
                capeAsset, null, PlayerSkinType.WIDE, true);
    }

    private static SkinTextures loadCustomSkin() {
        return loadCustomSkinFromPath(customSkinPath, null);
    }

    private static void load() {
        CACHE.clear();
        if (cachePath == null || !Files.exists(cachePath)) return;
        try {
            JsonObject root = JsonParser.parseString(Files.readString(cachePath)).getAsJsonObject();
            if (!root.has("profiles")) return;
            JsonObject profiles = root.getAsJsonObject("profiles");
            for (Map.Entry<String, JsonElement> e : profiles.entrySet()) {
                JsonObject p = e.getValue().getAsJsonObject();
                String name = p.has("name") ? p.get("name").getAsString() : "?";
                String textures = p.get("textures").getAsString();
                String sig = p.has("signature") ? p.get("signature").getAsString() : null;
                try {
                    CACHE.put(UUID.fromString(e.getKey()), new CachedProfile(name, textures, sig));
                } catch (IllegalArgumentException ignored) {}
            }
        } catch (IOException e) {
            System.err.println("[OWA] Failed to load skin cache: " + e);
        }
    }

    private static final Object SAVE_LOCK = new Object();
    private static volatile boolean savePending = false;

    private static void saveAsync() {
        synchronized (SAVE_LOCK) {
            if (savePending) return;
            savePending = true;
        }
        EXEC.execute(() -> {
            synchronized (SAVE_LOCK) { savePending = false; }
            save();
        });
    }

    private static void save() {
        if (cachePath == null) return;
        try {
            JsonObject root = new JsonObject();
            JsonObject profiles = new JsonObject();
            for (Map.Entry<UUID, CachedProfile> e : CACHE.entrySet()) {
                JsonObject p = new JsonObject();
                p.addProperty("name", e.getValue().name);
                p.addProperty("textures", e.getValue().textures);
                if (e.getValue().signature != null) p.addProperty("signature", e.getValue().signature);
                profiles.add(e.getKey().toString(), p);
            }
            root.add("profiles", profiles);
            Files.createDirectories(cachePath.getParent());
            Files.writeString(cachePath, GSON.toJson(root));
        } catch (IOException e) {
            System.err.println("[OWA] Failed to save skin cache: " + e);
        }
    }

    public static int cachedProfileCount() {
        return CACHE.size();
    }

    public static void resetUsed() {
        USED_CACHED.clear();
    }

    @SuppressWarnings("unused")
    private static List<UUID> unused() { return Collections.emptyList(); }
}
*///?} else if neoforge {
/*import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.mojang.authlib.properties.PropertyMap;
import com.mojang.blaze3d.platform.NativeImage;
import net.neoforged.fml.loading.FMLPaths;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.resources.ResourceLocation;
import tecna.oldwalkinganimation.config.Config;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.function.Supplier;

public final class OwaSkins {
    public static final String MODE_RANDOM_DEFAULT = "random_default";
    public static final String MODE_STEVE = "steve";
    public static final String MODE_PLAYER = "player";
    public static final String MODE_RANDOM_CACHED = "random_cached";
    public static final String MODE_CUSTOM = "custom";

    // ResourceLocation static factories were added in 1.21; 1.20.x mojmap only exposes the public ctor.
    //? if >=1.21 {
    private static ResourceLocation rl(String ns, String path) { return ResourceLocation.fromNamespaceAndPath(ns, path); }
    private static ResourceLocation rlVanilla(String path) { return ResourceLocation.withDefaultNamespace(path); }
    //?} else {
    /^private static ResourceLocation rl(String ns, String path) { return new ResourceLocation(ns, path); }
    private static ResourceLocation rlVanilla(String path) { return new ResourceLocation(path); }
    ^///?}

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Executor EXEC = Executors.newFixedThreadPool(2, r -> {
        Thread t = new Thread(r, "OWA-Skins");
        t.setDaemon(true);
        return t;
    });
    private static final Random RNG = new Random();

    private record CachedProfile(String name, String textures, String signature) {}

    private static final Map<UUID, CachedProfile> CACHE = new ConcurrentHashMap<>();
    private static final java.util.Set<UUID> USED_CACHED = java.util.concurrent.ConcurrentHashMap.newKeySet();

    private static ResourceLocation customSkinId;
    private static long customSkinMtime = -1L;

    private static Path cachePath;
    private static Path customSkinPath;

    private OwaSkins() {}

    public static void init(String modId) {
        Path cfg = FMLPaths.CONFIGDIR.get();
        cachePath = cfg.resolve(modId + "-skin-cache.json");
        customSkinPath = cfg.resolve(modId + "-custom-skin.png");
        OwaCustomSkinSources.init(cfg);
        load();
    }

    public static Path customSkinFile() { return customSkinPath; }

    public static void onProfileSeen(GameProfile profile) {
        if (profile == null) return;
        UUID id = profile.getId();
        if (id == null) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc != null && mc.player != null && id.equals(mc.player.getUUID())) return;
        var texturesProps = profile.getProperties().get("textures");
        if (texturesProps.isEmpty()) return;
        Property prop = texturesProps.iterator().next();
        String value = prop.value();
        String sig = prop.signature();
        CachedProfile existing = CACHE.get(id);
        if (existing != null && Objects.equals(existing.textures, value)) return;
        CACHE.put(id, new CachedProfile(profile.getName(), value, sig));
        saveAsync();
    }

    public record SkinResolution(Supplier<PlayerSkin> skin, Supplier<String> name) {}

    public static SkinResolution resolveSkin(UUID steveUuid) {
        String mode = Config.steveSkinMode == null ? MODE_RANDOM_DEFAULT : Config.steveSkinMode;
        switch (mode) {
            case MODE_STEVE: return steveSkinSupplier();
            case MODE_PLAYER: return playerSkinSupplier(steveUuid);
            case MODE_RANDOM_CACHED: return cachedSkinSupplier(steveUuid);
            case MODE_CUSTOM: return customSkinSupplier(steveUuid);
            case MODE_RANDOM_DEFAULT:
            default: return defaultSkinSupplier(steveUuid);
        }
    }

    private static SkinResolution defaultSkinSupplier(UUID steveUuid) {
        PlayerSkin skin = DefaultPlayerSkin.get(steveUuid);
        return new SkinResolution(() -> skin, () -> skinDisplayName(skin));
    }

    private static ResourceLocation classicSteveId;
    private static boolean classicSteveTried;

    private static SkinResolution steveSkinSupplier() {
        PlayerSkin skin = loadClassicSteveSkin();
        if (skin == null) {
            PlayerSkin fallback = new PlayerSkin(
                    rlVanilla("textures/entity/player/wide/steve.png"),
                    null, null, null, PlayerSkin.Model.WIDE, true);
            return new SkinResolution(() -> fallback, () -> "Steve");
        }
        return new SkinResolution(() -> skin, () -> "Steve");
    }

    private static synchronized PlayerSkin loadClassicSteveSkin() {
        if (classicSteveId != null) {
            return new PlayerSkin(classicSteveId, null, null, null, PlayerSkin.Model.WIDE, true);
        }
        if (classicSteveTried) return null;
        classicSteveTried = true;
        Minecraft mc = Minecraft.getInstance();
        if (mc == null) return null;
        ResourceLocation src = rl("oldwalkinganimation", "textures/entity/classic_steve.png");
        var opt = mc.getResourceManager().getResource(src);
        if (opt.isEmpty()) return null;
        try (InputStream in = opt.get().open()) {
            NativeImage img = NativeImage.read(in);
            img = processLegacySkin(img);
            //? if >=1.21.5 {
            DynamicTexture tex = new DynamicTexture(() -> "owa_classic_steve", img);
            //?} else {
            /^DynamicTexture tex = new DynamicTexture(img);
            ^///?}
            ResourceLocation id = rl("oldwalkinganimation", "classic_steve_processed");
            mc.getTextureManager().register(id, tex);
            classicSteveId = id;
            return new PlayerSkin(id, null, null, null, PlayerSkin.Model.WIDE, true);
        } catch (IOException e) {
            System.err.println("[OWA] Failed to load classic Steve skin: " + e);
            return null;
        }
    }

    private static NativeImage processLegacySkin(NativeImage img) {
        int w = img.getWidth();
        int h = img.getHeight();
        if (w != 64 || (h != 32 && h != 64)) return img;
        boolean legacy = h == 32;
        if (!legacy) return img;

        NativeImage out = new NativeImage(64, 64, true);
        out.copyFrom(img);
        img.close();
        out.fillRect(0, 32, 64, 32, 0);
        out.copyRect(4, 16, 16, 32, 4, 4, true, false);
        out.copyRect(8, 16, 16, 32, 4, 4, true, false);
        out.copyRect(0, 20, 24, 32, 4, 12, true, false);
        out.copyRect(4, 20, 16, 32, 4, 12, true, false);
        out.copyRect(8, 20, 8, 32, 4, 12, true, false);
        out.copyRect(12, 20, 16, 32, 4, 12, true, false);
        out.copyRect(44, 16, -8, 32, 4, 4, true, false);
        out.copyRect(48, 16, -8, 32, 4, 4, true, false);
        out.copyRect(40, 20, 0, 32, 4, 12, true, false);
        out.copyRect(44, 20, -8, 32, 4, 12, true, false);
        out.copyRect(48, 20, -16, 32, 4, 12, true, false);
        out.copyRect(52, 20, -8, 32, 4, 12, true, false);
        return out;
    }

    private static SkinResolution playerSkinSupplier(UUID fallback) {
        Supplier<PlayerSkin> skin = () -> {
            Minecraft mc = Minecraft.getInstance();
            if (mc != null && mc.player != null) return mc.player.getSkin();
            return DefaultPlayerSkin.get(fallback);
        };
        Supplier<String> name = () -> {
            Minecraft mc = Minecraft.getInstance();
            if (mc != null && mc.player != null) return mc.player.getGameProfile().getName();
            return "Player";
        };
        return new SkinResolution(skin, name);
    }

    private static String skinDisplayName(PlayerSkin skin) {
        try {
            String path = skin.texture().getPath();
            int slash = path.lastIndexOf('/');
            String last = slash >= 0 ? path.substring(slash + 1) : path;
            if (last.endsWith(".png")) last = last.substring(0, last.length() - 4);
            if (last.isEmpty()) return "Steve";
            return Character.toUpperCase(last.charAt(0)) + last.substring(1);
        } catch (Throwable t) {
            return "Steve";
        }
    }

    private static SkinResolution cachedSkinSupplier(UUID fallback) {
        List<UUID> keys = new ArrayList<>(CACHE.keySet());
        keys.removeAll(USED_CACHED);
        if (keys.isEmpty()) return new SkinResolution(
                () -> DefaultPlayerSkin.get(fallback), () -> "Player");
        UUID pick = keys.get(RNG.nextInt(keys.size()));
        USED_CACHED.add(pick);
        CachedProfile c = CACHE.get(pick);
        if (c == null) return new SkinResolution(
                () -> DefaultPlayerSkin.get(fallback), () -> "Player");
        PropertyMap props = new PropertyMap();
        props.put("textures", new Property("textures", c.textures, c.signature));
        GameProfile profile = new GameProfile(pick, c.name);
        profile.getProperties().putAll(props);
        Minecraft mc = Minecraft.getInstance();
        if (mc == null) return new SkinResolution(
                () -> DefaultPlayerSkin.get(fallback), () -> c.name);
        //? if >=1.21.4 {
        CompletableFuture<java.util.Optional<PlayerSkin>> future = mc.getSkinManager().getOrLoad(profile);
        Supplier<PlayerSkin> skin = () -> {
            if (future.isDone()) {
                try {
                    var s = future.getNow(null);
                    if (s != null && s.isPresent()) return s.get();
                } catch (Throwable ignored) {}
            }
            return DefaultPlayerSkin.get(fallback);
        };
        //?} else {
        /^CompletableFuture<PlayerSkin> future = mc.getSkinManager().getOrLoad(profile);
        Supplier<PlayerSkin> skin = () -> {
            if (future.isDone()) {
                try {
                    PlayerSkin s = future.getNow(null);
                    if (s != null) return s;
                } catch (Throwable ignored) {}
            }
            return DefaultPlayerSkin.get(fallback);
        };
        ^///?}
        return new SkinResolution(skin, () -> c.name);
    }

    private static SkinResolution customSkinSupplier(UUID fallback) {
        return new SkinResolution(() -> {
            OwaCustomSkinSources.tick(Config.steveCustomSkinSources);
            java.util.List<Path> sources = OwaCustomSkinSources.resolved();
            if (!sources.isEmpty()) {
                int idx = Math.floorMod(fallback.hashCode(), sources.size());
                Path picked = sources.get(idx);
                Path capePath = OwaCustomSkinSources.capeForPath(picked);
                PlayerSkin s = loadCustomSkinFromPath(picked, capePath);
                if (s != null) return s;
            }
            PlayerSkin s = loadCustomSkin();
            if (s != null) return s;
            return DefaultPlayerSkin.get(fallback);
        }, () -> {
            java.util.List<Path> sources = OwaCustomSkinSources.resolved();
            if (!sources.isEmpty()) {
                int idx = Math.floorMod(fallback.hashCode(), sources.size());
                String name = OwaCustomSkinSources.nameForPath(sources.get(idx));
                if (name != null) return name;
            }
            return "Custom";
        });
    }

    private static final java.util.Map<Path, Long> CUSTOM_TEX_MTIME = new java.util.concurrent.ConcurrentHashMap<>();
    private static final java.util.Map<Path, ResourceLocation> CUSTOM_TEX_ID = new java.util.concurrent.ConcurrentHashMap<>();

    private static synchronized ResourceLocation owa$ensureTexture(Path path, String namePrefix, boolean processSkin) {
        if (path == null || !Files.exists(path)) return null;
        Minecraft mc = Minecraft.getInstance();
        if (mc == null) return null;
        try {
            long mtime = Files.getLastModifiedTime(path).toMillis();
            Long known = CUSTOM_TEX_MTIME.get(path);
            ResourceLocation cachedId = CUSTOM_TEX_ID.get(path);
            if (cachedId != null && known != null && known == mtime) return cachedId;
            byte[] bytes = Files.readAllBytes(path);
            NativeImage img = NativeImage.read(bytes);
            if (processSkin) img = processLegacySkin(img);
            //? if >=1.21.5 {
            DynamicTexture tex = new DynamicTexture(() -> namePrefix, img);
            //?} else {
            /^DynamicTexture tex = new DynamicTexture(img);
            ^///?}
            String pathHash = Integer.toHexString(path.toString().hashCode());
            ResourceLocation id = rl("oldwalkinganimation", namePrefix + "_" + pathHash);
            if (cachedId != null) mc.getTextureManager().release(cachedId);
            mc.getTextureManager().register(id, tex);
            CUSTOM_TEX_MTIME.put(path, mtime);
            CUSTOM_TEX_ID.put(path, id);
            return id;
        } catch (IOException e) {
            System.err.println("[OWA] Failed to load texture " + path + ": " + e);
            return null;
        }
    }

    private static PlayerSkin loadCustomSkinFromPath(Path skinPath, Path capePath) {
        ResourceLocation skinId = owa$ensureTexture(skinPath, "owa_custom_skin", true);
        if (skinId == null) return null;
        ResourceLocation capeId = capePath == null ? null : owa$ensureTexture(capePath, "owa_custom_cape", false);
        return new PlayerSkin(skinId, null, capeId, null, PlayerSkin.Model.WIDE, true);
    }

    private static PlayerSkin loadCustomSkin() {
        return loadCustomSkinFromPath(customSkinPath, null);
    }

    private static void load() {
        CACHE.clear();
        if (cachePath == null || !Files.exists(cachePath)) return;
        try {
            JsonObject root = JsonParser.parseString(Files.readString(cachePath)).getAsJsonObject();
            if (!root.has("profiles")) return;
            JsonObject profiles = root.getAsJsonObject("profiles");
            for (Map.Entry<String, JsonElement> e : profiles.entrySet()) {
                JsonObject p = e.getValue().getAsJsonObject();
                String name = p.has("name") ? p.get("name").getAsString() : "?";
                String textures = p.get("textures").getAsString();
                String sig = p.has("signature") ? p.get("signature").getAsString() : null;
                try {
                    CACHE.put(UUID.fromString(e.getKey()), new CachedProfile(name, textures, sig));
                } catch (IllegalArgumentException ignored) {}
            }
        } catch (IOException e) {
            System.err.println("[OWA] Failed to load skin cache: " + e);
        }
    }

    private static final Object SAVE_LOCK = new Object();
    private static volatile boolean savePending = false;

    private static void saveAsync() {
        synchronized (SAVE_LOCK) {
            if (savePending) return;
            savePending = true;
        }
        EXEC.execute(() -> {
            synchronized (SAVE_LOCK) { savePending = false; }
            save();
        });
    }

    private static void save() {
        if (cachePath == null) return;
        try {
            JsonObject root = new JsonObject();
            JsonObject profiles = new JsonObject();
            for (Map.Entry<UUID, CachedProfile> e : CACHE.entrySet()) {
                JsonObject p = new JsonObject();
                p.addProperty("name", e.getValue().name);
                p.addProperty("textures", e.getValue().textures);
                if (e.getValue().signature != null) p.addProperty("signature", e.getValue().signature);
                profiles.add(e.getKey().toString(), p);
            }
            root.add("profiles", profiles);
            Files.createDirectories(cachePath.getParent());
            Files.writeString(cachePath, GSON.toJson(root));
        } catch (IOException e) {
            System.err.println("[OWA] Failed to save skin cache: " + e);
        }
    }

    public static int cachedProfileCount() {
        return CACHE.size();
    }

    public static void resetUsed() {
        USED_CACHED.clear();
    }

    @SuppressWarnings("unused")
    private static List<UUID> unused() { return Collections.emptyList(); }
}
*///?} else {
/*import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.mojang.authlib.properties.PropertyMap;
import net.minecraft.client.texture.NativeImage;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.client.util.DefaultSkinHelper;
import net.minecraft.client.util.SkinTextures;
import net.minecraft.util.Identifier;
import tecna.oldwalkinganimation.config.Config;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.function.Supplier;

public final class OwaSkins {
    public static final String MODE_RANDOM_DEFAULT = "random_default";
    public static final String MODE_STEVE = "steve";
    public static final String MODE_PLAYER = "player";
    public static final String MODE_RANDOM_CACHED = "random_cached";
    public static final String MODE_CUSTOM = "custom";

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Executor EXEC = Executors.newFixedThreadPool(2, r -> {
        Thread t = new Thread(r, "OWA-Skins");
        t.setDaemon(true);
        return t;
    });
    private static final Random RNG = new Random();

    private record CachedProfile(String name, String textures, String signature) {}

    private static final Map<UUID, CachedProfile> CACHE = new ConcurrentHashMap<>();
    private static final java.util.Set<UUID> USED_CACHED = java.util.concurrent.ConcurrentHashMap.newKeySet();

    private static Identifier customSkinId;
    private static long customSkinMtime = -1L;

    private static Path cachePath;
    private static Path customSkinPath;

    private OwaSkins() {}

    public static void init(String modId) {
        Path cfg = FabricLoader.getInstance().getConfigDir();
        cachePath = cfg.resolve(modId + "-skin-cache.json");
        customSkinPath = cfg.resolve(modId + "-custom-skin.png");
        OwaCustomSkinSources.init(cfg);
        load();
    }

    public static Path customSkinFile() { return customSkinPath; }

    public static void onProfileSeen(GameProfile profile) {
        if (profile == null) return;
        UUID id = profile.getId();
        if (id == null) return;
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc != null && mc.player != null && id.equals(mc.player.getUuid())) return;
        var texturesProps = profile.getProperties().get("textures");
        if (texturesProps.isEmpty()) return;
        Property prop = texturesProps.iterator().next();
        String value = prop.value();
        String sig = prop.signature();
        CachedProfile existing = CACHE.get(id);
        if (existing != null && Objects.equals(existing.textures, value)) return;
        CACHE.put(id, new CachedProfile(profile.getName(), value, sig));
        saveAsync();
    }

    public record SkinResolution(Supplier<SkinTextures> skin, Supplier<String> name) {}

    public static SkinResolution resolveSkin(UUID steveUuid) {
        String mode = Config.steveSkinMode == null ? MODE_RANDOM_DEFAULT : Config.steveSkinMode;
        switch (mode) {
            case MODE_STEVE: return steveSkinSupplier();
            case MODE_PLAYER: return playerSkinSupplier(steveUuid);
            case MODE_RANDOM_CACHED: return cachedSkinSupplier(steveUuid);
            case MODE_CUSTOM: return customSkinSupplier(steveUuid);
            case MODE_RANDOM_DEFAULT:
            default: return defaultSkinSupplier(steveUuid);
        }
    }

    private static SkinResolution defaultSkinSupplier(UUID steveUuid) {
        SkinTextures skin = DefaultSkinHelper.getSkinTextures(steveUuid);
        return new SkinResolution(() -> skin, () -> skinDisplayName(skin));
    }

    private static Identifier classicSteveId;
    private static boolean classicSteveTried;

    private static SkinResolution steveSkinSupplier() {
        SkinTextures skin = loadClassicSteveSkin();
        if (skin == null) {
            SkinTextures fallback = new SkinTextures(
                    Identifier.of("minecraft", "textures/entity/player/wide/steve.png"),
                    null, null, null, SkinTextures.Model.WIDE, true);
            return new SkinResolution(() -> fallback, () -> "Steve");
        }
        return new SkinResolution(() -> skin, () -> "Steve");
    }

    private static synchronized SkinTextures loadClassicSteveSkin() {
        if (classicSteveId != null) {
            return new SkinTextures(classicSteveId, null, null, null, SkinTextures.Model.WIDE, true);
        }
        if (classicSteveTried) return null;
        classicSteveTried = true;
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc == null) return null;
        Identifier src = Identifier.of("oldwalkinganimation", "textures/entity/classic_steve.png");
        var opt = mc.getResourceManager().getResource(src);
        if (opt.isEmpty()) return null;
        try (InputStream in = opt.get().getInputStream()) {
            NativeImage img = NativeImage.read(in);
            img = processLegacySkin(img);
            //? if >=1.21.5 {
            NativeImageBackedTexture tex = new NativeImageBackedTexture(() -> "owa_classic_steve", img);
            //?} else
            /^NativeImageBackedTexture tex = new NativeImageBackedTexture(img);^/
            Identifier id = Identifier.of("oldwalkinganimation", "classic_steve_processed");
            mc.getTextureManager().registerTexture(id, tex);
            classicSteveId = id;
            return new SkinTextures(id, null, null, null, SkinTextures.Model.WIDE, true);
        } catch (IOException e) {
            System.err.println("[OWA] Failed to load classic Steve skin: " + e);
            return null;
        }
    }

    private static NativeImage processLegacySkin(NativeImage img) {
        int w = img.getWidth();
        int h = img.getHeight();
        if (w != 64 || (h != 32 && h != 64)) return img;
        boolean legacy = h == 32;
        if (!legacy) return img;

        NativeImage out = new NativeImage(64, 64, true);
        out.copyFrom(img);
        img.close();
        out.fillRect(0, 32, 64, 32, 0);
        out.copyRect(4, 16, 16, 32, 4, 4, true, false);
        out.copyRect(8, 16, 16, 32, 4, 4, true, false);
        out.copyRect(0, 20, 24, 32, 4, 12, true, false);
        out.copyRect(4, 20, 16, 32, 4, 12, true, false);
        out.copyRect(8, 20, 8, 32, 4, 12, true, false);
        out.copyRect(12, 20, 16, 32, 4, 12, true, false);
        out.copyRect(44, 16, -8, 32, 4, 4, true, false);
        out.copyRect(48, 16, -8, 32, 4, 4, true, false);
        out.copyRect(40, 20, 0, 32, 4, 12, true, false);
        out.copyRect(44, 20, -8, 32, 4, 12, true, false);
        out.copyRect(48, 20, -16, 32, 4, 12, true, false);
        out.copyRect(52, 20, -8, 32, 4, 12, true, false);
        return out;
    }

    private static SkinResolution playerSkinSupplier(UUID fallback) {
        Supplier<SkinTextures> skin = () -> {
            MinecraftClient mc = MinecraftClient.getInstance();
            if (mc != null && mc.player != null) return mc.player.getSkinTextures();
            return DefaultSkinHelper.getSkinTextures(fallback);
        };
        Supplier<String> name = () -> {
            MinecraftClient mc = MinecraftClient.getInstance();
            if (mc != null && mc.player != null) return mc.player.getGameProfile().getName();
            return "Player";
        };
        return new SkinResolution(skin, name);
    }

    private static String skinDisplayName(SkinTextures skin) {
        try {
            String path = skin.texture().getPath();
            int slash = path.lastIndexOf('/');
            String last = slash >= 0 ? path.substring(slash + 1) : path;
            if (last.endsWith(".png")) last = last.substring(0, last.length() - 4);
            if (last.isEmpty()) return "Steve";
            return Character.toUpperCase(last.charAt(0)) + last.substring(1);
        } catch (Throwable t) {
            return "Steve";
        }
    }

    private static SkinResolution cachedSkinSupplier(UUID fallback) {
        List<UUID> keys = new ArrayList<>(CACHE.keySet());
        keys.removeAll(USED_CACHED);
        if (keys.isEmpty()) return new SkinResolution(
                () -> DefaultSkinHelper.getSkinTextures(fallback), () -> "Player");
        UUID pick = keys.get(RNG.nextInt(keys.size()));
        USED_CACHED.add(pick);
        CachedProfile c = CACHE.get(pick);
        if (c == null) return new SkinResolution(
                () -> DefaultSkinHelper.getSkinTextures(fallback), () -> "Player");
        PropertyMap props = new PropertyMap();
        props.put("textures", new Property("textures", c.textures, c.signature));
        GameProfile profile = new GameProfile(pick, c.name);
        profile.getProperties().putAll(props);
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc == null) return new SkinResolution(
                () -> DefaultSkinHelper.getSkinTextures(fallback), () -> c.name);
        //? if >=1.21.4 {
        CompletableFuture<java.util.Optional<SkinTextures>> future = mc.getSkinProvider().fetchSkinTextures(profile);
        Supplier<SkinTextures> skin = () -> {
            if (future.isDone()) {
                try {
                    var s = future.getNow(null);
                    if (s != null && s.isPresent()) return s.get();
                } catch (Throwable ignored) {}
            }
            return DefaultSkinHelper.getSkinTextures(fallback);
        //?} else {
        /^CompletableFuture<SkinTextures> future = mc.getSkinProvider().fetchSkinTextures(profile);
        Supplier<SkinTextures> skin = () -> {
            if (future.isDone()) {
                try {
                    SkinTextures s = future.getNow(null);
                    if (s != null) return s;
                } catch (Throwable ignored) {}
            }
            return DefaultSkinHelper.getSkinTextures(fallback);
        ^///?}
        };
        return new SkinResolution(skin, () -> c.name);
    }

    private static SkinResolution customSkinSupplier(UUID fallback) {
        return new SkinResolution(() -> {
            OwaCustomSkinSources.tick(Config.steveCustomSkinSources);
            java.util.List<Path> sources = OwaCustomSkinSources.resolved();
            if (!sources.isEmpty()) {
                int idx = Math.floorMod(fallback.hashCode(), sources.size());
                Path picked = sources.get(idx);
                Path capePath = OwaCustomSkinSources.capeForPath(picked);
                SkinTextures s = loadCustomSkinFromPath(picked, capePath);
                if (s != null) return s;
            }
            SkinTextures s = loadCustomSkin();
            if (s != null) return s;
            return DefaultSkinHelper.getSkinTextures(fallback);
        }, () -> {
            java.util.List<Path> sources = OwaCustomSkinSources.resolved();
            if (!sources.isEmpty()) {
                int idx = Math.floorMod(fallback.hashCode(), sources.size());
                String name = OwaCustomSkinSources.nameForPath(sources.get(idx));
                if (name != null) return name;
            }
            return "Custom";
        });
    }

    // Per-path texture cache. mtime-checked so an updated source PNG re-loads automatically.
    private static final java.util.Map<Path, Long> CUSTOM_TEX_MTIME = new java.util.concurrent.ConcurrentHashMap<>();
    private static final java.util.Map<Path, Identifier> CUSTOM_TEX_ID = new java.util.concurrent.ConcurrentHashMap<>();

    private static synchronized Identifier owa$ensureTexture(Path path, String namePrefix, boolean processSkin) {
        if (path == null || !Files.exists(path)) return null;
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc == null) return null;
        try {
            long mtime = Files.getLastModifiedTime(path).toMillis();
            Long known = CUSTOM_TEX_MTIME.get(path);
            Identifier cachedId = CUSTOM_TEX_ID.get(path);
            if (cachedId != null && known != null && known == mtime) return cachedId;
            byte[] bytes = Files.readAllBytes(path);
            NativeImage img = NativeImage.read(bytes);
            if (processSkin) img = processLegacySkin(img);
            //? if >=1.21.5 {
            NativeImageBackedTexture tex = new NativeImageBackedTexture(() -> namePrefix, img);
            //?} else
            /^NativeImageBackedTexture tex = new NativeImageBackedTexture(img);^/
            String pathHash = Integer.toHexString(path.toString().hashCode());
            Identifier id = Identifier.of("oldwalkinganimation", namePrefix + "_" + pathHash);
            if (cachedId != null) mc.getTextureManager().destroyTexture(cachedId);
            mc.getTextureManager().registerTexture(id, tex);
            CUSTOM_TEX_MTIME.put(path, mtime);
            CUSTOM_TEX_ID.put(path, id);
            return id;
        } catch (IOException e) {
            System.err.println("[OWA] Failed to load texture " + path + ": " + e);
            return null;
        }
    }

    private static SkinTextures loadCustomSkinFromPath(Path skinPath, Path capePath) {
        Identifier skinId = owa$ensureTexture(skinPath, "owa_custom_skin", true);
        if (skinId == null) return null;
        Identifier capeId = capePath == null ? null : owa$ensureTexture(capePath, "owa_custom_cape", false);
        return new SkinTextures(skinId, null, capeId, null, SkinTextures.Model.WIDE, true);
    }

    private static SkinTextures loadCustomSkin() {
        return loadCustomSkinFromPath(customSkinPath, null);
    }

    private static void load() {
        CACHE.clear();
        if (cachePath == null || !Files.exists(cachePath)) return;
        try {
            JsonObject root = JsonParser.parseString(Files.readString(cachePath)).getAsJsonObject();
            if (!root.has("profiles")) return;
            JsonObject profiles = root.getAsJsonObject("profiles");
            for (Map.Entry<String, JsonElement> e : profiles.entrySet()) {
                JsonObject p = e.getValue().getAsJsonObject();
                String name = p.has("name") ? p.get("name").getAsString() : "?";
                String textures = p.get("textures").getAsString();
                String sig = p.has("signature") ? p.get("signature").getAsString() : null;
                try {
                    CACHE.put(UUID.fromString(e.getKey()), new CachedProfile(name, textures, sig));
                } catch (IllegalArgumentException ignored) {}
            }
        } catch (IOException e) {
            System.err.println("[OWA] Failed to load skin cache: " + e);
        }
    }

    private static final Object SAVE_LOCK = new Object();
    private static volatile boolean savePending = false;

    private static void saveAsync() {
        synchronized (SAVE_LOCK) {
            if (savePending) return;
            savePending = true;
        }
        EXEC.execute(() -> {
            synchronized (SAVE_LOCK) { savePending = false; }
            save();
        });
    }

    private static void save() {
        if (cachePath == null) return;
        try {
            JsonObject root = new JsonObject();
            JsonObject profiles = new JsonObject();
            for (Map.Entry<UUID, CachedProfile> e : CACHE.entrySet()) {
                JsonObject p = new JsonObject();
                p.addProperty("name", e.getValue().name);
                p.addProperty("textures", e.getValue().textures);
                if (e.getValue().signature != null) p.addProperty("signature", e.getValue().signature);
                profiles.add(e.getKey().toString(), p);
            }
            root.add("profiles", profiles);
            Files.createDirectories(cachePath.getParent());
            Files.writeString(cachePath, GSON.toJson(root));
        } catch (IOException e) {
            System.err.println("[OWA] Failed to save skin cache: " + e);
        }
    }

    public static int cachedProfileCount() {
        return CACHE.size();
    }

    public static void resetUsed() {
        USED_CACHED.clear();
    }

    @SuppressWarnings("unused")
    private static List<UUID> unused() { return Collections.emptyList(); }
}
*///?}
