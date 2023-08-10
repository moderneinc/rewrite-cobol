package io.moderne.cobol.internal;

import static org.fusesource.jansi.Ansi.ansi;

public enum ModerneColors {
    Green(0x66FFB3),
    Blue(0x7E90FF),
    Red(0xFF4D64),
    Yellow(0xFCBF4E);

    private final int rgb;

    ModerneColors(int rgb) {
        this.rgb = rgb;
    }

    public int rgb() {
        return rgb;
    }

    public String highlight(String text) {
        return ansi().fgRgb(rgb).a(text).reset().toString();
    }
}
