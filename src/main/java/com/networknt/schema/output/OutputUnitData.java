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
import java.util.function.Function;

import com.networknt.schema.ExecutionContext;
import com.networknt.schema.SchemaLocation;
import com.networknt.schema.Error;
import com.networknt.schema.annotation.Annotation;

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

    public static String formatError(Error error) {
        return error.getMessage();
    }

    @SuppressWarnings("unchecked")
    public static OutputUnitData from(List<Error> validationErrors, ExecutionContext executionContext,
            Function<Error, Object> errorMapper) {
        OutputUnitData data = new OutputUnitData();

        Map<OutputUnitKey, Boolean> valid = data.valid;
        Map<OutputUnitKey, Map<String, Object>> errors = data.errors;
        Map<OutputUnitKey, Map<String, Object>> annotations = data.annotations;
        Map<OutputUnitKey, Map<String, Object>> droppedAnnotations = data.droppedAnnotations;

        for (Error error : validationErrors) {
            SchemaLocation assertionSchemaLocation = new SchemaLocation(error.getSchemaLocation().getAbsoluteIri(),
                    error.getSchemaLocation().getFragment().getParent());
            OutputUnitKey key = new OutputUnitKey(error.getEvaluationPath().getParent(),
                    assertionSchemaLocation, error.getInstanceLocation());
            valid.put(key, false);
            Map<String, Object> errorMap = errors.computeIfAbsent(key, k -> new LinkedHashMap<>());
            Object value = errorMap.get(error.getKeyword());
            if (value == null) {
                errorMap.put(error.getKeyword(), errorMapper.apply(error));
            } else {
                // Existing error, make it into a list
                if (value instanceof List) {
                    ((List<Object>) value).add(errorMapper.apply(error));
                } else {
                    List<Object> values = new ArrayList<>();
                    values.add(value.toString());
                    values.add(errorMapper.apply(error));
                    errorMap.put(error.getKeyword(), values);
                }
            }
        }

        for (List<Annotation> annotationsResult : executionContext.getAnnotations().asMap().values()) {
            for (Annotation annotation : annotationsResult) {
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
