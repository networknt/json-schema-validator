/*
 * Copyright (c) 2023 Network New Technologies Inc.
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
import com.networknt.schema.SpecVersion.VersionFlag;
import com.networknt.schema.annotation.JsonNodeAnnotation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static com.networknt.schema.VersionCode.MinV202012;

/**
 * {@link JsonValidator} for unevaluatedItems.
 */
public class UnevaluatedItemsValidator extends BaseJsonValidator {
    private static final Logger logger = LoggerFactory.getLogger(UnevaluatedItemsValidator.class);

    private final JsonSchema schema;

    private final boolean isMinV202012;
    private static final VersionFlag DEFAULT_VERSION = VersionFlag.V201909;

    public UnevaluatedItemsValidator(SchemaLocation schemaLocation, JsonNodePath evaluationPath, JsonNode schemaNode,
            JsonSchema parentSchema, ValidationContext validationContext) {
        super(schemaLocation, evaluationPath, schemaNode, parentSchema, ValidatorTypeCode.UNEVALUATED_ITEMS,
                validationContext);
        isMinV202012 = MinV202012.getVersions().contains(validationContext.activeDialect().orElse(DEFAULT_VERSION));
        if (schemaNode.isObject() || schemaNode.isBoolean()) {
            this.schema = validationContext.newSchema(schemaLocation, evaluationPath, schemaNode, parentSchema);
        } else {
            throw new IllegalArgumentException("The value of 'unevaluatedItems' MUST be a valid JSON Schema.");
        }
    }

