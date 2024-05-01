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

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.Set;

import com.networknt.schema.ExecutionContext;
import com.networknt.schema.ValidationContext;
import com.networknt.schema.ValidationMessage;

/**
 * ListOutputUnitFormatter.
 */
public class ListOutputUnitFormatter {
    public static OutputUnit format(OutputUnit root, OutputUnitData data) {
        Map<OutputUnitKey, Boolean> valid = data.getValid();
        Map<OutputUnitKey, Map<String, Object>> errors = data.getErrors();
        Map<OutputUnitKey, Map<String, Object>> annotations = data.getAnnotations();
        Map<OutputUnitKey, Map<String, Object>> droppedAnnotations = data.getDroppedAnnotations();

        // Process the list
        for (Entry<OutputUnitKey, Boolean> entry : valid.entrySet()) {
            OutputUnit output = new OutputUnit();
            OutputUnitKey key = entry.getKey();
            output.setValid(entry.getValue());
            output.setEvaluationPath(key.getEvaluationPath().toString());
            output.setSchemaLocation(key.getSchemaLocation().toString());
            output.setInstanceLocation(key.getInstanceLocation().toString());

            // Errors
            Map<String, Object> errorMap = errors.get(key);
            if (errorMap != null && !errorMap.isEmpty()) {
                if (output.getErrors() == null) {
                    output.setErrors(new LinkedHashMap<>());
                }
                for (Entry<String, Object> errorEntry : errorMap.entrySet()) {
                    output.getErrors().put(errorEntry.getKey(), errorEntry.getValue());
                }
            }

            // Annotations
            Map<String, Object> annotationsMap = annotations.get(key);
            if (annotationsMap != null && !annotationsMap.isEmpty()) {
                if (output.getAnnotations() == null) {
                    output.setAnnotations(new LinkedHashMap<>());
                }
                for (Entry<String, Object> annotationEntry : annotationsMap.entrySet()) {
                    output.getAnnotations().put(annotationEntry.getKey(), annotationEntry.getValue());
                }
            }

            // Dropped Annotations
            Map<String, Object> droppedAnnotationsMap = droppedAnnotations.get(key);
            if (droppedAnnotationsMap != null && !droppedAnnotationsMap.isEmpty()) {
                if (output.getDroppedAnnotations() == null) {
                    output.setDroppedAnnotations(new LinkedHashMap<>());
                }
                for (Entry<String, Object> droppedAnnotationEntry : droppedAnnotationsMap.entrySet()) {
                    output.getDroppedAnnotations().put(droppedAnnotationEntry.getKey(),
                            droppedAnnotationEntry.getValue());
                }
            }

            List<OutputUnit> details = root.getDetails();
            if (details == null) {
                details = new ArrayList<>();
                root.setDetails(details);
            }
            details.add(output);
        }

        return root;
    }

    public static OutputUnit format(Set<ValidationMessage> validationMessages, ExecutionContext executionContext,
            ValidationContext validationContext, Function<ValidationMessage, Object> assertionMapper) {
        OutputUnit root = new OutputUnit();
        root.setValid(validationMessages.isEmpty());
        return format(root, OutputUnitData.from(validationMessages, executionContext, assertionMapper));
    }
}
