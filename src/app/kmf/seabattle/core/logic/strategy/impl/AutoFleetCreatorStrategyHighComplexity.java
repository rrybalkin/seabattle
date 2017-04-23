package app.kmf.seabattle.core.logic.strategy.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import app.kmf.seabattle.core.datamodel.IFleet;
import app.kmf.seabattle.core.datamodel.ISea;
import app.kmf.seabattle.core.datamodel.ISeaObject;
import app.kmf.seabattle.core.datamodel.SeaDrop;
import app.kmf.seabattle.core.datamodel.impl.Fleet;
import app.kmf.seabattle.core.datamodel.impl.Sea;
import app.kmf.seabattle.core.datamodel.impl.Ship;
import app.kmf.seabattle.core.logic.descriptors.FleetDescriptor;
import app.kmf.seabattle.core.logic.strategy.IFleetCreatorStrategy;
import app.kmf.seabattle.enums.Orientation;
import app.kmf.seabattle.enums.ShipType;
import app.kmf.seabattle.util.settings.GameSettings;

public class AutoFleetCreatorStrategyHighComplexity implements IFleetCreatorStrategy {
	private boolean debug = false;
	
	private IFleet fleet;
	private ISea sea;
	private List<ISeaObject> seaObjects;

	private Random randomGenerator;
	/**
	 * Number of possible variants for locate ships on sea.
	 */
	private static final int NUM_VARIANTS = 7;
	/**
	 * List of free sea objects which are not located on sea yet.
	 */
	private List<ISeaObject> freeObjects;
	/**
	 * Matrix represents sea.
	 *   If cell is free - value is 0, if cell was aggregated - value is 1.
	 *   For example:
	 *      1 2 3 4 5 6 7 8 9 10
	 *     
	 *   1  0 0 0 0 0 0 0 0 0 0
	 *	 2  0 1 1 1 0 0 0 0 0 0
     *   3  0 1 1 1 0 0 0 0 0 0
     *   4  0 1 1 1 0 0 0 0 0 0
     *   5  0 0 0 1 1 1 0 0 0 0
     *   6  0 0 0 1 1 1 0 0 0 0
     *   7  0 0 0 1 1 1 0 0 0 0
     *   8  0 0 0 1 1 1 0 0 0 0
     *   9  0 0 0 1 1 1 0 0 0 0
     *  10  0 0 0 0 0 0 0 0 0 0
     *  
     *  There is 2 ships - BOAT (3,3) and CRUISER( (5,6),(5,7),(5,8) )
	 */
	private int[][] fakeSea;
	
	public AutoFleetCreatorStrategyHighComplexity() {
		System.out.println("AutoFleetCreatorStrategyHighComplexity constructor ...");
		init();
	}
	
	public IFleet createFleet() {
		buildFleet();
		
		return fleet;
	}

	public IFleet createFleet(FleetDescriptor descriptor) {
		return this.createFleet();
	}

	public void reset() {
		sea.reset();
		
		for (ISeaObject obj : seaObjects)
			obj.reset();
	}
	
	private void init() {
		randomGenerator = new Random();
	}
	
	private void buildFleet() {
		if (sea == null)
			buildSea();
		if (seaObjects == null )
			buildSeaObjects();
		
		locateShips();
		
		fleet = new Fleet( sea, seaObjects );
	}
	
	/**
	 * Method for building Sea object.
	 *   There starts 2 cycles for X- and Y-coordinates and forming SeaDrop objects,
	 *   which will be aggregating in Sea object.
	 */
	private void buildSea() {
		sea = new Sea( GameSettings.X_SIZE, GameSettings.Y_SIZE );
		
		int dropID = 1;
		for (int x = 1; x <= sea.getXSize(); x++)
			for (int y = 1; y <= sea.getYSize(); y++) {
				SeaDrop drop = new SeaDrop(dropID, x, y);
				sea.addSeaDrop(drop);
				
				dropID++;
			}
	}
	
