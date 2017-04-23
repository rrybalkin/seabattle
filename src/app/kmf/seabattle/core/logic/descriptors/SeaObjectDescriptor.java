package app.kmf.seabattle.core.logic.descriptors;

import org.json.JSONException;
import org.json.JSONObject;

import app.kmf.seabattle.util.json.JSONable;

/**
 * This class contains description for sea object
 */
public class SeaObjectDescriptor implements JSONable {
	/**
	 * Aggregated coordinates on X axis
	 */
	protected int[] xValues;
	/**
	 * Aggregated coordinates on Y axis
	 */
	protected int[] yValues;
	
	public SeaObjectDescriptor() {}
	
	public SeaObjectDescriptor(int[] xValues, int[] yValues) {
		this.xValues = xValues;
		this.yValues = yValues;
	}
	
	public int[] getXValues() {
		return xValues;
	}

	public void setXValues(int[] xValues) {
		this.xValues = xValues;
	}

	public int[] getYValues() {
		return yValues;
	}

	public void setYValues(int[] yValues) {
		this.yValues = yValues;
	}
	
	/**
	 * Constructor for creating object from JSON.
	 * @throws JSONException 
	 */
	public SeaObjectDescriptor(JSONObject json) throws JSONException {
		fromJSON( json );
	}

	@Override
	public JSONObject toJSON() throws JSONException {
		JSONObject json = null;
		json = new JSONObject();
		json.put("x", xValues);
		json.put("y", yValues);
			
		return json;
	}

	@Override
	public void fromJSON(JSONObject jsonObject) throws JSONException {
		xValues = (int[]) jsonObject.get("x");
		xValues = (int[]) jsonObject.get("x");
	}
}
