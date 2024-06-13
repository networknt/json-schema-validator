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
import java.util.Map.Entry;
import java.util.Set;

/**
 * Context for holding the output returned by the {@link Collector}
 * implementations.
 */
public class CollectorContext {
    /**
     * Map for holding the name and {@link Collector} or a simple Object.
     */
    private final Map<String, Object> collectorMap;

    /**
     * Map for holding the name and {@link Collector} class collect method output.
     */
    private final Map<String, Object> collectorLoadMap;

    /**
     * Default constructor will use an unsynchronized HashMap to store data. This is
     * suitable if the collector context is not shared with multiple threads.
     */
    public CollectorContext() {
        this(new HashMap<>(), new HashMap<>());
    }

    /**
     * Constructor that creates the context using the specified instances to store
     * data.
     * <p>
     * If for instance the collector context needs to be shared with multiple
     * threads a ConcurrentHashMap can be used.
     *
     * @param collectorMap the collector map
     * @param collectorLoadMap the collector load map
     */
    public CollectorContext(Map<String, Object> collectorMap, Map<String, Object> collectorLoadMap) {
        this.collectorMap = collectorMap;
        this.collectorLoadMap = collectorLoadMap;
    }

    /**
     * Adds a collector with give name. Preserving this method for backward
     * compatibility.
     *
     * @param <E>       element
     * @param name      String
     * @param collector Collector
     */
    public <E> void add(String name, Collector<E> collector) {
        this.collectorMap.put(name, collector);
    }

    /**
     * Adds a collector or a simple object with give name.
     *
     * @param <E>    element
     * @param object Object
     * @param name   String
     */
    public <E> void add(String name, Object object) {
        this.collectorMap.put(name, object);
    }

    /**
     * Gets the data associated with a given name. Please note if you are collecting
     * {@link Collector} instances you should wait till the validation is complete
     * to gather all data.
     * <p>
     * When {@link CollectorContext} is used to collect {@link Collector} instances
     * for a particular key, this method will return the {@link Collector} instance
     * as long as {@link #loadCollectors} method is not called. Once
     * the {@link #loadCollectors} method is called this method will
     * return the actual data collected by collector.
     *
     * @param name String
     * @return Object
     */
    public Object get(String name) {
        Object object = this.collectorMap.get(name);
        if (object instanceof Collector<?> && (this.collectorLoadMap.get(name) != null)) {
            return this.collectorLoadMap.get(name);
        }
        return this.collectorMap.get(name);
    }

    /**
     * Gets the collector map.
     * 
     * @return the collector map
     */
    public Map<String, Object> getCollectorMap() {
        return this.collectorMap;
    }

    /**
     * Returns all the collected data. Please look into {@link #get(String)} method for more details.
     * @return Map
     */
    public Map<String, Object> getAll() {
        Map<String, Object> mergedMap = new HashMap<>();
        mergedMap.putAll(this.collectorMap);
        mergedMap.putAll(this.collectorLoadMap);
        return mergedMap;
    }

    /**
     * Combines data with Collector identified by the given name.
     *
     * @param name String
     * @param data Object
     */
    public void combineWithCollector(String name, Object data) {
        Object object = this.collectorMap.get(name);
        if (object instanceof Collector<?>) {
            Collector<?> collector = (Collector<?>) object;
            collector.combine(data);
        }
    }

    /**
     * Loads data from all collectors.
     */
    public void loadCollectors() {
        Set<Entry<String, Object>> entrySet = this.collectorMap.entrySet();
        for (Entry<String, Object> entry : entrySet) {
            if (entry.getValue() instanceof Collector<?>) {
                Collector<?> collector = (Collector<?>) entry.getValue();
                this.collectorLoadMap.put(entry.getKey(), collector.collect());
            }
        }
    }
}
