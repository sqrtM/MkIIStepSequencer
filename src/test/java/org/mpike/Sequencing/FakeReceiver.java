package org.mpike.Sequencing;

import javax.sound.midi.MidiMessage;
import javax.sound.midi.Receiver;

public class FakeReceiver implements Receiver {
    @Override
    public void send(MidiMessage message, long timeStamp) {
        System.out.println("message sent");
    }

    @Override
    public void close() {
        System.out.println("successfully closed");
    }
}
