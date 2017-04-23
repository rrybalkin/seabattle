package app.kmf.seabattle.enums;

public enum ShipType {
	LINKOR(4), CRUISER(3), DESTROYER(2), BOAT(1);
	
	private int cnt;
	
	ShipType(int cntCells) {
		this.cnt = cntCells;
	}
	
	public int getCntCells() {
		return cnt;
	}
	
	public static ShipType getShipType(int cntCells) {
		for (ShipType type : values()) {
			if (type.cnt == cntCells) {
				return type;
			}
		}
		return null;
	}
}
