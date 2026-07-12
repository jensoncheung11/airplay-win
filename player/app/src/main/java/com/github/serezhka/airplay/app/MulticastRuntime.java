package com.github.serezhka.airplay.app;

final class MulticastRuntime {

    private static final String PREFER_IPV4_STACK = "java.net.preferIPv4Stack";

    private MulticastRuntime() {
    }

    static void applyDefaults() {
        System.setProperty(PREFER_IPV4_STACK, "true");
    }
}