	/**
	 * Method for creating SeaObject objects - ships for example.
	 *   There only created sea objects, and aggregating sea drops will be in method "locateShips".
	 *   By default: create 1 LINKOR, 2 CRUISERS, 3 DESTROYERS, 4 BOATS
	 */
	private void buildSeaObjects() {
		seaObjects = new ArrayList<ISeaObject>();
		
		ISeaObject linkor = new Ship(ShipType.LINKOR);
		seaObjects.add(linkor);
		
		ISeaObject cruiser_1 = new Ship(ShipType.CRUISER);
		ISeaObject cruiser_2 = new Ship(ShipType.CRUISER);
		seaObjects.add(cruiser_1);
		seaObjects.add(cruiser_2);
		
		ISeaObject destroyer_1 = new Ship(ShipType.DESTROYER);
		ISeaObject destroyer_2 = new Ship(ShipType.DESTROYER);
		ISeaObject destroyer_3 = new Ship(ShipType.DESTROYER);
		seaObjects.add(destroyer_1);
		seaObjects.add(destroyer_2);
		seaObjects.add(destroyer_3);
		
		ISeaObject boat_1 = new Ship(ShipType.BOAT);
		ISeaObject boat_2 = new Ship(ShipType.BOAT);
		ISeaObject boat_3 = new Ship(ShipType.BOAT);
		ISeaObject boat_4 = new Ship(ShipType.BOAT);
		seaObjects.add(boat_1);
		seaObjects.add(boat_2);
		seaObjects.add(boat_3);
		seaObjects.add(boat_4);
	}
	
	/**
	 * Method locates ships on the sea.
	 * There uses some variants for locating ships, variant choosing as random.
	 */
	private void locateShips() {
		int variant = randomGenerator.nextInt(NUM_VARIANTS) + 1;
		Orientation o = Orientation.create( randomGenerator.nextInt(2) );
		
		// collects all sea object with size > 1, boats will be locate later
		freeObjects = new ArrayList<ISeaObject>(seaObjects.size());
		for (ISeaObject obj : seaObjects) {
			if (obj.getSize() > 1)
				freeObjects.add(obj);
		}
		// forming fake sea
		fakeSea = new int[sea.getYSize()][sea.getXSize()];
		for (int x = 0; x < sea.getXSize(); x++) {
			for (int y = 0; y < sea.getYSize(); y++) {
				fakeSea[y][x] = 0;
			}
		}
		
		if (debug) System.out.println("Variant = " + variant);
		execVariantByNumber(variant, o);
		// locate boats
		locateBoats();
	}
	/**
	 * Method locates only boats.
	 * Logic of method:
	 *   - collects all boats in array - boats;
	 *   - start cycle on boats and for every collects array from free cells;
	 *   - for every boat random choosing one cell from available;
	 *   - boat aggregates chosen cell.
	 */
	private void locateBoats() {
		List<ISeaObject> boats = new ArrayList<ISeaObject>();
		for (ISeaObject obj : seaObjects) {
			if (obj.getSize() == 1) boats.add(obj);
		}
		
		for (ISeaObject boat : boats) {
			List<SeaDrop> freeDrops = new ArrayList<SeaDrop>();
			for (int x = 0; x < sea.getXSize(); x++) {
				for (int y = 0; y < sea.getYSize(); y++) {
					if (fakeSea[y][x] == 0) 
						freeDrops.add(sea.getSeaDrop(x+1, y+1));
				}
			}
			
			SeaDrop drop = freeDrops.get( randomGenerator.nextInt(freeDrops.size()) );
				
			aggregateCells(drop.getX(), drop.getY(), boat.getSize(), Orientation.HORIZONTAL /*no difference*/, true, boat);
		}
	}
	
	/**
	 * Method for execute choosing variant of locating sea objects.
	 * @param variant - number of variant;
	 * @param o - locating orientation;
	 */
	private void execVariantByNumber(int variant, Orientation o) {
		switch (variant) {
		case 1:
			variant_1(o);
			break;
		case 2:
			variant_2(o);
			break;
		case 3:
			variant_3(o);
			break;
		case 4:
			variant_4(0);
			break;
		case 5:
			variant_4(90);
			break;
		case 6:
			variant_4(180);
			break;
		case 7:
			variant_4(270);
			break;
		}
	}
	
