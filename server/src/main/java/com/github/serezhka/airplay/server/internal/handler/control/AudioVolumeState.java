package com.github.serezhka.airplay.server.internal.handler.control;

import java.util.Locale;

final class AudioVolumeState {

    private static final double MUTE_VOLUME_DB = -144.0;
    private static final String PREFIX = "volume:";

    private double airPlayVolumeDb = 0.0;

    String currentParameterValue() {
        return PREFIX + " " + String.format(Locale.US, "%.6f", airPlayVolumeDb) + "\r\n";
    }

    void updateFromRtspParameter(String body) {
        for (String line : body.split("\\r?\\n")) {
            if (line.startsWith(PREFIX)) {
                airPlayVolumeDb = Double.parseDouble(line.substring(PREFIX.length()).trim());
                return;
            }
        }
    }

    double linearVolume() {
        if (airPlayVolumeDb <= MUTE_VOLUME_DB) {
            return 0.0;
        }
        return Math.pow(10.0, airPlayVolumeDb / 20.0);
    }
}
