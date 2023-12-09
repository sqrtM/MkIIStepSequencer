package org.mpike.gui;

import org.mpike.sequencing.Sequencer;

import javax.sound.midi.InvalidMidiDataException;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Vector;

public class GUIWindow extends JFrame {

    public void run(SequencerGUI gui) {
        EventQueue.invokeLater(() -> {
            JFrame window = new JFrame();
            window.setTitle("MkII Step Sequencer by mason :)");
            window.setSize(840, 560);
            window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            window.add(createMenuBar(gui), BorderLayout.NORTH);
            window.add(gui);
            window.setVisible(true);
        });
    }

    private JMenuBar createMenuBar(SequencerGUI gui) {
        JMenuBar menuBar = new JMenuBar();

        JMenu fileMenu = new JMenu("File");
        JMenuItem exitMenuItem = new JMenuItem("Exit");
        exitMenuItem.addActionListener(e -> System.exit(0));
        fileMenu.add(exitMenuItem);

        JMenu banksMenu = new JMenu("Banks");

        JMenu addMenu = new JMenu("Add");
        for (int i = 1; i <= 16; i++) {
            int index = i;
            JMenuItem addItem = new JMenuItem(Integer.toString(index));
            addItem.addActionListener(e -> gui.addBank(index));
            addMenu.add(addItem);
        }

        JMenu removeMenu = new JMenu("Remove");
        for (int i = 1; i <= 16; i++) {
            int index = i;
            JMenuItem removeItem = new JMenuItem(Integer.toString(index));
            removeItem.addActionListener(e -> gui.removeBank(index));
            removeMenu.add(removeItem);
        }

        banksMenu.add(addMenu);
        banksMenu.add(removeMenu);

        menuBar.add(fileMenu);
        menuBar.add(banksMenu);

        return menuBar;
    }


    public static class SequencerGUI extends JPanel {

        private final Sequencer sequencer;
        private final JButton[][] stepButtons;

