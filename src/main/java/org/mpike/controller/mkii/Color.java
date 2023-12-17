package org.mpike.controller.mkii;

public enum Color {
    NONE((byte) 0x00),
    RED((byte) 0x01),
    GREEN((byte) 0x04),
    YELLOW((byte) 0x05),
    BLUE((byte) 0x10),
    CYAN((byte) 0x14),
    PURPLE((byte) 0x11),
    WHITE((byte) 0x7F);

    private final byte color;

    Color(byte color) {
        this.color = color;
    }

    public static byte inactiveOffColor() {
        return CYAN.color;
    }

    public static byte inactiveOnColor() {
        return PURPLE.color;
    }

    public static byte activeOnColor() {
        return WHITE.color;
    }

    public static byte activeOffColor() {
        return GREEN.color;
    }

    public static byte bankColor() {
        return BLUE.color;
    }

    public static byte inaccessibleBankColor() {
        return RED.color;
    }

    public static byte noColor() {
        return NONE.color;
    }

    public byte getColor() {
        return color;
    }

    public java.awt.Color toAwtColor() {
        return switch (this) {
            case NONE -> java.awt.Color.BLACK;
            case RED -> java.awt.Color.RED;
            case GREEN -> java.awt.Color.GREEN;
            case YELLOW -> java.awt.Color.YELLOW;
            case BLUE -> java.awt.Color.BLUE;
            case CYAN -> java.awt.Color.CYAN;
            case PURPLE -> java.awt.Color.MAGENTA;
            case WHITE -> java.awt.Color.WHITE;
        };
    }
}
