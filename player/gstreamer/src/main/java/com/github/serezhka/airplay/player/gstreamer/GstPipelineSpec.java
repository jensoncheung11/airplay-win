package com.github.serezhka.airplay.player.gstreamer;

final class GstPipelineSpec {

    private GstPipelineSpec() {
    }

    static String video(String sink) {
        return "appsrc name=h264-src is-live=true format=time"
                + " ! h264parse ! avdec_h264 ! videoconvert ! " + sink;
    }

    static String alac() {
        return "appsrc name=alac-src is-live=true format=time"
                + " ! queue ! avdec_alac ! audioconvert ! audioresample"
                + " ! volume name=audio-volume ! wasapi2sink low-latency=true sync=false";
    }

    static String aacEld() {
        return "appsrc name=aac-eld-src is-live=true format=time"
                + " ! queue ! avdec_aac ! audioconvert ! audioresample"
                + " ! volume name=audio-volume ! wasapi2sink low-latency=true sync=false";
    }
}
