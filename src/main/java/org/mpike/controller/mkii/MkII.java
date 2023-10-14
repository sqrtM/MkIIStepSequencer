package org.mpike.controller.mkii;

import org.mpike.Messenger;
import org.mpike.controller.PhysicalController;

import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Receiver;
import javax.sound.midi.Transmitter;

public class MkII implements PhysicalController {

    private final Receiver RECEIVER;
    private final Transmitter TRANSMITTER;
    private final Messenger messenger;

    public MkII() throws MidiUnavailableException {
        DeviceInitializer deviceInitializer = new DeviceInitializer();


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
        messenger = new Messenger();
        messenger.setReceiver(deviceInitializer.openDeviceReceiver(externalMidiIn));

        System.out.println("all ports configured! Initializing sequencer...");
    }

    public byte[] DefaultSysexMessage() {
        return new byte[]{
                (byte) 0xF0,
                0x00, 0x20, 0x6B, 0x7F, 0x42,
                0x02, 0x00, 0x10, 0x70, 0x14,
                (byte) 0xF7
        };
    }

    public byte hexOffset() {
        // 112
        return (byte) 0x70;
    }

    public int noteOffset() {
        return 36;
    }

    public int padAddress() {
        return 9;
    }

    public int padColor() {
        return 10;
    }

    public Receiver receiver() {
        return RECEIVER;
    }

    public Transmitter transmitter() {
        return TRANSMITTER;
    }

    public Messenger messenger() {
        return messenger;
    }

}
