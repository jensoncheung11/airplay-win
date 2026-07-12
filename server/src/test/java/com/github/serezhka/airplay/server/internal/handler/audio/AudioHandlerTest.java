package com.github.serezhka.airplay.server.internal.handler.audio;

import com.github.serezhka.airplay.lib.AirPlay;
import com.github.serezhka.airplay.lib.AudioStreamInfo;
import com.github.serezhka.airplay.lib.VideoStreamInfo;
import com.github.serezhka.airplay.server.AirPlayConsumer;
import com.github.serezhka.airplay.server.internal.packet.AudioPacket;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AudioHandlerTest {

    @Test
    void continuesPlaybackAfterMissingSequenceNumber() throws Exception {
        RecordingConsumer consumer = new RecordingConsumer();
        AudioHandler handler = new AudioHandler(new NoOpAirPlay(), consumer);

        handler.channelRead(null, packet(1, (byte) 1));
        handler.channelRead(null, packet(3, (byte) 3));
        handler.channelRead(null, packet(4, (byte) 4));

        assertEquals(List.of((byte) 1, (byte) 3, (byte) 4), consumer.audioMarkers);
    }

    private AudioPacket packet(int sequenceNumber, byte marker) {
        AudioPacket packet = AudioPacket.builder()
                .available(true)
                .sequenceNumber(sequenceNumber)
                .encodedAudioSize(1)
                .build();
        packet.getEncodedAudio()[0] = marker;
        return packet;
    }

    private static final class NoOpAirPlay extends AirPlay {
        @Override
        public void decryptAudio(byte[] audio, int audioLength) {
        }
    }

    private static final class RecordingConsumer implements AirPlayConsumer {
        private final List<Byte> audioMarkers = new ArrayList<>();

        @Override public void onVideoFormat(VideoStreamInfo videoStreamInfo) { }
        @Override public void onVideo(byte[] bytes) { }
        @Override public void onVideoSrcDisconnect() { }
        @Override public void onAudioFormat(AudioStreamInfo audioStreamInfo) { }
        @Override public void onAudio(byte[] bytes) { audioMarkers.add(bytes[0]); }
        @Override public void onAudioSrcDisconnect() { }
    }
}
