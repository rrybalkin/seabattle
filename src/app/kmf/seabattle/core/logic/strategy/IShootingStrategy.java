package app.kmf.seabattle.core.logic.strategy;

import app.kmf.seabattle.enums.ShotResult;

/**
 * Interface describes strategy for shooting on fleet
 */
public interface IShootingStrategy {
	
	/**
	 * Method generates coordinates for next shot 
	 *   based on result of last shot
	 * @param lastShotResult - result of last shot
	 * @return coordinates for new shot by Cell object
	 */
	Cell getCoordinatesForShot(ShotResult lastShotResult);
	
	/**
	 * Method for reset any old properties
	 *   use for reusing objects and to reduce the load on GC
	 */
	void reset();
}
