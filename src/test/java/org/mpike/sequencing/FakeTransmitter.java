package org.mpike.sequencing;

import javax.sound.midi.Receiver;
import javax.sound.midi.Transmitter;

public class FakeTransmitter implements Transmitter {
    Receiver receiver = new FakeReceiver();

    @Override
    public Receiver getReceiver() {
        return this.receiver;
    }

    @Override
    public void setReceiver(Receiver receiver) {
        this.receiver = receiver;
    }

    @Override
    public void close() {
        System.out.println("successfully closed");
    }
}
