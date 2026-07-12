package com.github.serezhka.airplay.player.gstreamer;

import com.github.serezhka.airplay.lib.AudioStreamInfo;

final class AudioPipelineSelector {

    private AudioPipelineSelector() {
    }

    static Kind select(AudioStreamInfo.CompressionType compressionType) {
        return switch (compressionType) {
            case ALAC -> Kind.ALAC;
            case AAC_ELD -> Kind.AAC_ELD;
            default -> throw new IllegalArgumentException("Unsupported audio compression: " + compressionType);
        };
    }

    enum Kind {
        ALAC,
        AAC_ELD
    }
}
