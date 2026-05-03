package tecna.oldwalkinganimation.bundle;

import net.neoforged.neoforgespi.locating.IDependencyLocator;
import net.neoforged.neoforgespi.locating.IDiscoveryPipeline;
import net.neoforged.neoforgespi.locating.IModFile;
import net.neoforged.neoforgespi.locating.IncompatibleFileReporting;
import net.neoforged.neoforgespi.locating.ModFileDiscoveryAttributes;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Picks the per-MC neoforge inner jar at runtime so all NeoForge versions
 * can ship in a single bundle. Reflection is used for the FMLLoader version
 * accessor since the API differs between FML 4.x and 11.x.
 */
public final class OwaBundleLocator implements IDependencyLocator {

    // FML 1.x/2.x/3.x (NeoForge 20.2-20.6) IDependencyLocator extends IModProvider which has
    // these abstract methods. FML 9.x/11.x (NeoForge 21.x+) dropped IModProvider and uses
    // IOrderedProvider with a different scanMods. We declare both signatures so the locator class
    // satisfies whichever interface FML resolves at runtime. The new-API scanMods(List,
    // IDiscoveryPipeline) is in this same class below; both call into shared helpers.
    public void initArguments(java.util.Map<String, ?> arguments) {}
    public String name() { return "owabundle"; }
    public boolean isValid(IModFile modFile) { return modFile != null; }
    public void scanFile(IModFile modFile, java.util.function.Consumer<Path> consumer) {
        try {
            // Mirror AbstractJarFileModProvider.scanFile: walk the SecureJar root, peek paths
            // through the consumer (FML's scanner ASM-reads each one) and use verifyPath to
            // compute the security status. Filter to *.class only - FML's scanner trips
            // "Unsupported class file major version" on assets/lang JSON/PNGs.
            Object sj = modFile.getClass().getMethod("getSecureJar").invoke(modFile);
            Path root = (Path) sj.getClass().getMethod("getRootPath").invoke(sj);
            java.lang.reflect.Method verifyPath = sj.getClass().getMethod("verifyPath", Path.class);
            Class<?> statusCls = Class.forName("cpw.mods.jarhandling.SecureJar$Status");
            java.lang.reflect.Method ordinal = statusCls.getMethod("ordinal");
            Object[] worst = {null};
            try (java.util.stream.Stream<Path> s = Files.find(root, Integer.MAX_VALUE,
                    (p, a) -> p.getNameCount() > 0 && p.getFileName().toString().endsWith(".class"))) {
                s.forEach(p -> {
                    consumer.accept(p);
                    try {
                        Object st = verifyPath.invoke(sj, p);
                        if (worst[0] == null || (int) ordinal.invoke(st) < (int) ordinal.invoke(worst[0])) {
                            worst[0] = st;
                        }
                    } catch (Throwable ignored) {}
                });
            }
            if (worst[0] != null) {
                try {
                    java.lang.reflect.Method setStatus = modFile.getClass().getMethod("setSecurityStatus", statusCls);
                    setStatus.invoke(modFile, worst[0]);
                } catch (Throwable ignored) {}
            }
        } catch (Throwable ignored) {
            // FML 11.x never calls scanFile on us; nothing to do.
        }
    }

