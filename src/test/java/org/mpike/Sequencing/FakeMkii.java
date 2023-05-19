package org.mpike.Sequencing;

import org.mpike.PhysicalController;

import javax.sound.midi.Receiver;
import javax.sound.midi.Transmitter;

public class FakeMkii implements PhysicalController {
    @Override
    public byte[] DefaultSysexMessage() {
        return new byte[] {
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
    public Receiver receiver() {
        return new FakeReceiver();
    }

    @Override
    public Transmitter transmitter() {
        return new FakeTransmitter();
    }
}
