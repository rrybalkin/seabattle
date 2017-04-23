package app.kmf.seabattle.controller;

import org.json.JSONException;
import org.json.JSONObject;

import app.kmf.seabattle.core.datamodel.IFleet;
import app.kmf.seabattle.core.logic.descriptors.FleetDescriptor;
import app.kmf.seabattle.core.logic.descriptors.Transformer;
import app.kmf.seabattle.core.logic.strategy.Cell;
import app.kmf.seabattle.core.logic.strategy.FleetCreatorStrategyFactory;
import app.kmf.seabattle.core.logic.strategy.IFleetCreatorStrategy;
import app.kmf.seabattle.core.logic.strategy.IShootingStrategy;
import app.kmf.seabattle.core.logic.strategy.ShootingStrategyFactory;
import app.kmf.seabattle.enums.PlayerType;
import app.kmf.seabattle.enums.ShotResult;
import app.kmf.seabattle.util.json.JSONOperations;
import app.kmf.seabattle.util.settings.GameSettings;

public class RemoteGameManager {
	private static RemoteGameManager instance;
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
	public static RemoteGameManager getInstance() {
		if (instance == null) {
			instance = new RemoteGameManager();
		}
		
		return instance;
	}

	/**
	 * Method for creating Game object for player#1.
	 * @param jsonParams - params of creating game
	 * @return params of created game
	 *   "game_id" : indentifier of created game
	 */
	public String createGame_1(String jsonParams) {
		JSONObject answer = null;
		try {
			answer = new JSONObject();
			game_1 = createGame( jsonParams, 1 );
			answer.put("game_id", 1);

			return answer.toString();
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	/**
	 * Method for creating Game object for player#2.
	 * @param jsonParams - params of creating game
	 * @return params of created game
	 *   "game_id" : indentifier of created game
	 */
	public String createGame_2(String jsonParams) {
		JSONObject answer = null;
		try {
			answer = new JSONObject();
			game_2 = createGame( jsonParams, 2 );
			answer.put("game_id", 2);
			
			return answer.toString();
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	/**
	 * Method for creating fleet object for player.
	 * @param jsonParams - params of creating fleet:
	 *   "game_id"          : identifier of game for which fleet will be created
	 *   "create_mode"      : "auto", "manual"
	 *   "fleet_descriptor" : descriptor of fleet (in case manual creating)
	 *   
	 * @return params of created fleet in case auto creating
	 *   "fleet" : fleet object in JSON presentation
	 */
	public String createFleet(String jsonParams) {
		JSONObject answer = null;
		try {
			answer = new JSONObject();
			JSONObject params = JSONOperations.parseJSON(jsonParams);
			
			Integer gameID = params.getInt("game_id");
			Game game = (gameID == 1) ? game_1 : game_2;
			
			String create_mode = params.getString("create_mode");
			
			/**
			 * In this mode Fleet object creating by auto create strategy
			 */
			if ( create_mode.equals("auto") )
			{	
				IFleet fleet = null;
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
				
				answer.put("fleet", Transformer.transformFleet(fleet).toJSON().toString());
			}
			/**
			 * In this mode Fleet object creating by using FleetDescriptor
			 */
			else if ( create_mode.equals("manual") )
			{
				String fleet_descriptor = params.getString("fleet_descriptor");
				JSONObject jsonFleetDescriptor = JSONOperations.parseJSON( fleet_descriptor );
				FleetDescriptor fleetDescriptor = new FleetDescriptor( jsonFleetDescriptor );
				
				IFleet fleet = null;
				if (game.getFleetCreator() != null) {
					fleet = game.getFleetCreator().createFleet(fleetDescriptor);
				} else {
					IFleetCreatorStrategy fleetCreator = fleetCreatorFactory.getManualStrategy();
					fleet = fleetCreator.createFleet( fleetDescriptor );
					game.setFleetCreator(fleetCreator);
				}
				
				game.setFleet(fleet);
			}
			
			return answer.toString();
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}	
	}
	
	/**
	 * Method for execute shooting on fleet of rival.
	 * @param jsonParams - parameters of shot:
	 *   "game_id" : identified of game
	 *   "player_type" : "player", "android", "bluetooth"
	 * if player_type = player:
	 *   "shot_x" : X-coordinate of shot
	 *   "shot_y" : Y-coordinate of shot
	 * if player_type = android:
	 *   "last_shot_result" : shot result of last shot;
	 *   
	 * @return params and result of executing shot:
	 *   "shot_x" : X-coordinate of current shot;
	 *   "shot_y" : Y-coordinate of current shot;
	 *   "shot_result" : result of current shot 
	 *                   - "past", "wounded", "killed", "gameover";
	 */
	public String executeShot(String jsonParams) {
		JSONObject answer = null;
		try {
			answer = new JSONObject();
			
			JSONObject params = JSONOperations.parseJSON(jsonParams);
			PlayerType playerType = PlayerType.create( params.getString("player_type") );
			Integer gameID = params.getInt("game_id");
			
			Game my_game = (gameID == 1) ? 
					game_1 : game_2;
			Game foreign_game = (gameID == 1) ? 
					game_2 : game_1;
			
			int x = 0, y = 0;
			ShotResult shotResult = null;
			/**
			 * If player shoots:
			 *   read X and Y coordinates for shot and execute shot on fleet of river;
			 */
			if ( playerType.equals(PlayerType.PLAYER) ) 
			{
				x = params.getInt("shot_x");
				y = params.getInt("shot_y");
				
				shotResult = foreign_game.fleet.doShot( x, y );
			}
			/**
			 * If android shoots:
			 *   read last shot result and by using it getting new coordinates for next shot;
			 *   execute next shot on fleet of river;
			 */
			else if ( playerType.equals(PlayerType.ANDROID) ) 
			{
				String lash_shot_result = params.getString("last_shot_result");
				ShotResult lastShotResult = ShotResult.create( lash_shot_result.toLowerCase() );
				
				Cell shootingCell = null;
				if (my_game.getShootingStrategy() != null) {
					shootingCell = my_game.getShootingStrategy().getCoordinatesForShot(lastShotResult);
				} else {
					IShootingStrategy shootingStrategy = shootingStrategyFactory.getStrategyByComplexity(GameSettings.ANDROID_COMPLEXITY);
					my_game.setShootingStrategy(shootingStrategy);
					
					shootingCell = shootingStrategy.getCoordinatesForShot(lastShotResult);
				}
				
				x = shootingCell.getX();
				y = shootingCell.getY();
				shotResult = foreign_game.fleet.doShot( x, y );
			}
			/**
			 * If player via bluetooth shoots:
			 *   read X and Y coordinates for shot and execute shot on fleet of river;
			 */
			else if ( playerType.equals(PlayerType.BLUETOOTH) )
			{
				x = params.getInt("shot_x");
				y = params.getInt("shot_y");
				
				shotResult = foreign_game.fleet.doShot( x, y );
			}
			else
			{
				answer.put("error", "unknown \"player_type\" property value");
			}
			
			if ( shotResult != null )
				answer.put("shot_result", shotResult.toString().toLowerCase());
			answer.put("shot_x", x);
			answer.put("shot_y", y);
			
			return answer.toString();
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	/**
	 * Method for getting info about fleet of river.
	 * @param jsonParams - params for getting fleet of river:
	 *   "game_id" : identifier of game current player
	 * 
	 * @return info about fleet of river:
	 *   "fleet" : river's fleet object in string presentation;
	 *   "fleet_descriptor" : river's FleetDescriptor object in JSON presentation;
	 */
	public String getRiverFleet(String jsonParams) {
		JSONObject answer = null;
		try {
			answer = new JSONObject();
			
			JSONObject params = JSONOperations.parseJSON(jsonParams);
			int gameID = params.getInt("game_id");
			
			Game river_game = (gameID == 1) ?
					game_2 : game_1;
			
			String fleet_str = river_game.fleet.toString();
			FleetDescriptor fleetDescriptor = Transformer.transformFleet(river_game.fleet);
			JSONObject fleet_descriptor = fleetDescriptor.toJSON();
			
			answer.put("fleet", fleet_str);
			answer.put("fleet_descriptor", fleet_descriptor.toString());
			
			return answer.toString();			
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	/**
	 * Method for creating game for 1st player.
	 * @param gameID identifier of game
	 * @param jsonParams JSON object with parameters:
	 *   "player_type" : "player" , "android" , "bluetooth"
	 *     "player_id" : identifier of player
	 *
	 * @return created Game object
	 * @throws JSONException 
	 */
	private Game createGame(String jsonParams, int gameID) throws JSONException {
		JSONObject params = JSONOperations.parseJSON(jsonParams);

		PlayerType playerType = PlayerType.create( params.getString("player_type") );
		String playerID = null;

		if ( playerType.equals(PlayerType.PLAYER) )
		{
			playerID = params.getString("player_id");
		}
		else if ( playerType.equals(PlayerType.ANDROID) )
		{
			playerID = "android";
		}
		else if ( playerType.equals(PlayerType.BLUETOOTH) )
		{
			playerID = "bluetooth";
		}

		return new Game( gameID, playerID, playerType );
	}
	
	/**
	 *  private constructor (singleton)
	 */
	private RemoteGameManager() {}
}
