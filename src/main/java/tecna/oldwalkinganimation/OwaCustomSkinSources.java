package tecna.oldwalkinganimation;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Resolves the user's comma-separated `steveCustomSkinSources` config string into a list of
 * cached skin texture files that {@code OwaSkins#customSkinSupplier} can hand out to Steves.
 *
 * <p>Each entry is one of:
 * <ul>
 *   <li><b>HTTP/HTTPS URL</b>: downloaded directly to the cache directory.</li>
 *   <li><b>Player name</b>: looked up via Mojang's name->UUID API, then session API to get the
 *       skin texture URL, which is downloaded.</li>
 * </ul>
 *
 * <p>Resolution is async; entries return the previously-cached file while a refresh is in
 * progress, so the picker never blocks the render thread. Each entry is re-checked every
 * {@link #REFRESH_TTL}, so external updates to a player's skin propagate without a restart.
 */
public final class OwaCustomSkinSources {
    private static final Duration REFRESH_TTL = Duration.ofMinutes(15);
    private static final HttpClient HTTP = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(8))
            .followRedirects(HttpClient.Redirect.NORMAL)
            .build();
    private static final Executor EXEC = Executors.newFixedThreadPool(2, r -> {
        Thread t = new Thread(r, "OWA-SkinSources");
        t.setDaemon(true);
        return t;
    });

    private static final Map<String, Entry> ENTRIES = new ConcurrentHashMap<>();
    private static final AtomicReference<List<Path>> RESOLVED = new AtomicReference<>(List.of());
    private static volatile String lastConfigString = "";
    private static Path cacheDir;

    private static final class Entry {
        final String source;
        volatile Path file;          // cached skin PNG, null until first fetch succeeds
        volatile Path capeFile;      // cached cape PNG, null when entry has no cape
        volatile String displayName; // canonical player name (Mojang-cased) for name entries
        volatile long lastFetchedAt; // System.currentTimeMillis()
        volatile boolean fetching;
        Entry(String source) { this.source = source; }
    }

    private OwaCustomSkinSources() {}

    /** Initialize with the mod's config dir; called once on mod init. */
    public static void init(Path configDir) {
        cacheDir = configDir.resolve("oldwalkinganimation-custom-skins");
        try { Files.createDirectories(cacheDir); } catch (IOException ignored) {}
    }

    /**
     * Returns the current list of resolved skin files. May be empty during initial fetch.
     * Triggers async refresh of any stale entries as a side effect.
     */
    public static List<Path> resolved() {
        return RESOLVED.get();
    }

    /** Returns the canonical Mojang-cased player name for a resolved skin file, or null. */
    public static String nameForPath(Path file) {
        if (file == null) return null;
        for (Entry e : ENTRIES.values()) {
            if (file.equals(e.file)) return e.displayName;
        }
        return null;
    }

    /** Returns the cached cape PNG for a resolved skin file, or null when none. */
    public static Path capeForPath(Path file) {
        if (file == null) return null;
        for (Entry e : ENTRIES.values()) {
            if (file.equals(e.file)) return e.capeFile;
        }
        return null;
    }

    /**
     * Reparse the config string if it changed; trigger refresh of stale entries. Cheap to call
     * each tick - bails out fast when nothing changed.
     */
    public static void tick(String configValue) {
        if (cacheDir == null) return;
        String cfg = configValue == null ? "" : configValue.trim();
        if (!cfg.equals(lastConfigString)) {
            reparse(cfg);
            lastConfigString = cfg;
        }
        long now = System.currentTimeMillis();
        long ttl = REFRESH_TTL.toMillis();
        for (Entry e : ENTRIES.values()) {
            if (e.fetching) continue;
            if (e.file != null && (now - e.lastFetchedAt) < ttl) continue;
            e.fetching = true;
            EXEC.execute(() -> fetchEntry(e));
        }
    }

    private static void reparse(String cfg) {
        java.util.Set<String> wanted = new java.util.LinkedHashSet<>();
        for (String raw : cfg.split(",")) {
            String s = raw.trim();
            if (!s.isEmpty()) wanted.add(s);
        }
        ENTRIES.keySet().retainAll(wanted);
        for (String s : wanted) ENTRIES.computeIfAbsent(s, Entry::new);
        rebuildResolvedList();
    }

    private static void rebuildResolvedList() {
        List<Path> out = new ArrayList<>(ENTRIES.size());
        for (Entry e : ENTRIES.values()) if (e.file != null) out.add(e.file);
        RESOLVED.set(Collections.unmodifiableList(out));
    }

    private static void fetchEntry(Entry e) {
        try {
            if (looksLikeUrl(e.source)) {
                Path target = downloadSkinFromUrl(e.source, hashKey("url:" + e.source));
                if (target != null) e.file = target;
                // URL entries have no name/cape attached.
                e.displayName = null;
                e.capeFile = null;
            } else {
                fetchPlayerNameEntry(e);
            }
            e.lastFetchedAt = System.currentTimeMillis();
            rebuildResolvedList();
        } catch (Exception ex) {
            System.err.println("[OWA] custom skin fetch failed for '" + e.source + "': " + ex);
        } finally {
            e.fetching = false;
        }
    }

    private static void fetchPlayerNameEntry(Entry e) throws IOException, InterruptedException {
        String name = e.source;

        HttpResponse<String> r1 = HTTP.send(
                HttpRequest.newBuilder(URI.create("https://api.mojang.com/users/profiles/minecraft/" + name))
                        .header("User-Agent", "OldWalkingAnimation")
                        .timeout(Duration.ofSeconds(15))
                        .GET().build(),
                HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        if (r1.statusCode() == 404 || r1.statusCode() == 204) {
            throw new IOException("no such player: " + name);
        }
        if (r1.statusCode() / 100 != 2) {
            throw new IOException("name->uuid HTTP " + r1.statusCode() + " for " + name);
        }
        String uuidHex = jsonString(r1.body(), "id");
        String canonicalName = jsonString(r1.body(), "name");
        if (uuidHex == null) throw new IOException("name->uuid: missing id field");

        HttpResponse<String> r2 = HTTP.send(
                HttpRequest.newBuilder(URI.create("https://sessionserver.mojang.com/session/minecraft/profile/" + uuidHex + "?unsigned=true"))
                        .header("User-Agent", "OldWalkingAnimation")
                        .timeout(Duration.ofSeconds(15))
                        .GET().build(),
                HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        if (r2.statusCode() / 100 != 2) {
            throw new IOException("uuid->profile HTTP " + r2.statusCode() + " for " + name);
        }
        String b64 = extractTexturesValue(r2.body());
        if (b64 == null) throw new IOException("uuid->profile: missing textures property");
        String texturesJson = new String(Base64.getDecoder().decode(b64), StandardCharsets.UTF_8);
        String texturesField = jsonObjectField(texturesJson, "textures");
        String skinUrl = jsonString(jsonObjectField(texturesField, "SKIN"), "url");
        String capeUrl = jsonString(jsonObjectField(texturesField, "CAPE"), "url");
        if (skinUrl == null) throw new IOException("uuid->profile: no SKIN.url");

        String key = name.toLowerCase(Locale.ROOT);
        Path skinPath = downloadSkinFromUrl(skinUrl, hashKey("name:" + key));
        e.file = skinPath;
        e.displayName = canonicalName != null ? canonicalName : name;
        if (capeUrl != null) {
            try {
                e.capeFile = downloadSkinFromUrl(capeUrl, hashKey("cape:" + key));
            } catch (Exception capeEx) {
                System.err.println("[OWA] cape fetch failed for '" + name + "': " + capeEx);
                e.capeFile = null;
            }
        } else {
            e.capeFile = null;
        }
    }

    private static boolean looksLikeUrl(String s) {
        String l = s.toLowerCase(Locale.ROOT);
        return l.startsWith("http://") || l.startsWith("https://");
    }

    private static Path downloadSkinFromUrl(String url, String key) throws IOException, InterruptedException {
        Path dst = cacheDir.resolve(key + ".png");
        HttpRequest req = HttpRequest.newBuilder(URI.create(url))
                .header("User-Agent", "OldWalkingAnimation")
                .timeout(Duration.ofSeconds(15))
                .GET().build();
        HttpResponse<InputStream> resp = HTTP.send(req, HttpResponse.BodyHandlers.ofInputStream());
        if (resp.statusCode() / 100 != 2) {
            throw new IOException("HTTP " + resp.statusCode() + " for " + url);
        }
        try (InputStream in = resp.body()) {
            Files.copy(in, dst, StandardCopyOption.REPLACE_EXISTING);
        }
        return dst;
    }

    // Tiny no-deps JSON extractors. Skin payloads are simple flat objects so we don't need a
    // full parser, and pulling in Gson here would force a Mojang-API dep at compile time on
    // versions where the package path differs.
    private static String jsonString(String json, String key) {
        if (json == null) return null;
        int k = json.indexOf("\"" + key + "\"");
        if (k < 0) return null;
        int colon = json.indexOf(':', k);
        if (colon < 0) return null;
        int q1 = json.indexOf('"', colon + 1);
        if (q1 < 0) return null;
        int q2 = q1 + 1;
        while (q2 < json.length()) {
            char c = json.charAt(q2);
            if (c == '\\') { q2 += 2; continue; }
            if (c == '"') break;
            q2++;
        }
        return json.substring(q1 + 1, q2);
    }

    private static String jsonObjectField(String json, String key) {
        if (json == null) return null;
        int k = json.indexOf("\"" + key + "\"");
        if (k < 0) return null;
        int colon = json.indexOf(':', k);
        if (colon < 0) return null;
        int brace = json.indexOf('{', colon);
        if (brace < 0) return null;
        int depth = 0;
        int end = brace;
        while (end < json.length()) {
            char c = json.charAt(end);
            if (c == '{') depth++;
            else if (c == '}') { depth--; if (depth == 0) { end++; break; } }
            end++;
        }
        return json.substring(brace, end);
    }

    private static String extractTexturesValue(String profileJson) {
        // Walk to properties array, find {"name":"textures","value":"..."}.
        int idx = profileJson.indexOf("\"properties\"");
        if (idx < 0) return null;
        int end = profileJson.indexOf(']', idx);
        if (end < 0) return null;
        String props = profileJson.substring(idx, end);
        int t = props.indexOf("\"textures\"");
        if (t < 0) return null;
        return jsonString(props.substring(t), "value");
    }

    private static String hashKey(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            byte[] h = md.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(40);
            for (byte b : h) sb.append(String.format("%02x", b));
            return sb.substring(0, 16);
        } catch (Exception ex) {
            return Integer.toHexString(Objects.hashCode(input));
        }
    }
}
