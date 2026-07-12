package com.github.serezhka.airplay.player.gstreamer;

import com.formdev.flatlaf.FlatDarkLaf;
import com.github.serezhka.airplay.lib.VideoStreamInfo;
import org.freedesktop.gstreamer.Gst;
import org.freedesktop.gstreamer.Pipeline;
import org.freedesktop.gstreamer.elements.AppSink;
import org.freedesktop.gstreamer.swing.GstVideoComponent;
import lombok.extern.slf4j.Slf4j;

import javax.swing.*;
import java.awt.*;

@Slf4j
public class GstPlayerSwing extends GstPlayer {

    static {
        FlatDarkLaf.setup();
    }

    private final JFrame window;
    private final GstVideoComponent videoComponent;
    private final Timer sizeMonitor;
    private Dimension currentVideoSize = new Dimension(960, 540);

    public GstPlayerSwing() {
        AppSink sink = (AppSink) h264Pipeline.getElementByName("sink");
        videoComponent = new GstVideoComponent(sink);
        videoComponent.setKeepAspect(true);
        sizeMonitor = new Timer(200, event -> updateVideoSizeFromComponent());
        sizeMonitor.start();

        window = new JFrame("AirPlay player");
        window.setLayout(new BorderLayout());
        window.add(videoComponent, BorderLayout.CENTER);
        videoComponent.setPreferredSize(currentVideoSize);
        window.pack();
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    @Override
    protected Pipeline createH264Pipeline() {
        return (Pipeline) Gst.parseLaunch(GstPipelineSpec.video("appsink name=sink sync=true"));
    }

    @Override
    public void onVideoFormat(VideoStreamInfo videoStreamInfo) {
        updateVideoSizeFromComponent();
        applyVideoSize(currentVideoSize.width, currentVideoSize.height);
        window.setVisible(true);
        super.onVideoFormat(videoStreamInfo);
    }

    @Override
    public void onVideoSrcDisconnect() {
        window.setVisible(false);
        super.onVideoSrcDisconnect();
    }

    private void updateVideoSizeFromComponent() {
        VideoComponentSizeProbe.read(videoComponent)
                .ifPresent(size -> applyVideoSize(size.width, size.height));
    }

    private void applyVideoSize(int width, int height) {
        Rectangle bounds = GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();
        Dimension fitted = VideoLayout.fitInside(width, height, Math.max(320, bounds.width), Math.max(240, bounds.height));
        if (fitted.equals(currentVideoSize)) {
            return;
        }
        currentVideoSize = fitted;
        videoComponent.setPreferredSize(fitted);
        window.pack();
        window.setLocationRelativeTo(null);
        log.info("Updated video window size to {}x{} for content {}x{}", fitted.width, fitted.height, width, height);
    }
}
