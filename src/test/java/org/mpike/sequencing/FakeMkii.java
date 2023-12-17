package org.mpike.sequencing;

import org.mpike.Messenger;
import org.mpike.controller.PhysicalController;
import org.mpike.controller.mkii.Color;
import org.mpike.controller.mkii.ColorType;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.Receiver;
import javax.sound.midi.SysexMessage;
import javax.sound.midi.Transmitter;

public class FakeMkii implements PhysicalController {
    @Override
    public byte[] DefaultSysexMessage() {
        return new byte[]{
                (byte) 0xF0,
                0x00, 0x20, 0x6B, 0x7F, 0x42,
                0x02, 0x00, 0x10, 0x70, 0x14,
                (byte) 0xF7
        };
    }

    @Override
    public byte hexOffset() {
        return 0x70;
    }

    @Override
    public int noteOffset() {
        return 36;
    }

    @Override
    public int padAddress() {
        return 9;
    }

    @Override
    public int padColor() {
        return 10;
    }

    @Override
    public int padsPerRow() {
        return 8;
    }

    @Override
    public int totalPads() {
        return 16;
    }

    @Override
    public SysexMessage constructSysexMessage(byte[] outgoingMessage) throws InvalidMidiDataException {
        assert outgoingMessage.length == this.DefaultSysexMessage().length;
        SysexMessage msg = new SysexMessage();
        msg.setMessage(outgoingMessage, this.DefaultSysexMessage().length);
        return msg;
    }

    @Override
    public Receiver receiver() {
        return new FakeReceiver();
    }

    @Override
    public Transmitter transmitter() {
        return new FakeTransmitter();
    }

    @Override
    public Messenger messenger() {
        return new Messenger();
    }

    @Override
    public void setColor(ColorType colorType, Color color) {
    }

    @Override
    public Color getColor(ColorType colorType) {
        return null;
    }
}
