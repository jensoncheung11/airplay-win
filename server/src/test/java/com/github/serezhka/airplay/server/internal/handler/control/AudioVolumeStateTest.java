package com.github.serezhka.airplay.server.internal.handler.control;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AudioVolumeStateTest {

    @Test
    void defaultsToZeroDbAndFullVolume() {
        AudioVolumeState state = new AudioVolumeState();

        assertEquals("volume: 0.000000\r\n", state.currentParameterValue());
        assertEquals(1.0, state.linearVolume());
    }

    @Test
    void updatesVolumeFromRtspParameterBody() {
        AudioVolumeState state = new AudioVolumeState();

        state.updateFromRtspParameter("volume: -20.000000\r\n");

        assertEquals("volume: -20.000000\r\n", state.currentParameterValue());
        assertEquals(0.1, state.linearVolume(), 0.000001);
    }

    @Test
    void treatsMuteAsZeroVolume() {
        AudioVolumeState state = new AudioVolumeState();

        state.updateFromRtspParameter("volume: -144.000000\r\n");

        assertEquals(0.0, state.linearVolume());
    }
}
