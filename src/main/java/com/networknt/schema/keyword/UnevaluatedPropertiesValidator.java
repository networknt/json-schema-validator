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

package com.networknt.schema.keyword;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.JsonNode;
import com.networknt.schema.ExecutionContext;
import com.networknt.schema.Schema;
import com.networknt.schema.SchemaLocation;
import com.networknt.schema.SchemaContext;
import com.networknt.schema.annotation.Annotation;
import com.networknt.schema.path.NodePath;

/**
 * {@link KeywordValidator} for unevaluatedProperties.
 */
public class UnevaluatedPropertiesValidator extends BaseKeywordValidator {
    private final Schema schema;

    public UnevaluatedPropertiesValidator(SchemaLocation schemaLocation, NodePath evaluationPath, JsonNode schemaNode, Schema parentSchema, SchemaContext schemaContext) {
        super(KeywordType.UNEVALUATED_PROPERTIES, schemaNode, schemaLocation, parentSchema, schemaContext, evaluationPath);

        if (schemaNode.isObject() || schemaNode.isBoolean()) {
            this.schema = schemaContext.newSchema(schemaLocation, evaluationPath, schemaNode, parentSchema);
        } else {
            throw new IllegalArgumentException("The value of 'unevaluatedProperties' MUST be a valid JSON Schema.");
        }
    }

    @Override
    public void validate(ExecutionContext executionContext, JsonNode node, JsonNode rootNode, NodePath instanceLocation) {
        if (!node.isObject()) {
            return;
        }

        
        // Get all the valid adjacent annotations
        Predicate<Annotation> validEvaluationPathFilter = a -> executionContext.getInstanceResults().isValid(instanceLocation, a.getEvaluationPath());

        Predicate<Annotation> adjacentEvaluationPathFilter = a -> a.getEvaluationPath()
                .startsWith(this.evaluationPath.getParent());

        List<Annotation> instanceLocationAnnotations = executionContext.getAnnotations().asMap()
                .getOrDefault(instanceLocation, Collections.emptyList());

        Set<String> evaluatedProperties = new LinkedHashSet<>(); // The properties that unevaluatedProperties schema
        Set<String> existingEvaluatedProperties = new LinkedHashSet<>();
        // Get all the "properties" for the instanceLocation
        List<Annotation> properties = instanceLocationAnnotations.stream()
                .filter(a -> "properties".equals(a.getKeyword())).filter(adjacentEvaluationPathFilter)
                .filter(validEvaluationPathFilter).collect(Collectors.toList());
        for (Annotation annotation : properties) {
            if (annotation.getValue() instanceof Set) {
                Set<String> p = annotation.getValue();
                existingEvaluatedProperties.addAll(p);
            }
        }

        // Get all the "patternProperties" for the instanceLocation
        List<Annotation> patternProperties = instanceLocationAnnotations.stream()
                .filter(a -> "patternProperties".equals(a.getKeyword())).filter(adjacentEvaluationPathFilter)
                .filter(validEvaluationPathFilter).collect(Collectors.toList());
        for (Annotation annotation : patternProperties) {
            if (annotation.getValue() instanceof Set) {
                Set<String> p = annotation.getValue();
                existingEvaluatedProperties.addAll(p);
            }
        }

        // Get all the "patternProperties" for the instanceLocation
        List<Annotation> additionalProperties = instanceLocationAnnotations.stream()
                .filter(a -> "additionalProperties".equals(a.getKeyword())).filter(adjacentEvaluationPathFilter)
                .filter(validEvaluationPathFilter).collect(Collectors.toList());
        for (Annotation annotation : additionalProperties) {
            if (annotation.getValue() instanceof Set) {
                Set<String> p = annotation.getValue();
                existingEvaluatedProperties.addAll(p);
            }
        }

        // Get all the "unevaluatedProperties" for the instanceLocation
        List<Annotation> unevaluatedProperties = instanceLocationAnnotations.stream()
                .filter(a -> "unevaluatedProperties".equals(a.getKeyword())).filter(adjacentEvaluationPathFilter)
                .filter(validEvaluationPathFilter).collect(Collectors.toList());
        for (Annotation annotation : unevaluatedProperties) {
            if (annotation.getValue() instanceof Set) {
                Set<String> p = annotation.getValue();
                existingEvaluatedProperties.addAll(p);
            }
        }

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
                        executionContext.addError(error().instanceNode(node).instanceLocation(instanceLocation).property(fieldName)
                                .arguments(fieldName).locale(executionContext.getExecutionConfig().getLocale())
                                .build());
                    } else {
                        // Schema errors will be reported as is
                        this.schema.validate(executionContext, node.get(fieldName), node,
                                instanceLocation.append(fieldName));
                    }
                }
            }
        } finally {
            executionContext.setFailFast(failFast); // restore flag
        }
        executionContext.getAnnotations()
                .put(Annotation.builder().instanceLocation(instanceLocation).evaluationPath(this.evaluationPath)
                        .schemaLocation(this.schemaLocation).keyword(getKeyword()).value(evaluatedProperties).build());

        return;
    }
}
