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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * Context for holding the output returned by the {@link Collector}
 * implementations.
 */
public class CollectorContext {

    // Using a namespace string as key in ThreadLocal so that it is unique in
    // ThreadLocal.
    static final String COLLECTOR_CONTEXT_THREAD_LOCAL_KEY = "com.networknt.schema.CollectorKey";

    // Get an instance from thread info (which uses ThreadLocal).
    public static CollectorContext getInstance() {
        return (CollectorContext) ThreadInfo.get(COLLECTOR_CONTEXT_THREAD_LOCAL_KEY);
    }

    /**
     * Map for holding the name and {@link Collector} or a simple Object.
     */
    private Map<String, Object> collectorMap = new HashMap<>();

    /**
     * Map for holding the name and {@link Collector} class collect method output.
     */
    private Map<String, Object> collectorLoadMap = new HashMap<>();

    private final Deque<Scope> dynamicScopes = new LinkedList<>();

    CollectorContext() {
        this.dynamicScopes.push(new Scope());
    }

    /**
     * Creates a new scope
     * @return the previous, parent scope
     */
    public Scope enterDynamicScope() {
        Scope parent = this.dynamicScopes.peek();
        this.dynamicScopes.push(new Scope());
        return parent;
    }

    /**
     * Restores the previous, parent scope
     * @return the exited scope
     */
    public Scope exitDynamicScope() {
        return this.dynamicScopes.pop();
    }

    /**
     * Provides the currently active scope
     * @return the active scope
     */
    public Scope getDynamicScope() {
        return this.dynamicScopes.peek();
    }

    /**
     * Identifies which array items have been evaluated.
     * 
     * @return the set of evaluated items (never null)
     */
    public Collection<String> getEvaluatedItems() {
        return getDynamicScope().getEvaluatedItems();
    }

    /**
     * Identifies which properties have been evaluated.
     * 
     * @return the set of evaluated properties (never null)
     */
    public Collection<String> getEvaluatedProperties() {
        return getDynamicScope().getEvaluatedProperties();
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
     * Reset the context
     */
    public void reset() {
        this.collectorMap = new HashMap<>();
        this.collectorLoadMap = new HashMap<>();
        this.dynamicScopes.clear();
        this.dynamicScopes.push(new Scope());
    }

    /**
     * Loads data from all collectors.
     */
    void loadCollectors() {
        Set<Entry<String, Object>> entrySet = this.collectorMap.entrySet();
        for (Entry<String, Object> entry : entrySet) {
            if (entry.getValue() instanceof Collector<?>) {
                Collector<?> collector = (Collector<?>) entry.getValue();
                this.collectorLoadMap.put(entry.getKey(), collector.collect());
            }
        }

    }

    public static class Scope {

        /**
         * Used to track which array items have been evaluated.
         */
        private final Collection<String> evaluatedItems = new ArrayList<>();

        /**
         * Used to track which properties have been evaluated.
         */
        private final Collection<String> evaluatedProperties = new ArrayList<>();

        /**
         * Identifies which array items have been evaluated.
         * 
         * @return the set of evaluated items (never null)
         */
        public Collection<String> getEvaluatedItems() {
            return this.evaluatedItems;
        }

        /**
         * Identifies which properties have been evaluated.
         * 
         * @return the set of evaluated properties (never null)
         */
        public Collection<String> getEvaluatedProperties() {
            return this.evaluatedProperties;
        }

        /**
         * Merges the provided scope into this scope.
         * @param scope the scope to merge
         * @return this scope
         */
        public Scope mergeWith(Scope scope) {
            getEvaluatedItems().addAll(scope.getEvaluatedItems());
            getEvaluatedProperties().addAll(scope.getEvaluatedProperties());
            return this;
        }

        @Override
        public String toString() {
            return new StringBuilder("{ ")
                .append("\"evaluatedItems\": ").append(this.evaluatedItems)
                .append(", ")
                .append("\"evaluatedProperties\": ").append(this.evaluatedProperties)
                .append(" }").toString();
        }

    }
}
