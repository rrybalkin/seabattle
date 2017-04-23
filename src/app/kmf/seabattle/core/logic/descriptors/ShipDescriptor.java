package app.kmf.seabattle.core.logic.descriptors;

import org.json.JSONException;
import org.json.JSONObject;

import app.kmf.seabattle.enums.Orientation;

public class ShipDescriptor extends SeaObjectDescriptor {
	/**
	 * Orientation of ship on sea
	 */
	private Orientation orientation;
	
	public ShipDescriptor(int[] xValues, int[] yValues, Orientation orientation) {
		super(xValues, yValues);
		this.orientation = orientation;
	}
	
	public Orientation getOrientation() {
		return orientation;
	}

	public void setOrientation(Orientation orientation) {
		this.orientation = orientation;
	}
	
	@Override
	public JSONObject toJSON() throws JSONException {
		JSONObject json = super.toJSON();
		json.put("orientation", orientation.getValue());
		
		return json;
	}

	/**
	 * Constructor for creating object from JSON.
	 * @throws JSONException 
	 */
	public ShipDescriptor(JSONObject json) throws JSONException {
		fromJSON( json );
	}
	
	@Override
	public void fromJSON(JSONObject json) throws JSONException {
			int orientation = (Integer) json.get("orientation");
			this.orientation = Orientation.create(orientation);
			
			this.xValues = (int[]) json.get("x");
			this.yValues = (int[]) json.get("y");
	}
}
