package org.mpike;

import javax.sound.midi.MidiMessage;
import javax.sound.midi.Receiver;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.Transmitter;

public class Messenger implements Transmitter {

    private Receiver receiver;

    public void prepareMessage() {
        ShortMessage msg = new ShortMessage();
        this.send(msg, -1);
    }

    public void send(MidiMessage message, int timestamp) {
        this.receiver.send(message, timestamp);
    }

    @Override
    public Receiver getReceiver() {
        return receiver;
    }

    @Override
    public void setReceiver(Receiver receiver) {
        this.receiver = receiver;
    }

    @Override
    public void close() {
    }
}
