package com.github.serezhka.airplay.app.session;

import com.github.serezhka.airplay.lib.AudioStreamInfo;
import com.github.serezhka.airplay.lib.VideoStreamInfo;
import com.github.serezhka.airplay.server.AirPlayConsumer;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

public final class ReceiverController implements AirPlayConsumer, AutoCloseable {

    private final ReceiverService receiver;
    private final PlaybackService playback;
    private final ExecutorService stateExecutor = Executors.newSingleThreadExecutor(runnable -> {
        Thread thread = new Thread(runnable, "receiver-state");
        thread.setDaemon(true);
        return thread;
    });
    private final List<Consumer<ReceiverState>> listeners = new CopyOnWriteArrayList<>();
    private volatile ReceiverState state = ReceiverState.STOPPED;

    public ReceiverController(ReceiverService receiver, PlaybackService playback) {
        this.receiver = receiver;
        this.playback = playback;
    }

    public ReceiverState state() {
        return state;
    }

    public void addStateListener(Consumer<ReceiverState> listener) {
        listeners.add(listener);
    }

    public void start() {
        runSerialized(() -> {
            if (state == ReceiverState.STOPPED) {
                receiver.start();
                transitionTo(ReceiverState.WAITING);
            }
        });
    }

    public void stop() {
        runSerialized(() -> {
            playback.reset();
            receiver.stop();
            transitionTo(ReceiverState.STOPPED);
        });
    }

    public void disconnect() {
        runSerialized(() -> {
            receiver.disconnect();
            playback.reset();
            transitionTo(ReceiverState.WAITING);
        });
    }

    @Override
    public void onSessionConnecting() {
        runSerialized(() -> transitionTo(ReceiverState.CONNECTING));
    }

    @Override
    public void onSessionError(Throwable error) {
        runSerialized(() -> {
            transitionTo(ReceiverState.ERROR);
            playback.reset();
            transitionTo(ReceiverState.WAITING);
        });
    }

    @Override
    public void onVideoFormat(VideoStreamInfo videoStreamInfo) {
        runSerialized(() -> {
            playback.onVideoFormat(videoStreamInfo);
            transitionTo(ReceiverState.PLAYING);
        });
    }

    @Override
    public void onVideo(byte[] bytes) {
        playback.onVideo(bytes);
    }

    @Override
    public void onVideoSrcDisconnect() {
        runSerialized(() -> {
            playback.onVideoSrcDisconnect();
            transitionTo(ReceiverState.WAITING);
        });
    }

    @Override
    public void onAudioFormat(AudioStreamInfo audioStreamInfo) {
        runSerialized(() -> {
            playback.onAudioFormat(audioStreamInfo);
            transitionTo(ReceiverState.PLAYING);
        });
    }

    @Override
    public void onAudio(byte[] bytes) {
        playback.onAudio(bytes);
    }

    @Override
    public void onAudioSrcDisconnect() {
        runSerialized(() -> {
            playback.onAudioSrcDisconnect();
            transitionTo(ReceiverState.WAITING);
        });
    }

    private void runSerialized(Runnable action) {
        CompletableFuture.runAsync(action, stateExecutor).join();
    }

    private void transitionTo(ReceiverState nextState) {
        state = nextState;
        listeners.forEach(listener -> listener.accept(nextState));
    }

    @Override
    public void close() {
        stateExecutor.shutdownNow();
    }
}
