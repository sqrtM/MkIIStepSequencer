package org.mpike.gui;

import org.mpike.controller.mkii.Color;
import org.mpike.controller.mkii.ColorType;
import org.mpike.sequencing.Bank;
import org.mpike.sequencing.Sequencer;

import javax.sound.midi.InvalidMidiDataException;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
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

        JMenu banksMenu = buildBanksMenu(gui);
        JMenu colorsMenu = buildColorsMenu(gui);

        menuBar.add(fileMenu);
        menuBar.add(banksMenu);
        menuBar.add(colorsMenu);

        return menuBar;
    }

    private JMenu buildColorsMenu(SequencerGUI gui) {
        JMenu colorsMenu = new JMenu("Colors");

        for (ColorType colorType : ColorType.values()) {
            JMenu colorSubMenu = new JMenu(colorType.toString());
            for (Color color : Color.values()) {
                JMenuItem colorItem = new JCheckBoxMenuItem(color.toString());
                colorItem.addActionListener(e -> {
                    gui.changeColor(colorType, color);
                    for (Component component : colorSubMenu.getMenuComponents()) {
                        ((JCheckBoxMenuItem) component).setState(false);
                    }
                    ((JCheckBoxMenuItem) e.getSource()).setState(true);
                });
                colorSubMenu.add(colorItem);
                // auto check the default option
                if (color == gui.sequencer.getColor(colorType)) {
                    colorItem.setSelected(true);
                }
            }
            colorsMenu.add(colorSubMenu);
        }
        return colorsMenu;
    }

    private JMenu buildBanksMenu(SequencerGUI gui) {
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
        return banksMenu;
    }


    public static class SequencerGUI extends JPanel {
        private final Sequencer sequencer;
        private final ArrayList<ArrayList<JButton>> stepButtons;

        public SequencerGUI(Sequencer sequencer) {
            this.sequencer = sequencer;
            Vector<Integer> bankLengths = sequencer.getBankLengths();
            int numRows = bankLengths.size();
            int numCols = this.sequencer.getTotalPadsPerRow();

            setLayout(new GridLayout(numRows, numCols));

            stepButtons = new ArrayList<>();

            for (int row = 0; row < numRows; row++) {
                stepButtons.add(new ArrayList<>());
                for (int col = 0; col < numCols; col++) {
                    initNewButton(row, col);
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

        private void initNewButton(int row, int col) {
            stepButtons.get(row).add(new JButton());
            Color color = col >= sequencer.getBank(row).getPads().size() ? sequencer.getColor(ColorType.INACCESSIBLE) : Color.WHITE;
            stepButtons.get(row).get(col).setBackground(color.toAwtColor());
            add(stepButtons.get(row).get(col));

            // Left Click
            stepButtons.get(row).get(col).addActionListener(e -> {
                if (!(col >= sequencer.getBank(row).getPads().size())) {
                    try {
                        sequencer.updateFromGui(row, col);
                    } catch (InvalidMidiDataException ex) {
                        throw new RuntimeException(ex);
                    }
                }
            });
            // Right Click
            stepButtons.get(row).get(col).addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if (SwingUtilities.isRightMouseButton(e)) {
                        showPopupMenu(e.getComponent(), row, col);
                    }
                }
            });
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
                stepButtons.get(bankIndex).get(sequencer.getBank(bankIndex).getPads().size()).setBackground(sequencer.getColor(ColorType.INACCESSIBLE).toAwtColor());
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

        public void addBank(int banksToAdd) {
            for (int i = 0; i < banksToAdd; i++) {
                Bank bank = this.sequencer.addNewBank();
                this.stepButtons.add(new ArrayList<>());
                for (int j = 0; j < this.sequencer.getTotalPadsPerRow(); j++) {
                    this.initNewButton(this.stepButtons.size() - 1, j);
                }
                bank.start();
            }
            setLayout(new GridLayout(this.stepButtons.size(), this.sequencer.getTotalPadsPerRow()));
            revalidate();
            repaint();
        }

        public void changeColor(ColorType colorType, Color color) {
            this.sequencer.setColor(colorType, color);
            updatePads();
        }

        public void removeBank(int index) {
            System.out.println("Remove Bank " + index);
        }

        public void updatePads() {
            for (int row = 0; row < stepButtons.size(); row++) {
                int bankLength = sequencer.getBank(row).getPads().size();
                int numCols = Math.min(sequencer.getTotalPadsPerRow(), bankLength);

                for (int col = 0; col < sequencer.getTotalPadsPerRow(); col++) {
                    Color color;
                    if (col < numCols) {
                        boolean padState = sequencer.getBank(row).getPads().get(col).getStatus().isOn();
                        int currentBeat = sequencer.getBeatFromBank(row);

                        if (padState) {
                            color = currentBeat == col ? sequencer.getColor(ColorType.ACTIVE_ON) : sequencer.getColor(ColorType.INACTIVE_ON);
                        } else {
                            color = currentBeat == col ? sequencer.getColor(ColorType.ACTIVE_OFF) : sequencer.getColor(ColorType.INACTIVE_OFF);
                        }
                        stepButtons.get(row).get(col).setBackground(color.toAwtColor());
                    } else {
                        stepButtons.get(row).get(col).setBackground(sequencer.getColor(ColorType.INACCESSIBLE).toAwtColor());
                    }
                }
            }
        }
    }
}