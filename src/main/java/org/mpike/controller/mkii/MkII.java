package org.mpike.controller.mkii;

import org.mpike.Messenger;
import org.mpike.controller.PhysicalController;

import javax.sound.midi.*;

public class MkII implements PhysicalController {

    private final Receiver RECEIVER;
    private final Transmitter TRANSMITTER;
    private final Messenger messenger;

    public MkII() throws MidiUnavailableException, InvalidMidiDataException {
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
        setAllPadsToToggle();
    }

    private void setAllPadsToToggle() throws InvalidMidiDataException {
        for (byte i = 0; i < this.totalPads(); i++) {
            SysexMessage changeModeToNote = constructSysexMessage(new byte[]{
                    (byte) 0xF0,
                    0x00, 0x20, 0x6B, 0x7F, 0x42,
                    0x02, 0x00, 0x01, (byte) (0x70 + i), 0x09,
                    (byte) 0xF7
            });
            SysexMessage changeBehaviorToToggle = constructSysexMessage(new byte[]{
                    (byte) 0xF0,
                    0x00, 0x20, 0x6B, 0x7F, 0x42,
                    0x02, 0x00, 0x06, (byte) (0x70 + i), 0x00,
                    (byte) 0xF7
            });
            this.RECEIVER.send(changeModeToNote, -1);
            this.RECEIVER.send(changeBehaviorToToggle, -1);
        }
    }

    public SysexMessage constructSysexMessage(byte[] outgoingMessage) throws InvalidMidiDataException {
        assert outgoingMessage.length == this.DefaultSysexMessage().length;
        SysexMessage msg = new SysexMessage();
        msg.setMessage(outgoingMessage, this.DefaultSysexMessage().length);
        return msg;
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

    public int padsPerRow() {
        return 16;
    }

    public int totalPads() {
        return 16;
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
