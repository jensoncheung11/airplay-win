package com.github.serezhka.airplay.app;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AppPathsTest {

    @TempDir
    Path temp;

    @Test
    void explicitHomeControlsPortableFiles() {
        var paths = AppPaths.fromHome(temp);

        assertEquals(temp.toAbsolutePath(), paths.home());
        assertEquals(temp.resolve("config.properties").toAbsolutePath(), paths.configFile());
        assertEquals(temp.resolve("logs").toAbsolutePath(), paths.logsDirectory());
    }
}
