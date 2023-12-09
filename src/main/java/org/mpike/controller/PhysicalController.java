package org.mpike.controller;

import org.mpike.Messenger;

import javax.sound.midi.Receiver;
import javax.sound.midi.Transmitter;

public interface PhysicalController {
    byte[] DefaultSysexMessage();

    byte hexOffset();

    int noteOffset();

    int padAddress();

    int padColor();

    int padsPerRow();
    int totalRows();

    Receiver receiver();

    Transmitter transmitter();

    Messenger messenger();
}
