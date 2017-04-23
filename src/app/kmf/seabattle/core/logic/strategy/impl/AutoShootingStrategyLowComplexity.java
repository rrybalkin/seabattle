package app.kmf.seabattle.core.logic.strategy.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import app.kmf.seabattle.core.logic.strategy.IShootingStrategy;
import app.kmf.seabattle.core.logic.strategy.Cell;
import app.kmf.seabattle.enums.Orientation;
import app.kmf.seabattle.enums.ShotResult;

public class AutoShootingStrategyLowComplexity implements IShootingStrategy {
	private int xSize = 10 /*default*/;
	private int ySize = 10 /*default*/;
	
	private int numShots;
	
	/**
	 * Container of cell codes of Sea coordinates 
	 * 	For example, when xSize = 10 and ySize = 10:
	 *     1  2  3  4  5  6  7  8  9  10	
	 *    ------------------------------
	 *  1| 1  2  3  4  5  6  7  8  9  10
	 *  2| 11 12 13 14 15 16 17 18 19 20
	 *  3| 21 22 23 24 25 26 27 28 29 30
	 *  4| 31 32 33 34 35 36 37 38 39 40
	 *  5| 41 42 43 44 45 46 47 48 49 50
	 *  6| 51 52 53 54 55 56 57 58 59 60
	 *  7| 61 62 63 64 65 66 67 68 69 70
	 *  8| 71 72 73 74 75 76 77 78 79 80
	 *  9| 81 82 83 84 85 86 87 88 89 90
	 * 10| 91 92 93 94 95 96 97 98 99 100
	 */
	private List<Integer> cellCodes;
	
	private Cell lastShot;
	private Random randomGenerator;
	
	/**
	 * Mode of hunting is turn on when ship was wounded by last shot but isn't killed yet
	 * 
	 * Variables for hunting mode:
	 *   shotsOnVictim - list of the last shots which are hit on a victim - ship
	 *   probableCells - list of cells which are probably included in a wounded ship
	 */
	private boolean huntingMode = false;
	private List<Cell> shotsOnVictim;
	private List<Integer> probableCells;
	
	public AutoShootingStrategyLowComplexity() {
		System.out.println("AutoShootingStrategyLowComplexity constructor ...");
		init();
	}
	
	private void init() {
		randomGenerator = new Random();
		numShots = 0;
		
		// forming cell codes for sea
		cellCodes = new ArrayList<Integer>(xSize * ySize);
		for (int i = 1; i <= xSize * ySize; i++) {
			cellCodes.add(i);
		}
		
		shotsOnVictim = new ArrayList<Cell>();
		probableCells = new ArrayList<Integer>();
	}

	@Override
	public Cell getCoordinatesForShot(ShotResult lastShotResult) {
		if (lastShotResult == null /* first shot */)
			return getRandomShot();
			
		processLastShotResult( lastShotResult );
		
		if ((lastShotResult.equals(ShotResult.GAMEOVER)) || cellCodes.size() == 0) {
			System.out.println("Shooting is ended! Number of shots = " + numShots);
			reset();
			return null;
		} else {
			numShots++;
		}
		
		if (huntingMode) {
			return huntingShot();
		} else {
			return getRandomShot();
		}
	}
	
	/**
	 * Method for reset all local variables
	 *   strategy will be ready for next game
	 */
	@Override
	public void reset() {
		
	}
	
	/**
	 * Method for processing result of last shot
	 * @param res - last shot result
	 */
	private void processLastShotResult(ShotResult res) {
		// first shot
		if (res == null) return;
		
		if (res.equals(ShotResult.WOUNDED)) {
			if(!huntingMode /*First hit*/) {
				huntingMode = true;
			}
			// adding last shot to hitting on victim shots
			shotsOnVictim.add(lastShot);
			
			// forming probable cells for next shots
			formingProbableCells();
		} else if (res.equals(ShotResult.KILLED)) {
			shotsOnVictim.add(lastShot);
			// set cells around victim to broken
			processVictim();
			
			// clear all variables of hunting mode
			shotsOnVictim.clear();
			probableCells.clear();
			huntingMode = false;
		}
		
		setCellAsBroken(lastShot.getX(), lastShot.getY());
	}
	
