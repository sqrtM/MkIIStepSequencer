package org.mpike.sequencing;

import org.mpike.Messenger;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.SysexMessage;
import java.util.Vector;

public class Bank extends Thread {

    private final int bankId;
    private final Sequencer sequencer;
    private final Vector<Pad> pads = new Vector<>();
    private int beat;
    private boolean running = true;

    public Bank(int bankId, int bankLength, Sequencer sequencer) {
        this.bankId = bankId;
        this.sequencer = sequencer;
        for (int i = 0; i < bankLength; i++) {
            pads.add(new Pad());
        }
    }

    @Override
    public void run() {
        while (running) {
            beat = beat < this.getPads().size() - 1 ? beat + 1 : 0;
            if (this.pads.get(beat).getStatus().isOn()) {
                try (Messenger messenger = this.sequencer.getMessenger()) {
                    messenger.prepareMessage();
                }
            }
            if (this.bankId == this.sequencer.getActiveMemory()) {
                try {
                    for (int pad = 0; pad < this.getPads().size(); pad++) {
                        SysexMessage sequencerColorMessage = sequencer.buildSequencerColorMessage(pad, bankId, beat);
                        sequencer.send(sequencerColorMessage, -1);
                    }
                    SysexMessage bankSelectionMessage = sequencer.buildBankSelectionMessage(bankId);
                    sequencer.send(bankSelectionMessage, -1);
                } catch (InvalidMidiDataException e) {
                    throw new RuntimeException(e);
                }

            }
            try {
                sleep(300L * (bankId + 1));
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        } while (true);
    }

    public int getBeat() {
        return beat;
    }

    public Vector<Pad> getPads() {
        return pads;
    }

    public void addPad() {
        this.pads.add(new Pad());
    }

    public void removePad() {
        this.pads.removeElementAt(this.pads.size() - 1);
    }

    public void kill() {
        this.running = false;
    }
}
