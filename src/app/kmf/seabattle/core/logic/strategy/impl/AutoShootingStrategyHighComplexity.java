package app.kmf.seabattle.core.logic.strategy.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import app.kmf.seabattle.core.logic.strategy.Cell;
import app.kmf.seabattle.core.logic.strategy.IShootingStrategy;
import app.kmf.seabattle.enums.Orientation;
import app.kmf.seabattle.enums.ShotResult;
import app.kmf.seabattle.util.settings.GameSettings;

public class AutoShootingStrategyHighComplexity implements IShootingStrategy {
	/**
	 * Turn on and turn off mode for mixing shot: 
	 *   aimed shots and random shots.
	 */
	private final boolean mixMode = false;
	/**
	 * In case mixMode = true, number of aimed shots after that will be random.
	 *   For example: limitAimedShots = 3, then 
	 *     shot#N-1 = random, 
	 *     shot#N   = aimed, 
	 *     shot#N+1 = aimed, 
	 *     shot#N+2 = aimed, 
	 *     shot#N+3 = random
	 *     shot#N+4 = aimed and etc... 
	 */
	private final int limitAimedShots = 3;
	/**
	 * X-size and Y-size of Sea
	 */
	private int xSize = GameSettings.X_SIZE;
	private int ySize = GameSettings.Y_SIZE;
	
	/**
	 * Matrix of probabilities - it contains number of probable variants locating ship in any cell.
	 * For example:
	 *   before first shot it will be:
	 *      1 2 3 4 5 6 7 8 9 10
	 *     
	 *   1  2 3 4 5 5 5 5 4 3 2
	 *	 2  3 4 5 6 6 6 6 5 4 3
     *   3  4 5 6 7 7 7 7 6 5 4
     *   4  5 6 7 8 8 8 8 7 6 5
     *   5  5 6 7 8 8 8 8 7 6 5
     *   6  5 6 7 8 8 8 8 7 6 5
     *   7  5 6 7 8 8 8 8 7 6 5
     *   8  4 5 6 7 7 7 7 6 5 4
     *   9  3 4 5 6 6 6 6 5 4 3
     *  10  2 3 4 5 5 5 5 4 3 2
     *  
     *  This matrix use for aimed shots - choose cells which are the most probably (if matrix is normalize - '1'),
     *    and if this number is more one - choose randrom from them.
     *  For example, for first shot this cells will be (4,4)...(7,7), and then choose random from them.
	 */
	private int[][] pMatrix;
	/**
	 * Matrix represents sea of river.
	 *   If cell is live - value is 0, if cell was broken - value is 1.
	 *   For example:
	 *      1 2 3 4 5 6 7 8 9 10
	 *     
	 *   1  0 0 0 0 0 0 0 0 0 0
	 *	 2  0 0 1 0 0 0 0 0 0 0
     *   3  0 0 0 0 0 0 0 0 0 0
     *   4  0 0 0 0 0 0 0 0 0 0
     *   5  0 0 0 1 1 1 0 0 0 0
     *   6  0 0 0 1 1 1 0 0 0 0
     *   7  0 0 0 1 1 1 0 0 0 0
     *   8  0 0 0 1 1 1 0 1 0 0
     *   9  0 0 0 0 0 0 0 0 0 0
     *  10  0 0 0 0 0 0 0 0 0 0
     *  
     *  There is 2 past shots - (3,2),(8,8) and 1 broken ship - CRUISER(3 cells)
	 */
	private int[][] fakeSea;
	/**
	 * Contatins size of rest ships for river.
	 *   Initiality for classic game ships = [4, 3, 3, 2, 2, 2]
	 *   * boats aren't include! 
	 */
	private List<Integer> ships;
	/**
	 * Contains coordinates of last shot.
	 */
	private Cell lastShot;
	/**
	 * Mode of hunting, 
	 *   it is turn on when ship of river was wounded and will turn off when ship will killed.
	 */
	private boolean huntingMode = false;
	/**
	 * Contatins wounded cells of victim,  uses in hunting mode.
	 */
	private List<Cell> victim;
	/**
	 * Mode for aimed shots, 
	 *   it is turn on when game was started and turn off when all ships with size > 1 will be killed.
	 *   For boats impossible using aimed shots!
	 */
	private boolean aimedMode = true;
	/**
	 * It's number already done aimed shots,  uses in mix mode.
	 */
	private int numAimedShots = 0;
	/**
	 * Flag about first shot, true - if it's first shot, else - false.
	 */
	private boolean firstShot = true;
	private Random randomGenerator;
	
	
	public AutoShootingStrategyHighComplexity() {
		System.out.println("AutoShootingStrategyHighComplexity constructor ...");
		init();
	}
	
