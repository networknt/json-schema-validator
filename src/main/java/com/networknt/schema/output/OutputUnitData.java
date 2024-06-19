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
import java.util.Set;
import java.util.function.Function;

import com.networknt.schema.ExecutionContext;
import com.networknt.schema.SchemaLocation;
import com.networknt.schema.ValidationMessage;
import com.networknt.schema.annotation.JsonNodeAnnotation;

/**
 * Output Unit Data.
 */
public class OutputUnitData {
    private final Map<OutputUnitKey, Boolean> valid = new LinkedHashMap<>();
    private final Map<OutputUnitKey, Map<String, Object>> errors = new LinkedHashMap<>();
    private final Map<OutputUnitKey, Map<String, Object>> annotations = new LinkedHashMap<>();
    private final Map<OutputUnitKey, Map<String, Object>> droppedAnnotations = new LinkedHashMap<>();

    public Map<OutputUnitKey, Boolean> getValid() {
        return valid;
    }

    public Map<OutputUnitKey, Map<String, Object>> getErrors() {
        return errors;
    }

    public Map<OutputUnitKey, Map<String, Object>> getAnnotations() {
        return annotations;
    }

    public Map<OutputUnitKey, Map<String, Object>> getDroppedAnnotations() {
        return droppedAnnotations;
    }

    public static String formatAssertion(ValidationMessage validationMessage) {
        return formatMessage(validationMessage.getMessage());
    }

    public static String formatMessage(String message) {
        int index = message.indexOf(':');
        if (index != -1) {
            int length = message.length();
            while (index + 1 < length) {
                if (message.charAt(index + 1) == ' ') {
                    index++;
                } else {
                    break;
                }
            }
            return message.substring(index + 1);
        }
        return message;
    }

    @SuppressWarnings("unchecked")
    public static OutputUnitData from(Set<ValidationMessage> validationMessages, ExecutionContext executionContext,
            Function<ValidationMessage, Object> assertionMapper) {
        OutputUnitData data = new OutputUnitData();

        Map<OutputUnitKey, Boolean> valid = data.valid;
        Map<OutputUnitKey, Map<String, Object>> errors = data.errors;
        Map<OutputUnitKey, Map<String, Object>> annotations = data.annotations;
        Map<OutputUnitKey, Map<String, Object>> droppedAnnotations = data.droppedAnnotations;

        for (ValidationMessage assertion : validationMessages) {
            SchemaLocation assertionSchemaLocation = new SchemaLocation(assertion.getSchemaLocation().getAbsoluteIri(),
                    assertion.getSchemaLocation().getFragment().getParent());
            OutputUnitKey key = new OutputUnitKey(assertion.getEvaluationPath().getParent(),
                    assertionSchemaLocation, assertion.getInstanceLocation());
            valid.put(key, false);
            Map<String, Object> errorMap = errors.computeIfAbsent(key, k -> new LinkedHashMap<>());
            Object value = errorMap.get(assertion.getType());
            if (value == null) {
                errorMap.put(assertion.getType(), assertionMapper.apply(assertion));
            } else {
                // Existing error, make it into a list
                if (value instanceof List) {
                    ((List<Object>) value).add(assertionMapper.apply(assertion));
                } else {
                    List<Object> values = new ArrayList<>();
                    values.add(value.toString());
                    values.add(assertionMapper.apply(assertion));
                    errorMap.put(assertion.getType(), values);
                }
            }
        }

        for (List<JsonNodeAnnotation> annotationsResult : executionContext.getAnnotations().asMap().values()) {
            for (JsonNodeAnnotation annotation : annotationsResult) {
                // As some annotations are required for computation, filter those that are not
                // required for reporting
                if (executionContext.getExecutionConfig().getAnnotationCollectionFilter()
                        .test(annotation.getKeyword())) {
                    SchemaLocation annotationSchemaLocation = new SchemaLocation(
                            annotation.getSchemaLocation().getAbsoluteIri(),
                            annotation.getSchemaLocation().getFragment().getParent());

                    OutputUnitKey key = new OutputUnitKey(annotation.getEvaluationPath().getParent(),
                            annotationSchemaLocation, annotation.getInstanceLocation());
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
        return data;
    }
}
