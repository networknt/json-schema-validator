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
        if (!node.isObject()) return Collections.emptySet();

        debug(logger, node, rootNode, instanceLocation);
        CollectorContext collectorContext = executionContext.getCollectorContext();

        collectorContext.exitDynamicScope();
        try {
            // Get all the valid adjacent annotations
            Predicate<JsonNodeAnnotation> validEvaluationPathFilter = a -> {
                for (ValidationMessage assertion : executionContext.getAssertions().values()) {
                    JsonNodePath e = assertion.getEvaluationPath();
                    if (e.getParent().startsWith(a.getEvaluationPath())
                            || a.getEvaluationPath().startsWith(e.getParent())) {
                        // Invalid
                        System.out.println(e.toString());
                        System.out.println(a.getEvaluationPath().toString());
                        return false;
                    }
                }
                return true;
            };

            Predicate<JsonNodeAnnotation> adjacentEvaluationPathFilter = a -> a.getEvaluationPath()
                    .startsWith(this.evaluationPath.getParent());

            Map<String, Map<JsonNodePath, JsonNodeAnnotation>> instanceLocationAnnotations = executionContext
                    .getAnnotations().asMap().getOrDefault(instanceLocation, Collections.emptyMap());

            Set<String> evaluatedProperties = new LinkedHashSet<>(); // The properties that unevaluatedProperties schema
            Set<String> existingEvaluatedProperties = new LinkedHashSet<>();
            // Get all the "properties" for the instanceLocation
            List<JsonNodeAnnotation> properties = instanceLocationAnnotations
                    .getOrDefault("properties", Collections.emptyMap()).values().stream()
                    .filter(adjacentEvaluationPathFilter).filter(validEvaluationPathFilter)
                    .collect(Collectors.toList());
            for (JsonNodeAnnotation annotation : properties) {
                if (annotation.getValue() instanceof Set) {
                    Set<String> p = annotation.getValue();
                    existingEvaluatedProperties.addAll(p);
                }
            }

            // Get all the "patternProperties" for the instanceLocation
            List<JsonNodeAnnotation> patternProperties = instanceLocationAnnotations
                    .getOrDefault("patternProperties", Collections.emptyMap()).values().stream()
                    .filter(adjacentEvaluationPathFilter).filter(validEvaluationPathFilter)
                    .collect(Collectors.toList());
            for (JsonNodeAnnotation annotation : patternProperties) {
                if (annotation.getValue() instanceof Set) {
                    Set<String> p = annotation.getValue();
                    existingEvaluatedProperties.addAll(p);
                }
            }

            // Get all the "patternProperties" for the instanceLocation
            List<JsonNodeAnnotation> additionalProperties = instanceLocationAnnotations
                    .getOrDefault("additionalProperties", Collections.emptyMap()).values().stream()
                    .filter(adjacentEvaluationPathFilter).filter(validEvaluationPathFilter)
                    .collect(Collectors.toList());
            for (JsonNodeAnnotation annotation : additionalProperties) {
                if (annotation.getValue() instanceof Set) {
                    Set<String> p = annotation.getValue();
                    existingEvaluatedProperties.addAll(p);
                }
            }

            // Get all the "unevaluatedProperties" for the instanceLocation
            List<JsonNodeAnnotation> unevaluatedProperties = instanceLocationAnnotations
                    .getOrDefault("unevaluatedProperties", Collections.emptyMap()).values().stream()
                    .filter(adjacentEvaluationPathFilter).filter(validEvaluationPathFilter)
                    .collect(Collectors.toList());
            for (JsonNodeAnnotation annotation : unevaluatedProperties) {
                if (annotation.getValue() instanceof Set) {
                    Set<String> p = annotation.getValue();
                    existingEvaluatedProperties.addAll(p);
                }
            }
            
            Set<ValidationMessage> messages = new LinkedHashSet<>();
            for (Iterator<String> it = node.fieldNames(); it.hasNext();) {
                String fieldName = it.next();
                if (!existingEvaluatedProperties.contains(fieldName)) {
                    evaluatedProperties.add(fieldName);
                    if (this.schemaNode.isBoolean() && this.schemaNode.booleanValue() == false) {
                        // All fails as "unevaluatedProperties: false"
                        messages.add(message().instanceLocation(instanceLocation.append(fieldName))
                                .locale(executionContext.getExecutionConfig().getLocale()).build());
                    } else {
                        messages.addAll(this.schema.validate(executionContext, node.get(fieldName), node,
                                instanceLocation.append(fieldName)));
                    }
                }
            }
            if (!messages.isEmpty()) {
                // Report these as unevaluated paths or not matching the unevaluatedProperties
                // schema
                messages = messages.stream()
                        .map(m -> message().instanceLocation(m.getInstanceLocation())
                                .locale(executionContext.getExecutionConfig().getLocale()).build())
                        .collect(Collectors.toCollection(LinkedHashSet::new));
            }
            executionContext.getAnnotations()
                    .put(JsonNodeAnnotation.builder().instanceLocation(instanceLocation)
                            .evaluationPath(this.evaluationPath).schemaLocation(this.schemaLocation)
                            .keyword(getKeyword()).value(evaluatedProperties).build());

            return messages == null || messages.isEmpty() ? Collections.emptySet() : messages;
            
            /**
            Set<JsonNodePath> allPaths = allPaths(node, instanceLocation);

            // Short-circuit since schema is 'true'
            if (super.schemaNode.isBoolean() && super.schemaNode.asBoolean()) {
                collectorContext.getEvaluatedProperties().addAll(allPaths);
                return Collections.emptySet();
            }

            Set<JsonNodePath> unevaluatedPaths = unevaluatedPaths(collectorContext, allPaths);

            // Short-circuit since schema is 'false'
            if (super.schemaNode.isBoolean() && !super.schemaNode.asBoolean() && !unevaluatedPaths.isEmpty()) {
                return reportUnevaluatedPaths(unevaluatedPaths, executionContext);
            }

            Set<JsonNodePath> failingPaths = new LinkedHashSet<>();
            unevaluatedPaths.forEach(path -> {
                String pointer = path.getPathType().convertToJsonPointer(path.toString());
                JsonNode property = rootNode.at(pointer);
                if (!this.schema.validate(executionContext, property, rootNode, path).isEmpty()) {
                    failingPaths.add(path);
                }
            });

            if (failingPaths.isEmpty()) {
                collectorContext.getEvaluatedProperties().addAll(allPaths);
            } else {
                return reportUnevaluatedPaths(failingPaths, executionContext);
            }

            return Collections.emptySet();
            **/
        } finally {
            collectorContext.enterDynamicScope();
        }
    }

//    private Set<JsonNodePath> allPaths(JsonNode node, JsonNodePath instanceLocation) {
//        Set<JsonNodePath> collector = new LinkedHashSet<>();
//        node.fields().forEachRemaining(entry -> {
//            collector.add(instanceLocation.resolve(entry.getKey()));
//        });
//        return collector;
//    }
//
//    private Set<ValidationMessage> reportUnevaluatedPaths(Set<JsonNodePath> unevaluatedPaths, ExecutionContext executionContext) {
//        return unevaluatedPaths
//                .stream().map(path -> message().instanceLocation(path)
//                        .locale(executionContext.getExecutionConfig().getLocale()).build())
//                .collect(Collectors.toCollection(LinkedHashSet::new));
//    }
//
//    private static Set<JsonNodePath> unevaluatedPaths(CollectorContext collectorContext, Set<JsonNodePath> allPaths) {
//        Set<JsonNodePath> unevaluatedProperties = new LinkedHashSet<>(allPaths);
//        unevaluatedProperties.removeAll(collectorContext.getEvaluatedProperties());
//        return unevaluatedProperties;
//    }
}
