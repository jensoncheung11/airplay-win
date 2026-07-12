package com.github.serezhka.airplay.app;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MulticastRuntimeTest {

    private static final String PREFER_IPV4_STACK = "java.net.preferIPv4Stack";

    @AfterEach
    void clearProperty() {
        System.clearProperty(PREFER_IPV4_STACK);
    }

    @Test
    void applyDefaultsForcesIpv4MulticastStack() throws Exception {
        System.clearProperty(PREFER_IPV4_STACK);

        Class<?> type = Class.forName("com.github.serezhka.airplay.app.MulticastRuntime");
        Method method = type.getDeclaredMethod("applyDefaults");
        method.invoke(null);

        assertEquals("true", System.getProperty(PREFER_IPV4_STACK));
    }
}