	/**
	 * Variant of locating #1
	 * @param o - location orientation;
	 * Ships are located on the right (top) side of sea: VERTICAL or HORIZONTAL orientation respectively
	 * For example: 
	 *   orientation = VERTICAL
	 *     1 2 3 4 5 6 7 8 9 10
	 *   1 X O X O O O O O O O
	 *   2 X O X O O O O O O O
	 *   3 O O X O O O O O O O
	 *   4 X O O O O O O O O O
	 *   5 X O X O O O O O O O
	 *   6 X O X O O O O O O O
	 *   7 X O X O O O O O O O
	 *   8 O O O O O O O O O O
	 *   9 O X O X O O O O O O
	 *  10 O X O X O O O O O O
	 *  
	 *  orientation = HORIZONTAL
	 *     1 2 3 4 5 6 7 8 9 10
	 *   1 X X X O X X X X O O
	 *   2 O O O O O O O O O O
	 *   3 X X X O X X O X X O
	 *   4 O O O O O O O O O O
	 *   5 X X O O O O O O O O
	 *   6 O O O O O O O O O O
	 *   7 O O O O O O O O O O
	 *   8 O O O O O O O O O O
	 *   9 O O O O O O O O O O
	 *  10 O O O O O O O O O O
	 *  
	 *  * BOATS will be located later on free space at random!
	 */
	private void variant_1(Orientation o) {
		if (o.equals(Orientation.VERTICAL)) 
		{
			for (int x = 1; x <= sea.getXSize(); x++) {
				for (int y = 1; y <= sea.getYSize(); y++) {
					if (freeObjects.size() == 0) return;
					
					ISeaObject curObject = freeObjects.get( randomGenerator.nextInt(freeObjects.size()) );
					int length = curObject.getSize();
					
					if (checkAvailableCells(x, y, length, o, true)) {
						aggregateCells(x, y, length, o, true, curObject);
						freeObjects.remove(curObject);
						y = y + length;
					}
				}
			}
		}
		else 
		{
			for (int y = 1; y <= sea.getYSize(); y++) {
				for (int x = 1; x <= sea.getXSize(); x++) {
					if (freeObjects.size() == 0) return;
					
					ISeaObject curObject = freeObjects.get( randomGenerator.nextInt(freeObjects.size()) );
					int length = curObject.getSize();
					
					if (checkAvailableCells(x, y, length, o, true)) {
						aggregateCells(x, y, length, o, true, curObject);
						freeObjects.remove(curObject);
						x = x + length;
					}
				}
			}
		}
	}
	
	/**
	 * Variant of locating #2
	 * @param o - location orientation;
	 * Ships are located on the left (lower) side of sea: VERTICAL or HORIZONTAL orientation respectively
	 * For example: 
	 *   orientation = VERTICAL
	 *     1 2 3 4 5 6 7 8 9 10
	 *   1 O O O O O O O O O O
	 *   2 O O O O O O O X O O
	 *   3 O O O O O O O X O X
	 *   4 O O O O O O O O O X
	 *   5 O O O O O O O X O X
	 *   6 O O O O O O O X O O
	 *   7 O O O O O O O X O X
	 *   8 O O O O O O O O O X
	 *   9 O O O O O X O X O X
	 *  10 O O O O O X O X O X
	 *  
	 *  orientation = HORIZONTAL
	 *     1 2 3 4 5 6 7 8 9 10
	 *   1 O O O O O O O O O O
	 *   2 O O O O O O O O O O
	 *   3 O O O O O O O O O O
	 *   4 O O O O O O O O O O
	 *   5 O O O O O O O O O O
	 *   6 O O O O O O O X X X
	 *   7 O O O O O O O O O O
	 *   8 O O O X X O X X X X
	 *   9 O O O O O O O O O O
	 *  10 X X X O X X X O X X
	 *  
	 *  * BOATS will be located later on free space at random!
	 */
	private void variant_2(Orientation o) {
		if (o.equals(Orientation.VERTICAL)) 
		{
			for (int x = sea.getXSize(); x > 0; x--) {
				for (int y = sea.getYSize(); y > 0; y--) {
					if (freeObjects.size() == 0) return;
					
					ISeaObject curObject = freeObjects.get( randomGenerator.nextInt(freeObjects.size()) );
					int length = curObject.getSize();
					
					if (checkAvailableCells(x, y, length, o, false)) {
						aggregateCells(x, y, length, o, false, curObject);
						freeObjects.remove(curObject);
						y = y - length;
					}
				}
			}
		}
		else 
		{
			for (int y = sea.getYSize(); y > 0; y--) {
				for (int x = sea.getXSize(); x > 0; x--) {
					if (freeObjects.size() == 0) return;
					
					ISeaObject curObject = freeObjects.get( randomGenerator.nextInt(freeObjects.size()) );
					int length = curObject.getSize();
					
					if (checkAvailableCells(x, y, length, o, false)) {
						aggregateCells(x, y, length, o, false, curObject);
						freeObjects.remove(curObject);
						x = x - length;
					}
				}
			}
		}
	}
	
