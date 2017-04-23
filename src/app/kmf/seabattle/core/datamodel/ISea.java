package app.kmf.seabattle.core.datamodel;

/**
 * Interface describes object for Sea which aggregates SeaObjects
 *   *it is container for sea drops
 */
public interface ISea {
	/**
	 * @return size of sea by X axis
	 */
	int getXSize();
	/**
	 * @return size of sea by Y axis
	 */
	int getYSize();
	/**
	 * Method for adding SeaDrop objects to Sea object
	 * @param drop - adding SeaDrop object
	 */
	void addSeaDrop(SeaDrop drop);
	/**
	 * @param x - X-coordinate of needed drop
	 * @param y - Y-coordinate of needed drop
	 * @return drop with these coordinates
	 */
	SeaDrop getSeaDrop(int x, int y);
	
	/**
	 * Method for reset all properties of sea drops
	 */
	void reset();
}
