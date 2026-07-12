package com.github.serezhka.airplay.lib.internal;

import com.dd.plist.BinaryPropertyListWriter;
import com.dd.plist.NSArray;
import com.dd.plist.NSDictionary;
import com.github.serezhka.airplay.lib.AudioStreamInfo;
import com.github.serezhka.airplay.lib.MediaStreamInfo;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RTSPTest {

    @Test
    void parsesAudioFormatCodesStoredAsLong() throws Exception {
        NSDictionary audioStream = new NSDictionary();
        audioStream.put("type", 96);
        audioStream.put("ct", 8);
        audioStream.put("spf", 480);
        audioStream.put("audioFormat", 4294967296L);

        NSArray streams = new NSArray(1);
        streams.setValue(0, audioStream);

        NSDictionary payload = new NSDictionary();
        payload.put("streams", streams);

        MediaStreamInfo info = new RTSP()
                .setup(new ByteArrayInputStream(BinaryPropertyListWriter.writeToArray(payload)))
                .orElseThrow();

        assertTrue(info instanceof AudioStreamInfo);
        AudioStreamInfo audio = (AudioStreamInfo) info;
        assertEquals(AudioStreamInfo.CompressionType.AAC_ELD, audio.getCompressionType());
        assertEquals(AudioStreamInfo.AudioFormat.AAC_ELD_48000_1, audio.getAudioFormat());
        assertEquals(480, audio.getSamplesPerFrame());
    }
}
