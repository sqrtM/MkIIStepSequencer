package org.mpike.gui;

import org.mpike.sequencing.Sequencer;

import java.awt.*;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.*;

public class GUIWindow extends JFrame {

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
                    Color color = col >= sequencer.pads(row).length ? Color.RED : Color.WHITE;
                    stepButtons[row][col].setBackground(color);
                    add(stepButtons[row][col]);

                    int bankIndex = row;
                    int padIndex = col;
                    stepButtons[row][col].addActionListener(e -> {
                        if (!(padIndex >= sequencer.pads(bankIndex).length)) {
                            sequencer.updateFromGui(bankIndex, padIndex);
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

        public void updatePads() {
            int numRows = stepButtons.length;

            for (int row = 0; row < numRows; row++) {
                int bankLength = sequencer.pads(row).length;
                int numCols = Math.min(16, bankLength);

                for (int col = 0; col < numCols; col++) {
                    Color color;
                    boolean padState = sequencer.pads(row)[col];
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

}


