package com.github.serezhka.airplay.player.gstreamer;

import java.awt.*;

final class VideoLayout {

    private VideoLayout() {
    }

    static Dimension fitInside(int videoWidth, int videoHeight, int maxWidth, int maxHeight) {
        if (videoWidth <= 0 || videoHeight <= 0 || maxWidth <= 0 || maxHeight <= 0) {
            throw new IllegalArgumentException("Video and bounds dimensions must be positive");
        }

        double scale = Math.min((double) maxWidth / videoWidth, (double) maxHeight / videoHeight);
        int fittedWidth = Math.max(1, (int) Math.round(videoWidth * scale));
        int fittedHeight = Math.max(1, (int) Math.round(videoHeight * scale));
        return new Dimension(fittedWidth, fittedHeight);
    }
}
