package org.mpike.sequencing;

public class Pad {
    private PadStatus status = PadStatus.OFF;

    public Pad() {
    }

    public PadStatus getStatus() {
        return this.status;
    }

    public void toggleStatus() {
        this.status = this.getStatus() == PadStatus.ON ? PadStatus.OFF : PadStatus.ON;
    }
}