	/**
	 * Method for random shooting (huntingMode = off)
	 * @return coordinates of shot
	 */
	private Cell getRandomShot() {
		//TODO do it as error
		if (cellCodes.size() == 0) {
			System.out.println("Error! Number of available cells = 0.");
			return null;
		}
		int index = randomGenerator.nextInt(cellCodes.size());
		int cell_code = cellCodes.get(index);
		
		Cell shot = this.fromCellCode(cell_code);
		lastShot = shot;
		
		return shot;
	};
	
	/**
	 * Method for sharpshooting (huntingMode = on)
	 * @return coordinates of shot
	 */
	private Cell huntingShot() {
		//TODO do it as error
		if (probableCells.size() == 0) {
			System.out.println("Error! Number of available probable cells = 0.");
			return null;
		}
		int index = randomGenerator.nextInt(probableCells.size());
		int cell_code = probableCells.get(index);
		
		Cell shot = this.fromCellCode(cell_code);
		lastShot = shot;
		
		return shot;
	}
	
	/**
	 * Method for forming list of probable cells for next shot
	 * Logic of this method:
	 *  1. If wounded shot is first - then shotsOnVictim has size = 1
	 *     - probableCells is empty and probable cells will be
	 *     [(X of hit shot) + 1 , (Y of hit shot)]
	 *     [(X of hit shot) - 1 , (Y of hit shot)]
	 *     [(X of hit shot) , (Y of hit shot) + 1]
	 *     [(X of hit shot) , (Y of hit shot) - 1]
	 *     
	 *  Example: 
	 *  		  1 2 3 4 ...
	 *   		1  
	 *   		2   ?
	 *   		3 ? X ?
	 *   		4   ?
	 *  	   ...  
	 *  
	 *  2. If wounded shot isn't first - then shotsOnVictim has size > 1
	 *     - probableCells should be clear and reforming
	 *     probable cells will be depends from orientation victim
	 *     a) if victim has horizontal orientation then probable cells will be
	 *        [(min X of shots) - 1 , (Y of any shot)]
	 *        [(max X of shots) + 1 , (Y of any shot)]
	 *        
	 *      Example:
	 *    		  1 2 3 4 ...
	 *   		1  
	 *   		2 ? X X ?
	 *   		3  
	 *   		4   
	 *  	   ...  
	 *  	b) if victim has vertical orientation then probable cells will be
	 *  	  [(X of any shot) , (min Y of shots) - 1]
	 *        [(X of any shot) , (max Y of shots) + 1]
	 *        
	 *      Example:
	 *    		  1 2 3 4 ...
	 *   		1     ?
	 *   		2     X
	 *   		3     X
	 *   		4     ?
	 *  	   ...  
	 *      
	 */
	private void formingProbableCells() {	
		// variant #1
		if (shotsOnVictim.size() == 1 && probableCells.size() == 0) {
			int x = shotsOnVictim.get(0).getX();
			int y = shotsOnVictim.get(0).getY();
			
			Integer probableCell = null;
			
			if (y != 10) 
			{
				probableCell = this.toCellCode(x, y+1);
				if (cellCodes.contains(probableCell)) {
					probableCells.add(probableCell);
				}
			}
			if (y != 1)
			{
				probableCell = this.toCellCode(x, y-1);
				if (cellCodes.contains(probableCell)) {
					probableCells.add(probableCell);
				}
			}
			if (x != 10)
			{
				probableCell = this.toCellCode(x+1, y);
				if (cellCodes.contains(probableCell)) {
					probableCells.add(probableCell);
				}
			}
			if (x != 1)
			{
				probableCell = this.toCellCode(x-1, y);
				if (cellCodes.contains(probableCell)) {
					probableCells.add(probableCell);
				}
			}
		}
		// variant #2
		else if (shotsOnVictim.size() > 1) 
		{
			// remove old probable cells
			probableCells.clear();
			
			Orientation o = null;
			
			if (shotsOnVictim.get(0).getY() == shotsOnVictim.get(1).getY()) o = Orientation.HORIZONTAL;
			else o = Orientation.VERTICAL;
			
			Integer probableCell = null;
			
			// variant a
			if (o.equals(Orientation.HORIZONTAL))
			{
				int y = shotsOnVictim.get(0).getY();
				int firstX = shotsOnVictim.get(0).getX();
				int lastX = shotsOnVictim.get(shotsOnVictim.size()-1).getX();
				
				int minX, maxX;
				if (firstX > lastX) {
					minX = lastX;
					maxX = firstX;
				} else {
					minX = firstX;
					maxX = lastX;
				}
				
				if (minX != 1)
				{
					probableCell = this.toCellCode(minX-1, y);
					if (cellCodes.contains(probableCell)) {
						probableCells.add(probableCell);
					}
				}
				if (maxX != 10)
				{
					probableCell = this.toCellCode(maxX+1, y);
					if (cellCodes.contains(probableCell)) {
						probableCells.add(probableCell);
					}
				}
			}
			// variant b
			else if (o.equals(Orientation.VERTICAL)) 
			{
				int x = shotsOnVictim.get(0).getX();
				int firstY = shotsOnVictim.get(0).getY();
				int lastY = shotsOnVictim.get(shotsOnVictim.size()-1).getY();
				
				int minY, maxY;
				if (firstY > lastY) {
					minY = lastY;
					maxY = firstY;
				} else {
					minY = firstY;
					maxY = lastY;
				}
				
				if (minY != 1)
				{
					probableCell = this.toCellCode(x, minY-1);
					if (cellCodes.contains(probableCell)) {
						probableCells.add(probableCell);
					}
				}
				if (maxY != 10)
				{
					probableCell = this.toCellCode(x, maxY+1);
					if (cellCodes.contains(probableCell)) {
						probableCells.add(probableCell);
					}
				}
			}
		}
	}
	
