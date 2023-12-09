package org.mpike.sequencing;

public enum PadStatus {
    ON(true),
    OFF(false);

    private final boolean value;

    PadStatus(boolean value) {
        this.value = value;
    }

    public boolean isOn() {
        return value;
    }
}
