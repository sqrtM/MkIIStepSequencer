package org.mpike.Sequencing;

import org.mpike.mkii.Color;
import org.mpike.mkii.MkII;

import javax.sound.midi.*;

public class Sequencer implements Receiver {

    private final int numberOfBanks;
    public int activeMemory = 0;
    private final boolean[][] pads;
    private final MkII mkii = MkII.getPhysicalController();

    public Sequencer(int[] bankLengths) throws InvalidMidiDataException {
        this.numberOfBanks = bankLengths.length;
        this.pads = new boolean[numberOfBanks][];
        for (int i = 0; i < numberOfBanks; i++) {
            (new Bank(i, bankLengths[i], this)).start();
            this.pads[i] = new boolean[bankLengths[i]];
        }
        clearPads();
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
            // ... then it's already formatted. We just send it.
            mkii.receiver().send(message, -1);
        }

        // If it comes from the controller ...
        if (message instanceof ShortMessage) {
            // ... we check if it's a bank switch message...
            if (message.getMessage()[1] >= mkii.noteOffset() + pads(activeMemory).length) {
                // ... is it out of range?...
                if (message.getMessage()[1] >= mkii.noteOffset() + numberOfBanks + pads(activeMemory).length) {
                    try {
                        clearPads();
                        System.out.println("outside of range :)");
                    } catch (InvalidMidiDataException e) {
                        throw new RuntimeException(e);
                    }
                    // ... if it's in range...
                } else {
                    try {
                        changeActiveMemoryBank(message);
                        System.out.println("active mem is now " + activeMemory);
                    } catch (InvalidMidiDataException e) {
                        throw new RuntimeException(e);
                    }
                }
                // ... or a toggle pad message.
            } else {
                try {
                    togglePad(message);
                } catch (InvalidMidiDataException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    private void togglePad(MidiMessage incomingMessage) throws InvalidMidiDataException {
        int padAddress = incomingMessage.getMessage()[1] - mkii.noteOffset();
        pads[activeMemory][padAddress] = !pads[activeMemory][padAddress];
        SysexMessage msg = buildSequencerColorMessage(padAddress, activeMemory, -1);
        send(msg, -1);
    }

    private void changeActiveMemoryBank(MidiMessage incomingMessage) throws InvalidMidiDataException {
        activeMemory = incomingMessage.getMessage()[1] - mkii.noteOffset() - pads[activeMemory].length;
        clearPads();
    }

    private void clearPads() throws InvalidMidiDataException {
        byte[] message = mkii.DefaultSysexMessage();
        message[mkii.padColor()] = Color.NONE.getColor();
        // start at the end of the active memory array
        for (int i = 0; i < 16; i++) {
            if (i < pads(activeMemory).length) {
                message[mkii.padColor()] = Color.inactiveOffColor();
            // is the pad the active memory selector pad?
            } else if (pads(activeMemory).length - i == activeMemory) {
                message[mkii.padColor()] = Color.bankColor();
            // is the pad outside the selector range?
            } else if (i >= numberOfBanks + pads(activeMemory).length) {
                message[mkii.padColor()] = Color.inaccessibleBankColor();
            } else {
                message[mkii.padColor()] = Color.noColor();
            }
            message[mkii.padAddress()] = (byte) (i + mkii.hexOffset());
            SysexMessage msg = constructSysexMessage(message);
            send(msg, -1);
        }
    }

    SysexMessage constructSysexMessage(byte[] outgoingMessage) throws InvalidMidiDataException {
        SysexMessage msg = new SysexMessage();
        msg.setMessage(outgoingMessage, mkii.DefaultSysexMessage().length);
        return msg;
    }

    SysexMessage buildSequencerColorMessage(int pad, int bankId, int beat) throws InvalidMidiDataException {
        byte[] outgoingMessage = mkii.DefaultSysexMessage();
        if (pads(bankId)[pad]) {
            outgoingMessage[mkii.padColor()] = beat == pad ? Color.activeOnColor() : Color.inactiveOnColor();
        } else {
            outgoingMessage[mkii.padColor()] = beat == pad ? Color.activeOffColor() : Color.inactiveOffColor();
        }
        outgoingMessage[mkii.padAddress()] = (byte) (pad + mkii.hexOffset());
        return constructSysexMessage(outgoingMessage);
    }

    @Override
    public void close() {
    }

    public boolean[] pads(int padRow) {
        return pads[padRow];
    }
}
