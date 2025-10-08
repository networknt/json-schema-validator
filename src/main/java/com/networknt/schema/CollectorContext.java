/*
 * Copyright (c) 2020 Network New Technologies Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.networknt.schema;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Context for holding data which can be set by custom walkers or validators and
 * can be retrieved later from the execution context.
 */
public class CollectorContext {
    /**
     * Map for the data.
     */
    private final Map<Object, Object> data;

    /**
     * Default constructor will use an unsynchronized HashMap to store data. This is
     * suitable if the collector context is not shared with multiple threads.
     */
    public CollectorContext() {
        this(new HashMap<>());
    }

	/**
	 * Constructor that creates the context using the specified instances to store
	 * data.
	 * <p>
	 * If for instance the collector context needs to be shared with multiple
	 * threads a ConcurrentHashMap can be used.
	 * <p>
	 * It is however more likely that the data will only be used after the walk or
	 * validation is complete rather then during processing.
	 *
	 * @param data the data map
	 */
    public CollectorContext(Map<Object, Object> data) {
        this.data = data;
    }

    /**
     * Sets data associated with a given key.
     *
     * @param <T> the return type
     * @param key   the key
     * @param value the value
     * @return the previous value
     */
    @SuppressWarnings("unchecked")
    public <T> T put(Object key, Object value) {
        return (T) this.data.put(key, value);
    }

    /**
     * Gets the data associated with a given key.
     * 
     * @param <T> the return type
     * @param key the key
     * @return the value
     */
    @SuppressWarnings("unchecked")
	public <T> T get(Object key) {
        return (T) this.data.get(key);
    }

    /**
     * Computes the value if absent.
     *
     * @param <T> the return type
     * @param key the key
     * @param mappingFunction the mapping function
     * @return the value
     */
    @SuppressWarnings("unchecked")
    public <T> T computeIfAbsent(Object key, Function<Object,Object> mappingFunction) {
    	return (T) this.data.computeIfAbsent(key, mappingFunction);
    }

    /**
     * Gets the data map.
     * 
     * @return the data map
     */
    public Map<Object, Object> getData() {
        return this.data;
    }
}
