package org.mpike.sequencing;

import org.mpike.controller.PhysicalController;

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
