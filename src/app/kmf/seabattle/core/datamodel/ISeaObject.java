package app.kmf.seabattle.core.datamodel;

import java.util.List;

/**
 * Interface describes object which can be use on Sea Object
 */
public interface ISeaObject {
	
	/**
	 * @return amount of cells for this object
	 */
	int getSize();
	
	/**
	 * This method use for aggregate SeaDrop objects by this SeaObject
	 * @param drops - aggregating SeaDrop objects
	 */
	void aggregateDrops(List<SeaDrop> drops);
	
	/**
	 * This method use for aggregate SeaDrop object by this SeaObject
	 * @param drop - aggregating SeaDrop object
	 */
	void aggregateDrop(SeaDrop drop);
	
	/**
	 * Method for checking to containing SeaDrop object in this SeaObject
	 * @param drop - checking SeaDrop object
	 * @return true - if SeaObject contains this drop, false - if it not contains
	 */
	boolean containsDrop(SeaDrop drop);
	
	/**
	 * @return amount surving drops of this sea object
	 */
	int getCntSurvivedDrops();
	
	/**
	 * Method for reset all old properties
	 */
	void reset();
}
