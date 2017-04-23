package app.kmf.seabattle.core.datamodel;

public class SeaDrop {
	/**
	 * Identifier of drop
	 */
	int id;
	/**
	 * X-coordinate of drop
	 */
	int x;
	/**
	 * Y-coordinate of drop
	 */
	int y;
	/**
	 * True if drop isn't aggregated by any SeaObject,
	 *   False if is aggregated
	 */
	boolean free;
	/**
	 * True if drop was broken in game,
	 *   False if drop wasn't broken yet
	 */
	boolean broken;
	
	public SeaDrop(int id, int x, int y) {
		this.id = id;
		this.x = x;
		this.y = y;
		this.free = true;
		this.broken = false;
	}
	
	public void setFreeStatus(boolean status) {
		this.free = status;
	}
	
	public void setBrokenStatus(boolean status) {
		this.broken = status;
	}
	
	public boolean isFree() {
		return free;
	}
	
	public boolean isBroken() {
		return broken;
	}
	
	public int getX() {
		return x;
	}
	
	public int getY() {
		return y;
	}

	@Override
	public String toString() {
		return "SeaDrop [id=" + id + ", x=" + x + ", y=" + y + ", free=" + free
				+ ", broken=" + broken + "]";
	}
}
