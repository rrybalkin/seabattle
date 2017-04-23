package app.kmf.seabattle.enums;

public enum Orientation {
	VERTICAL(0), HORIZONTAL(1);

	private int number;

	Orientation(int num) {
		this.number = num;
	}
	
	public int getValue() {
		return number;
	}

	public static Orientation create(int num) {
		for (Orientation o : values()) {
			if (o.number == num) {
				return o;
			}
		}
		return null;
	}
	
	public static Orientation opposite(Orientation o) {
		if (o.equals(VERTICAL))
			return HORIZONTAL;
		else 
			return VERTICAL;
	}
};
