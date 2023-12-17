package org.mpike.sequencing;

import org.mpike.Messenger;
import org.mpike.controller.PhysicalController;
import org.mpike.controller.mkii.Color;
import org.mpike.controller.mkii.ColorType;

import javax.sound.midi.*;
import java.util.Vector;

public class Sequencer implements Receiver {

    private final PhysicalController mkii;
    private final Vector<Bank> banks = new Vector<>();
    private int activeMemory = 0;

    public Sequencer(PhysicalController physCon, int[] bankLengths) {
        this.mkii = physCon;
        for (int i = 0; i < bankLengths.length; i++) {
            this.banks.add(new Bank(i, bankLengths[i], this));
        }
    }

    /**
     * @param message   the MIDI message to send
     * @param timeStamp the time-stamp for the message, in microseconds
     * @apiNote This is probably better referred to as "handle," rather than "send".
     * This takes an incoming message, either from the Bank or from the physical
     * controller itself, and sends it DIRECTLY to the controller.
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
            if (message.getMessage()[1] >= mkii.noteOffset() + this.banks.get(activeMemory).getPads().size()) {
                // ... is it out of range?...
                if (message.getMessage()[1] >= mkii.noteOffset() + this.banks.size() + this.banks.get(activeMemory).getPads().size()) {
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

    public void start() throws InvalidMidiDataException {
        for (Bank bank : this.banks) {
            bank.start();
        }
        clearPads();
    }

    private void togglePad(MidiMessage incomingMessage) throws InvalidMidiDataException {
        int padAddress = incomingMessage.getMessage()[1] - mkii.noteOffset();
        this.banks.get(activeMemory).getPads().get(padAddress).toggleStatus();
        SysexMessage msg = buildSequencerColorMessage(padAddress, activeMemory, -1);
        send(msg, -1);
    }

    private void changeActiveMemoryBank(MidiMessage incomingMessage) throws InvalidMidiDataException {
        activeMemory = incomingMessage.getMessage()[1] - mkii.noteOffset() - banks.get(activeMemory).getPads().size();
        clearPads();
    }

    public void clearPads() throws InvalidMidiDataException {
        byte[] message = mkii.DefaultSysexMessage();
        message[mkii.padColor()] = Color.NONE.getColor();
        // start at the end of the active memory array
        for (int i = 0; i < 16; i++) {
            if (i < this.banks.get(activeMemory).getPads().size()) {
                if (this.banks.get(activeMemory).getPads().get(i).getStatus().isOn()) {
                    message[mkii.padColor()] = this.banks.get(activeMemory).getBeat() == i ? mkii.getColor(ColorType.ACTIVE_ON).getColor() : mkii.getColor(ColorType.INACTIVE_ON).getColor();
                } else {
                    message[mkii.padColor()] = this.banks.get(activeMemory).getBeat() == i ? mkii.getColor(ColorType.ACTIVE_OFF).getColor() : mkii.getColor(ColorType.INACTIVE_OFF).getColor();
                }
            } else if (this.banks.get(activeMemory).getPads().size() - i == activeMemory) {
                // @todo: there's a weird lag with this, but not the others. Why?
                message[mkii.padColor()] = mkii.getColor(ColorType.BANK).getColor();
                // is the pad outside the selector range?
            } else if (i >= this.banks.size() + this.banks.get(activeMemory).getPads().size()) {
                message[mkii.padColor()] = mkii.getColor(ColorType.INACCESSIBLE).getColor();
            } else {
                message[mkii.padColor()] = Color.NONE.getColor();
            }
            message[mkii.padAddress()] = (byte) (i + mkii.hexOffset());
            SysexMessage msg = mkii.constructSysexMessage(message);
            send(msg, -1);
        }

    }

    SysexMessage buildSequencerColorMessage(int pad, int bankId, int beat) throws InvalidMidiDataException {
        byte[] outgoingMessage = mkii.DefaultSysexMessage();
        if (this.banks.get(bankId).getPads().get(pad).getStatus().isOn()) {
            outgoingMessage[mkii.padColor()] = beat == pad ? mkii.getColor(ColorType.ACTIVE_ON).getColor() : mkii.getColor(ColorType.INACTIVE_ON).getColor();
        } else {
            outgoingMessage[mkii.padColor()] = beat == pad ? mkii.getColor(ColorType.ACTIVE_OFF).getColor() : mkii.getColor(ColorType.INACTIVE_OFF).getColor();
        }
        outgoingMessage[mkii.padAddress()] = (byte) (pad + mkii.hexOffset());
        return mkii.constructSysexMessage(outgoingMessage);
    }

    SysexMessage buildBankSelectionMessage(int bankId) throws InvalidMidiDataException {
        byte[] outgoingMessage = mkii.DefaultSysexMessage();
        outgoingMessage[mkii.padColor()] = mkii.getColor(ColorType.BANK).getColor();
        outgoingMessage[mkii.padAddress()] = (byte) (mkii.hexOffset() + this.banks.get(bankId).getPads().size() + bankId);
        return mkii.constructSysexMessage(outgoingMessage);
    }

    public int getActiveMemory() {
        return activeMemory;
    }

    public Messenger getMessenger() {
        return this.mkii.messenger();
    }

    public void setColor(ColorType colorType, Color color) {
        this.mkii.setColor(colorType, color);
    }

    public Color getColor(ColorType colorType) {
        return this.mkii.getColor(colorType);
    }

    public Vector<Integer> getBankLengths() {
        Vector<Integer> bankLengths = new Vector<>();
        for (Bank bank : this.banks) {
            bankLengths.add(bank.getPads().size());
        }
        return bankLengths;
    }

    @Override
    public void close() {
    }

    public Vector<Pad> pads(int padRow) {
        return banks.get(padRow).getPads();
    }

    public Bank getBank(int bank) {
        return banks.get(bank);
    }

    public void updateFromGui(int bank, int pad) throws InvalidMidiDataException {
        this.banks.get(bank).getPads().get(pad).toggleStatus();
        if (bank == activeMemory && pad < getBankLengths().get(activeMemory)) {
            this.send(this.buildSequencerColorMessage(pad, bank, this.getBeatFromBank(bank)), -1);
        }
    }

    public int getBeatFromBank(int bank) {
        return this.banks.get(bank).getBeat();
    }

    public int getTotalPadsPerRow() {
        return this.mkii.padsPerRow();
    }

    public Bank addNewBank() {
        Bank newBank = new Bank(this.banks.size(), 8, this);
        this.banks.add(newBank);
        return newBank;
    }
}
