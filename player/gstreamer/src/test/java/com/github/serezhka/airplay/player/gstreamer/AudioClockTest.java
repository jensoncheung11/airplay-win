package com.github.serezhka.airplay.player.gstreamer;

import com.github.serezhka.airplay.lib.AudioStreamInfo;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AudioClockTest {

    @Test
    void usesSamplesPerFrameToAdvancePresentationTime() {
        AudioClock clock = new AudioClock();
        AudioStreamInfo info = new AudioStreamInfo.AudioStreamInfoBuilder()
                .compressionType(AudioStreamInfo.CompressionType.AAC_ELD)
                .audioFormat(AudioStreamInfo.AudioFormat.AAC_ELD_44100_2)
                .samplesPerFrame(480)
                .build();

        clock.configure(info);
        AudioClock.FrameTiming first = clock.nextFrame();
        AudioClock.FrameTiming second = clock.nextFrame();

        assertEquals(0L, first.presentationTimestampNanos());
        assertEquals(10_884_353L, first.durationNanos());
        assertEquals(10_884_353L, second.presentationTimestampNanos());
        assertEquals(10_884_353L, second.durationNanos());
    }

    @Test
    void resetsWhenReconfigured() {
        AudioClock clock = new AudioClock();
        AudioStreamInfo firstInfo = new AudioStreamInfo.AudioStreamInfoBuilder()
                .compressionType(AudioStreamInfo.CompressionType.ALAC)
                .audioFormat(AudioStreamInfo.AudioFormat.ALAC_44100_16_2)
                .samplesPerFrame(352)
                .build();
        AudioStreamInfo secondInfo = new AudioStreamInfo.AudioStreamInfoBuilder()
                .compressionType(AudioStreamInfo.CompressionType.AAC_ELD)
                .audioFormat(AudioStreamInfo.AudioFormat.AAC_ELD_48000_2)
                .samplesPerFrame(480)
                .build();

        clock.configure(firstInfo);
        clock.nextFrame();
        clock.configure(secondInfo);

        assertEquals(0L, clock.nextFrame().presentationTimestampNanos());
    }
}
