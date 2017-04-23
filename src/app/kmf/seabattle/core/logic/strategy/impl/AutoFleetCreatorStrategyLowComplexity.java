package app.kmf.seabattle.core.logic.strategy.impl;

import app.kmf.seabattle.core.datamodel.IFleet;
import app.kmf.seabattle.core.datamodel.ISea;
import app.kmf.seabattle.core.datamodel.ISeaObject;
import app.kmf.seabattle.core.datamodel.SeaDrop;
import app.kmf.seabattle.core.datamodel.impl.Fleet;
import app.kmf.seabattle.core.datamodel.impl.Sea;
import app.kmf.seabattle.core.datamodel.impl.Ship;
import app.kmf.seabattle.core.logic.descriptors.FleetDescriptor;
import app.kmf.seabattle.core.logic.strategy.Cell;
import app.kmf.seabattle.core.logic.strategy.IFleetCreatorStrategy;
import app.kmf.seabattle.enums.Orientation;
import app.kmf.seabattle.enums.ShipType;
import app.kmf.seabattle.util.settings.GameSettings;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class AutoFleetCreatorStrategyLowComplexity implements IFleetCreatorStrategy {
	private IFleet fleet;
	private ISea sea;
	private List<ISeaObject> seaObjects;
	
	private Random randomGenerator;
	/**
	 * List of cells (x, y) which represents Sea object.
	 * 	 Use for excepting cells which already used by ships 
	 *   and for search free cells for aggregating in the other ships.
	 */
	private List<Cell> cells;
	/**
	 * List of removed cells in process locate ships.
	 *   Use for reusing cell objects.
	 */
	private List<Cell> removedCells;
	 
	public AutoFleetCreatorStrategyLowComplexity() {
		System.out.println("AutoFleetCreatorStrategyLowComplexity constructor ...");
		init();
	}
	
	private void init() {
		randomGenerator = new Random();
		
		// create fake Sea object by list of cells
		cells = new ArrayList<Cell>();
		for (int x = 1; x <= GameSettings.X_SIZE; x++)
			for (int y = 1; y <= GameSettings.Y_SIZE; y++) {
				Cell cell = new Cell( x, y );
				cells.add(cell);
			}
		
		removedCells = new ArrayList<Cell>(cells.size());
	}
	
	@Override
	public IFleet createFleet() {
		buildFleet();
		
		return fleet;
	}
	
	@Override
	public IFleet createFleet(FleetDescriptor descriptor) {
		return this.createFleet();
	}
	
	@Override
	public void reset() {
		// all removed cells transfer in cells again
		for (Cell revivalCell : removedCells) {
			cells.add(revivalCell);
		}
		removedCells.clear();
		
		// reset all flags for Sea drops (free, broken, etc.)
		sea.reset();
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
	 *   which aggregating in Sea object.
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
	 * Method for auto locate sea objects on the sea.
	 *   Logic of method:
	 *   1. There starts cycle for sea objects ...
	 *   2. By random choose orientaion for current object ...
	 *   3. There starts operation for finding %size% free cells and with needed orientation - "findAvailableCells"
	 *   4. If needed cells aren't found - try find for other orientation ...
	 *   5. If needed cells are found - aggregating sea drops by this ship, else throw error.
	 */
	private void locateShips() {
		for (ISeaObject sea_object : seaObjects) {
			int size = sea_object.getSize();
			Orientation o = Orientation.create( randomGenerator.nextInt(2) );
			
			List<Cell> avb_cells = findAvailableCells( size, o );
			
			// try find available cells for other orientation
			if (avb_cells == null || avb_cells.size() == 0)
				avb_cells = findAvailableCells( size, Orientation.opposite(o) );
			
			if (avb_cells == null || avb_cells.size() == 0) {
				System.out.println("Impossible create sea object with size = " + size);
			} else {
				List<SeaDrop> drops = new ArrayList<SeaDrop>(avb_cells.size());
				for (Cell cell : avb_cells) {
					SeaDrop drop = sea.getSeaDrop( cell.getX(), cell.getY() );
					drops.add(drop);
				}
				sea_object.aggregateDrops(drops);
				
				removeUsedCells( avb_cells );
			}
		}
	}
	
	/**
	 * Method for finding %size% free cells and with needed orientation
	 * @param size - needed size of sea object
	 * @param o - needed orienation for sea object 
	 * @return list of suitable celss
	 * 
	 * Logic of method:
	 * 	1. By random choose start point - cell ...
	 *  2. Then try find rest %size%-1 cells by needed orientation ...
	 *  3. If needed cells are founded - return this cells,
	 *     If not found - try for other start point - cell.
	 *     
	 *  Example #1:
	 *   size = 4, Orientation = VERTICAL
	 *    		  1 2 3 4 ...
	 *   		1  
	 *   		2 
	 *   		3  
	 *   		4   
	 *  	    5
	 *          6
	 *          7
	 *          8   S    ...
	 *          9   N    ...
	 *         10   N    ...
	 *         
	 *   S - start point (cell), N - next points (cells)
	 *   This variant is not suitable so finding will be continue ...
	 *  
	 *   Example #2:
	 *   size = 4, Orientation = VERTICAL
	 *    		  1 2 3 4 5 6 7
	 *   		1       
	 *   		2       
	 *   		3       S    ...
	 *   		4       N    ... 
	 *  	    5       N    ...
	 *          6       N    ...
	 *          7
	 *         ...
	 *         
	 *   S - start point (cell), N - next points (cells)
	 *   This variant is suitable and method return list of cells = [(4,3) (4,4) (4,5) (4,6)]
	 */
	private List<Cell> findAvailableCells(int size, Orientation o) {
		List<Cell> avb_cells = new ArrayList<Cell>( size );
		List<Integer> use_indexes = new ArrayList<Integer>();
		
		find: do {
			Integer index = randomGenerator.nextInt(cells.size());
			if (use_indexes.contains(index))
				continue;
			
			Cell start_cell = cells.get(index);
			
			if (o.equals(Orientation.HORIZONTAL))
			{
				for (int i = start_cell.getX(); i < start_cell.getX() + size; i++) {
					Cell next_cell = toCell( i, start_cell.getY() );
					if ( next_cell != null ) {
						avb_cells.add( next_cell );
					} else {
						avb_cells.clear();
						use_indexes.add(index);
						continue find;
					}
				}
				
				return avb_cells;
			} else {
				for (int i = start_cell.getY(); i < start_cell.getY() + size; i++) {
					Cell next_cell = toCell( start_cell.getX(), i );
					if ( next_cell != null ) {
						avb_cells.add( next_cell );
					} else {
						avb_cells.clear();
						use_indexes.add(index);
						continue find;
					}
				}
				
				return avb_cells;
			}
		} while (use_indexes.size() != cells.size());
		
		return null;
	}
	
	/**
	 * Method for removed aggregated cells and every around them from list of cells
	 * @param rem_cells - removing cells
	 * 
	 * Example:
	 *   1 2 3 4 5 6 7 8 9 10
	 *  1
	 *  2
	 *  3    X X X
	 *  4    X R X
	 *  5    X R X
	 *  6    X R X
	 *  7    X R X
	 *  8    X X X
 	 *  9
	 * 10
	 * 
	 *  R - removing cells, X - cells around removing cells which also will be removed.
	 */
	private void removeUsedCells(List<Cell> rem_cells) {
		for (Cell rem_cell : rem_cells) {
			int x = rem_cell.getX();
			int y = rem_cell.getY();
			
			removeCell(toCell(x, y+1));
			removeCell(toCell(x, y-1));
			removeCell(toCell(x+1, y));
			removeCell(toCell(x+1, y+1));
			removeCell(toCell(x+1, y-1));
			removeCell(toCell(x-1, y));
			removeCell(toCell(x-1, y+1));
			removeCell(toCell(x-1, y-1));
			removeCell(rem_cell);
		}
	}
	
	/**
	 * Method for finding cell with X=x,Y=y in list of cells
	 * @param x - X-coordinate of finding cell
	 * @param y - Y-coordinate of finding cell
	 * @return cell id it exist, null - if isn't exist
	 */
	private Cell toCell(int x, int y) {
		for (Cell cell : cells) {
			if ( x == cell.getX() && y == cell.getY() )
				return cell;
		}
		
		return null;
	}
	
	/**
	 * Method for "removing" cell from list of cells and adding in list of removed cells for reusing after.
	 * @param cell - removing cell
	 */
	private void removeCell(Cell cell) {
		if (cell != null) {
			if (!removedCells.contains(cell))
				removedCells.add(cell);
			cells.remove(cell);
		}
	}
}
