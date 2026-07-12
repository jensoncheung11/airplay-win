package com.github.serezhka.airplay.player.gstreamer;

import com.github.serezhka.airplay.lib.AudioStreamInfo;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AudioPipelineSelectorTest {

    @Test
    void selectsAlacPipelineForAlacStreams() {
        assertEquals(AudioPipelineSelector.Kind.ALAC,
                AudioPipelineSelector.select(AudioStreamInfo.CompressionType.ALAC));
    }

    @Test
    void selectsAacPipelineForAacEldStreams() {
        assertEquals(AudioPipelineSelector.Kind.AAC_ELD,
                AudioPipelineSelector.select(AudioStreamInfo.CompressionType.AAC_ELD));
    }
}
