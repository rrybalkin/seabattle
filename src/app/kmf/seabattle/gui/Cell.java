package app.kmf.seabattle.gui;

import javax.swing.*;
import java.awt.*;

/**
 * Created by Roman Rybalkin
 * 14.03.17
 */
public class Cell extends JButton {
    private static final Dimension MAX_SIZE = new Dimension(30, 30);
    private int row, column;
    private boolean dead = false;

    public Cell(int row, int column) {
        this.row = row;
        this.column = column;
        initDefault();
    }

    private void initDefault() {
        this.setPreferredSize(MAX_SIZE);
        this.setEnabled(false);
        this.setBackground(Color.LIGHT_GRAY);
    }

    public int getColumn() {
        return column;
    }

    public int getRow() {
        return row;
    }

    public void shotPassed() {
        this.setBackground(Color.WHITE);
        this.setEnabled(false);
    }

    public void shotWounded() {
        this.setBackground(Color.YELLOW);
        this.dead = true;
    }

    public void shotKilled() {
        this.setBackground(Color.RED);
        this.setEnabled(false);
        this.dead = true;
    }

    public void shipPlaced() {
        this.setBackground(Color.BLUE);
        this.dead = false;
    }

    public void survived() {
        this.setBackground(Color.GREEN);
        this.setEnabled(false);
    }

    public boolean isDead() {
        return dead;
    }
}
