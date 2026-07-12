package com.github.serezhka.airplay.player.gstreamer;

import com.github.serezhka.airplay.lib.AudioStreamInfo;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AudioCapsFactoryTest {

    @Test
    void buildsAacEldCapsFor44100Stereo() {
        AudioStreamInfo streamInfo = new AudioStreamInfo.AudioStreamInfoBuilder()
                .compressionType(AudioStreamInfo.CompressionType.AAC_ELD)
                .audioFormat(AudioStreamInfo.AudioFormat.AAC_ELD_44100_2)
                .samplesPerFrame(480)
                .build();

        assertEquals(
                "audio/mpeg,mpegversion=(int)4,channels=(int)2,rate=(int)44100,stream-format=raw,codec_data=(buffer)f8e85000",
                AudioCapsFactory.aacEld(streamInfo));
    }

    @Test
    void buildsAacEldCapsFor48000Mono() {
        AudioStreamInfo streamInfo = new AudioStreamInfo.AudioStreamInfoBuilder()
                .compressionType(AudioStreamInfo.CompressionType.AAC_ELD)
                .audioFormat(AudioStreamInfo.AudioFormat.AAC_ELD_48000_1)
                .samplesPerFrame(480)
                .build();

        assertEquals(
                "audio/mpeg,mpegversion=(int)4,channels=(int)1,rate=(int)48000,stream-format=raw,codec_data=(buffer)f8e63000",
                AudioCapsFactory.aacEld(streamInfo));
    }
}
