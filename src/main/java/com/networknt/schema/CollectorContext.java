package com.networknt.schema;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * A Single Context for holding the output returned by the {@link Collector} implementations.
 *
 */
public enum CollectorContext {

	INSTANCE;

	/**
	 * Map for holding the collector type and {@link Collector}
	 */
	private Map<String, Collector<?>> collectorMap = new HashMap<>();

	/**
	 * Map for holding the collector type and {@link Collector} class collect method output.
	 */
	private Map<String, Object> collectorLoadMap = new HashMap<>();

	public <E> void add(String collectorType, Collector<E> collector) {
		collectorMap.put(collectorType, collector);
	}

	public Object get(String collectorType) {
		if (collectorLoadMap.get(collectorType) == null && collectorMap.get(collectorType) != null) {
			collectorLoadMap.put(collectorType, collectorMap.get(collectorType).collect());
		}
		return collectorLoadMap.get(collectorType);
	}

	/**
	 * Load all the collectors associated with the context.
	 */
	void load() {
		for (Entry<String, Collector<?>> collectorEntrySet : collectorMap.entrySet()) {
			collectorLoadMap.put(collectorEntrySet.getKey(), collectorEntrySet.getValue().collect());
		}
	}

	/**
	 * Reset the context
	 */
	void reset() {
		this.collectorMap = new HashMap<String, Collector<?>>();
		this.collectorLoadMap = new HashMap<String, Object>();
	}

}