    // FML 1.x/2.x/3.x SPI: extract our matching inner jar and return it as a freshly-built
    // IModFile so FML treats it as a discovered mod. We mirror AbstractModProvider.createMod via
    // reflection: JarContentsBuilder -> ModJarMetadata -> SecureJar.from(jarContents, mjm) ->
    // ModFileFactory.build -> mjm.setModFile(mod). The ModJarMetadata link is what makes the
    // SecureJar's module name match info.getOwningFile().moduleName() so FMLModContainer's
    // gameLayer.findModule(...) succeeds.
    public java.util.List<Object> scanMods(Iterable<?> loadedMods) {
        try {
            System.out.println("[OWA-Bundle] (old-SPI) scanMods invoked");
            Path selfPath = findSelfPathFromIterable(loadedMods);
            if (selfPath == null) selfPath = findSelfPathByModsDir();
            if (selfPath == null) {
                System.err.println("[OWA-Bundle] (old-SPI) Could not locate own jar path");
                return java.util.Collections.emptyList();
            }
            String mcVersion = detectMcVersionFromIterable(loadedMods);
            if (mcVersion == null) mcVersion = detectMcVersion();
            if (mcVersion == null) {
                System.err.println("[OWA-Bundle] (old-SPI) Could not detect MC version");
                return java.util.Collections.emptyList();
            }
            String entry = lookupInnerJarFromIndex(selfPath, mcVersion);
            if (entry == null) entry = INNER_DIR + mcVersion + "-neoforge.jar";
            Path innerJar = extract(selfPath, entry);
            if (innerJar == null) {
                System.err.println("[OWA-Bundle] (old-SPI) No inner jar for MC " + mcVersion);
                return java.util.Collections.emptyList();
            }
            System.out.println("[OWA-Bundle] (old-SPI) Extracted inner jar -> " + innerJar);

            Class<?> jcbCls = Class.forName("cpw.mods.jarhandling.JarContentsBuilder");
            Object jcb = jcbCls.getDeclaredConstructor().newInstance();
            jcbCls.getMethod("paths", Path[].class).invoke(jcb, (Object) new Path[]{innerJar});
            Object jarContents = jcbCls.getMethod("build").invoke(jcb);

            Class<?> mjmCls = Class.forName("net.neoforged.fml.loading.moddiscovery.ModJarMetadata");
            Class<?> jcCls = Class.forName("cpw.mods.jarhandling.JarContents");
            java.lang.reflect.Constructor<?> mjmCtor = mjmCls.getDeclaredConstructor(jcCls);
            mjmCtor.setAccessible(true);
            Object mjm = mjmCtor.newInstance(jarContents);

            Class<?> sjCls = Class.forName("cpw.mods.jarhandling.SecureJar");
            Class<?> jmCls = Class.forName("cpw.mods.jarhandling.JarMetadata");
            Object secureJar = sjCls.getMethod("from", jcCls, jmCls).invoke(null, jarContents, mjm);

            Class<?> mffCls = Class.forName("net.neoforged.neoforgespi.locating.ModFileFactory");
            Object factory = mffCls.getField("FACTORY").get(null);
            Class<?> mfipCls = Class.forName("net.neoforged.neoforgespi.locating.ModFileFactory$ModFileInfoParser");
            Class<?> imfCls = Class.forName("net.neoforged.neoforgespi.locating.IModFile");
            Class<?> mfpCls = Class.forName("net.neoforged.fml.loading.moddiscovery.ModFileParser");
            java.lang.reflect.Method tomlParser = mfpCls.getMethod("modsTomlParser", imfCls);
            Object parser = java.lang.reflect.Proxy.newProxyInstance(
                    mfipCls.getClassLoader(), new Class<?>[]{mfipCls},
                    (proxy, method, args) -> tomlParser.invoke(null, args[0]));
            Class<?> impCls = Class.forName("net.neoforged.neoforgespi.locating.IModProvider");
            java.lang.reflect.Method buildMethod = mffCls.getMethod("build", sjCls, impCls, mfipCls);
            Object modFile = buildMethod.invoke(factory, secureJar, this, parser);

            java.lang.reflect.Method setModFile = mjmCls.getMethod("setModFile", imfCls);
            setModFile.invoke(mjm, modFile);

            return java.util.Collections.singletonList(modFile);
        } catch (Throwable t) {
            Throwable cause = t;
            while (cause.getCause() != null && cause.getCause() != cause) cause = cause.getCause();
            System.err.println("[OWA-Bundle] (old-SPI) scanMods failed: " + t + " (root cause: " + cause + ")");
            cause.printStackTrace();
            return java.util.Collections.emptyList();
        }
    }

    private static Path findSelfPathFromIterable(Iterable<?> loadedMods) {
        if (loadedMods == null) return null;
        for (Object mf : loadedMods) {
            try {
                if (mf == null || mf.getClass().getMethod("getModFileInfo").invoke(mf) == null) continue;
                Object info = mf.getClass().getMethod("getModFileInfo").invoke(mf);
                Object mods = info.getClass().getMethod("getMods").invoke(info);
                for (Object modInfo : (Iterable<?>) mods) {
                    Object id = modInfo.getClass().getMethod("getModId").invoke(modInfo);
                    if (OUTER_MOD_ID.equals(id)) {
                        return (Path) mf.getClass().getMethod("getFilePath").invoke(mf);
                    }
                }
            } catch (Throwable ignored) {}
        }
        return null;
    }