	private void init() {
		randomGenerator = new Random();
		
		pMatrix = new int[ySize][xSize];
		fakeSea = new int[ySize][xSize];
		// firstly sea of river is intact (all cells = 0)
		for (int x = 0; x < xSize; x++) {
			for (int y = 0; y < ySize; y++) {
				fakeSea[y][x] = 0;
			}
		}
		
		ships = new ArrayList<Integer>();
		createShips();
		
		victim = new ArrayList<Cell>();
	}
	
	/**
	 * Method for getting coordinates of next shot based on last shot result.
	 */
	public Cell getCoordinatesForShot(ShotResult lastShotResult) {
		processLastShot(lastShotResult);
		
		Cell shot = null;
		// first shot will be random
		if (firstShot && mixMode)
		{
			shot = getRandomShot();
			firstShot = false;
		}
		// in hunting mode only aimed shots
		else if (aimedMode && huntingMode) 
		{
			shot = getAimedShot();
		}
		// if mixMode = on, strategy alternates aimed and random shots
		else if (aimedMode && mixMode)
		{
			if (numAimedShots < limitAimedShots) {
				shot = getAimedShot();
				numAimedShots++;
			} else {
				shot = getRandomShot();
				numAimedShots = 0;
			}
		}
		// if mixMode = off, strategy shots only aimed shots
		else if (aimedMode)
		{
			shot = getAimedShot();
		}
		// else aimedMode turn off - strategy does random shots
		else 
		{
			shot = getRandomShot();
		}
		
		lastShot = shot;
		return shot;
	}
	
	/**
	 * Method for reset all properties and strategy will be ready for new game.
	 */
	public void reset() {
		huntingMode = false;
		aimedMode = true;
		firstShot = true;
		
		// reset fakeSea
		for (int x = 0; x < xSize; x++) {
			for (int y = 0; y < ySize; y++) {
				fakeSea[y][x] = 0;
			}
		}
		
		lastShot = null;
		// reset victim
		victim.clear();
		
		// create ships
		ships.clear();
		createShips();
		
		numAimedShots = 0;
	}
	
	/**
	 * Method for processing last shot result.
	 * @param lastShotResult - result of last shot.
	 */
	private void processLastShot(ShotResult lastShotResult) {
		// first shot
		if (lastShotResult == null) {
			firstShot = true;
			return;
		}
		
		if (lastShotResult.equals(ShotResult.WOUNDED)) {
			huntingMode = true;
			victim.add(lastShot);
		} else if (lastShotResult.equals(ShotResult.KILLED)) {
			huntingMode = false;
			victim.add(lastShot);
			processVictim();
			victim.clear();
		} else if (lastShotResult.equals(ShotResult.PAST)) {
			fakeSea[lastShot.getY()-1][lastShot.getX()-1] = 1;
		}
	}
	
	/**
	 * Method for execute aimed shot.
	 * @return coordinates of aimed shot.
	 */
	private Cell getAimedShot() {
		// if all big ships were killed and only boats are left then aimed mode will be turn off
		if (ships.size() == 0) {
			aimedMode = false;
			return getRandomShot();
		}
		// finding the longest ship from rest
		Collections.sort(ships);
		int longest = 0;
		// in hunting mode choose minumal size of ships which have size > victim size
		// for example: vicim size = 2, then longest = 3
		if (huntingMode) {
			for (int ship : ships) {
				if (ship > victim.size()) {
					longest = ship;
					break;
				}
			}
		// get the longest ship
		} else {
			longest = ships.get(ships.size()-1);
		}
		
		// recalculate P-matrix for the longest ship
		calculatePMatrix( longest );
		
		// collects cells which have value of probability = 1
		List<Cell> cells = new ArrayList<Cell>();
		for (int x = 0; x < xSize; x++) {
			for (int y = 0; y < ySize; y++) {
				if (pMatrix[y][x] == 1) {
					cells.add(new Cell(x+1, y+1));
				}
			}
		}
		
		return cells.get( randomGenerator.nextInt(cells.size()) );
	}
	
