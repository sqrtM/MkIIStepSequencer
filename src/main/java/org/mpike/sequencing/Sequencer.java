package org.mpike.sequencing;

import org.mpike.controller.PhysicalController;
import org.mpike.controller.mkii.Color;

import javax.sound.midi.*;
import java.util.Vector;

public class Sequencer implements Receiver {

    private final PhysicalController mkii;
    private final Vector<Bank> banks = new Vector<>();
    private int activeMemory = 0;

    public Sequencer(PhysicalController physCon, int[] bankLengths) {
        this.mkii = physCon;
        for (int i = 0; i < bankLengths.length; i++) {
            this.banks.add(new Bank(i, bankLengths[i], this, this.mkii));
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
                    message[mkii.padColor()] = this.banks.get(activeMemory).getBeat() == i ? Color.activeOnColor() : Color.inactiveOnColor();
                } else {
                    message[mkii.padColor()] = this.banks.get(activeMemory).getBeat() == i ? Color.activeOffColor() : Color.inactiveOffColor();
                }
            } else if (this.banks.get(activeMemory).getPads().size() - i == activeMemory) {
                // @todo: there's a weird lag with this, but not the others. Why?
                message[mkii.padColor()] = Color.bankColor();
                // is the pad outside the selector range?
            } else if (i >= this.banks.size() + this.banks.get(activeMemory).getPads().size()) {
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
        assert outgoingMessage.length == mkii.DefaultSysexMessage().length;
        SysexMessage msg = new SysexMessage();
        msg.setMessage(outgoingMessage, mkii.DefaultSysexMessage().length);
        return msg;
    }

    SysexMessage buildSequencerColorMessage(int pad, int bankId, int beat) throws InvalidMidiDataException {
        byte[] outgoingMessage = mkii.DefaultSysexMessage();
        if (this.banks.get(bankId).getPads().get(pad).getStatus().isOn()) {
            outgoingMessage[mkii.padColor()] = beat == pad ? Color.activeOnColor() : Color.inactiveOnColor();
        } else {
            outgoingMessage[mkii.padColor()] = beat == pad ? Color.activeOffColor() : Color.inactiveOffColor();
        }
        outgoingMessage[mkii.padAddress()] = (byte) (pad + mkii.hexOffset());
        return constructSysexMessage(outgoingMessage);
    }

    public int getActiveMemory() {
        return activeMemory;
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

    public int getControllerPadRows() {
        return this.mkii.totalRows();
    }

    public int getTotalPadsPerRow() {
        return this.mkii.padsPerRow();
    }

    public Bank addNewBank() {
        Bank newBank = new Bank(this.banks.size(), 8, this, mkii);
        this.banks.add(newBank);
        return newBank;
    }
}
