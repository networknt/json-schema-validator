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
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;

import com.networknt.schema.ExecutionContext;
import com.networknt.schema.SchemaLocation;
import com.networknt.schema.ValidationContext;
import com.networknt.schema.ValidationMessage;
import com.networknt.schema.annotation.JsonNodeAnnotation;

/**
 * ListOutputUnitFormatter.
 */
public class ListOutputUnitFormatter {
    public static class Key {
        private final String evaluationPath;
        private final String schemaLocation;
        private final String instanceLocation;

        public Key(String evaluationPath, String schemaLocation, String instanceLocation) {
            super();
            this.evaluationPath = evaluationPath;
            this.schemaLocation = schemaLocation;
            this.instanceLocation = instanceLocation;
        }

        @Override
        public int hashCode() {
            return Objects.hash(evaluationPath, instanceLocation, schemaLocation);
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            Key other = (Key) obj;
            return Objects.equals(evaluationPath, other.evaluationPath)
                    && Objects.equals(instanceLocation, other.instanceLocation)
                    && Objects.equals(schemaLocation, other.schemaLocation);
        }
    }

    public static String formatMessage(String message) {
        int index = message.indexOf(": ");
        if (index != -1) {
            return message.substring(index + 2);
        }
        return message;
    }

    public static OutputUnit format(Set<ValidationMessage> validationMessages, ExecutionContext executionContext,
            ValidationContext validationContext) {
        OutputUnit root = new OutputUnit();
        root.setValid(validationMessages.isEmpty());

        Map<Key, Boolean> valid = new LinkedHashMap<>();
        Map<Key, Map<String, String>> errors = new HashMap<>();
        Map<Key, Map<String, Object>> annotations = new HashMap<>();
        Map<Key, Map<String, Object>> droppedAnnotations = new HashMap<>();

        for (ValidationMessage assertion : validationMessages) {
            SchemaLocation assertionSchemaLocation = new SchemaLocation(assertion.getSchemaLocation().getAbsoluteIri(),
                    assertion.getSchemaLocation().getFragment().getParent());
            Key key = new Key(assertion.getEvaluationPath().getParent().toString(), assertionSchemaLocation.toString(),
                    assertion.getInstanceLocation().toString());
            valid.put(key, false);
            Map<String, String> errorMap = errors.computeIfAbsent(key, k -> new LinkedHashMap<>());
            errorMap.put(assertion.getType(), formatMessage(assertion.getMessage()));
        }

        for (List<JsonNodeAnnotation> annotationsResult : executionContext.getAnnotations().asMap().values()) {
            for (JsonNodeAnnotation annotation : annotationsResult) {
                // As some annotations are required for computation, filter those that are not
                // required for reporting
                if (executionContext.getExecutionConfig().getAnnotationCollectionPredicate()
                        .test(annotation.getKeyword())) {
                    SchemaLocation annotationSchemaLocation = new SchemaLocation(
                            annotation.getSchemaLocation().getAbsoluteIri(),
                            annotation.getSchemaLocation().getFragment().getParent());

                    Key key = new Key(annotation.getEvaluationPath().getParent().toString(),
                            annotationSchemaLocation.toString(), annotation.getInstanceLocation().toString());
                    boolean validResult = executionContext.getResults().isValid(annotation.getInstanceLocation(),
                            annotation.getEvaluationPath());
                    valid.put(key, validResult);
                    if (validResult) {
                        // annotations
                        Map<String, Object> annotationMap = annotations.computeIfAbsent(key,
                                k -> new LinkedHashMap<>());
                        annotationMap.put(annotation.getKeyword(), annotation.getValue());
                    } else {
                        // dropped annotations
                        Map<String, Object> droppedAnnotationMap = droppedAnnotations.computeIfAbsent(key,
                                k -> new LinkedHashMap<>());
                        droppedAnnotationMap.put(annotation.getKeyword(), annotation.getValue());
                    }
                }
            }
        }

        // Process the list
        for (Entry<Key, Boolean> entry : valid.entrySet()) {
            OutputUnit output = new OutputUnit();
            Key key = entry.getKey();
            output.setValid(entry.getValue());
            output.setEvaluationPath(key.evaluationPath);
            output.setSchemaLocation(key.schemaLocation);
            output.setInstanceLocation(key.instanceLocation);

            // Errors
            Map<String, String> errorMap = errors.get(key);
            if (errorMap != null && !errorMap.isEmpty()) {
                if (output.getErrors() == null) {
                    output.setErrors(new LinkedHashMap<>());
                }
                for (Entry<String, String> errorEntry : errorMap.entrySet()) {
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
}
