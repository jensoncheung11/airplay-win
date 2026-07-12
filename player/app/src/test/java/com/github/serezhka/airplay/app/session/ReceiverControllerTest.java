package com.github.serezhka.airplay.app.session;

import com.github.serezhka.airplay.lib.AudioStreamInfo;
import com.github.serezhka.airplay.lib.VideoStreamInfo;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ReceiverControllerTest {

    private final FakeReceiverService receiver = new FakeReceiverService();
    private final FakePlaybackService playback = new FakePlaybackService();
    private final ReceiverController controller = new ReceiverController(receiver, playback);

    @AfterEach
    void closeController() {
        controller.close();
    }

    @Test
    void startMovesFromStoppedToWaiting() {
        controller.start();

        assertEquals(ReceiverState.WAITING, controller.state());
        assertEquals(1, receiver.startCalls);
    }

    @Test
    void mediaFormatMovesToPlaying() {
        controller.start();
        controller.onSessionConnecting();
        controller.onVideoFormat(null);

        assertEquals(ReceiverState.PLAYING, controller.state());
        assertEquals(1, playback.videoFormatCalls);
    }

    @Test
    void protocolErrorReturnsToWaitingAfterCleanup() {
        controller.start();
        controller.onSessionError(new IOException("handshake"));

        assertEquals(ReceiverState.WAITING, controller.state());
        assertEquals(1, playback.resetCalls);
    }

    @Test
    void disconnectResetsPlaybackAndReturnsToWaiting() {
        controller.start();
        controller.onSessionConnecting();
        controller.disconnect();

        assertEquals(ReceiverState.WAITING, controller.state());
        assertEquals(1, receiver.disconnectCalls);
        assertEquals(1, playback.resetCalls);
    }

    private static final class FakeReceiverService implements ReceiverService {
        int startCalls;
        int stopCalls;
        int disconnectCalls;

        @Override public void start() { startCalls++; }
        @Override public void stop() { stopCalls++; }
        @Override public void disconnect() { disconnectCalls++; }
    }

    private static final class FakePlaybackService implements PlaybackService {
        int videoFormatCalls;
        int resetCalls;
        double lastVolume = -1;

        @Override public void onVideoFormat(VideoStreamInfo videoStreamInfo) { videoFormatCalls++; }
        @Override public void onVideo(byte[] bytes) { }
        @Override public void onVideoSrcDisconnect() { }
        @Override public void onAudioFormat(AudioStreamInfo audioStreamInfo) { }
        @Override public void onAudio(byte[] bytes) { }
        @Override public void onAudioSrcDisconnect() { }
        @Override public void onAudioVolume(double volume) { lastVolume = volume; }
        @Override public void reset() { resetCalls++; }
    }
}