    private static Path findSelfPathByModsDir() {
        try {
            Class<?> pathsCls = Class.forName(FML_PATHS);
            Object modsDirEnum = pathsCls.getField("MODSDIR").get(null);
            Path modsDir = (Path) pathsCls.getMethod("get").invoke(modsDirEnum);
            if (modsDir != null && Files.isDirectory(modsDir)) {
                try (DirectoryStream<Path> ds = Files.newDirectoryStream(modsDir, BUNDLE_PREFIX + "*.jar")) {
                    for (Path p : ds) {
                        if (Files.isRegularFile(p) && jarHasInnerDir(p)) return p;
                    }
                }
            }
        } catch (Throwable ignored) {}
        return null;
    }

    private static String detectMcVersionFromIterable(Iterable<?> loadedMods) {
        if (loadedMods == null) return null;
        for (Object mf : loadedMods) {
            try {
                Object info = mf.getClass().getMethod("getModFileInfo").invoke(mf);
                if (info == null) continue;
                Object mods = info.getClass().getMethod("getMods").invoke(info);
                for (Object modInfo : (Iterable<?>) mods) {
                    Object id = modInfo.getClass().getMethod("getModId").invoke(modInfo);
                    if (!"minecraft".equals(id)) continue;
                    Object version = modInfo.getClass().getMethod("getVersion").invoke(modInfo);
                    if (version != null) {
                        String s = version.toString();
                        if (s != null && !s.isEmpty()) return s;
                    }
                }
            } catch (Throwable ignored) {}
        }
        return null;
    }

    private static final String OUTER_MOD_ID = "oldwalkinganimation_bundle";
    private static final String INNER_DIR = "META-INF/owa-jars/";
    private static final String FML_LOADER = "net.neoforged.fml.loading.FMLLoader";
    private static final String FML_PATHS = "net.neoforged.fml.loading.FMLPaths";
    private static final String BUNDLE_PREFIX = "OldWalkingAnimation-";
    private static final String INNER_SUFFIX = "-neoforge.jar";

    @Override
    public void scanMods(List<IModFile> loadedMods, IDiscoveryPipeline pipeline) {
        System.out.println("[OWA-Bundle] OwaBundleLocator.scanMods invoked; loadedMods=" + loadedMods.size());

        Path selfPath = findSelfPath(loadedMods);
        if (selfPath == null) {
            System.err.println("[OWA-Bundle] Could not locate own jar path; skipping bundle.");
            return;
        }
        System.out.println("[OWA-Bundle] Self path: " + selfPath);

        String mcVersion = detectMcVersionFromLoadedMods(loadedMods);
        if (mcVersion == null) mcVersion = detectMcVersion();
        if (mcVersion == null) {
            System.err.println("[OWA-Bundle] Could not detect Minecraft version; skipping bundle.");
            return;
        }
        System.out.println("[OWA-Bundle] Detected MC version: " + mcVersion);

        // First check the bundle index (newer combined bundles dedup identical-class jars and
        // map MC versions to a covering jar in META-INF/owa-jars/bundle-index.json). Fall back to
        // the legacy per-MC naming for older bundles.
        String entry = lookupInnerJarFromIndex(selfPath, mcVersion);
        if (entry == null) entry = INNER_DIR + mcVersion + "-neoforge.jar";
        System.out.println("[OWA-Bundle] Inner jar entry: " + entry);

        Path tempJar;
        try {
            tempJar = extract(selfPath, entry);
        } catch (IOException e) {
            System.err.println("[OWA-Bundle] Failed to extract " + entry + ": " + e);
            return;
        }
        if (tempJar == null) {
            System.err.println("[OWA-Bundle] No inner jar for MC " + mcVersion
                + " (looked for " + entry + " inside " + selfPath + ")");
            return;
        }

        System.out.println("[OWA-Bundle] Extracted inner jar -> " + tempJar);
        pipeline.addPath(tempJar, ModFileDiscoveryAttributes.DEFAULT, IncompatibleFileReporting.WARN_ALWAYS);
    }

