package org.mpike;

import org.mpike.Sequencing.Sequencer;
import org.mpike.mkii.MkII;

import javax.sound.midi.*;

public class Main {
    public static void main(String[] args) throws InvalidMidiDataException, MidiUnavailableException {

        // for now, only MKII is supported,
        // but this is being developed with extensibility in mind...
        MkII mkII = new MkII();
        int[] banksArray = {4, 7, 5, 3, 8, 8, 12, 6};
        try (Sequencer sequencer = new Sequencer(mkII, banksArray)) {
            mkII.transmitter().setReceiver(sequencer);
            System.out.println("Sequencer initialized. Starting.");
        }
    }
}
