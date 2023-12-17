package org.mpike.controller;

import org.mpike.Messenger;
import org.mpike.controller.mkii.Color;
import org.mpike.controller.mkii.ColorType;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.Receiver;
import javax.sound.midi.SysexMessage;
import javax.sound.midi.Transmitter;

public interface PhysicalController {
    byte[] DefaultSysexMessage();

    byte hexOffset();

    int noteOffset();

    int padAddress();

    int padColor();

    int padsPerRow();
    int totalPads();

    SysexMessage constructSysexMessage(byte[] outgoingMessage) throws InvalidMidiDataException;

    Receiver receiver();

    Transmitter transmitter();

    Messenger messenger();
    void setColor(ColorType colorType, Color color);
    Color getColor(ColorType colorType);
}
