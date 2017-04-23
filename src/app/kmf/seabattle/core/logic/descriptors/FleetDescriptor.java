package app.kmf.seabattle.core.logic.descriptors;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONObject;

import app.kmf.seabattle.util.json.JSONOperations;
import app.kmf.seabattle.util.json.JSONable;

/**
 * This class contains description for creating fleet object
 */
public class FleetDescriptor implements JSONable {
	private int seaSizeX;
	private int seaSizeY;
	private List<SeaObjectDescriptor> seaObjectDescriptors;
	
	public FleetDescriptor(int seaSizeX, int seaSizeY) {
		this.seaSizeX = seaSizeX;
		this.seaSizeY = seaSizeY;
		seaObjectDescriptors = new ArrayList<SeaObjectDescriptor>();
	}
	
	public FleetDescriptor(int seaSizeX, int seaSizeY, List<SeaObjectDescriptor> descriptors) {
		this.seaSizeX = seaSizeX;
		this.seaSizeY = seaSizeY;
		this.seaObjectDescriptors = descriptors;
	}
	
	public void addSeaObjectDescriptor(SeaObjectDescriptor descriptor) {
		if (seaObjectDescriptors == null)
			seaObjectDescriptors = new ArrayList<SeaObjectDescriptor>();
		
		seaObjectDescriptors.add(descriptor);
	}
	
	public int getSeaSizeX() {
		return seaSizeX;
	}
	
	public int getSeaSizeY() {
		return seaSizeY;
	}
	
	public List<SeaObjectDescriptor> getSeaObjectDescriptors() {
		return seaObjectDescriptors;
	}

	@Override
	public JSONObject toJSON() {
		JSONObject json = null;
		try {
			json = new JSONObject();
			
			json.put("x_size", seaSizeX);
			json.put("y_size", seaSizeY);
			
			List<String> sea_object_descriptors = new ArrayList<String>(seaObjectDescriptors.size());
			for (SeaObjectDescriptor descriptor : seaObjectDescriptors) {
				JSONObject jsonDescriptor = descriptor.toJSON();
				sea_object_descriptors.add(jsonDescriptor.toString());
			}
			
			json.put("sea_object_descriptors", sea_object_descriptors);
		} catch (Exception e) {
			
		}
		
		return json;
	}
	
	/**
	 * Consturctor for creating object from JSON.
	 */
	public FleetDescriptor(JSONObject json) {
		fromJSON( json );
	}

	@Override
	public void fromJSON(JSONObject json) {
		try {
			this.seaSizeX = (Integer) json.get("x_size");
			this.seaSizeY = (Integer) json.get("y_size");
			
			List<String> sea_object_descriptors = (List<String>) json.get("sea_object_descriptors");
			
			seaObjectDescriptors = new ArrayList<SeaObjectDescriptor>(sea_object_descriptors.size());
			
			for (String sea_object_descriptor : sea_object_descriptors) {
				JSONObject jsonDescriptor = JSONOperations.parseJSON(sea_object_descriptor);
				SeaObjectDescriptor descriptor = new ShipDescriptor( jsonDescriptor );
				
				seaObjectDescriptors.add(descriptor);
			}
		} catch (Exception e) {
			
		}
	}
	
	
}