	/**
	 * Method for execute random shot.
	 * @return coordinates of random shot.
	 */
	private Cell getRandomShot() {
		// collects cells which aren't broken
		List<Cell> cells = new ArrayList<Cell>();
		for (int x = 0; x < xSize; x++) {
			for (int y = 0; y < ySize; y++) {
				if (fakeSea[y][x] == 0) {
					cells.add(new Cell(x+1, y+1));
				}
			}
		}
		
		return cells.get( randomGenerator.nextInt(cells.size()) );
	}
	
	/**
	 * Method for marks cells around victim as broken.
	 */
	private void processVictim() {
		// remove killed ship from ships array
		ships.remove(Integer.valueOf(victim.size()));
		
		for (Cell victimCell : victim) {
			int x = victimCell.getX() - 1;
			int y = victimCell.getY() - 1;
			
			if (x != 0) fakeSea[y][x-1] = 1;
			if (x != 9) fakeSea[y][x+1] = 1;
			fakeSea[y][x] = 1;
			
			if (y != 0) {
				if (x != 0) fakeSea[y-1][x-1] = 1;
				if (x != 9) fakeSea[y-1][x+1] = 1;
				fakeSea[y-1][x] = 1;
			}
			
			if (y != 9) {
				if (x != 0) fakeSea[y+1][x-1] = 1;
				if (x != 9) fakeSea[y+1][x+1] = 1;
				fakeSea[y+1][x] = 1;
			}
		}
	}
	
	/**
	 * Method for calculate matrix of probabilities.
	 * @param shipLength - length of needed ship
	 * 
	 * Logic of method:
	 *   1. "huntingMode" is TURN OFF
	 *   - start cycle for X and Y coordinates ...;
	 *   - get cell from fakeSea for current x and y coordinate;
	 *     if (fakeSea[y][x] = 1) then this cell already broken and ship there can't be located -> P = 0;
	 *   - try locating ship so that ship includes current cell and not includes broken cells;
	 *     for example: ship with length = 3, current cell = (2,5)
	 *       1 2 3 4 5 6 7 8 9 10
	 *     1   
	 *     2   
	 *     3   c
	 *     4   c
	 *     5 n X 0 n
	 *     6   c
	 *     7   c
	 *     8
	 *     9
	 *    10
	 *    X - current cell, 0 - broken cell, c - possible variant, n - impossible variant
	 *    varians for locate: (4 on VERTICAL, but 0 on HORIZONTAL) -> P = 4
	 *   - and repeat last step for every cell;
	 *   - in end will be matrix of probabilities locating for this ship.
	 *   
	 *   2. "huntingMode" is TURN ON
	 *   - start cycle for X and Y coordinates ...;
	 *   - get cell from fakeSea for current x and y coordinate;
	 *     if cell is broken or belongs to victim -> P = 0
	 *   - try locate ship so that ship includes current cell and all cells of victim and not includes broken cells
	 *     for example: ship with length = 3, current cell = (1,4), victim = (2,4) 
	 *       1 2 3 4 5 6 7 8 9 10
	 *     1   
	 *     2 n  
	 *     3 n  
	 *     4 X V c
	 *     5 n   
	 *     6 n 
	 *     7   
	 *     8
	 *     9
	 *    10
	 *    X - current cell, 0 - broken cell, V - victim cell, c - possible variant, n - impossible variant
	 *    varians for locate: (0 on VERTICAL and 1 on HORIZONTAL) -> P = 1
	 *   - and repeat last step for every cell;
	 *   - in end will be matrix of probabilities locating for this ship with accounting current victim.
	 */
	private void calculatePMatrix( int shipLength ) {
		int maxCnt = 0;
		for (int x = 0; x < xSize; x++) {
			for (int y = 0; y < ySize; y++) {
				int cnt = 0;
				if (fakeSea[y][x] != 0) 
				{
					cnt = 0;
				}
				else if (huntingMode && victim.contains(new Cell(x+1, y+1)))
				{
					cnt = 0;
				}
				else
				{
					boolean available = false;
					for (int i=(x-shipLength)+1; i<=x; i++) {
						// horizontal variants
						List<Cell> includes = null;
						if (huntingMode) {
							includes = victim;
						}
						available = checkAvailableCells(i, y, shipLength, Orientation.HORIZONTAL, includes);
						if (available) cnt++;
					}
					for (int i=(y-shipLength)+1; i<=y; i++) {
						// vertical variants
						List<Cell> includes = null;
						if (huntingMode) {
							includes = victim;
						}
						available = checkAvailableCells(x, i, shipLength, Orientation.VERTICAL, includes);
						if (available) cnt++;
					}
				}
				if (cnt > maxCnt) maxCnt = cnt;
				pMatrix[y][x] = cnt;
			}
		}
		
		// normalize P-matrix
		for (int x = 0; x < xSize; x++) {
			for (int y = 0; y < ySize; y++) {
				if (maxCnt != 0) pMatrix[y][x] = pMatrix[y][x] / maxCnt;
			}
		}
	}
	
