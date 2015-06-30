package org.andrewnr.oauth.model;

import java.util.HashMap;
import java.util.Map;

public class MemoryCache {
	private static MemoryCache instance;
	private static final Map<String, Object> storage = new HashMap<String, Object>();
	
	private MemoryCache() {
	}
	
	public static MemoryCache getInstance() {
		if (instance == null) {
			instance = new MemoryCache();
		}
		return instance;
	}
	
	public Boolean containsKey(String key) {
		return storage.containsKey(key);
	}
	
	public Object get(String key) {
		return storage.get(key);
	}
	
	public void put(String key, Object cachedObj) {
		storage.put(key, cachedObj);
	}
}
