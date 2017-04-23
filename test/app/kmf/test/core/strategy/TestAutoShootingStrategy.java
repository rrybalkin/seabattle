package app.kmf.test.core.strategy;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import app.kmf.seabattle.core.logic.strategy.IShootingStrategy;
import app.kmf.seabattle.core.logic.strategy.ShootingStrategyFactory;
import app.kmf.seabattle.core.logic.strategy.Cell;
import app.kmf.seabattle.enums.ShotResult;
import app.kmf.seabattle.util.settings.GameSettings;

public class TestAutoShootingStrategy {
	public static void main(String[] args) throws IOException {
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		GameSettings.ANDROID_COMPLEXITY = 3;
		
		ShootingStrategyFactory factory = new ShootingStrategyFactory();
		IShootingStrategy strategy = factory.getStrategyByComplexity(GameSettings.ANDROID_COMPLEXITY);
		
		String cmd = null;
		ShotResult res = null;
		do {
			Cell shot = strategy.getCoordinatesForShot(res);
			if (shot != null) {
				System.out.println("Shot: x = " + shot.getX() + " y = " + shot.getY());
			}
			
			do {
				System.out.print("Input result of shot: ");
				cmd = br.readLine();
				
				if (cmd.equals("past")) 
					res = ShotResult.PAST;
				else if (cmd.equals("wounded")) 
					res = ShotResult.WOUNDED;
				else if (cmd.equals("killed"))
					res = ShotResult.KILLED;
				else if (cmd.equals("gameover"))
					res = ShotResult.GAMEOVER;
				else if (cmd.equals("exit"))
					break;
				else {
					System.out.println("Unknow result of shooting! Try againg...");
					res = null;
				}
			} while (res == null);
			
		} while (!cmd.equals("exit"));
	}
}
