package com.github.serezhka.airplay.app;

import java.nio.file.Path;

public record AppPaths(Path home, Path configFile, Path logsDirectory) {

    public static AppPaths fromHome(Path home) {
        Path absoluteHome = home.toAbsolutePath().normalize();
        return new AppPaths(
                absoluteHome,
                absoluteHome.resolve("config.properties"),
                absoluteHome.resolve("logs"));
    }

    public static AppPaths resolve() {
        String configuredHome = System.getProperty("airplay.home", ".");
        return fromHome(Path.of(configuredHome));
    }
}
