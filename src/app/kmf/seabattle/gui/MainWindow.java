package app.kmf.seabattle.gui;

import app.kmf.seabattle.util.settings.GameSettings;

import javax.swing.*;
import javax.swing.text.DefaultCaret;
import java.awt.*;

/**
 * Created by Roman Rybalkin
 * 13.03.17
 */
public class MainWindow {

    public static void main(String[] args) {
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                try {
                    GameSettings.ANDROID_COMPLEXITY = 3;
                    GameSettings.USER_COMPLEXITY = 1;
                    JFrame frame = new JFrame();
                    frame.setTitle("Sea Battle");
                    frame.setBounds(100, 100, 700, 500);
                    frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
                    JTextArea console = buildConsoleArea();
                    ShipBattleBoard window = new ShipBattleBoard(new ShipBattleConsole(console));
                    Container box = new JPanel();
                    box.setLayout(new BoxLayout(box, BoxLayout.Y_AXIS));
                    box.add(window);
                    box.add(new JScrollPane(console));
                    frame.setContentPane(box);
                    frame.setVisible(true);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private static JTextArea buildConsoleArea() {
        JTextArea console = new JTextArea();
        console.setRows(10);
        console.setBackground(Color.CYAN);
        DefaultCaret caret = (DefaultCaret) console.getCaret();
        caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
        return console;
    }
}
