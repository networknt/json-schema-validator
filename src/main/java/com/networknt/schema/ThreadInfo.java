package com.networknt.schema;

import java.util.HashMap;
import java.util.Map;

public class ThreadInfo {

	private static ThreadLocal<Map<String, Object>> threadLocal = new ThreadLocal<Map<String, Object>>() {
		protected java.util.Map<String, Object> initialValue() {
			return new HashMap<String, Object>();
		};
	};

	public static Object get(String key) {
		return threadLocal.get().get(key);
	}

	public static void set(String key, Object value) {
		Map<String, Object> threadLocalMap = threadLocal.get();
		threadLocalMap.put(key, value);
	}

	public static void remove(String key) {
		Map<String, Object> threadLocalMap = threadLocal.get();
		threadLocalMap.remove(key);
	}

}