    // Reads META-INF/owa-jars/bundle-index.json and returns the entry path covering the given MC
    // version, or null if no index exists or no entry matches.
    private static String lookupInnerJarFromIndex(Path bundleJar, String mcVersion) {
        try (ZipFile zf = new ZipFile(bundleJar.toFile())) {
            ZipEntry idxEntry = zf.getEntry(INNER_DIR + "bundle-index.json");
            if (idxEntry == null) return null;
            String json;
            try (InputStream in = zf.getInputStream(idxEntry)) {
                json = new String(in.readAllBytes(), java.nio.charset.StandardCharsets.UTF_8);
            }
            // Index shape: {"entries":[{"name":"1.21_to_1.21.1-neoforge.jar","mcVersions":["1.21","1.21.1"]}, ...]}
            // Avoid pulling a JSON dep - parse with a tiny scanner.
            int entriesIdx = json.indexOf("\"entries\"");
            if (entriesIdx < 0) return null;
            int cursor = entriesIdx;
            while (true) {
                int nameIdx = json.indexOf("\"name\"", cursor);
                if (nameIdx < 0) return null;
                int q1 = json.indexOf('"', json.indexOf(':', nameIdx) + 1);
                int q2 = json.indexOf('"', q1 + 1);
                String name = json.substring(q1 + 1, q2);

                int mcIdx = json.indexOf("\"mcVersions\"", q2);
                int br1 = json.indexOf('[', mcIdx);
                int br2 = json.indexOf(']', br1);
                String list = json.substring(br1 + 1, br2);
                cursor = br2;
                for (String tok : list.split(",")) {
                    String v = tok.trim();
                    if (v.startsWith("\"") && v.endsWith("\"")) v = v.substring(1, v.length() - 1);
                    if (v.equals(mcVersion)) return INNER_DIR + name;
                }
            }
        } catch (IOException e) {
            return null;
        }
    }

    private static Path findSelfPath(List<IModFile> loadedMods) {
        // 1) Loaded-mods scan - works in production where ModsFolderLocator already discovered the outer.
        for (IModFile mf : loadedMods) {
            if (mf.getModFileInfo() == null) continue;
            for (var info : mf.getModFileInfo().getMods()) {
                if (OUTER_MOD_ID.equals(info.getModId())) {
                    return mf.getFilePath();
                }
            }
        }
        // 2) Scan FMLPaths.MODSDIR for our bundle by name - works in dev mode where the bundle is
        //    only registered as a service jar, not as a mod, so it never appears in loadedMods.
        //    Accept any OldWalkingAnimation-*.jar that actually contains an inner-jars directory,
        //    so this works for both the neoforge-only bundle and the combined fabric+neoforge bundle.
        try {
            Class<?> pathsCls = Class.forName(FML_PATHS);
            Object modsDirEnum = pathsCls.getField("MODSDIR").get(null);
            Path modsDir = (Path) pathsCls.getMethod("get").invoke(modsDirEnum);
            if (modsDir != null && Files.isDirectory(modsDir)) {
                try (DirectoryStream<Path> ds = Files.newDirectoryStream(modsDir, BUNDLE_PREFIX + "*.jar")) {
                    for (Path p : ds) {
                        if (!Files.isRegularFile(p)) continue;
                        if (jarHasInnerDir(p)) return p;
                    }
                }
            }
        } catch (Throwable ignored) {
        }
        return null;
    }

    private static boolean jarHasInnerDir(Path jar) {
        try (ZipFile zf = new ZipFile(jar.toFile())) {
            // any entry under META-INF/owa-jars/ is enough to identify our bundle
            return zf.stream().anyMatch(e -> e.getName().startsWith(INNER_DIR));
        } catch (IOException e) {
            return false;
        }
    }

    private static String detectMcVersion() {
        // 1) FMLLoader reflection - covers most FML versions where the API is reachable.
        String v = detectMcVersionViaFmlLoader();
        if (v != null) { System.out.println("[OWA-Bundle] mcVersion via FMLLoader: " + v); return v; }
        // 2) Process arguments - FML always launches with `--fml.mcVersion <X>`.
        v = detectMcVersionViaProcessArgs();
        if (v != null) { System.out.println("[OWA-Bundle] mcVersion via ProcessHandle: " + v); return v; }
        // 3) /proc/self/cmdline - Linux-only fallback when ProcessHandle.info().arguments() is empty.
        v = detectMcVersionViaProcCmdline();
        if (v != null) { System.out.println("[OWA-Bundle] mcVersion via /proc/self/cmdline: " + v); return v; }
        return null;
    }

