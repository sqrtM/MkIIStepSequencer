package org.mpike;

import org.mpike.Sequencing.Sequencer;
import org.mpike.mkii.MkII;

import javax.sound.midi.*;

public class Main {
    public static void main(String[] args) throws InvalidMidiDataException {

        MkII mkII = MkII.getPhysicalController();
        int[] banksArray = {4, 7, 5, 3, 8, 8, 12, 6};
        try (Sequencer sequencer = new Sequencer(banksArray)) {
            mkII.transmitter().setReceiver(sequencer);
            System.out.println("Sequencer initialized. Starting.");
        }
    }
}
