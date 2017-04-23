package app.kmf.seabattle.enums;

public enum ShotResult {
	/**
	 * If shot isn't hit in ship
	 */
	PAST("past"), 
	/**
	 * If shot hits in ship, but not broke him
	 */
	WOUNDED("wounded"), 
	/**
	 * If whole ship was broken
	 */
	KILLED("killed"), 
	/**
	 * IF all ships of player were killed
	 */
	GAMEOVER("gameover");
	
	private String description;
	
	ShotResult(String description) {
		this.description = description;
	}
	
	public String getDescription() {
		return description;
	}
	
	public static ShotResult create(String description) {
        for (ShotResult result : values()) {
            if (result.description.equalsIgnoreCase(description)) {
                return result;
            }
        }
        return null;
	}
}
