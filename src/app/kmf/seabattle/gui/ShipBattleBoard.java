package app.kmf.seabattle.gui;

import app.kmf.seabattle.controller.LocalGameManager;
import app.kmf.seabattle.core.datamodel.Shot;
import app.kmf.seabattle.core.logic.descriptors.FleetDescriptor;
import app.kmf.seabattle.core.logic.descriptors.ShipDescriptor;
import app.kmf.seabattle.enums.Orientation;
import app.kmf.seabattle.enums.PlayerType;
import app.kmf.seabattle.enums.ShotResult;
import app.kmf.seabattle.util.settings.GameSettings;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import static app.kmf.seabattle.util.settings.GameSettings.X_SIZE;
import static app.kmf.seabattle.util.settings.GameSettings.Y_SIZE;

public class ShipBattleBoard extends JPanel implements ActionListener {
    private LocalGameManager gameManager = LocalGameManager.getInstance();
    private final ReentrantLock TURN_LOCK = new ReentrantLock();
    private final Condition myTurn = TURN_LOCK.newCondition(), enemyTurn = TURN_LOCK.newCondition();

    private int myGameID, enemyGameID;

    private Cell me[][] = new Cell[X_SIZE + 1][Y_SIZE + 1];
    private Cell enemy[][] = new Cell[X_SIZE + 1][Y_SIZE + 1];

    private ShotResult prevShot;

    private ShipBattleConsole infoPanel;
    private Random generator = new Random();

    public ShipBattleBoard(ShipBattleConsole infoPanel) {
        this.infoPanel = infoPanel;
        draw();
        initGame();
    }

    private void draw() {
        setLayout(new GridLayout(11, 23, 1, 1)); // Set new GridLayout for Ship Battle board

        JLabel letters1[] = new JLabel[10]; // JLabel array of letters A-J
        JLabel numbers1[] = new JLabel[11]; // JLabel array of numbers 1-10
        JLabel letters2[] = new JLabel[10]; // JLabel array of letters A-J
        JLabel numbers2[] = new JLabel[11]; // JLabel array of numbers 1-10

        numbers1[10] = new JLabel(); // Initialize blank number
        add(numbers1[10]); // Add blank number to GridLayout
        numbers2[10] = new JLabel();

        // Initialize JLabels for letters and numbers and add first numbers array to GridLayout
        for (int i = 0; i < X_SIZE; i++) {
            letters1[i] = new JLabel(Character.toString((char) (65 + i)));
            letters1[i].setHorizontalAlignment(SwingConstants.CENTER);
            letters2[i] = new JLabel(Character.toString((char) (65 + i)));
            letters2[i].setHorizontalAlignment(SwingConstants.CENTER);
            numbers1[i] = new JLabel(String.valueOf(i + 1));
            numbers1[i].setHorizontalAlignment(SwingConstants.CENTER);
            numbers2[i] = new JLabel(String.valueOf(i + 1));
            numbers2[i].setHorizontalAlignment(SwingConstants.CENTER);
            add(numbers1[i]);
        }

        add(numbers2[10]); // Add blank number to GridLayout

        // Add second numbers array to GridLayout
        for (int i = 0; i < X_SIZE; i++)
            add(numbers2[i]);

        // Initialize me and enemy grids and add them to GridLayout
        for (int i = 1; i <= X_SIZE; i++) {
            add(letters1[i - 1]); // Add first letters array element

            for (int j = 1; j <= Y_SIZE; j++) {
                add(me[i][j] = new Cell(i, j));
            }

            add(letters2[i - 1]); // Add second letters array element

            for (int j = 1; j <= Y_SIZE; j++) {
                add(enemy[i][j] = new Cell(i, j));
            }
        }

        // Add ActionListener for me
        for (int i = 1; i <= 10; i++)
            for (int j = 1; j <= 10; j++)
                enemy[i][j].addActionListener(this); // Add me button ActionListeners
    }

