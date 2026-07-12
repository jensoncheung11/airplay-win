package com.github.serezhka.airplay.app.config;

public record AppSettings(String receiverName, int width, int height, int fps, double volume) {

    public static AppSettings defaults() {
        return new AppSettings("iOS投屏", 1920, 1080, 30, 1.0);
    }

    public static AppSettings validated(String name, int width, int height, int fps, double volume) {
        boolean supportedSize = (width == 1920 && height == 1080)
                || (width == 1280 && height == 720);
        if (name == null || name.isBlank() || !supportedSize || fps != 30 || volume < 0 || volume > 1) {
            return defaults();
        }
        return new AppSettings(name.trim(), width, height, fps, volume);
    }
}
