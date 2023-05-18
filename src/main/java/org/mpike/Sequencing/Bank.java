package org.mpike.Sequencing;

import org.mpike.Color;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.SysexMessage;
import java.util.Arrays;

public class Bank extends Thread {

    private final byte[] mkiiDefaultSysexMessage = {
            (byte) 0xF0,
            0x00, 0x20, 0x6B, 0x7F, 0x42,
            0x02, 0x00, 0x10, 0x70, 0x14,
            (byte) 0xF7
    };

    private final int NOTE_OFFSET = 36;
    private final byte HEX_OFFSET = 0x70; // 112

    private final int PAD_ADDRESS = 9;
    private final int PAD_COLOR = 10;

    private final byte inactiveOffColor = Color.CYAN.getColor();
    private final byte inactiveOnColor = Color.PURPLE.getColor();
    private final byte activeOnColor = Color.WHITE.getColor();
    private final byte activeOffColor = Color.GREEN.getColor();
    private final byte bankColor = Color.BLUE.getColor();

    private final int bankId;
    private final int bankLength;
    private final Sequencer sequencer;
    private int beat;

    public Bank(int bankId, int bankLength, Sequencer sequencer) {
        this.bankId = bankId;
        this.bankLength = bankLength;
        this.sequencer = sequencer;
    }

    private SysexMessage constructSysexMessage(byte[] outgoingMessage) throws InvalidMidiDataException {
        SysexMessage msg = new SysexMessage();
        msg.setMessage(outgoingMessage, mkiiDefaultSysexMessage.length);
        return msg;
    }

    private SysexMessage buildSequencerColorMessage(int pad) throws InvalidMidiDataException {
        byte[] outgoingMessage = mkiiDefaultSysexMessage.clone();
        if (sequencer.pads(bankId)[pad]) {
            outgoingMessage[PAD_COLOR] = beat == pad ? activeOnColor : inactiveOnColor;
        } else {
            outgoingMessage[PAD_COLOR] = beat == pad ? activeOffColor : inactiveOffColor;
        }
        outgoingMessage[PAD_ADDRESS] = (byte) (pad + HEX_OFFSET);
        System.out.println(Arrays.toString(outgoingMessage));
        return constructSysexMessage(outgoingMessage);
    }

    private SysexMessage buildBankSelectionMessage() throws InvalidMidiDataException {
        byte[] outgoingMessage = mkiiDefaultSysexMessage.clone();
        outgoingMessage[PAD_COLOR] = bankColor;
        outgoingMessage[PAD_ADDRESS] = (byte) (HEX_OFFSET + bankLength + bankId);
        System.out.println(Arrays.toString(outgoingMessage));
        return constructSysexMessage(outgoingMessage);
    }

    @Override
    public void run() {
        do {
            beat = beat < bankLength - 1 ? beat + 1 : 0;
            if (this.bankId == this.sequencer.activeMemory) {
                try {
                    for (int pad = 0; pad < bankLength; pad++) {
                        SysexMessage msg = buildSequencerColorMessage(pad);
                        sequencer.send(msg, -1);
                    }
                    SysexMessage bankSelectionMessage = buildBankSelectionMessage();
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
