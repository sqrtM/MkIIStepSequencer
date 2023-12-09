package org.mpike;

import com.formdev.flatlaf.intellijthemes.FlatArcDarkIJTheme;
import org.mpike.controller.mkii.MkII;
import org.mpike.gui.GUIWindow;
import org.mpike.sequencing.Sequencer;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiUnavailableException;

public class Main {
    public static void main(String[] args) throws InvalidMidiDataException, MidiUnavailableException {

        // for now, only MKII is supported,
        // but this is being developed with extensibility in mind...
        MkII mkII = new MkII();

        try (Sequencer sequencer = new Sequencer(mkII, new int[]{8, 8, 8, 8, 8, 8, 8, 8})) {
            mkII.transmitter().setReceiver(sequencer);
            System.out.println("Sequencer initialized. Starting.");
            sequencer.start();

            FlatArcDarkIJTheme.setup();
            GUIWindow window = new GUIWindow();
            GUIWindow.SequencerGUI gui = new GUIWindow.SequencerGUI(sequencer);
            window.run(gui);
        }
    }
}
