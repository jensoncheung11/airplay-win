package com.github.serezhka.airplay.player.gstreamer;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class GstPipelineSpecTest {

    @Test
    void videoPipelineContainsRequiredDecoderElements() {
        String pipeline = GstPipelineSpec.video("appsink name=sink sync=true");

        assertTrue(pipeline.contains("appsrc name=h264-src"));
        assertTrue(pipeline.contains("h264parse"));
        assertTrue(pipeline.contains("avdec_h264"));
        assertTrue(pipeline.contains("videoconvert"));
    }

    @Test
    void audioPipelinesUseWindowsAudioAndNamedVolume() {
        String alac = GstPipelineSpec.alac();
        String aacEld = GstPipelineSpec.aacEld();

        assertTrue(alac.contains("appsrc name=alac-src"));
        assertTrue(alac.contains("avdec_alac"));
        assertTrue(aacEld.contains("appsrc name=aac-eld-src"));
        assertTrue(aacEld.contains("avdec_aac"));
        assertTrue(alac.contains("queue ! avdec_alac"));
        assertTrue(aacEld.contains("queue ! avdec_aac"));
        assertTrue(alac.contains("volume name=audio-volume"));
        assertTrue(aacEld.contains("volume name=audio-volume"));
        assertTrue(alac.contains("audioconvert ! audioresample"));
        assertTrue(aacEld.contains("audioconvert ! audioresample"));
        assertTrue(alac.contains("volume name=audio-volume ! wasapi2sink"));
        assertTrue(aacEld.contains("volume name=audio-volume ! wasapi2sink"));
    }
}
