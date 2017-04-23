package app.kmf.seabattle.core.datamodel;

import app.kmf.seabattle.enums.ShotResult;

public interface IFleet {
	
	/**
	 * This method doing shot on player's fleet
	 * @param x - X coordinate of shot
	 * @param y - Y coordinate of shot
	 * @return result of shooting by ResultShot
	 */
	ShotResult doShot(int x, int y);
}
