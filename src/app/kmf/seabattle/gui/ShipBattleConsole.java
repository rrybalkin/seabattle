package app.kmf.seabattle.gui;

import javax.swing.*;

/**
 * Created by Roman Rybalkin
 * 13.03.17
 */
public class ShipBattleConsole {
    private JTextArea console;

    public ShipBattleConsole(JTextArea console) {
        this.console = console;
    }

    public void print(String message) {
        this.console.append(message);
    }

    public void println(String message) {
        this.console.append(message + "\n");
    }

    public void clear() {
        this.console.setText("");
    }
}
