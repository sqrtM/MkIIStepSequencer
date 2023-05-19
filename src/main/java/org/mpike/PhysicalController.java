package org.mpike;

import javax.sound.midi.Receiver;
import javax.sound.midi.Transmitter;

public interface PhysicalController {
    byte[] DefaultSysexMessage();
    byte hexOffset();
    int noteOffset();
    int padAddress();
    int padColor();
    Receiver receiver();
    Transmitter transmitter();

}
