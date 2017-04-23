package app.kmf.seabattle.enums;

public enum PlayerType {
	PLAYER("player"), ANDROID("android"), BLUETOOTH("bluetooth");
	
	private String value;
	
	PlayerType(String value) {
		this.value = value;
	}
	
	public String getValue() {
		return value;
	}
	
	public static PlayerType create(String value) {
		for (PlayerType type : values()) {
			if (type.value.equalsIgnoreCase(value)) {
				return type;
			}
		}
		throw new IllegalArgumentException("value = " + value + " not recognized");
	}
}
