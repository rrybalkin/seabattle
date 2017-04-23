package app.kmf.test.controller;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.json.JSONException;
import org.json.JSONObject;

import app.kmf.seabattle.controller.RemoteGameManager;
import app.kmf.seabattle.util.json.JSONOperations;
import app.kmf.seabattle.util.settings.GameSettings;

public class TestGameManager {
	private static RemoteGameManager gm;
	
	public static void main(String[] args) throws JSONException, NumberFormatException, IOException {
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		GameSettings.ANDROID_COMPLEXITY = 3;
		gm = RemoteGameManager.getInstance();
		
		System.out.println("Hello, it's test for module \"Game Manager\" ...\n");
		System.out.println("Rules:\nCoordinates for shot enter separated by space: x y");
		System.out.print("Shot result enter as word, possible variants: ");
		System.out.println("\"past\", \"wounded\", \"killed\", \"gameover\"");
		System.out.println("\nPlayer#1: type = android, name = Droid");
		System.out.print("Palyer#2: type = player, name = ");
		String player_id;
		do {
			player_id = br.readLine();
		} while (player_id == null || player_id.length() == 0);
		
		JSONObject game_params_1 = new JSONObject();
		game_params_1.put("player_type", "android");
		game_params_1.put("player_id", "Droid");
		
		JSONObject json = JSONOperations.parseJSON(gm.createGame_1(game_params_1.toString()));
		int gameID_1 = json.getInt("game_id");
		
		JSONObject game_params_2 = new JSONObject();
		game_params_2.put("player_type", "player");
		game_params_2.put("player_id", player_id);
		
		json = JSONOperations.parseJSON(gm.createGame_2(game_params_2.toString()));
		int gameID_2 = json.getInt("game_id");
		
		JSONObject create_fleet_1 = new JSONObject();
		create_fleet_1.put("game_id", gameID_1);
		create_fleet_1.put("create_mode", "auto");
		json = JSONOperations.parseJSON(gm.createFleet(create_fleet_1.toString()));
		
		JSONObject create_fleet_2 = new JSONObject();
		create_fleet_2.put("game_id", gameID_2);
		create_fleet_2.put("create_mode", "auto");
		json = JSONOperations.parseJSON(gm.createFleet(create_fleet_2.toString()));
		
		System.out.println("\nLoad game...  Let's go! \n");
		
		String android_shot_result = "";
		String player_shot_result = "";
		while ( true ) {
			
			// if android last shot was success - then player skips step
			if ((android_shot_result.equals("wounded")) || (android_shot_result.equals("killed"))) 
			{
				
			} else {
				JSONObject player_shot = new JSONObject();
				player_shot.put("game_id", gameID_2);
				player_shot.put("player_type", "player");
				int x, y;
				do {
					try {
						System.out.print(player_id + "/ enter coordinates for shot: ");
						String line = br.readLine();
						x = Integer.valueOf(line.split(" ")[0]);
						y = Integer.valueOf(line.split(" ")[1]);
					} catch (Exception e) {
						x = 0;
						y = 0;
					}
				} while (x == 0 || y == 0);
				
				player_shot.put("shot_x", x).put("shot_y", y);
				
				json = JSONOperations.parseJSON( gm.executeShot(player_shot.toString()) );
				player_shot_result = json.getString("shot_result");
				System.out.println(player_id + "/ shot result = " + player_shot_result);
			}
			
			if (processGameOver( player_shot_result, player_id ))
				break;
			
			// if player last shot was success - then android skips step
			if ((player_shot_result.equals("wounded")) || (player_shot_result.equals("killed")))
			{
				
			} else {
				JSONObject android_shot = new JSONObject();
				android_shot.put("game_id", gameID_1);
				android_shot.put("player_type", "android");
				android_shot.put("last_shot_result", android_shot_result);
				
				json = JSONOperations.parseJSON( gm.executeShot(android_shot.toString()) );
				System.out.println("Droid/ coordinates for shot: x = " + json.getInt("shot_x") + ", y = " + json.getInt("shot_y"));
				
				do {
					System.out.print("Droid/ result of shot: ");
					android_shot_result = br.readLine();
				} while (!android_shot_result.equals("past")
						&& !android_shot_result.equals("wounded")
						&& !android_shot_result.equals("killed")
						&& !android_shot_result.equals("gameover")
						);
			}
			
			if (processGameOver( android_shot_result, "Droid"))
				break;
		}
	}
	
	private static boolean processGameOver(String res, String player) throws JSONException {
		if ( res.equalsIgnoreCase("gameover") ) {
			// game end - present of winner
			if (player.equals("Droid")) {
				System.out.println("\nAndroid is WINNER! Don't worry!\nInfo about fleet of Android:");	
				String jsonFleet = gm.getRiverFleet(new JSONObject().put("game_id", 2).toString());
				String fleet = JSONOperations.parseJSON(jsonFleet).getString("fleet");
				System.out.println(fleet);
			} else {
				System.out.println("\n" + player + " is WINNER! Congratulations !*!*!*");
			}			
			
			return true;
		}
		return false;
	}
}