    @Override
    public Set<ValidationMessage> validate(ExecutionContext executionContext, JsonNode node, JsonNode rootNode, JsonNodePath instanceLocation) {
        if (!node.isArray()) {
            return Collections.emptySet();
        }

        debug(logger, executionContext, node, rootNode, instanceLocation);
        /*
         * Keywords renamed in 2020-12
         * 
         * items -> prefixItems additionalItems -> items
         */
        String itemsKeyword = isMinV202012 ? "prefixItems" : "items";
        String additionalItemsKeyword = isMinV202012 ? "items" : "additionalItems";

        boolean valid = false;
        int validCount = 0;

        // This indicates whether the "unevaluatedItems" subschema was used for
        // evaluated for setting the annotation
        boolean evaluated = false;

        // Get all the valid adjacent annotations
        Predicate<JsonNodeAnnotation> validEvaluationPathFilter = a -> executionContext.getResults().isValid(instanceLocation, a.getEvaluationPath());

        Predicate<JsonNodeAnnotation> adjacentEvaluationPathFilter = a -> a.getEvaluationPath()
                .startsWith(this.evaluationPath.getParent());

        List<JsonNodeAnnotation> instanceLocationAnnotations = executionContext.getAnnotations().asMap()
                .getOrDefault(instanceLocation, Collections.emptyList());

        // If schema is "unevaluatedItems: true" this is valid
        if (getSchemaNode().isBoolean() && getSchemaNode().booleanValue()) {
            valid = true;
            // No need to actually evaluate since the schema is true but if there are any
            // items the annotation needs to be set
            if (!node.isEmpty()) {
                evaluated = true;
            }
        } else {
            // Get all the "items" for the instanceLocation
            List<JsonNodeAnnotation> items = instanceLocationAnnotations.stream()
                    .filter(a -> itemsKeyword.equals(a.getKeyword())).filter(adjacentEvaluationPathFilter)
                    .filter(validEvaluationPathFilter).collect(Collectors.toList());
            if (items.isEmpty()) {
                // The "items" wasn't applied meaning it is unevaluated if there is content
                valid = false;
            } else {
                // Annotation results for "items" keywords from multiple schemas applied to the
                // same instance location are combined by setting the combined result to true if
                // any of the values are true, and otherwise retaining the largest numerical
                // value.
                for (JsonNodeAnnotation annotation : items) {
                    if (annotation.getValue() instanceof Number) {
                        Number value = annotation.getValue();
                        int existing = value.intValue();
                        if (existing > validCount) {
                            validCount = existing;
                        }
                    } else if (annotation.getValue() instanceof Boolean) {
                        // The annotation "items: true"
                        valid = true;
                    }
                }
            }
            if (!valid) {
                // Check the additionalItems annotation
                // If the "additionalItems" subschema is applied to any positions within the
                // instance array, it produces an annotation result of boolean true, analogous
                // to the single schema behavior of "items". If any "additionalItems" keyword
                // from any subschema applied to the same instance location produces an
                // annotation value of true, then the combined result from these keywords is
                // also true.
                List<JsonNodeAnnotation> additionalItems = instanceLocationAnnotations.stream()
                        .filter(a -> additionalItemsKeyword.equals(a.getKeyword())).filter(adjacentEvaluationPathFilter)
                        .filter(validEvaluationPathFilter).collect(Collectors.toList());
                for (JsonNodeAnnotation annotation : additionalItems) {
                    if (annotation.getValue() instanceof Boolean && Boolean.TRUE.equals(annotation.getValue())) {
                        // The annotation "additionalItems: true"
                        valid = true;
                    }
                }
            }
            if (!valid) {
                // Unevaluated
                // Check if there are any "unevaluatedItems" annotations
                List<JsonNodeAnnotation> unevaluatedItems = instanceLocationAnnotations.stream()
                        .filter(a -> "unevaluatedItems".equals(a.getKeyword())).filter(adjacentEvaluationPathFilter)
                        .filter(validEvaluationPathFilter).collect(Collectors.toList());
                for (JsonNodeAnnotation annotation : unevaluatedItems) {
                    if (annotation.getValue() instanceof Boolean && Boolean.TRUE.equals(annotation.getValue())) {
                        // The annotation "unevaluatedItems: true"
                        valid = true;
                    }
                }
            }
        }
        Set<ValidationMessage> messages = null;
        if (!valid) {
            // Get all the "contains" for the instanceLocation
            List<JsonNodeAnnotation> contains = instanceLocationAnnotations.stream()
                    .filter(a -> "contains".equals(a.getKeyword())).filter(adjacentEvaluationPathFilter)
                    .filter(validEvaluationPathFilter).collect(Collectors.toList());

            Set<Integer> containsEvaluated = new HashSet<>();
            boolean containsEvaluatedAll = false;
            for (JsonNodeAnnotation a : contains) {
                if (a.getValue() instanceof List) {
                    List<Integer> values = a.getValue();
                    containsEvaluated.addAll(values);
                } else if (a.getValue() instanceof Boolean) {
                    containsEvaluatedAll = true;
                }
            }

            messages = new LinkedHashSet<>();
            if (!containsEvaluatedAll) {
                // Start evaluating from the valid count
                for (int x = validCount; x < node.size(); x++) {
                    // The schema is either "false" or an object schema
                    if (!containsEvaluated.contains(x)) {
                        if (this.schemaNode.isBoolean() && this.schemaNode.booleanValue() == false) {
                            // All fails as "unevaluatedItems: false"
                            messages.add(message().instanceNode(node).instanceLocation(instanceLocation).arguments(x)
                                    .locale(executionContext.getExecutionConfig().getLocale())
                                    .failFast(executionContext.isFailFast()).build());
                        } else {
                            // Schema errors will be reported as is
                            messages.addAll(this.schema.validate(executionContext, node.get(x), node,
                                    instanceLocation.append(x)));
                        }
                        evaluated = true;
                    }
                }
            }
            if (messages.isEmpty()) {
                valid = true;
            }
        }
        // If the "unevaluatedItems" subschema is applied to any positions within the
        // instance array, it produces an annotation result of boolean true, analogous
        // to the single schema behavior of "items". If any "unevaluatedItems" keyword
        // from any subschema applied to the same instance location produces an
        // annotation value of true, then the combined result from these keywords is
        // also true.
        if (evaluated) {
            executionContext.getAnnotations()
                    .put(JsonNodeAnnotation.builder().instanceLocation(instanceLocation)
                            .evaluationPath(this.evaluationPath).schemaLocation(this.schemaLocation)
                            .keyword("unevaluatedItems").value(true).build());
        }
        return messages == null || messages.isEmpty() ? Collections.emptySet() : messages;
    }
}
