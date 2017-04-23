package app.kmf.seabattle.core.logic.strategy.impl;

import java.util.ArrayList;
import java.util.List;

import app.kmf.seabattle.core.datamodel.IFleet;
import app.kmf.seabattle.core.datamodel.ISea;
import app.kmf.seabattle.core.datamodel.ISeaObject;
import app.kmf.seabattle.core.datamodel.SeaDrop;
import app.kmf.seabattle.core.datamodel.impl.Fleet;
import app.kmf.seabattle.core.datamodel.impl.Sea;
import app.kmf.seabattle.core.datamodel.impl.Ship;
import app.kmf.seabattle.core.logic.descriptors.FleetDescriptor;
import app.kmf.seabattle.core.logic.descriptors.SeaObjectDescriptor;
import app.kmf.seabattle.core.logic.descriptors.ShipDescriptor;
import app.kmf.seabattle.core.logic.strategy.IFleetCreatorStrategy;
import app.kmf.seabattle.enums.Orientation;
import app.kmf.seabattle.enums.ShipType;

public class ManualFleetCreatingStrategy implements IFleetCreatorStrategy {
	private IFleet fleet;
	private FleetDescriptor fleetDescriptor;
	
	public ManualFleetCreatingStrategy() {}
	
	/*stub*/
	@Override
	public IFleet createFleet() {
		return null;
	}
	
	@Override
	public IFleet createFleet(FleetDescriptor descriptor) {
		this.fleetDescriptor = descriptor;
		
		buildFleet();
		
		return fleet;
	}
	
	@Override
	public void reset() {
		
	}
	
	private void buildFleet() {
		// create sea object
		ISea sea = new Sea(fleetDescriptor.getSeaSizeX(), fleetDescriptor.getSeaSizeY());
		
		int x_size = sea.getXSize();
		int y_size = sea.getYSize();
		
		int dropID = 1;
		for (int x = 0; x < x_size; x++)
			for (int y = 0; y < y_size; y++) {
				SeaDrop drop = new SeaDrop(dropID, x, y);
				sea.addSeaDrop(drop);
				
				dropID++;
			}
		
		// create sea objects
		List<SeaObjectDescriptor> soDescriptors = fleetDescriptor.getSeaObjectDescriptors();
		List<ISeaObject> seaObjects = new ArrayList<ISeaObject>(soDescriptors.size());
		
		for (SeaObjectDescriptor sod : soDescriptors) {
			ISeaObject so;
			if (sod instanceof ShipDescriptor) {
				ShipDescriptor sd = (ShipDescriptor) sod;
				
				if (sd.getOrientation().equals(Orientation.HORIZONTAL)) {
					so = new Ship(ShipType.getShipType(sd.getXValues().length));
					
					int x = sd.getXValues().length;
					for (int y : sd.getYValues()) {
						so.aggregateDrop(sea.getSeaDrop(x, y));
					}
				} else {
					so = new Ship(ShipType.getShipType(sd.getYValues().length));
					
					int y = sd.getYValues().length;
					for (int x : sd.getXValues()) {
						so.aggregateDrop(sea.getSeaDrop(x, y));
					}
				}
				
				seaObjects.add(so);
			}
		}
		
		fleet = new Fleet(sea, seaObjects);
	}
}
