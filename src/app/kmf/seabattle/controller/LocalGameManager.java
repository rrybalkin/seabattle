package app.kmf.seabattle.controller;

import app.kmf.seabattle.core.datamodel.IFleet;
import app.kmf.seabattle.core.datamodel.Shot;
import app.kmf.seabattle.core.logic.descriptors.FleetDescriptor;
import app.kmf.seabattle.core.logic.descriptors.Transformer;
import app.kmf.seabattle.core.logic.strategy.*;
import app.kmf.seabattle.enums.PlayerType;
import app.kmf.seabattle.enums.ShotResult;
import app.kmf.seabattle.util.settings.GameSettings;
import org.json.JSONException;

public class LocalGameManager {
    private static LocalGameManager instance;
    /**
     * Factories for creating strategies.
     */
    private FleetCreatorStrategyFactory fleetCreatorFactory = new FleetCreatorStrategyFactory();
    private ShootingStrategyFactory shootingStrategyFactory = new ShootingStrategyFactory();
    /**
     * Game objects - for player#1 and player#2.
     */
    private Game game_1;
    private Game game_2;

    /**
     * @return instance of this class (singleton)
     */
    public static LocalGameManager getInstance() {
        if (instance == null) {
            instance = new LocalGameManager();
        }

        return instance;
    }

    /**
     * Method for creating Game object for player#1.
     */
    public int createGame_1(PlayerType playerType, String playerID) {
        try {
            game_1 = createGame(playerType, playerID, 1);
            return game_1.getGameID();
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }

    /**
     * Method for creating Game object for player#2.
     */
    public int createGame_2(PlayerType playerType, String playerID) {
        try {
            game_2 = createGame(playerType, playerID, 2);
            return game_2.getGameID();
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }

    /**
     * Method for creating fleet object for player.
     */
    public FleetDescriptor createAutoFleet(int gameID) {
        Game game = getMyGame(gameID);
        IFleet fleet;
        if (game.getFleetCreator() != null) {
            fleet = game.getFleetCreator().createFleet();
        } else {
            int complexity = (game.getPlayerType().equals(PlayerType.ANDROID)) ?
                    GameSettings.ANDROID_COMPLEXITY : GameSettings.USER_COMPLEXITY;
            IFleetCreatorStrategy fleetCreator = fleetCreatorFactory.getAutoStrategyByComplexity(complexity);
            fleet = fleetCreator.createFleet();
            game.setFleetCreator(fleetCreator);
        }
        game.setFleet(fleet);

        return Transformer.transformFleet(fleet);
    }

    public FleetDescriptor createManualFleet(int gameID, FleetDescriptor fleetDescriptor) {
        Game game = getMyGame(gameID);

        IFleet fleet;
        if (game.getFleetCreator() != null) {
            fleet = game.getFleetCreator().createFleet(fleetDescriptor);
        } else {
            IFleetCreatorStrategy fleetCreator = fleetCreatorFactory.getManualStrategy();
            fleet = fleetCreator.createFleet(fleetDescriptor);
            game.setFleetCreator(fleetCreator);
        }

        game.setFleet(fleet);
        return Transformer.transformFleet(fleet);
    }

    /**
     * Method for execute shooting on fleet of rival.
     */
    public Shot executeShot(int gameID, Shot shot) {
        try {
            Game me = getMyGame(gameID);
            Game enemy = getEnemyGame(gameID);

            ShotResult shotResult = null;
            /**
             * If player shoots:
             *   read X and Y coordinates for shot and execute shot on fleet of rival;
             */
            PlayerType playerType = me.getPlayerType();
            if (playerType.equals(PlayerType.PLAYER) || playerType.equals(PlayerType.BLUETOOTH)) {
                shotResult = enemy.fleet.doShot(shot.getX(), shot.getY());
            }
            /**
             * If android shoots:
             *   read last shot result and by using it getting new coordinates for next shot;
             *   execute next shot on fleet of river;
             */
            else if (playerType.equals(PlayerType.ANDROID)) {
                Cell shootingCell;
                if (me.getShootingStrategy() != null) {
                    shootingCell = me.getShootingStrategy().getCoordinatesForShot(shot.getPrev());
                } else {
                    IShootingStrategy shootingStrategy = shootingStrategyFactory.getStrategyByComplexity(GameSettings.ANDROID_COMPLEXITY);
                    me.setShootingStrategy(shootingStrategy);
                    shootingCell = shootingStrategy.getCoordinatesForShot(shot.getPrev());
                }

                int x = shootingCell.getX();
                int y = shootingCell.getY();
                shotResult = enemy.fleet.doShot(x, y);
                return new Shot(x, y, shot.getPrev(), shotResult);
            }

            shot.setCurrent(shotResult);
            return shot;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Method for getting info about fleet of rival.
     */
    public FleetDescriptor getEnemyFleet(int gameID) {
        try {
            return Transformer.transformFleet(getEnemyGame(gameID).fleet);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Method for creating game for 1st player.
     *
     * @return created Game object
     * @throws JSONException
     */
    private Game createGame(PlayerType playerType, String playerID, int gameID) throws JSONException {
        return new Game(gameID, playerID, playerType);
    }

    /**
     * private constructor (singleton)
     */
    private LocalGameManager() {
    }

    private Game getMyGame(int gameID) {
        return (gameID == 1) ? game_1 : game_2;
    }

    private Game getEnemyGame(int gameID) {
        return (gameID == 1) ? game_2 : game_1;
    }
}
