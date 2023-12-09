package org.mpike.gui;

import org.mpike.sequencing.Sequencer;

import javax.sound.midi.InvalidMidiDataException;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class GUIWindow extends JFrame {

    public void run(SequencerGUI gui) {
        EventQueue.invokeLater(() -> {
            JFrame window = new JFrame();
            window.setTitle("MkII Step Sequencer by mason :)");
            window.setSize(840, 560);
            window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            window.add(gui);
            window.setVisible(true);
        });
    }

    public static class SequencerGUI extends JPanel {

        private final Sequencer sequencer;
        private final JButton[][] stepButtons;

        public SequencerGUI(Sequencer sequencer) {
            this.sequencer = sequencer;
            int[] bankLengths = sequencer.getBankLengths();
            int numRows = bankLengths.length;
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

                    // Add ActionListener for left-click
                    stepButtons[row][col].addActionListener(e -> {
                        if (!(padIndex >= sequencer.getBank(bankIndex).getPads().size())) {
                            try {
                                sequencer.updateFromGui(bankIndex, padIndex);
                            } catch (InvalidMidiDataException ex) {
                                throw new RuntimeException(ex);
                            }
                        }
                    });

                    // Add MouseAdapter for right-click
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
            JMenuItem menuItem = new JMenuItem("Toggle Pad State");

            menuItem.addActionListener(e -> System.out.println("ckick!!!"));

            popupMenu.add(menuItem);
            popupMenu.show(invoker, 0, invoker.getHeight());
        }

        public void updatePads() {
            int numRows = stepButtons.length;

            for (int row = 0; row < numRows; row++) {
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