	/**
	 * Variant of locating #3
	 * @param o - location orientation;
	 * Ships are located on the left (lower) side and on the right (top) of sea: VERTICAL or HORIZONTAL orientation respectively
	 * For example: 
	 *   orientation = VERTICAL
	 *     1 2 3 4 5 6 7 8 9 10
	 *   1 X O X O O O O O O O
	 *   2 X O X O O O O O O O
	 *   3 X O O O O O O O O X
	 *   4 X O O O O O O O O X
	 *   5 O O O O O O O O O X
	 *   6 X O O O O O O O O O
	 *   7 X O O O O O O O O O
	 *   8 X O O O O O O O O O
	 *   9 O O O O O O O X O X
	 *  10 O O O O O O O X O X
	 *  
	 *  orientation = HORIZONTAL
	 *     1 2 3 4 5 6 7 8 9 10
	 *   1 X X X X O O X X X O
	 *   2 O O O O O O O O O O
	 *   3 O O O O O O O O O O
	 *   4 O O O O O O O O O O
	 *   5 O O O O O O O O O O
	 *   6 O O O O O O O O O O
	 *   7 O O O O O O O O O O
	 *   8 O O O O O O O O X X
	 *   9 O O O O O O O O O O
	 *  10 O X X O X X X O X X
	 *  
	 *  * BOATS will be located later on free space at random!
	 */
	private void variant_3(Orientation o) {
		int middle = freeObjects.size() / 2;
		int cnt = 0;
		
		if (o.equals(Orientation.VERTICAL)) 
		{
			root: for (int x = 1; x <= sea.getXSize(); x++) {
				for (int y = 1; y <= sea.getYSize(); y++) {
					if (cnt >= middle) break root;
					
					ISeaObject curObject = freeObjects.get( randomGenerator.nextInt(freeObjects.size()) );
					int length = curObject.getSize();
					
					if (checkAvailableCells(x, y, length, o, true)) {
						aggregateCells(x, y, length, o, true, curObject);
						freeObjects.remove(curObject);
						cnt++;
						y = y + length;
					}
				}
			}
			
			for (int x = sea.getXSize(); x > 0; x--) {
				for (int y = sea.getYSize(); y > 0; y--) {
					if (freeObjects.size() == 0) return;
					
					ISeaObject curObject = freeObjects.get( randomGenerator.nextInt(freeObjects.size()) );
					int length = curObject.getSize();
					
					if (checkAvailableCells(x, y, length, o, false)) {
						aggregateCells(x, y, length, o, false, curObject);
						freeObjects.remove(curObject);
						y = y - length;
					}
				}
			}
		}
		else 
		{
			root: for (int y = 1; y <= sea.getYSize(); y++) {
				for (int x = 1; x <= sea.getXSize(); x++) {
					if (cnt >= middle) break root;
					
					ISeaObject curObject = freeObjects.get( randomGenerator.nextInt(freeObjects.size()) );
					int length = curObject.getSize();
					
					if (checkAvailableCells(x, y, length, o, true)) {
						aggregateCells(x, y, length, o, true, curObject);
						freeObjects.remove(curObject);
						cnt++;
						x = x + length;
					}
				}
			}
		
			for (int y = sea.getYSize(); y > 0; y--) {
				for (int x = sea.getXSize(); x > 0; x--) {
					if (freeObjects.size() == 0) return;
					
					ISeaObject curObject = freeObjects.get( randomGenerator.nextInt(freeObjects.size()) );
					int length = curObject.getSize();
					
					if (checkAvailableCells(x, y, length, o, false)) {
						aggregateCells(x, y, length, o, false, curObject);
						freeObjects.remove(curObject);
						x = x - length;
					}
				}
			}
		}
	}
	
