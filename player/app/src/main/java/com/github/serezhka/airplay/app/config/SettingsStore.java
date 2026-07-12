package com.github.serezhka.airplay.app.config;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Properties;

public final class SettingsStore {

    private final Path file;

    public SettingsStore(Path file) {
        this.file = file;
    }

    public AppSettings load() throws IOException {
        if (Files.notExists(file)) {
            return AppSettings.defaults();
        }

        Properties properties = new Properties();
        try (Reader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            properties.load(reader);
        }

        try {
            return AppSettings.validated(
                    properties.getProperty("receiver.name"),
                    Integer.parseInt(properties.getProperty("video.width", "0")),
                    Integer.parseInt(properties.getProperty("video.height", "0")),
                    Integer.parseInt(properties.getProperty("video.fps", "0")),
                    Double.parseDouble(properties.getProperty("audio.volume", "-1")));
        } catch (NumberFormatException exception) {
            return AppSettings.defaults();
        }
    }

    public void save(AppSettings settings) throws IOException {
        Path parent = file.toAbsolutePath().getParent();
        Files.createDirectories(parent);
        Path temporary = parent.resolve(file.getFileName() + ".tmp");

        Properties properties = new Properties();
        properties.setProperty("receiver.name", settings.receiverName());
        properties.setProperty("video.width", Integer.toString(settings.width()));
        properties.setProperty("video.height", Integer.toString(settings.height()));
        properties.setProperty("video.fps", Integer.toString(settings.fps()));
        properties.setProperty("audio.volume", Double.toString(settings.volume()));

        try (Writer writer = Files.newBufferedWriter(temporary, StandardCharsets.UTF_8)) {
            properties.store(writer, "iOS AirPlay receiver");
        }
        Files.move(temporary, file, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
    }
}
