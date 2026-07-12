package com.github.serezhka.airplay.player.gstreamer;

import com.github.serezhka.airplay.lib.AudioStreamInfo;
import com.github.serezhka.airplay.lib.VideoStreamInfo;
import com.github.serezhka.airplay.server.AirPlayConsumer;
import lombok.extern.slf4j.Slf4j;
import org.freedesktop.gstreamer.*;
import org.freedesktop.gstreamer.elements.AppSrc;
import org.freedesktop.gstreamer.glib.GLib;

import java.util.concurrent.TimeUnit;

@Slf4j
public abstract class GstPlayer implements AirPlayConsumer {

    static {
        GstPlayerUtils.configurePaths();
        GLib.setEnv("GST_DEBUG", "3", true);
        Gst.init(Version.of(1, 10), "BasicPipeline");
    }

    protected final Pipeline h264Pipeline;
    private final Pipeline alacPipeline;
    private final Pipeline aacEldPipeline;

    private final AppSrc h264Src;
    private final AppSrc alacSrc;
    private final AppSrc aacEldSrc;

    private Pipeline hlsPipeline;

    private AudioStreamInfo.CompressionType audioCompressionType;

    public GstPlayer() {
        h264Pipeline = createH264Pipeline();

        h264Src = (AppSrc) h264Pipeline.getElementByName("h264-src");
        h264Src.setStreamType(AppSrc.StreamType.STREAM);
        h264Src.setCaps(Caps.fromString("video/x-h264,colorimetry=bt709,stream-format=(string)byte-stream,alignment=(string)au"));
        h264Src.set("is-live", true);
        h264Src.set("format", Format.TIME);
        h264Src.set("emit-signals", true);

        alacPipeline = (Pipeline) Gst.parseLaunch(GstPipelineSpec.alac());

        alacSrc = (AppSrc) alacPipeline.getElementByName("alac-src");
        alacSrc.setStreamType(AppSrc.StreamType.STREAM);
        alacSrc.setCaps(Caps.fromString("audio/x-alac,mpegversion=(int)4,channels=(int)2,rate=(int)44100,stream-format=raw,codec_data=(buffer)00000024616c616300000000000001600010280a0e0200ff00000000000000000000ac44"));
        alacSrc.set("is-live", true);
        alacSrc.set("format", Format.TIME);
        alacSrc.set("emit-signals", true);

        aacEldPipeline = (Pipeline) Gst.parseLaunch(GstPipelineSpec.aacEld());

        aacEldSrc = (AppSrc) aacEldPipeline.getElementByName("aac-eld-src");
        aacEldSrc.setStreamType(AppSrc.StreamType.STREAM);
        aacEldSrc.setCaps(Caps.fromString("audio/mpeg,mpegversion=(int)4,channels=(int)2,rate=(int)44100,stream-format=raw,codec_data=(buffer)f8e85000"));
        aacEldSrc.set("is-live", true);
        aacEldSrc.set("format", Format.TIME);
        aacEldSrc.set("emit-signals", true);
    }

    protected abstract Pipeline createH264Pipeline();

    @Override
    public void onVideoFormat(VideoStreamInfo videoStreamInfo) {
        h264Pipeline.play();
    }

    @Override
    public void onVideo(byte[] bytes) {
        Buffer buf = new Buffer(bytes.length);
        buf.map(true).put(bytes); // ByteBuffer.wrap(bytes)
        h264Src.pushBuffer(buf);
    }

    @Override
    public void onVideoSrcDisconnect() {
        h264Pipeline.stop();
    }

    @Override
    public void onAudioFormat(AudioStreamInfo audioStreamInfo) {
        this.audioCompressionType = audioStreamInfo.getCompressionType();
        alacPipeline.play();
        aacEldPipeline.play();
    }

    @Override
    public void onAudio(byte[] bytes) {
        Buffer buf = new Buffer(bytes.length);
        buf.map(true).put(bytes); // ByteBuffer.wrap(bytes)
        switch (audioCompressionType) {
            case ALAC -> alacSrc.pushBuffer(buf);
            case AAC_ELD -> aacEldSrc.pushBuffer(buf);
        }
    }

    @Override
    public void onAudioSrcDisconnect() {
        alacPipeline.stop();
        aacEldPipeline.stop();
    }

    public void setVolume(double volume) {
        double clamped = Math.max(0.0, Math.min(1.0, volume));
        alacPipeline.getElementByName("audio-volume").set("volume", clamped);
        aacEldPipeline.getElementByName("audio-volume").set("volume", clamped);
    }

    public void reset() {
        h264Pipeline.stop();
        alacPipeline.stop();
        aacEldPipeline.stop();
        if (hlsPipeline != null) {
            hlsPipeline.stop();
            hlsPipeline = null;
        }
        audioCompressionType = null;
    }

    @Override
    public void onMediaPlaylist(String playlistUri) {
        hlsPipeline = (Pipeline) Gst.parseLaunch("playbin3 uri=" + playlistUri);
        hlsPipeline.play();
    }

    @Override
    public void onMediaPlaylistRemove() {
        if (hlsPipeline != null) {
            hlsPipeline.stop();
        }
    }

    @Override
    public void onMediaPlaylistPause() {
        if (hlsPipeline != null && hlsPipeline.isPlaying()) {
            hlsPipeline.pause();
        }
    }

    @Override
    public void onMediaPlaylistResume() {
        if (hlsPipeline != null && !hlsPipeline.isPlaying()) {
            hlsPipeline.play();
        }
    }

    @Override
    public PlaybackInfo playbackInfo() {
        if (hlsPipeline != null) {
            return new PlaybackInfo(
                    hlsPipeline.queryDuration(TimeUnit.SECONDS),
                    hlsPipeline.queryPosition(TimeUnit.SECONDS));
        }
        return AirPlayConsumer.super.playbackInfo();
    }
}