    private void initGame() {
        infoPanel.println("<<<< Welcome to Sea Battle game! >>>>");
        myGameID = gameManager.createGame_1(PlayerType.PLAYER, "Player");
        enemyGameID = gameManager.createGame_2(PlayerType.ANDROID, "Altron");
        resetBoard();
        infoPanel.println("Building fleets for players...");
        buildMyFleet();
        infoPanel.println("Player fleet is built");
        buildEnemyFleet();
        infoPanel.println("Enemy fleet is built");
        infoPanel.println("Let's start! Good luck, dude :)");

        try {
            TURN_LOCK.lock();
            myTurn.signal();
        } finally {
            TURN_LOCK.unlock();
        }
        Executors.newSingleThreadExecutor().submit(() -> {
                    while (prevShot != ShotResult.GAMEOVER) {
                        try {
                            TURN_LOCK.lock();
                            enemyTurn.await();
                            setEnabledEnemyFleet(false);
                            boolean doShot = true;
                            while (doShot) {
                                infoPanel.print("Altron: ");
                                TimeUnit.SECONDS.sleep(GameSettings.ANDROID_COMPLEXITY);
                                Shot nextShot = Shot.ofAndroid(prevShot);
                                Shot shotResult = gameManager.executeShot(enemyGameID, nextShot);
                                prevShot = shotResult.getCurrent();
                                doShot = markShotResult(me[shotResult.getX()][shotResult.getY()], shotResult.getCurrent());
                            }
                            myTurn.signal();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        } catch (GameOverException e) {
                            infoPanel.println("Altron wins! Don't upset, try one more time ;)");
                            setEnabledEnemyFleet(false);
                            markSurvivedEnemyFleet();
                        } finally {
                            TURN_LOCK.unlock();
                            setEnabledEnemyFleet(true);
                        }
                    }
                }
        );
    }

    private void buildMyFleet() {
        FleetDescriptor myFleet = gameManager.createAutoFleet(myGameID);
        myFleet.getSeaObjectDescriptors().stream().
                map(seaObjectDescriptor -> (ShipDescriptor) seaObjectDescriptor).
                forEach(this::placeShip);
    }

    private void placeShip(ShipDescriptor ship) {
        if (ship.getOrientation() == Orientation.HORIZONTAL) {
            int y = ship.getYValues()[0];
            for (int x : ship.getXValues()) {
                me[x][y].shipPlaced();
            }
        } else {
            int x = ship.getXValues()[0];
            for (int y : ship.getYValues()) {
                me[x][y].shipPlaced();
            }
        }
    }

    private void buildEnemyFleet() {
        gameManager.createAutoFleet(enemyGameID);
    }

    public void actionPerformed(ActionEvent event) {
        try {
            TURN_LOCK.lock();
            infoPanel.print("Player: ");
            Cell target = (Cell) event.getSource();
            Shot shot = Shot.ofPlayer(target.getRow(), target.getColumn());
            Shot result = gameManager.executeShot(myGameID, shot);
            boolean caught = markShotResult(target, result.getCurrent());
            if (!caught) {
                enemyTurn.signal();
            }
        } catch (GameOverException e) {
            infoPanel.println("Player wins! Congratulations!!!");
            setEnabledEnemyFleet(false);
        } finally {
            TURN_LOCK.unlock();
        }
    }

    private boolean markShotResult(Cell target, ShotResult result) throws GameOverException {
        switch (result) {
            case WOUNDED:
                target.shotWounded();
                infoPanel.println("The ship is wounded");
                return true;
            case KILLED:
                target.shotKilled();
                infoPanel.println("The ship has been killed!");
                return true;
            case PAST:
                target.shotPassed();
                infoPanel.println("Missed");
                return false;
            case GAMEOVER:
                target.shotKilled();
                infoPanel.println("Game over!");
                throw new GameOverException();
        }
        return false;
    }

    public void resetBoard() {
        for (int i = 1; i <= 10; i++) {
            for (int j = 1; j <= 10; j++) {
                enemy[i][j].setEnabled(true);
                enemy[i][j].setBackground(Color.LIGHT_GRAY);
                me[i][j].setBackground(Color.LIGHT_GRAY);
            }
        }
    }

    private void setEnabledEnemyFleet(boolean enabled) {
        for (int i = 1; i <= 10; i++) {
            for (int j = 1; j <= 10; j++) {
                enemy[i][j].setEnabled(enabled);
            }
        }
    }

    private void markSurvivedEnemyFleet() {
        FleetDescriptor fleetDescriptor = gameManager.getEnemyFleet(myGameID);
        fleetDescriptor.getSeaObjectDescriptors().stream().map(o -> (ShipDescriptor) o).forEach(ship -> {
            for (int x : ship.getXValues()) {
                for (int y : ship.getYValues()) {
                    if (!enemy[x][y].isDead()) {
                        enemy[x][y].survived();
                    }
                }
            }
        });
    }

    private static class GameOverException extends Exception {

    }
}