    private static String detectMcVersionViaProcCmdline() {
        try {
            Path cmdline = java.nio.file.Paths.get("/proc/self/cmdline");
            if (!Files.exists(cmdline)) return null;
            byte[] bytes = Files.readAllBytes(cmdline);
            String raw = new String(bytes, java.nio.charset.StandardCharsets.UTF_8);
            String[] args = raw.split("\0");
            for (int i = 0; i < args.length; i++) {
                String a = args[i];
                if ("--fml.mcVersion".equals(a) && i + 1 < args.length) return args[i + 1];
                if (a.startsWith("--fml.mcVersion=")) return a.substring("--fml.mcVersion=".length());
            }
        } catch (Throwable ignored) {
        }
        return null;
    }

    private static String detectMcVersionFromLoadedMods(List<IModFile> loadedMods) {
        for (IModFile mf : loadedMods) {
            if (mf.getModFileInfo() == null) continue;
            for (var info : mf.getModFileInfo().getMods()) {
                if (!"minecraft".equals(info.getModId())) continue;
                try {
                    // info.getVersion() returns ArtifactVersion (maven-artifact) which isn't on our
                    // compile classpath; reflect to call toString() so we don't need that dep.
                    Object v = info.getClass().getMethod("getVersion").invoke(info);
                    if (v != null) {
                        String s = v.toString();
                        if (s != null && !s.isEmpty()) return s;
                    }
                } catch (Throwable ignored) {
                }
            }
        }
        return null;
    }

    private static String detectMcVersionViaFmlLoader() {
        try {
            Class<?> loaderCls = Class.forName(FML_LOADER);
            Object versionInfo;
            try {
                Method m = loaderCls.getMethod("versionInfo");
                versionInfo = m.invoke(null);
            } catch (NoSuchMethodException e) {
                Method getCurrent;
                try {
                    getCurrent = loaderCls.getMethod("getCurrentOrNull");
                } catch (NoSuchMethodException e2) {
                    getCurrent = loaderCls.getMethod("getCurrent");
                }
                Object current = getCurrent.invoke(null);
                if (current == null) return null;
                Method getVI = current.getClass().getMethod("getVersionInfo");
                versionInfo = getVI.invoke(current);
            }
            if (versionInfo == null) return null;
            Method mc = versionInfo.getClass().getMethod("mcVersion");
            return (String) mc.invoke(versionInfo);
        } catch (Throwable t) {
            return null;
        }
    }

    private static String detectMcVersionViaProcessArgs() {
        try {
            java.util.Optional<String[]> argsOpt = ProcessHandle.current().info().arguments();
            if (argsOpt.isEmpty()) {
                System.out.println("[OWA-Bundle] ProcessHandle returned no arguments");
                return null;
            }
            String[] args = argsOpt.get();
            for (int i = 0; i < args.length; i++) {
                String a = args[i];
                if ("--fml.mcVersion".equals(a) && i + 1 < args.length) return args[i + 1];
                if (a.startsWith("--fml.mcVersion=")) return a.substring("--fml.mcVersion=".length());
            }
        } catch (Throwable t) {
            System.out.println("[OWA-Bundle] ProcessHandle args lookup failed: " + t);
        }
        return null;
    }

    private static Path extract(Path outerJar, String entryName) throws IOException {
        try (ZipFile zf = new ZipFile(outerJar.toFile())) {
            ZipEntry ze = zf.getEntry(entryName);
            if (ze == null) return null;
            Path tempDir = Files.createTempDirectory("owa-bundle");
            Path out = tempDir.resolve(entryName.substring(entryName.lastIndexOf('/') + 1));
            try (InputStream in = zf.getInputStream(ze)) {
                Files.copy(in, out, StandardCopyOption.REPLACE_EXISTING);
            }
            out.toFile().deleteOnExit();
            tempDir.toFile().deleteOnExit();
            return out;
        }
    }
}