	/**
	 * Method for checking available locating ship on sea.
	 * @param x - X-coordinate of start cell;
	 * @param y - Y-coordinate of start cell;
	 * @param length - length of ship;
	 * @param o - orientation of ship;
	 * @param includes - cells which should be included in locating ship;
	 * @return true if locating is possible, false - if locating is impossible
	 * 
	 * For example: 
	 *   1. start cell = (4, 5), length = 3, o - HORIZONTAL, includes = null
	 *     1 2 3 4 5 6 7 8 9 10
	 *   1       
	 *   2
	 *   3
	 *   4
	 *   5       S c c  
	 *   6
	 *   7
	 *   8
	 *   9
	 *  10
	 *  S - start cell, c - possible cell 
	 *    in this case method returns TRUE;
	 *    
	 *  2. start cell = (4, 5), length = 3, o - HORIZONTAL, includes = null
	 *     1 2 3 4 5 6 7 8 9 10
	 *   1       
	 *   2
	 *   3
	 *   4
	 *   5       S c 1  
	 *   6
	 *   7
	 *   8
	 *   9
	 *  10
	 *  S - start cell, c - possible cell, 1 - broken cell 
	 *    in this case method returns FALSE;
	 *  
	 *  3. start cell = (4, 5), length = 3, o - HORIZONTAL, includes = (2,2)
	 *     1 2 3 4 5 6 7 8 9 10
	 *   1       
	 *   2   I
	 *   3
	 *   4
	 *   5       S c c
	 *   6
	 *   7
	 *   8
	 *   9
	 *  10
	 *  S - start cell, c - possible cell, I - include cell 
	 *    in this case method returns FALSE because include cells aren't accounting in ship;
	 */
	private boolean checkAvailableCells(int x, int y, int length, Orientation o, List<Cell> includes) {
		List<Cell> checkedCells = null;
		if (includes != null) checkedCells = new ArrayList<Cell>();
		
		if (o.equals(Orientation.HORIZONTAL)) {
			if (x < 0 || (x+length-1) > 9)
				return false;
			
			for (int i = x; i < (x+length); i++) {
				if (fakeSea[y][i] == 1) 
					return false;
				
				if (includes != null)
					checkedCells.add(new Cell(i+1, y+1));
			}
		} else {
			if (y < 0 || (y+length-1) > 9)
				return false;
			
			for (int i = y; i < (y+length); i++) {
				if (fakeSea[i][x] == 1) 
					return false;
				
				if (includes != null)
					checkedCells.add(new Cell(x+1, i+1));
			}
		}
		
		if (includes != null) {
			if (checkedCells.containsAll(includes)) {
				return true;
			} else {
				return false;
			}
		} else {
			return true;
		}
	}
	
	/**
	 * Method for adding initial info about ships.
	 * In classic game: 1 linkor, 2 cruisers, 3 destroyers.
	 *  * boats isn't consider!
	 */
	private void createShips() {
		ships.add(4); // LINKOR
		
		ships.add(3); // CRUISERS
		ships.add(3);
		
		ships.add(2); // DESTROYERS
		ships.add(2);
		ships.add(2);
	}
	
	/**
	 * debug method
	 */
	public void printPMatrix() {
		for (int y = 0; y < xSize; y++) {
			for (int x = 0; x < ySize; x++) {
				System.out.print(pMatrix[y][x]+ " ");
			}
			System.out.print("\n");
		}
	}
}
