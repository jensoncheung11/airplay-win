package com.github.serezhka.airplay.player.gstreamer;

import com.github.serezhka.airplay.lib.AudioStreamInfo;

final class AudioCapsFactory {

    private static final int AAC_ELD_AUDIO_OBJECT_TYPE = 39;
    private static final int AAC_ELD_CONFIG_SUFFIX = 0x1000;

    private AudioCapsFactory() {
    }

    static String aacEld(AudioStreamInfo streamInfo) {
        int sampleRate = audioFormatSampleRate(streamInfo.getAudioFormat());
        int channels = audioFormatChannels(streamInfo.getAudioFormat());
        String codecData = buildAacEldCodecData(sampleRate, channels);
        return "audio/mpeg,mpegversion=(int)4,channels=(int)" + channels
                + ",rate=(int)" + sampleRate
                + ",stream-format=raw,codec_data=(buffer)" + codecData;
    }

    private static String buildAacEldCodecData(int sampleRate, int channels) {
        int sampleRateIndex = switch (sampleRate) {
            case 96000 -> 0;
            case 88200 -> 1;
            case 64000 -> 2;
            case 48000 -> 3;
            case 44100 -> 4;
            case 32000 -> 5;
            case 24000 -> 6;
            case 22050 -> 7;
            case 16000 -> 8;
            case 12000 -> 9;
            case 11025 -> 10;
            case 8000 -> 11;
            case 7350 -> 12;
            default -> throw new IllegalArgumentException("Unsupported AAC sample rate: " + sampleRate);
        };
        int audioConfig = (0x1F << 27)
                | ((AAC_ELD_AUDIO_OBJECT_TYPE - 32) << 21)
                | (sampleRateIndex << 17)
                | (channels << 13)
                | AAC_ELD_CONFIG_SUFFIX;
        return String.format("%08x", audioConfig);
    }

    private static int audioFormatSampleRate(AudioStreamInfo.AudioFormat audioFormat) {
        return switch (audioFormat) {
            case AAC_ELD_16000_1 -> 16000;
            case AAC_ELD_24000_1 -> 24000;
            case AAC_ELD_44100_1, AAC_ELD_44100_2 -> 44100;
            case AAC_ELD_48000_1, AAC_ELD_48000_2 -> 48000;
            default -> throw new IllegalArgumentException("Unsupported AAC-ELD audio format: " + audioFormat);
        };
    }

    private static int audioFormatChannels(AudioStreamInfo.AudioFormat audioFormat) {
        return switch (audioFormat) {
            case AAC_ELD_16000_1, AAC_ELD_24000_1, AAC_ELD_44100_1, AAC_ELD_48000_1 -> 1;
            case AAC_ELD_44100_2, AAC_ELD_48000_2 -> 2;
            default -> throw new IllegalArgumentException("Unsupported AAC-ELD audio format: " + audioFormat);
        };
    }
}
