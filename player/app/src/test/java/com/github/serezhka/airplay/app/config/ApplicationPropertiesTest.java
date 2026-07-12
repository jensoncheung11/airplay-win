package com.github.serezhka.airplay.app.config;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ApplicationPropertiesTest {

    @Test
    void defaultsToSwingWindowForGstreamerPlayback() throws IOException {
        Properties properties = new Properties();
        try (InputStream input = getClass().getResourceAsStream("/application.properties")) {
            properties.load(input);
        }

        assertEquals("true", properties.getProperty("player.gstreamer.swing"));
    }
}
