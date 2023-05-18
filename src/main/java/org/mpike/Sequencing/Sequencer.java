package org.mpike.Sequencing;

import org.mpike.Main;

import javax.sound.midi.MidiMessage;
import javax.sound.midi.Receiver;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.SysexMessage;
import java.util.Arrays;

public class Sequencer implements Receiver {
    public int activeMemory = 0;
    private final boolean[][] pads;
    private final int NOTE_OFFSET = 36;
    int bankLength = 8;

    public Sequencer(int numberOfBanks) {
        this.pads = new boolean[numberOfBanks][bankLength];
        for (int i = 0; i < numberOfBanks; i++) {
            (new Bank(i, bankLength, this)).start();
        }
    }

    /**
     * @apiNote This is probably better referred to as "handle," rather than "send".
     * This takes an incoming message, either from the Bank or from the physical
     * controller itself, and sends it DIRECTLY to the controller.
     *
     * @param message the MIDI message to send
     * @param timeStamp the time-stamp for the message, in microseconds
     */
    @Override
    public void send(MidiMessage message, long timeStamp) {
        // if it comes from the bank ...
        if (message instanceof SysexMessage) {
            //System.out.println(Arrays.toString(message.getMessage()));
            Main.mkiiReceiver.send(message, -1);
        }
        // If it comes from the controller ...
        if (message instanceof ShortMessage) {
            System.out.println(Arrays.toString(message.getMessage()));
            if (message.getMessage()[1] >= NOTE_OFFSET + bankLength) {
                changeActiveMemoryBank(message);
                System.out.println("active mem is now " + activeMemory);
            } else {
                togglePad(message);
            }
        }
    }

    private void togglePad(MidiMessage incomingMessage) {
        int padAddress = incomingMessage.getMessage()[1] - NOTE_OFFSET;
        pads[activeMemory][padAddress] = !pads[activeMemory][padAddress];
    }

    private void changeActiveMemoryBank(MidiMessage incomingMessage) {
        activeMemory = incomingMessage.getMessage()[1] - NOTE_OFFSET - bankLength;
    }

    @Override
    public void close() {
    }

    public boolean[] pads(int padRow) {
        return pads[padRow];
    }
}
