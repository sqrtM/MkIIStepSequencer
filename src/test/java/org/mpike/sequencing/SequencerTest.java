package org.mpike.sequencing;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.ShortMessage;
import java.util.Arrays;
import java.util.Random;

class SequencerTest {

    private final Sequencer sequencer;
    int[] randomBankLengths;
    Random random = new Random();

    SequencerTest() throws InvalidMidiDataException {
        int numberOfBankLengths = random.nextInt(1, 16);
        this.randomBankLengths = new int[numberOfBankLengths];
        for (int i = 0; i < numberOfBankLengths; i++) {
            randomBankLengths[i] = random.nextInt(1, 16);
        }
        System.out.println("the bank lengths are " + Arrays.toString(randomBankLengths));
        sequencer = new Sequencer(new FakeMkii(), randomBankLengths);
        sequencer.start();
    }

    @org.junit.jupiter.api.Test
    void constructSysexMessage() throws InvalidMidiDataException {
        sequencer.constructSysexMessage(new byte[]{
                (byte) 0xF0,
                0x00, 0x20, 0x6B, 0x7F, 0x42,
                0x02, 0x00, 0x10, 0x70, 0x14,
                (byte) 0xF7
        });
    }

    @org.junit.jupiter.api.Test
    void send() {
        MidiMessage msg = new ShortMessage();
        sequencer.send(msg, -1);
    }

    @org.junit.jupiter.api.Test
    void buildSequencerColorMessage() throws InvalidMidiDataException {

        sequencer.buildSequencerColorMessage(0, 0, 0);
    }

    @org.junit.jupiter.api.Test
    void pads() {
        for (int i = 0; i < sequencer.getBankLengths().length; i++) {
            assert (sequencer.pads(i).size() == randomBankLengths[i]);
        }
    }
}