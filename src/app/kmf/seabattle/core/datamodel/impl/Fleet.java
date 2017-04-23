package app.kmf.seabattle.core.datamodel.impl;

import app.kmf.seabattle.core.datamodel.IFleet;
import app.kmf.seabattle.core.datamodel.ISea;
import app.kmf.seabattle.core.datamodel.ISeaObject;
import app.kmf.seabattle.core.datamodel.SeaDrop;
import app.kmf.seabattle.enums.ShotResult;

import java.util.List;

public class Fleet implements IFleet {
	private ISea sea;
	private List<ISeaObject> seaObjects;
	private int cntDeadObjects = 0;
	
	public Fleet(ISea sea, List<ISeaObject> seaObjects) {
		this.sea = sea;
		this.seaObjects = seaObjects;
	}

	public ShotResult doShot(int x, int y) {
		SeaDrop shotDrop = sea.getSeaDrop(x, y);
		
		if (shotDrop.isFree()) {
			return ShotResult.PAST;
		}
		
		for (ISeaObject seaObject : seaObjects) {
			if (seaObject.containsDrop(shotDrop)) {
				shotDrop.setBrokenStatus(true);
				
				int cntSurviveCells = seaObject.getCntSurvivedDrops();
				if (cntSurviveCells > 0) {
					return ShotResult.WOUNDED;
				} else {
					cntDeadObjects++;
					
					if (cntDeadObjects == seaObjects.size()) 
						return ShotResult.GAMEOVER;
					else 
						return ShotResult.KILLED;
				}
			}
		}
		
		return null;
	}
	
	public ISea getSea() {
		return sea;
	}
	
	public List<ISeaObject> getSeaObjects() {
		return seaObjects;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder(1000);
		sb.append("Fleet Sea:\n").append(sea.toString()).append("\n");
		sb.append("Fleet Sea Objects:\n");
		for (ISeaObject seaObject : seaObjects) {
			sb.append(seaObject.toString()).append("\n");
		}
		
		return sb.toString();
	}
	
}
