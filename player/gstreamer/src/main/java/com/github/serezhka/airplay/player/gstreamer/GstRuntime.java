package com.github.serezhka.airplay.player.gstreamer;

import com.sun.jna.platform.win32.Kernel32;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

final class GstRuntime {

    private GstRuntime() {
    }

    static Path resolve(Path appHome) {
        String override = System.getProperty("airplay.gstreamer");
        Path root = override == null || override.isBlank()
                ? appHome.resolve("runtime/gstreamer/1.0/msvc_x86_64")
                : Path.of(override);
        root = root.toAbsolutePath().normalize();

        Path library = root.resolve("bin/gstreamer-1.0-0.dll");
        Path plugins = root.resolve("lib/gstreamer-1.0");
        if (Files.notExists(library) || !Files.isDirectory(plugins)) {
            throw new IllegalStateException("Bundled GStreamer is incomplete: " + root);
        }
        return root;
    }

    static void configureEnvironment(Path appHome, Path root) {
        Path bin = root.resolve("bin");
        Path plugins = root.resolve("lib/gstreamer-1.0");
        Path registry = appHome.resolve("cache/gstreamer-registry.bin");
        String systemPath = System.getenv("PATH");
        String combinedPath = systemPath == null || systemPath.isBlank()
                ? bin.toString()
                : bin + File.pathSeparator + systemPath;

        System.setProperty("jna.library.path", bin.toString());
        Kernel32.INSTANCE.SetEnvironmentVariable("PATH", combinedPath);
        Kernel32.INSTANCE.SetEnvironmentVariable("GST_PLUGIN_PATH", plugins.toString());
        Kernel32.INSTANCE.SetEnvironmentVariable("GST_REGISTRY", registry.toString());
    }
}
