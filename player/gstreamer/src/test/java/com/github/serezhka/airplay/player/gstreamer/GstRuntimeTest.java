package com.github.serezhka.airplay.player.gstreamer;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GstRuntimeTest {

    @TempDir
    Path temp;

    @Test
    void resolvesCompleteBundledRuntime() throws Exception {
        Path root = temp.resolve("runtime/gstreamer/1.0/msvc_x86_64");
        Files.createDirectories(root.resolve("bin"));
        Files.createDirectories(root.resolve("lib/gstreamer-1.0"));
        Files.createFile(root.resolve("bin/gstreamer-1.0-0.dll"));

        assertEquals(root, GstRuntime.resolve(temp));
    }

    @Test
    void rejectsIncompleteBundledRuntime() {
        var exception = assertThrows(IllegalStateException.class, () -> GstRuntime.resolve(temp));

        assertTrue(exception.getMessage().startsWith("Bundled GStreamer is incomplete:"));
    }
}
