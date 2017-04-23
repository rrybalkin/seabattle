package app.kmf.seabattle.util.json;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Interface describes objects which can be transformed to JSON and back.
 */
public interface JSONable {
	/**
	 * Parse object to JSON
	 * @return JSON object
	 */
	public JSONObject toJSON() throws JSONException;
	
	/**
	 * Parse object from JSON
	 * @param jsonObject - JSON description of this object
	 * @return transformed object
	 */
	public void fromJSON(JSONObject json) throws JSONException;
}