	/**
	 * Method for marking cells around "dead victim" as broken
	 */
	private void processVictim() {
		for (Cell shot : shotsOnVictim) {
			int x = shot.getX();
			int y = shot.getY();
			
			//TODO remove excess operation
			setCellAsBroken(x, y+1);
			setCellAsBroken(x, y-1);
			setCellAsBroken(x+1, y);
			setCellAsBroken(x+1, y+1);
			setCellAsBroken(x+1, y-1);
			setCellAsBroken(x-1, y);
			setCellAsBroken(x-1, y+1);
			setCellAsBroken(x-1, y-1);
			setCellAsBroken(x+1, y);
		}
	}
	
	/**
	 * Method for markin cell as broken
	 * @param x - X-coordinate of broken cell
	 * @param y - Y-coordinate of broken cell
	 */
	private void setCellAsBroken(int x, int y) {
		if ( (x > 0 && x <= xSize) && (y > 0 && y <= ySize) ) {
			Integer removedCellCode = this.toCellCode(x, y);
			
			cellCodes.remove(removedCellCode);
			probableCells.remove(removedCellCode);
		}
	}
	
	/**
	 * Method for conversion {X, Y} to cell code
	 * @param x - X-coordinate of cell
	 * @param y - Y-coordinate of cell
	 * @return cell code
	 */
	private Integer toCellCode(int x, int y) {
		int code = (y-1)*10 + x;
		return Integer.valueOf(code);
	}
	
	/**
	 * Method for conversion cell code to {X, Y}
	 * @param cellCode - conversing cell code
	 * @return object Shot which contains X and Y coordinates
	 */
	private Cell fromCellCode(int cellCode) {
		int y = cellCode / 10 + 1;
		int x = cellCode % 10;
		
		if (x == 0) {
			x = 10;
			y = y - 1;
		}
		
		Cell shot = new Cell(x, y);
		
		return shot;
	}
}