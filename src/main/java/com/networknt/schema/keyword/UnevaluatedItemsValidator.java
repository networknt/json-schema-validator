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

package com.networknt.schema.keyword;

import com.fasterxml.jackson.databind.JsonNode;
import com.networknt.schema.ExecutionContext;
import com.networknt.schema.NodePath;
import com.networknt.schema.Schema;
import com.networknt.schema.SchemaLocation;
import com.networknt.schema.SchemaContext;
import com.networknt.schema.Specification.Version;
import com.networknt.schema.annotation.JsonNodeAnnotation;

import static com.networknt.schema.keyword.VersionCode.MinV202012;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * {@link KeywordValidator} for unevaluatedItems.
 */
public class UnevaluatedItemsValidator extends BaseKeywordValidator {
    private final Schema schema;

    private final boolean isMinV202012;
    private static final Version DEFAULT_VERSION = Version.DRAFT_2019_09;

    public UnevaluatedItemsValidator(SchemaLocation schemaLocation, NodePath evaluationPath, JsonNode schemaNode,
            Schema parentSchema, SchemaContext schemaContext) {
        super(ValidatorTypeCode.UNEVALUATED_ITEMS, schemaNode, schemaLocation, parentSchema, schemaContext,
                evaluationPath);
        isMinV202012 = MinV202012.getVersions().contains(schemaContext.activeDialect().orElse(DEFAULT_VERSION));
        if (schemaNode.isObject() || schemaNode.isBoolean()) {
            this.schema = schemaContext.newSchema(schemaLocation, evaluationPath, schemaNode, parentSchema);
        } else {
            throw new IllegalArgumentException("The value of 'unevaluatedItems' MUST be a valid JSON Schema.");
        }
    }

    @Override
    public void validate(ExecutionContext executionContext, JsonNode node, JsonNode rootNode, NodePath instanceLocation) {
        if (!node.isArray()) {
            return;
        }

        
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
        if (!valid) {
            int currentErrors = executionContext.getErrors().size();
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

            if (!containsEvaluatedAll) {
                // Start evaluating from the valid count
                for (int x = validCount; x < node.size(); x++) {
                    // The schema is either "false" or an object schema
                    if (!containsEvaluated.contains(x)) {
                        if (this.schemaNode.isBoolean() && this.schemaNode.booleanValue() == false) {
                            // All fails as "unevaluatedItems: false"
                            executionContext.addError(error().instanceNode(node).instanceLocation(instanceLocation).arguments(x)
                                    .locale(executionContext.getExecutionConfig().getLocale())
                                    .build());
                        } else {
                            // Schema errors will be reported as is
                            this.schema.validate(executionContext, node.get(x), node, instanceLocation.append(x));
                        }
                        evaluated = true;
                    }
                }
            }
            if (currentErrors == executionContext.getErrors().size()) { // No new errors
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
    }
}
