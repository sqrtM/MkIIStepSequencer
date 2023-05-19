package org.mpike.Sequencing;

import org.mpike.Messenger;
import org.mpike.PhysicalController;
import org.mpike.mkii.Color;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.SysexMessage;

public class Bank extends Thread {

    private final PhysicalController physCon;
    private final int bankId;
    private final int bankLength;
    private final Sequencer sequencer;
    private final Messenger messenger = Messenger.getMessenger();

    private int beat;

    public Bank(int bankId, int bankLength, Sequencer sequencer, PhysicalController physCon) {
        this.bankId = bankId;
        this.bankLength = bankLength;
        this.sequencer = sequencer;
        this.physCon = physCon;
    }

    /**
     * I think this function is unnecessary. Can probably be removed.
     * @param bankId
     * @return
     * @throws InvalidMidiDataException
     */
    private SysexMessage buildBankSelectionMessage(int bankId) throws InvalidMidiDataException {
        byte[] outgoingMessage = physCon.DefaultSysexMessage();
        outgoingMessage[physCon.padColor()] = Color.bankColor();
        outgoingMessage[physCon.padAddress()] = (byte) (physCon.hexOffset() + sequencer.pads(bankId).length + bankId);
        return sequencer.constructSysexMessage(outgoingMessage);
    }

    @Override
    public void run() {
        do {
            beat = beat < bankLength - 1 ? beat + 1 : 0;
            if (sequencer.pads(bankId)[beat]) {
                messenger.prepareMessage();
            }
            if (this.bankId == this.sequencer.activeMemory) {
                try {
                    for (int pad = 0; pad < bankLength; pad++) {
                        SysexMessage sequencerColorMessage = sequencer.buildSequencerColorMessage(pad, bankId, beat);
                        sequencer.send(sequencerColorMessage, -1);
                    }
                    SysexMessage bankSelectionMessage = buildBankSelectionMessage(bankId);
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
}
