package org.mpike;

import javax.sound.midi.MidiMessage;
import javax.sound.midi.Receiver;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.Transmitter;

public class Messenger implements Transmitter {

    private Receiver receiver;

    private static final Messenger messenger = new Messenger();

    private Messenger() {}

    public static Messenger getMessenger() {
        return messenger;
    }

    public void prepareMessage() {
        ShortMessage msg = new ShortMessage();
        this.send(msg, -1);
    }

    public void send(MidiMessage message, int timestamp) {
        this.receiver.send(message, timestamp);
    }

    @Override
    public void setReceiver(Receiver receiver) {
        this.receiver = receiver;
    }

    @Override
    public Receiver getReceiver() {
        return receiver;
    }

    @Override
    public void close() {
    }
}
