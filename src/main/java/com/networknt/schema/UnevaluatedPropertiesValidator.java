/*
 * Copyright (c) 2016 Network New Technologies Inc.
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

import com.fasterxml.jackson.databind.JsonNode;
import com.networknt.schema.annotation.JsonNodeAnnotation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * {@link JsonValidator} for unevaluatedProperties.
 */
public class UnevaluatedPropertiesValidator extends BaseJsonValidator {
    private static final Logger logger = LoggerFactory.getLogger(UnevaluatedPropertiesValidator.class);

    private final JsonSchema schema;

    public UnevaluatedPropertiesValidator(SchemaLocation schemaLocation, JsonNodePath evaluationPath, JsonNode schemaNode, JsonSchema parentSchema, ValidationContext validationContext) {
        super(schemaLocation, evaluationPath, schemaNode, parentSchema, ValidatorTypeCode.UNEVALUATED_PROPERTIES, validationContext);

        if (schemaNode.isObject() || schemaNode.isBoolean()) {
            this.schema = validationContext.newSchema(schemaLocation, evaluationPath, schemaNode, parentSchema);
        } else {
            throw new IllegalArgumentException("The value of 'unevaluatedProperties' MUST be a valid JSON Schema.");
        }
    }

    @Override
    public Set<ValidationMessage> validate(ExecutionContext executionContext, JsonNode node, JsonNode rootNode, JsonNodePath instanceLocation) {
        if (!node.isObject()) {
            return Collections.emptySet();
        }

        debug(logger, executionContext, node, rootNode, instanceLocation);
        // Get all the valid adjacent annotations
        Predicate<JsonNodeAnnotation> validEvaluationPathFilter = a -> executionContext.getResults().isValid(instanceLocation, a.getEvaluationPath());

        Predicate<JsonNodeAnnotation> adjacentEvaluationPathFilter = a -> a.getEvaluationPath()
                .startsWith(this.evaluationPath.getParent());

        List<JsonNodeAnnotation> instanceLocationAnnotations = executionContext.getAnnotations().asMap()
                .getOrDefault(instanceLocation, Collections.emptyList());

        Set<String> evaluatedProperties = new LinkedHashSet<>(); // The properties that unevaluatedProperties schema
        Set<String> existingEvaluatedProperties = new LinkedHashSet<>();
        // Get all the "properties" for the instanceLocation
        List<JsonNodeAnnotation> properties = instanceLocationAnnotations.stream()
                .filter(a -> "properties".equals(a.getKeyword())).filter(adjacentEvaluationPathFilter)
                .filter(validEvaluationPathFilter).collect(Collectors.toList());
        for (JsonNodeAnnotation annotation : properties) {
            if (annotation.getValue() instanceof Set) {
                Set<String> p = annotation.getValue();
                existingEvaluatedProperties.addAll(p);
            }
        }

        // Get all the "patternProperties" for the instanceLocation
        List<JsonNodeAnnotation> patternProperties = instanceLocationAnnotations.stream()
                .filter(a -> "patternProperties".equals(a.getKeyword())).filter(adjacentEvaluationPathFilter)
                .filter(validEvaluationPathFilter).collect(Collectors.toList());
        for (JsonNodeAnnotation annotation : patternProperties) {
            if (annotation.getValue() instanceof Set) {
                Set<String> p = annotation.getValue();
                existingEvaluatedProperties.addAll(p);
            }
        }

        // Get all the "patternProperties" for the instanceLocation
        List<JsonNodeAnnotation> additionalProperties = instanceLocationAnnotations.stream()
                .filter(a -> "additionalProperties".equals(a.getKeyword())).filter(adjacentEvaluationPathFilter)
                .filter(validEvaluationPathFilter).collect(Collectors.toList());
        for (JsonNodeAnnotation annotation : additionalProperties) {
            if (annotation.getValue() instanceof Set) {
                Set<String> p = annotation.getValue();
                existingEvaluatedProperties.addAll(p);
            }
        }

        // Get all the "unevaluatedProperties" for the instanceLocation
        List<JsonNodeAnnotation> unevaluatedProperties = instanceLocationAnnotations.stream()
                .filter(a -> "unevaluatedProperties".equals(a.getKeyword())).filter(adjacentEvaluationPathFilter)
                .filter(validEvaluationPathFilter).collect(Collectors.toList());
        for (JsonNodeAnnotation annotation : unevaluatedProperties) {
            if (annotation.getValue() instanceof Set) {
                Set<String> p = annotation.getValue();
                existingEvaluatedProperties.addAll(p);
            }
        }

        Set<ValidationMessage> messages = new LinkedHashSet<>();
        // Save flag as nested schema evaluation shouldn't trigger fail fast
        boolean failFast = executionContext.isFailFast();
        try {
            executionContext.setFailFast(false);
            for (Iterator<String> it = node.fieldNames(); it.hasNext();) {
                String fieldName = it.next();
                if (!existingEvaluatedProperties.contains(fieldName)) {
                    evaluatedProperties.add(fieldName);
                    if (this.schemaNode.isBoolean() && this.schemaNode.booleanValue() == false) {
                        // All fails as "unevaluatedProperties: false"
                        messages.add(message().instanceNode(node).instanceLocation(instanceLocation).property(fieldName)
                                .arguments(fieldName).locale(executionContext.getExecutionConfig().getLocale())
                                .failFast(executionContext.isFailFast()).build());
                    } else {
                        // Schema errors will be reported as is
                        messages.addAll(this.schema.validate(executionContext, node.get(fieldName), node,
                                instanceLocation.append(fieldName)));
                    }
                }
            }
        } finally {
            executionContext.setFailFast(failFast); // restore flag
        }
        executionContext.getAnnotations()
                .put(JsonNodeAnnotation.builder().instanceLocation(instanceLocation).evaluationPath(this.evaluationPath)
                        .schemaLocation(this.schemaLocation).keyword(getKeyword()).value(evaluatedProperties).build());

        return messages == null || messages.isEmpty() ? Collections.emptySet() : messages;
    }
}
