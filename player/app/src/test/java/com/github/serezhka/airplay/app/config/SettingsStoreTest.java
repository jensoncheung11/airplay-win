package com.github.serezhka.airplay.app.config;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SettingsStoreTest {

    @TempDir
    Path temp;

    @Test
    void missingFileUsesDefaults() throws Exception {
        var store = new SettingsStore(temp.resolve("config.properties"));

        assertEquals(AppSettings.defaults(), store.load());
    }

    @Test
    void savesAndLoadsSettings() throws Exception {
        var file = temp.resolve("config.properties");
        var store = new SettingsStore(file);
        var expected = new AppSettings("客厅电脑", 1280, 720, 30, 0.4);

        store.save(expected);

        assertEquals(expected, store.load());
    }

    @Test
    void invalidValuesFallBackToDefaults() throws Exception {
        var file = temp.resolve("config.properties");
        Files.writeString(file, """
                receiver.name=
                video.width=999
                video.height=999
                video.fps=120
                audio.volume=4
                """);

        assertEquals(AppSettings.defaults(), new SettingsStore(file).load());
    }
}
