package com.github.serezhka.airplay.player.gstreamer;

import com.github.serezhka.airplay.lib.AudioStreamInfo;

final class AudioClock {

    private long nextPresentationTimestampNanos;
    private long frameDurationNanos;

    void configure(AudioStreamInfo info) {
        nextPresentationTimestampNanos = 0L;
        frameDurationNanos = TimeMath.frameDurationNanos(info.getSamplesPerFrame(), sampleRate(info));
    }

    FrameTiming nextFrame() {
        FrameTiming timing = new FrameTiming(nextPresentationTimestampNanos, frameDurationNanos);
        nextPresentationTimestampNanos += frameDurationNanos;
        return timing;
    }

    private int sampleRate(AudioStreamInfo info) {
        String name = info.getAudioFormat().name();
        if (name.contains("_48000_")) {
            return 48000;
        }
        if (name.contains("_44100_")) {
            return 44100;
        }
        if (name.contains("_32000_")) {
            return 32000;
        }
        if (name.contains("_24000_")) {
            return 24000;
        }
        if (name.contains("_16000_")) {
            return 16000;
        }
        if (name.contains("_8000_")) {
            return 8000;
        }
        throw new IllegalArgumentException("Unsupported audio format: " + info.getAudioFormat());
    }

    record FrameTiming(long presentationTimestampNanos, long durationNanos) {
    }

    private static final class TimeMath {
        private TimeMath() {
        }

        static long frameDurationNanos(int samplesPerFrame, int sampleRate) {
            return (samplesPerFrame * 1_000_000_000L) / sampleRate;
        }
    }
}