	/**
	 * Variant of locating #4 - includes variants #4, #5, #6, #7
	 * @param angle - location angle orientation;
	 * Ships are located along edge of sea, it depends from angle - different start side of sea.
	 *   if (angle = 0  ) starts from cell (1 , 1) and along X axis to increase;
	 *   if (angle = 90 ) starts from cell (10, 1) and along Y axis to increase;
	 *   if (angle = 180) starts from cell (10,10) and along X axis to reduce;
	 *   if (angle = 270) starts from cell (1, 10) and along Y axis to reduce;
	 * For example: 
	 *   angle = 0
	 *     1 2 3 4 5 6 7 8 9 10
	 *   1 X X X O X X O X X X
	 *   2 O O O O O O O O O O
	 *   3 O O O O O O O O O X
	 *   4 O O O O O O O O O X
	 *   5 O O O O O O O O O X
	 *   6 O O O O O O O O O X
	 *   7 O O O O O O O O O O
	 *   8 O O O O O O O O O X
	 *   9 O O O O O O O O O X
	 *  10 O O O O O O X X O O
	 *  
	 *   angle = 90
	 *     1 2 3 4 5 6 7 8 9 10
	 *   1 O O O O O O O O O X
	 *   2 O O O O O O O O O X
	 *   3 O O O O O O O O O O
	 *   4 O O O O O O O O O X
	 *   5 O O O O O O O O O X
	 *   6 O O O O O O O O O X
	 *   7 X O O O O O O O O O
	 *   8 X O O O O O O O O X
	 *   9 O O O O O O O O O X
	 *  10 X X X O X X X X O O
	 *  
	 *   angle = 180
	 *     1 2 3 4 5 6 7 8 9 10
	 *   1 X O X X O O O O O O
	 *   2 X O O O O O O O O O
	 *   3 O O O O O O O O O O
	 *   4 X O O O O O O O O O
	 *   5 X O O O O O O O O O
	 *   6 O O O O O O O O O O
	 *   7 X O O O O O O O O O
	 *   8 X O O O O O O O O O
	 *   9 X O O O O O O O O O
	 *  10 O O X X X O X X X X
	 *  
	 *   angle = 270
	 *     1 2 3 4 5 6 7 8 9 10
	 *   1 X O X X O X X X O X
	 *   2 X O O O O O O O O X
	 *   3 O O O O O O O O O X
	 *   4 X O O O O O O O O O
	 *   5 X O O O O O O O O O
	 *   6 O O O O O O O O O O
	 *   7 X O O O O O O O O O
	 *   8 X O O O O O O O O O
	 *   9 X O O O O O O O O O
	 *  10 X O O O O O O O O O
	 *  
	 *  * BOATS will be located later on free space at random!
	 */
	private void variant_4(int angle) {
		boolean first = false;
		boolean second = false;
		boolean third = false;
		boolean fourth = false;
		
		if (angle == 0) {
			first = true;
		} else if (angle == 90) {
			second = true;
		} else if (angle == 180) {
			third = true;
		} else if (angle == 270) {
			fourth = true;
		}
		
		int loop = 0;
		while (true) {
			if ( first ) {
				for (int x = 1; x <= sea.getXSize(); x++) {
					int y = 1 + loop / 4;
					if (freeObjects.size() == 0) return;
					
					ISeaObject curObject = freeObjects.get( randomGenerator.nextInt(freeObjects.size()) );
					int length = curObject.getSize();
					
					if (checkAvailableCells(x, y, length, Orientation.HORIZONTAL, true)) {
						aggregateCells(x, y, length, Orientation.HORIZONTAL, true, curObject);
						freeObjects.remove(curObject);
						x = x + length;
					}
				}
				
				first = false;
				second = true;
				loop++;
			}
			
			if ( second ) {
				for (int y = 1; y <= sea.getYSize(); y++) {
					int x = sea.getXSize() - loop / 4;
					if (freeObjects.size() == 0) return;
					
					ISeaObject curObject = freeObjects.get( randomGenerator.nextInt(freeObjects.size()) );
					int length = curObject.getSize();
					
					if (checkAvailableCells(x, y, length, Orientation.VERTICAL, true)) {
						aggregateCells(x, y, length, Orientation.VERTICAL, true, curObject);
						freeObjects.remove(curObject);
						y = y + length;
					}
				}
				
				second = false;
				third = true;
				loop++;
			}
			
			if ( third ) {
				for (int x = sea.getXSize(); x > 0; x--) {
					int y = sea.getYSize() - loop / 4;
					if (freeObjects.size() == 0) return;
					
					ISeaObject curObject = freeObjects.get( randomGenerator.nextInt(freeObjects.size()) );
					int length = curObject.getSize();
					
					if (checkAvailableCells(x, y, length, Orientation.HORIZONTAL, false)) {
						aggregateCells(x, y, length, Orientation.HORIZONTAL, false, curObject);
						freeObjects.remove(curObject);
						x = x - length;
					}
				}
				
				third = false;
				fourth = true;
				loop++;
			}
			
			if ( fourth ) {
				for (int y = sea.getYSize(); y > 0; y--) {
					int x = 1 + loop / 4;
					if (freeObjects.size() == 0) return;
					
					ISeaObject curObject = freeObjects.get( randomGenerator.nextInt(freeObjects.size()) );
					int length = curObject.getSize();
					
					if (checkAvailableCells(x, y, length, Orientation.VERTICAL, false)) {
						aggregateCells(x, y, length, Orientation.VERTICAL, false, curObject);
						freeObjects.remove(curObject);
						y = y - length;
					}
				}
				
				fourth = false;
				first = true;
				loop++;
			}
		}
	}
	
