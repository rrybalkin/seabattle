package app.kmf.seabattle.core.datamodel.impl;

import java.util.ArrayList;
import java.util.List;

import app.kmf.seabattle.core.datamodel.ISeaObject;
import app.kmf.seabattle.core.datamodel.SeaDrop;
import app.kmf.seabattle.enums.ShipType;

public class Ship implements ISeaObject {
	private ShipType type;
	private List<SeaDrop> drops;
	
	public Ship(ShipType type) {
		this.type = type;
	}
	
	@Override
	public int getSize() {
		return type.getCntCells();
	}

	@Override
	public void aggregateDrops(List<SeaDrop> drops) {
		if (drops != null) {
			this.drops = drops;
		}
		
		for (SeaDrop drop : this.drops)
			drop.setFreeStatus(false);
	}
	
	@Override
	public void aggregateDrop(SeaDrop drop) {
		if (drops == null)
			drops = new ArrayList<SeaDrop>();
		
		drops.add(drop);
		drop.setFreeStatus(false);
	}
	
	@Override
	public boolean containsDrop(SeaDrop drop) {
		return drops.contains(drop);
	}
	
	/**
	 * @return number of survived cells of this ship
	 */
	public int getCntSurvivedDrops() {
		int cnt = 0;
		for (SeaDrop drop : drops) {
			if (!drop.isBroken())
				cnt++;
		}
		
		return cnt;
	}
	
	public void reset() {
		drops.clear();
	}
	
	public List<SeaDrop> getDrops() {
		return drops;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder(100);
		sb.append("Ship: type = ").append(type).append(", drops =");
		if (drops == null) {
			sb.append("(empty)");
		} else {
			for (SeaDrop drop : drops) {
				sb.append(" (").append(drop.getX()).append(",").append(drop.getY()).append(")");
			}
		}
		
		return sb.toString();
	}
}
