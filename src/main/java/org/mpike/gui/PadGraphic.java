package org.mpike.gui;

import javax.swing.*;
import java.awt.*;

public class PadGraphic extends JPanel {

    public int x;
    public int y;
    public int size;
    public Color color;

    public PadGraphic(int x, int y, int size, Color color) {
        this.x = x;
        this.y = y;
        this.size = size;
        this.color = color;
    }


    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.setColor(this.color);
        g.fillRect(x, y, size, size);
    }
}