	/**
	 * Method for checking that sequence cells from (startX, yStart) to startCell + length is available.
	 * @param startX - X-coordinate of start cell;
	 * @param startY - Y-coordinate of start cell;
	 * @param length - length of needed sequence cells (length of sea object);
	 * @param o - orientation of needed sequence cells;
	 * @param direct - true if sequence should be created as increasing the starting cell, false in case of reducing;
	 * @return true - if such siquence is available, false - is not.
	 */
	private boolean checkAvailableCells(int startX, int startY, int length, Orientation o, boolean direct) {
		if (debug) System.out.println("\nCheck available: x = " + startX + ", y = " + startY + ", length = " + length + ", o = " + o + ", direct = " + direct);
		
		if (o.equals(Orientation.VERTICAL))
		{
			if ( direct && (startY+length) > sea.getYSize() ) return false;
			if ( !direct && (startY-length) < 1) return false;
			
			if ( direct ) {
				for (int y = startY; y < (startY+length); y++) {
					// cell is engaged
					if (fakeSea[y-1][startX-1] == 1)
						return false;
				}
			} else {
				for (int y = startY; y > (startY-length); y--) {
					// cell is engaged
					if (fakeSea[y-1][startX-1] == 1)
						return false;
				}
			}
			
			return true;
		}
		else 
		{
			if ( direct && (startX+length) > sea.getXSize() ) return false;
			if ( !direct && (startX-length) < 1) return false;
			
			if ( direct ) {
				for (int x = startX; x < (startX+length); x++) {
					// cell is engaged
					if (fakeSea[startY-1][x-1] == 1)
						return false;
				}
			} else {
				for (int x = startX; x > (startX-length); x--) {
					// cell is engaged
					if (fakeSea[startY-1][x-1] == 1)
						return false;
				}
			}
			
			return true;
		}
	}
	
