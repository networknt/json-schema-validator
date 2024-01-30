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
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import com.networknt.schema.ExecutionContext;
import com.networknt.schema.JsonNodePath;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.ValidationContext;
import com.networknt.schema.ValidationMessage;

/**
 * HierarchicalOutputUnitFormatter.
 */
public class HierarchicalOutputUnitFormatter {
    public static OutputUnit format(JsonSchema jsonSchema, Set<ValidationMessage> validationMessages,
            ExecutionContext executionContext, ValidationContext validationContext) {
        
        OutputUnit root = new OutputUnit();
        root.setValid(validationMessages.isEmpty());
        
        root.setInstanceLocation(validationContext.getConfig().getPathType().getRoot());
        root.setEvaluationPath(validationContext.getConfig().getPathType().getRoot());
        root.setSchemaLocation(jsonSchema.getSchemaLocation().toString());

        OutputUnitData data = OutputUnitData.from(validationMessages, executionContext);

        Map<OutputUnitKey, Boolean> valid = data.getValid();
        Map<OutputUnitKey, Map<String, String>> errors = data.getErrors();
        Map<OutputUnitKey, Map<String, Object>> annotations = data.getAnnotations();
        Map<OutputUnitKey, Map<String, Object>> droppedAnnotations = data.getDroppedAnnotations();
        
        // Evaluation path to output unit
        Map<JsonNodePath, OutputUnit> index = new LinkedHashMap<>();
        index.put(new JsonNodePath(validationContext.getConfig().getPathType()), root);
        
        return null;
    }
    
    protected static void process(OutputUnitKey key,  Map<JsonNodePath, OutputUnit> index) {
        if(index.containsKey(key.getEvaluationPath())) {
            return;
        }
        // Ensure the path is created
        JsonNodePath path = key.getEvaluationPath();
        Deque<JsonNodePath> stack = new ArrayDeque<>();
        while(!index.containsKey(path)) {
            stack.push(path);
        }

    }
}
