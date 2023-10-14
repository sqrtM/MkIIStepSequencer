package org.mpike.gui;

import org.mpike.sequencing.Sequencer;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import javax.swing.JFrame;
import javax.swing.JPanel;

public class GUIWindow extends JFrame {

    public static class SequencerGUI extends JPanel {

        private final List<PadGraphic[]> fillCells;
        private boolean[][] padsGUI;
        private final int columns;
        private final int rows;
        private final int rectSize = 40;

        public SequencerGUI(Sequencer sequencer) {
            int[] bankLengths = sequencer.getBankLengths();
            this.columns = getNumberOfColumns(bankLengths);
            this.rows = bankLengths.length + 1;
            fillCells = new ArrayList<PadGraphic[]>();
            for (int i = 0; i < rows; i++) {
                PadGraphic[] row = new PadGraphic[columns];
                for (int j = 0; j < columns; j++) {
                    row[j] = new PadGraphic(
                            (rectSize * j) + rectSize,
                            (rectSize * i) + rectSize,
                            rectSize,
                            Color.cyan);
                }
                fillCells.add(row);
            }
        }

        // stinky!!!!!!!!!!
        private int getNumberOfColumns(int[] arr) {
            Integer[] arrInt = new Integer[arr.length];
            Arrays.setAll(arrInt, i -> arr[i]);
            List<Integer> list = Arrays.asList(arrInt);
            Collections.sort(list);
            return list.get(arr.length - 1) + 1;
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            this.getGraphics().setColor(Color.cyan);
            for (int i = rectSize; i <= columns * rectSize; i += rectSize) {
                g.drawLine(i, rectSize, i, columns * rectSize);
            }

            for (int i = rectSize; i <= rows * rectSize; i += rectSize) {
                g.drawLine(rectSize, i, rows * columns * rectSize, i);
            }
        }

        public void updatePads(boolean[][] pads) {
            if (!Arrays.deepEquals(this.padsGUI, pads)) {
                this.padsGUI = pads;
                for (int i = 0; i < pads.length; i++) {
                    for (int j = 0; j < pads[i].length; j++) {
                        if (pads[i][j]) {
                            fillCells.get(i)[j].color = Color.cyan;
                        } else {
                            fillCells.get(i)[j].color = Color.green;
                        }
                    }
                }
            }
            repaint();
            System.out.println(fillCells.toString());
        }

    }

    public void main(SequencerGUI gui) {
        EventQueue.invokeLater(() -> {
            JFrame window = new JFrame();
            window.setSize(840, 560);
            window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            window.add(gui);
            window.setVisible(true);
        });
    }

}