	/**
	 * Aggregates sequence cells from (startX, yStart) to startCell + length by orientation.
	 * @param startX - X-coordinate of start cell;
	 * @param startY - Y-coordinate of start cell;
	 * @param length - length of aggregating sequence cells (length of sea object);
	 * @param o - orientation of aggregating sequence cells;
	 * @param direct - true if sequence should be created as increasing the starting cell, false in case of reducing;
	 * 
	 * Also this method reflects aggregating on fakeSea - sets all aggregated and unavailable cells in 1.
	 */
	private void aggregateCells(int startX, int startY, int length, Orientation o, boolean direct, ISeaObject obj) {
		if (debug) System.out.println("Aggregate cells: x = " + startX + ", y = " + startY + ", length = " + length + ", o = " + o);
		List<SeaDrop> drops = new ArrayList<SeaDrop>(length);
		if (o.equals(Orientation.VERTICAL))
		{
			if (direct) {
				for (int y = startY; y < (startY+length); y++) {
					SeaDrop drop = sea.getSeaDrop(startX, y);
					drops.add(drop);
					
					int x_index = startX - 1;
					int y_index = y - 1;
	
					if (x_index != 0) fakeSea[y_index][x_index-1] = 1;
					if (x_index != 9) fakeSea[y_index][x_index+1] = 1;
					fakeSea[y_index][x_index] = 1;
					
					if (y_index != 0) {
						if (x_index != 0) fakeSea[y_index-1][x_index-1] = 1;
						if (x_index != 9) fakeSea[y_index-1][x_index+1] = 1;
						fakeSea[y_index-1][x_index] = 1;
					}
					
					if (y_index != 9) {
						if (x_index != 0) fakeSea[y_index+1][x_index-1] = 1;
						if (x_index != 9) fakeSea[y_index+1][x_index+1] = 1;
						fakeSea[y_index+1][x_index] = 1;
					}
				}
			} else {
				for (int y = startY; y > (startY-length); y--) {
					SeaDrop drop = sea.getSeaDrop(startX, y);
					drops.add(drop);
					
					int x_index = startX - 1;
					int y_index = y - 1;
	
					if (x_index != 0) fakeSea[y_index][x_index-1] = 1;
					if (x_index != 9) fakeSea[y_index][x_index+1] = 1;
					fakeSea[y_index][x_index] = 1;
					
					if (y_index != 0) {
						if (x_index != 0) fakeSea[y_index-1][x_index-1] = 1;
						if (x_index != 9) fakeSea[y_index-1][x_index+1] = 1;
						fakeSea[y_index-1][x_index] = 1;
					}
					
					if (y_index != 9) {
						if (x_index != 0) fakeSea[y_index+1][x_index-1] = 1;
						if (x_index != 9) fakeSea[y_index+1][x_index+1] = 1;
						fakeSea[y_index+1][x_index] = 1;
					}
				}
			}
		}
		else
		{
			if (direct) 
			{
				for (int x = startX; x < (startX+length); x++) {
					SeaDrop drop = sea.getSeaDrop(x, startY);
					drops.add(drop);
					
					int x_index = x - 1;
					int y_index = startY - 1;
	
					if (x_index != 0) fakeSea[y_index][x_index-1] = 1;
					if (x_index != 9) fakeSea[y_index][x_index+1] = 1;
					fakeSea[y_index][x_index] = 1;
					
					if (y_index != 0) {
						if (x_index != 0) fakeSea[y_index-1][x_index-1] = 1;
						if (x_index != 9) fakeSea[y_index-1][x_index+1] = 1;
						fakeSea[y_index-1][x_index] = 1;
					}
					
					if (y_index != 9) {
						if (x_index != 0) fakeSea[y_index+1][x_index-1] = 1;
						if (x_index != 9) fakeSea[y_index+1][x_index+1] = 1;
						fakeSea[y_index+1][x_index] = 1;
					}
				}
			} else {
				for (int x = startX; x > (startX-length); x--) {
					SeaDrop drop = sea.getSeaDrop(x, startY);
					drops.add(drop);
					
					int x_index = x - 1;
					int y_index = startY - 1;
	
					if (x_index != 0) fakeSea[y_index][x_index-1] = 1;
					if (x_index != 9) fakeSea[y_index][x_index+1] = 1;
					fakeSea[y_index][x_index] = 1;
					
					if (y_index != 0) {
						if (x_index != 0) fakeSea[y_index-1][x_index-1] = 1;
						if (x_index != 9) fakeSea[y_index-1][x_index+1] = 1;
						fakeSea[y_index-1][x_index] = 1;
					}
					
					if (y_index != 9) {
						if (x_index != 0) fakeSea[y_index+1][x_index-1] = 1;
						if (x_index != 9) fakeSea[y_index+1][x_index+1] = 1;
						fakeSea[y_index+1][x_index] = 1;
					}
				}
			}
		}
		
		obj.aggregateDrops(drops);
	}
}
