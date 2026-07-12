package com.github.serezhka.airplay.player.gstreamer;

import org.junit.jupiter.api.Test;

import java.awt.*;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class VideoComponentSizeProbeTest {

    @Test
    void readsRenderedSizeFromComponentFields() {
        FakeVideoComponent component = new FakeVideoComponent();
        component.imgWidth = 1920;
        component.imgHeight = 1080;

        Optional<Dimension> size = VideoComponentSizeProbe.read(component);

        assertTrue(size.isPresent());
        assertEquals(new Dimension(1920, 1080), size.get());
    }

    @Test
    void ignoresIncompleteRenderedSize() {
        FakeVideoComponent component = new FakeVideoComponent();
        component.imgWidth = 0;
        component.imgHeight = 1080;

        Optional<Dimension> size = VideoComponentSizeProbe.read(component);

        assertTrue(size.isEmpty());
    }

    private static final class FakeVideoComponent {
        @SuppressWarnings("unused")
        private int imgWidth;
        @SuppressWarnings("unused")
        private int imgHeight;
    }
}
