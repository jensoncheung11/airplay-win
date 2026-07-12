package com.github.serezhka.airplay.player.gstreamer;

import java.awt.*;
import java.lang.reflect.Field;
import java.util.Optional;

final class VideoComponentSizeProbe {

    private VideoComponentSizeProbe() {
    }

    static Optional<Dimension> read(Object component) {
        if (component == null) {
            return Optional.empty();
        }
        try {
            Field widthField = component.getClass().getDeclaredField("imgWidth");
            Field heightField = component.getClass().getDeclaredField("imgHeight");
            widthField.setAccessible(true);
            heightField.setAccessible(true);
            int width = widthField.getInt(component);
            int height = heightField.getInt(component);
            if (width <= 0 || height <= 0) {
                return Optional.empty();
            }
            return Optional.of(new Dimension(width, height));
        } catch (ReflectiveOperationException e) {
            return Optional.empty();
        }
    }
}
