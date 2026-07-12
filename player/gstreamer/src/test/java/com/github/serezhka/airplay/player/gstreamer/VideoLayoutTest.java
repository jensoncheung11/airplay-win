package com.github.serezhka.airplay.player.gstreamer;

import org.junit.jupiter.api.Test;

import java.awt.*;

import static org.junit.jupiter.api.Assertions.assertEquals;

class VideoLayoutTest {

    @Test
    void fitsLandscapeVideoInsideBounds() {
        Dimension fitted = VideoLayout.fitInside(1920, 1080, 1280, 720);

        assertEquals(new Dimension(1280, 720), fitted);
    }

    @Test
    void fitsPortraitVideoInsideBounds() {
        Dimension fitted = VideoLayout.fitInside(1080, 1920, 1280, 720);

        assertEquals(new Dimension(405, 720), fitted);
    }
}
