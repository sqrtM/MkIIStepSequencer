package org.mpike.mkii;

import org.mpike.Messenger;
import org.mpike.PhysicalController;

import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Receiver;
import javax.sound.midi.Transmitter;

public class MkII implements PhysicalController {

    private final byte[] defaultSysexMessage = {
            (byte) 0xF0,
            0x00, 0x20, 0x6B, 0x7F, 0x42,
            0x02, 0x00, 0x10, 0x70, 0x14,
            (byte) 0xF7
    };

    private final Receiver RECEIVER;
    private final Transmitter TRANSMITTER;

    private final int NOTE_OFFSET = 36;
    private final byte HEX_OFFSET = 0x70; // 112

    private final int PAD_ADDRESS = 9;
    private final int PAD_COLOR = 10;

    public MkII() throws MidiUnavailableException {
        DeviceInitializer deviceInitializer = new DeviceInitializer();
        Messenger messenger = Messenger.getMessenger();

        // get list of all midi ports
        MidiDevice.Info[] allMidiDeviceInfo = deviceInitializer.findMidiDevices();

        // select midiIn, attempt to open the port, then open the receiver
        MidiDevice selectedMidiIn = deviceInitializer.selectMidiIn(allMidiDeviceInfo);
        deviceInitializer.openMidiDevice(selectedMidiIn);
        RECEIVER = deviceInitializer.openDeviceReceiver(selectedMidiIn);

        // select midiOut, attempt to open the port, then open the transmitter
        MidiDevice selectedMidiOut = deviceInitializer.selectMidiOut(allMidiDeviceInfo);
        deviceInitializer.openMidiDevice(selectedMidiOut);
        TRANSMITTER = deviceInitializer.openDeviceTransmitter(selectedMidiOut);

        //FINALLY, select the external receiver...
        MidiDevice externalMidiIn = deviceInitializer.selectExternalReceiver(allMidiDeviceInfo);
        deviceInitializer.openMidiDevice(externalMidiIn);
        messenger.setReceiver(deviceInitializer.openDeviceReceiver(externalMidiIn));

        System.out.println("all ports configured! Initializing sequencer...");
    }

    public byte[] DefaultSysexMessage() {
        return defaultSysexMessage;
    }

    public byte hexOffset() {
        return HEX_OFFSET;
    }

    public int noteOffset() {
        return NOTE_OFFSET;
    }

    public int padAddress() {
        return PAD_ADDRESS;
    }

    public int padColor() {
        return PAD_COLOR;
    }

    public Receiver receiver() { return RECEIVER; }

    public Transmitter transmitter() { return TRANSMITTER; }

}
