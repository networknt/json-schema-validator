/*
 * Copyright (c) 2024 the original author or authors.
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
package com.networknt.schema.output;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.Set;

import com.networknt.schema.ExecutionContext;
import com.networknt.schema.JsonNodePath;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.ValidationContext;
import com.networknt.schema.ValidationMessage;

/**
 * HierarchicalOutputUnitFormatter.
 */
public class HierarchicalOutputUnitFormatter {
    public static OutputUnit format(OutputUnit root, OutputUnitData data, JsonNodePath rootPath) {
        Map<OutputUnitKey, Boolean> valid = data.getValid();
        Map<OutputUnitKey, Map<String, Object>> errors = data.getErrors();
        Map<OutputUnitKey, Map<String, Object>> annotations = data.getAnnotations();
        Map<OutputUnitKey, Map<String, Object>> droppedAnnotations = data.getDroppedAnnotations();
        
        // Evaluation path to output unit
        Map<JsonNodePath, Map<JsonNodePath, OutputUnit>> index = new LinkedHashMap<>();
        Map<JsonNodePath, OutputUnit> r = new LinkedHashMap<>();
        r.put(rootPath, root);
        index.put(rootPath, r);
        
        // Get all the evaluation paths with data
        // This is a map of evaluation path to instance location
        Map<JsonNodePath, Set<JsonNodePath>> keys = new LinkedHashMap<>();
        errors.keySet().stream().forEach(k -> keys.computeIfAbsent(k.getEvaluationPath(), a -> new LinkedHashSet<>())
                .add(k.getInstanceLocation()));
        annotations.keySet().stream().forEach(k -> keys
                .computeIfAbsent(k.getEvaluationPath(), a -> new LinkedHashSet<>()).add(k.getInstanceLocation()));
        droppedAnnotations.keySet().stream().forEach(k -> keys
                .computeIfAbsent(k.getEvaluationPath(), a -> new LinkedHashSet<>()).add(k.getInstanceLocation()));
        
        errors.keySet().stream().forEach(k -> buildIndex(k, index, keys, root));
        annotations.keySet().stream().forEach(k -> buildIndex(k, index, keys, root));
        droppedAnnotations.keySet().stream().forEach(k -> buildIndex(k, index, keys, root));
        
        // Process all the data
        for (Entry<OutputUnitKey, Map<String, Object>> error : errors.entrySet()) {
            OutputUnitKey key = error.getKey();
            OutputUnit unit = index.get(key.getEvaluationPath()).get(key.getInstanceLocation());
            unit.setInstanceLocation(key.getInstanceLocation().toString());
            unit.setSchemaLocation(key.getSchemaLocation().toString());
            unit.setValid(false);
            unit.setErrors(error.getValue());
        }

        for (Entry<OutputUnitKey, Map<String, Object>> annotation : annotations.entrySet()) {
            OutputUnitKey key = annotation.getKey();
            OutputUnit unit = index.get(key.getEvaluationPath()).get(key.getInstanceLocation());
            String instanceLocation = key.getInstanceLocation().toString();
            String schemaLocation = key.getSchemaLocation().toString();
            if (unit.getInstanceLocation() != null && !unit.getInstanceLocation().equals(instanceLocation)) {
                throw new IllegalArgumentException();
            }
            if (unit.getSchemaLocation() != null && !unit.getSchemaLocation().equals(schemaLocation)) {
                throw new IllegalArgumentException();
            }
            unit.setInstanceLocation(instanceLocation);
            unit.setSchemaLocation(schemaLocation);
            unit.setAnnotations(annotation.getValue());
            unit.setValid(valid.get(key));
        }
        
        for (Entry<OutputUnitKey, Map<String, Object>> droppedAnnotation : droppedAnnotations.entrySet()) {
            OutputUnitKey key = droppedAnnotation.getKey();
            OutputUnit unit = index.get(key.getEvaluationPath()).get(key.getInstanceLocation());
            String instanceLocation = key.getInstanceLocation().toString();
            String schemaLocation = key.getSchemaLocation().toString();
            if (unit.getInstanceLocation() != null && !unit.getInstanceLocation().equals(instanceLocation)) {
                throw new IllegalArgumentException();
            }
            if (unit.getSchemaLocation() != null && !unit.getSchemaLocation().equals(schemaLocation)) {
                throw new IllegalArgumentException();
            }
            unit.setInstanceLocation(instanceLocation);
            unit.setSchemaLocation(schemaLocation);
            unit.setDroppedAnnotations(droppedAnnotation.getValue());
            unit.setValid(valid.get(key));
        }
        return root;
    }

    public static OutputUnit format(JsonSchema jsonSchema, Set<ValidationMessage> validationMessages,
            ExecutionContext executionContext, ValidationContext validationContext,
            Function<ValidationMessage, Object> assertionMapper) {
        OutputUnit root = new OutputUnit();
        root.setValid(validationMessages.isEmpty());
        
        root.setInstanceLocation(validationContext.getConfig().getPathType().getRoot());
        root.setEvaluationPath(validationContext.getConfig().getPathType().getRoot());
        root.setSchemaLocation(jsonSchema.getSchemaLocation().toString());

        OutputUnitData data = OutputUnitData.from(validationMessages, executionContext, assertionMapper);
        
        return format(root, data, new JsonNodePath(validationContext.getConfig().getPathType()));
    }
    
    /**
     * Builds in the index of evaluation path to output units to be populated later
     * and modify the root to add the appropriate children.
     * 
     * @param key   the current key to process
     * @param index contains all the mappings from evaluation path to output units
     * @param keys  that contain all the evaluation paths with instance data
     * @param root  the root output unit
     */
    protected static void buildIndex(OutputUnitKey key, Map<JsonNodePath, Map<JsonNodePath, OutputUnit>> index,
            Map<JsonNodePath, Set<JsonNodePath>> keys, OutputUnit root) {
        if (index.containsKey(key.getEvaluationPath())) {
            return;
        }
        // Ensure the path is created
        JsonNodePath path = key.getEvaluationPath();
        Deque<JsonNodePath> stack = new ArrayDeque<>();
        while (path != null && path.getElement(-1) != null) {
            stack.push(path);
            path = path.getParent();
        }

        OutputUnit parent = root;
        while (!stack.isEmpty()) {
            JsonNodePath current = stack.pop();
            if (!index.containsKey(current) && keys.containsKey(current)) {
                // the index doesn't contain this path but this is a path with data
                for (JsonNodePath instanceLocation : keys.get(current)) {
                    OutputUnit child = new OutputUnit();
                    child.setValid(true);
                    child.setEvaluationPath(current.toString());
                    child.setInstanceLocation(instanceLocation.toString());
                    index.computeIfAbsent(current, n -> new LinkedHashMap<>()).put(instanceLocation, child);
                    if (parent.getDetails() == null) {
                        parent.setDetails(new ArrayList<>());
                    }
                    parent.getDetails().add(child);
                }
            }

            // If exists in the index this is the new parent
            // Otherwise this is an evaluation path with no data and hence should be skipped
            // InstanceLocation to OutputUnit
            Map<JsonNodePath, OutputUnit> result = index.get(current);
            if (result != null) {
                for (Entry<JsonNodePath, OutputUnit> entry : result.entrySet()) {
                    if (key.getInstanceLocation().startsWith(entry.getKey())) {
                        parent = entry.getValue();
                        break;
                    }
                }
            }
        }
    }
}
