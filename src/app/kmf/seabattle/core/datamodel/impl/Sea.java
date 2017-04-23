package app.kmf.seabattle.core.datamodel.impl;

import app.kmf.seabattle.core.datamodel.ISea;
import app.kmf.seabattle.core.datamodel.SeaDrop;

public class Sea implements ISea {
	private int X_SIZE = 10;
	private int Y_SIZE = 10;
	/**
	 * container for SeaDrop objects
	 *   first index - Y axis
	 *   second index - X axis
	 *   
	 * Example:
	 *                             X - axis
	 * drops =        1          2           3        4 5 6 7 8 9 10
	 *         1 drops[1][1] drops[1][2] drops[1][3] ...
	 *         2 drops[2][1] drops[2][2]    ...
	 *         3 drops[3][1]    ...
	 *    Y    4
	 *    |    5
	 *    a    6
	 *    x    7
	 *    i    8
	 *    s    9
	 *        10
	 */
	private SeaDrop[][] drops;
	
	public Sea() {
		drops = new SeaDrop[Y_SIZE][X_SIZE];
	}
	
	public Sea(int xSize, int ySize) {
		this.X_SIZE = xSize;
		this.Y_SIZE = ySize;
		drops = new SeaDrop[Y_SIZE][X_SIZE];
	}

	@Override
	public int getXSize() {
		return X_SIZE;
	}

	@Override
	public int getYSize() {
		return Y_SIZE;
	}

	@Override
	public void addSeaDrop(SeaDrop drop) {
		int x = drop.getX(), y = drop.getY();
		if (x <= X_SIZE && y <= Y_SIZE) {
			drops[y-1][x-1] = drop;
		}
	}
	
	public SeaDrop getSeaDrop(int x, int y) {
		if (x <= X_SIZE && y <= Y_SIZE) {
			return drops[y-1][x-1];
		} else {
			return null;
		}
	}
	
	public void reset() {
		for (int x = 0; x < X_SIZE; x++)
			for (int y = 0; y < Y_SIZE; y++) {
				drops[y][x].setFreeStatus(true);
				drops[y][x].setBrokenStatus(false);
			}
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder(1000);
		sb.append("Sea:\n").append("   ");
		
		for(int x = 1; x <= X_SIZE; x++)
			sb.append(x).append(" ");
		
		sb.append("\n");
		
		for (int y = 1; y <= Y_SIZE; y++) {
			sb.append(y);
			if (y >= 10 ) sb.append(" ");
			else sb.append("  ");
			
			for (int x = 1; x <= X_SIZE; x++) {
				if (drops[y-1][x-1].isFree()) sb.append("O");
				else sb.append("X");
				sb.append(" ");
			}
			sb.append("\n");
		}
		
		return sb.toString();
	}
}
