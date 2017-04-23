package app.kmf.seabattle.core.logic.strategy;

import app.kmf.seabattle.core.datamodel.IFleet;
import app.kmf.seabattle.core.logic.descriptors.FleetDescriptor;

/**
 * Interface describes strategy of creating fleet object
 */
public interface IFleetCreatorStrategy {
	
	/**
	 * Method for creating fleet object
	 * @return created fleet object
	 */
	IFleet createFleet();
	
	/**
	 * Method for case of manual creating a fleet
	 * @param descriptor - description for creating fleet
	 * @return created fleet object
	 */
	IFleet createFleet(FleetDescriptor descriptor);
	
	/**
	 * Method for reset any old properties
	 *   use for reusing objects and to reduce the load on GC
	 */
	void reset();
}
