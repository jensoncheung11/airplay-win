package com.github.serezhka.airplay.app.session;

import com.github.serezhka.airplay.server.AirPlayConsumer;

public interface PlaybackService extends AirPlayConsumer {
    void reset();
}
