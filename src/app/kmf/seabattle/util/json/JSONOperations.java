package app.kmf.seabattle.util.json;

import org.json.JSONObject;
import org.json.JSONTokener;

public class JSONOperations {
	
	/**
	 * Method for transforming string of JSON to JSON object
	 * @param json_str - string of JSON
	 * @return JSON object
	 */
	public static JSONObject parseJSON(String json_str) {	
		JSONObject json = null;
		try {
			JSONTokener tokener = new JSONTokener(json_str);
			json = new JSONObject(tokener);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return json;
	}
}
