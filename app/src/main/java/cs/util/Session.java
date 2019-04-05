package cs.util;

import org.json.JSONException;
import org.json.JSONObject;

import android.os.Bundle;

public class Session {
	private static Session _instance;
	public static Session getInstance() {
		if (_instance == null) {
			_instance = new Session();
		}
		return _instance;
	}
	
	JSONObject map;
	private Session() {
		map = new JSONObject();
	}
	
	public String serialize() {
		return map.toString();
	}

	public void unserialize(String m) {
		try {
			this.map = new JSONObject(m);
		} catch (Exception ex) {
		}
	}

	public void setValue(String key, String value) {
		try {
			this.map.put(key, value);
		} catch (Exception ex) {
		}
	}

	public void removeValue(String key) {
		this.map.remove(key);
	}

	public String getValue(String key) {
		try {
			return this.map.getString(key);
		} catch (JSONException e) {
		}
		return null;
	}
	
	public static void saveStates(Bundle savedInstanceBundle){
		savedInstanceBundle.putString("Session", getInstance().serialize());
	}
	
	public static void restoreStates(Bundle savedInstanceBundle){
		getInstance().unserialize(savedInstanceBundle.getString("Session"));
	}
}
