package org.mpike.controller.mkii;

import javax.sound.midi.*;
import java.util.Scanner;
import java.util.Vector;

import static java.lang.Integer.parseInt;
import static javax.sound.midi.MidiSystem.getMidiDevice;

class DeviceInitializer {

    Scanner in = new Scanner(System.in);

    MidiDevice.Info[] findMidiDevices() {

        Vector<MidiDevice.Info> midiDeviceInfo = new Vector<>();
        MidiDevice.Info[] allMidiDeviceInfo = MidiSystem.getMidiDeviceInfo();

        for (int i = 0; i < allMidiDeviceInfo.length; i++) {
            midiDeviceInfo.add(allMidiDeviceInfo[i]);
            System.out.println(i + ".) " + allMidiDeviceInfo[i]);
        }
        return allMidiDeviceInfo;
    }

    MidiDevice selectMidiIn(MidiDevice.Info[] allMidiDeviceInfo) throws MidiUnavailableException {

        System.out.println("Please select a valid MIDI IN port from the list above.");
        MidiDevice.Info selectedDeviceInfo = allMidiDeviceInfo[parseInt(in.nextLine())];
        System.out.println(selectedDeviceInfo + " selected. Connecting...");
        return getMidiDevice(selectedDeviceInfo);
    }

    MidiDevice selectMidiOut(MidiDevice.Info[] allMidiDeviceInfo) throws MidiUnavailableException {

        System.out.println("Please select a valid MIDI Out port from the list above.");
        MidiDevice.Info selectedDeviceInfo = allMidiDeviceInfo[parseInt(in.nextLine())];
        System.out.println(selectedDeviceInfo + " selected. Connecting...");
        return getMidiDevice(selectedDeviceInfo);
    }

    MidiDevice selectExternalReceiver(MidiDevice.Info[] allMidiDeviceInfo) throws MidiUnavailableException {
        System.out.println("Please select a valid, external, MIDI In port from the list above.");
        MidiDevice.Info selectedDeviceInfo = allMidiDeviceInfo[parseInt(in.nextLine())];
        System.out.println(selectedDeviceInfo + " selected. Connecting...");
        return getMidiDevice(selectedDeviceInfo);
    }

    void openMidiDevice(MidiDevice selectedDevice) throws MidiUnavailableException {

        if (!(selectedDevice).isOpen()) {
            try {
                System.out.println("Attempting to open " + selectedDevice.getDeviceInfo() + "...");
                selectedDevice.open();
                System.out.println("Success.");
            } catch (MidiUnavailableException e) {
                throw new MidiUnavailableException();
            }
        }
    }

    Receiver openDeviceReceiver(MidiDevice selectedDevice) throws MidiUnavailableException {
        return selectedDevice.getReceiver();
    }

    Transmitter openDeviceTransmitter(MidiDevice selectedDevice) throws MidiUnavailableException {
        return selectedDevice.getTransmitter();
    }
}