        public SequencerGUI(Sequencer sequencer) {
            this.sequencer = sequencer;
            Vector<Integer> bankLengths = sequencer.getBankLengths();
            int numRows = bankLengths.size();
            int numCols = 16;

            setLayout(new GridLayout(numRows, numCols));

            stepButtons = new JButton[numRows][numCols];

            for (int row = 0; row < numRows; row++) {
                for (int col = 0; col < numCols; col++) {
                    stepButtons[row][col] = new JButton();
                    Color color = col >= sequencer.getBank(row).getPads().size() ? Color.RED : Color.WHITE;
                    stepButtons[row][col].setBackground(color);
                    add(stepButtons[row][col]);

                    int bankIndex = row;
                    int padIndex = col;

                    // Left Click
                    stepButtons[row][col].addActionListener(e -> {
                        if (!(padIndex >= sequencer.getBank(bankIndex).getPads().size())) {
                            try {
                                sequencer.updateFromGui(bankIndex, padIndex);
                            } catch (InvalidMidiDataException ex) {
                                throw new RuntimeException(ex);
                            }
                        }
                    });
                    // Right Click
                    stepButtons[row][col].addMouseListener(new MouseAdapter() {
                        @Override
                        public void mouseClicked(MouseEvent e) {
                            if (SwingUtilities.isRightMouseButton(e)) {
                                showPopupMenu(e.getComponent(), bankIndex, padIndex);
                            }
                        }
                    });
                }
            }

            setVisible(true);

            Thread t = new Thread(() -> {
                try {
                    while (true) {
                        Thread.sleep(100);
                        updatePads();
                    }
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            });
            t.start();
        }

        private void showPopupMenu(Component invoker, int bankIndex, int padIndex) {
            JPopupMenu popupMenu = new JPopupMenu();

            JMenu padMenu = new JMenu("Pad");
            JMenuItem togglePadStateItem = new JMenuItem("Toggle Pad State");
            togglePadStateItem.addActionListener(e -> togglePadState(bankIndex, padIndex));
            padMenu.add(togglePadStateItem);

            JMenu bankMenu = getBankMenu(bankIndex);

            JMenu sequencerMenu = new JMenu("Sequencer");
            JMenuItem generalSettingsItem = new JMenuItem("General Settings");
            generalSettingsItem.addActionListener(e -> modifySequencer());
            sequencerMenu.add(generalSettingsItem);

            popupMenu.add(padMenu);
            popupMenu.add(bankMenu);
            popupMenu.add(sequencerMenu);

            popupMenu.show(invoker, 0, invoker.getHeight());
        }

        private JMenu getBankMenu(int bankIndex) {
            JMenu bankMenu = new JMenu("Bank");

            JMenu addPadsSubMenu = new JMenu("Add Pads");
            for (int i = 1; i <= 16; i++) {
                int padsToAdd = i;
                JMenuItem addPadsItem = new JMenuItem(Integer.toString(padsToAdd));
                addPadsItem.addActionListener(e -> addPadsToBank(bankIndex, padsToAdd));
                addPadsSubMenu.add(addPadsItem);
            }

            JMenu removePadsSubMenu = getPadsSubMenu(bankIndex);

            bankMenu.add(addPadsSubMenu);
            bankMenu.add(removePadsSubMenu);

            JMenuItem modifyBankItem = new JMenuItem("Modify Bank");
            modifyBankItem.addActionListener(e -> modifyBank(bankIndex));
            bankMenu.add(modifyBankItem);
            return bankMenu;
        }

        private JMenu getPadsSubMenu(int bankIndex) {
            JMenu removePadsSubMenu = new JMenu("Remove Pads");
            for (int i = 1; i <= 16; i++) {
                int padsToRemove = i;
                JMenuItem removePadsItem = new JMenuItem(Integer.toString(padsToRemove));
                removePadsItem.addActionListener(e -> {
                    try {
                        removePadsFromBank(bankIndex, padsToRemove);
                    } catch (InvalidMidiDataException ex) {
                        throw new RuntimeException(ex);
                    }
                });
                removePadsSubMenu.add(removePadsItem);
            }
            return removePadsSubMenu;
        }

        private void addPadsToBank(int bankIndex, int padsToAdd) {
            System.out.println("Add " + padsToAdd + " Pads to Bank " + bankIndex);
            for (int i = 0; i < padsToAdd; i++) {
                this.sequencer.getBank(bankIndex).addPad();
            }
        }

        private void removePadsFromBank(int bankIndex, int padsToRemove) throws InvalidMidiDataException {
            for (int i = 0; i < padsToRemove; i++) {
                this.sequencer.getBank(bankIndex).removePad();
                stepButtons[bankIndex][sequencer.getBank(bankIndex).getPads().size()].setBackground(Color.RED);
            }
            this.sequencer.clearPads();
        }

        private void togglePadState(int bankIndex, int padIndex) {
            System.out.println("Toggle Pad State for Bank " + bankIndex + ", Pad " + padIndex);
        }

        private void modifyBank(int bankIndex) {
            System.out.println("Modify Bank " + bankIndex);
        }

        private void modifySequencer() {
            System.out.println("Modify Sequencer General Settings");
        }

        public void addBank(int index) {
            System.out.println("Add Bank " + index);
        }

        public void removeBank(int index) {
            System.out.println("Remove Bank " + index);
        }

        public void updatePads() {
            for (int row = 0; row < stepButtons.length; row++) {
                int bankLength = sequencer.getBank(row).getPads().size();
                int numCols = Math.min(16, bankLength);

                for (int col = 0; col < numCols; col++) {
                    Color color;
                    boolean padState = sequencer.getBank(row).getPads().get(col).getStatus().isOn();
                    int currentBeat = sequencer.getBeatFromBank(row);

                    if (padState) {
                        color = currentBeat == col ? Color.WHITE : Color.PINK;
                    } else {
                        color = currentBeat == col ? Color.GREEN : Color.BLUE;
                    }
                    stepButtons[row][col].setBackground(color);
                }
            }
        }
    }
}