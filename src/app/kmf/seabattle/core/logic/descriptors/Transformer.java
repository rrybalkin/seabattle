package app.kmf.seabattle.core.logic.descriptors;

import java.util.ArrayList;
import java.util.List;

import app.kmf.seabattle.core.datamodel.IFleet;
import app.kmf.seabattle.core.datamodel.ISeaObject;
import app.kmf.seabattle.core.datamodel.SeaDrop;
import app.kmf.seabattle.core.datamodel.impl.Fleet;
import app.kmf.seabattle.core.datamodel.impl.Ship;
import app.kmf.seabattle.enums.Orientation;

/**
 * This class contains methods for transforming Game Objects to their descriptors.
 */
public class Transformer {
	
	public static FleetDescriptor transformFleet(IFleet fleet) {
		FleetDescriptor descriptor = null;
		
		if (fleet instanceof Fleet) {
			int seaSizeX = ((Fleet) fleet).getSea().getXSize();
			int seaSizeY = ((Fleet) fleet).getSea().getYSize();
			
			List<ISeaObject> seaObjects = ((Fleet) fleet).getSeaObjects();
			List<SeaObjectDescriptor> soDescriptors = new ArrayList<SeaObjectDescriptor>(seaObjects.size());
			for (ISeaObject seaObject : seaObjects) {
				SeaObjectDescriptor soDescriptor = Transformer.transformSeaObject(seaObject);
				soDescriptors.add(soDescriptor);
			}
			
			descriptor = new FleetDescriptor(seaSizeX, seaSizeY, soDescriptors);
			return descriptor;
		} else {
			return null;
		}
	}
	
	public static SeaObjectDescriptor transformSeaObject(ISeaObject seaObject) {
		SeaObjectDescriptor descriptor = null;
		
		if (seaObject instanceof Ship) {
			List<SeaDrop> drops = ((Ship) seaObject).getDrops();
			
			if (drops.size() == 1) {
				int[] x = new int[]{drops.get(0).getX()};
				int[] y = new int[]{drops.get(0).getY()};
				
				descriptor = new ShipDescriptor(x, y, Orientation.VERTICAL);
			} else {
				Orientation o = (drops.get(0).getX() == drops.get(1).getX()) ? Orientation.VERTICAL : Orientation.HORIZONTAL;
				
				if (o.equals(Orientation.VERTICAL)) {
					int[] x = new int[]{drops.get(0).getX()};
					int[] y = new int[drops.size()];
					
					int i = 0;
					for (SeaDrop drop : drops) {
						y[i] = drop.getY();
						i++;
					}
					
					descriptor = new ShipDescriptor(x, y, o);
				} else {
					int[] y = new int[]{drops.get(0).getY()};
					int[] x = new int[drops.size()];
					
					int i = 0;
					for (SeaDrop drop : drops) {
						x[i] = drop.getX();
						i++;
					}
					
					descriptor = new ShipDescriptor(x, y, o);
				}
			}
			
			return descriptor;
		} else {
			return null;
		}
	}
}
