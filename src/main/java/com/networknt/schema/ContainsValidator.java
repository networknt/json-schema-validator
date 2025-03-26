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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;

import static com.networknt.schema.VersionCode.MinV201909;

/**
 * {@link JsonValidator} for contains.
 */
public class ContainsValidator extends BaseJsonValidator {
    private static final Logger logger = LoggerFactory.getLogger(ContainsValidator.class);
    private static final String CONTAINS_MAX = "contains.max";
    private static final String CONTAINS_MIN = "contains.min";

    protected final JsonSchema schema;
    protected final boolean isMinV201909;

    protected final Integer min;
    protected final Integer max;

//    private Boolean hasUnevaluatedItemsValidator = null;

    public ContainsValidator(SchemaLocation schemaLocation, JsonNodePath evaluationPath, JsonNode schemaNode, JsonSchema parentSchema, ValidationContext validationContext) {
        super(schemaLocation, evaluationPath, schemaNode, parentSchema, ValidatorTypeCode.CONTAINS, validationContext);

        // Draft 6 added the contains keyword but maxContains and minContains first
        // appeared in Draft 2019-09 so the semantics of the validation changes
        // slightly.
        this.isMinV201909 = MinV201909.getVersions().contains(this.validationContext.getMetaSchema().getSpecification());

        Integer currentMax = null;
        Integer currentMin = null;
        if (schemaNode.isObject() || schemaNode.isBoolean()) {
            this.schema = validationContext.newSchema(schemaLocation, evaluationPath, schemaNode, parentSchema);
            JsonNode parentSchemaNode = parentSchema.getSchemaNode();
            Optional<JsonNode> maxNode = Optional
                    .ofNullable(parentSchemaNode.get(ValidatorTypeCode.MAX_CONTAINS.getValue()))
                    .filter(JsonNode::canConvertToExactIntegral);
            if (maxNode.isPresent()) {
                currentMax = maxNode.get().intValue();
            }

            Optional<JsonNode> minNode = Optional
                    .ofNullable(parentSchemaNode.get(ValidatorTypeCode.MIN_CONTAINS.getValue()))
                    .filter(JsonNode::canConvertToExactIntegral);
            if (minNode.isPresent()) {
                currentMin = minNode.get().intValue();
            }
        } else {
            this.schema = null;
        }
        this.max = currentMax;
        this.min = currentMin;
    }

    @Override
    public Set<ValidationMessage> validate(ExecutionContext executionContext, JsonNode node, JsonNode rootNode,
                                           JsonNodePath instanceLocation) {
        debug(logger, executionContext, node, rootNode, instanceLocation);

        if (schema == null || !node.isArray()) {
            return Collections.emptySet();
        }

        int actual = 0;
        boolean failFast = executionContext.isFailFast();
        try {
            executionContext.setFailFast(false);
            for (int i = 0; i < node.size(); i++) {
                JsonNodePath path = instanceLocation.append(i);
                if (schema.validate(executionContext, node.get(i), rootNode, path).isEmpty()) {
                    actual++;
                }
            }
        } finally {
            executionContext.setFailFast(failFast);
        }

        int effectiveMin = min != null ? min : 1;
        if (actual < effectiveMin) {
            return boundsViolated(isMinV201909 ? ValidatorTypeCode.MIN_CONTAINS : ValidatorTypeCode.CONTAINS,
                    executionContext.getExecutionConfig().getLocale(),
                    executionContext.isFailFast(), node, instanceLocation, effectiveMin);
        }

        if (max != null && actual > max) {
            return boundsViolated(isMinV201909 ? ValidatorTypeCode.MAX_CONTAINS : ValidatorTypeCode.CONTAINS,
                    executionContext.getExecutionConfig().getLocale(),
                    executionContext.isFailFast(), node, instanceLocation, max);
        }

        return Collections.emptySet();
    }
    @Override
    public void preloadJsonSchema() {
        Optional.ofNullable(this.schema).ifPresent(JsonSchema::initializeValidators);
    }

    protected Set<ValidationMessage> boundsViolated(ValidatorTypeCode validatorTypeCode, Locale locale,
                                                    boolean failFast, JsonNode instanceNode,
                                                    JsonNodePath instanceLocation, int bounds) {
        String messageKey = "contains";
        if (ValidatorTypeCode.MIN_CONTAINS.equals(validatorTypeCode)) {
            messageKey = CONTAINS_MIN;
        } else if (ValidatorTypeCode.MAX_CONTAINS.equals(validatorTypeCode)) {
            messageKey = CONTAINS_MAX;
        }
        return Collections.singleton(message().instanceNode(instanceNode).instanceLocation(instanceLocation)
                .messageKey(messageKey).locale(locale).failFast(failFast)
                .arguments(String.valueOf(bounds), schema.getSchemaNode().toString())
                .code(validatorTypeCode.getErrorCode()).type(validatorTypeCode.getValue()).build());
    }
}
class AnnotatedContainsValidator extends ContainsValidator {
    private Boolean hasUnevaluatedItemsValidator = null;

    public AnnotatedContainsValidator(SchemaLocation schemaLocation, JsonNodePath evaluationPath, JsonNode schemaNode,
                                      JsonSchema parentSchema, ValidationContext validationContext) {
        super(schemaLocation, evaluationPath, schemaNode, parentSchema, validationContext);
    }

    @Override
    public Set<ValidationMessage> validate(ExecutionContext executionContext, JsonNode node, JsonNode rootNode,
                                           JsonNodePath instanceLocation) {
        Set<ValidationMessage> results = super.validate(executionContext, node, rootNode, instanceLocation);
        if (schema != null && node.isArray() && (collectAnnotations() || collectAnnotations(executionContext, "contains"))) {
            int actual = 0, total = node.size();
            java.util.ArrayList<Integer> indexes = new java.util.ArrayList<>();
            boolean failFast = executionContext.isFailFast();
            try {
                executionContext.setFailFast(false);
                for (int i = 0; i < total; i++) {
                    JsonNodePath path = instanceLocation.append(i);
                    if (schema.validate(executionContext, node.get(i), rootNode, path).isEmpty()) {
                        actual++;
                        indexes.add(i);
                    }
                }
            } finally {
                executionContext.setFailFast(failFast);
            }

            if (actual == total) {
                executionContext.getAnnotations().put(JsonNodeAnnotation.builder()
                        .instanceLocation(instanceLocation).evaluationPath(evaluationPath)
                        .schemaLocation(schemaLocation).keyword("contains").value(true).build());
            } else {
                executionContext.getAnnotations().put(JsonNodeAnnotation.builder()
                        .instanceLocation(instanceLocation).evaluationPath(evaluationPath)
                        .schemaLocation(schemaLocation).keyword("contains").value(indexes).build());
            }

            if (min != null && (collectAnnotations() || collectAnnotations(executionContext, "minContains"))) {
                executionContext.getAnnotations().put(JsonNodeAnnotation.builder()
                        .instanceLocation(instanceLocation).evaluationPath(evaluationPath.append("minContains"))
                        .schemaLocation(schemaLocation.append("minContains")).keyword("minContains")
                        .value(min).build());
            }

            if (max != null && (collectAnnotations() || collectAnnotations(executionContext, "maxContains"))) {
                executionContext.getAnnotations().put(JsonNodeAnnotation.builder()
                        .instanceLocation(instanceLocation).evaluationPath(evaluationPath.append("maxContains"))
                        .schemaLocation(schemaLocation.append("maxContains")).keyword("maxContains")
                        .value(max).build());
            }
        }
        return results;
    }

    private boolean collectAnnotations() {
        return hasUnevaluatedItemsValidator();
    }

    @Override
    protected boolean collectAnnotations(ExecutionContext executionContext, String keyword) {
        return executionContext.getExecutionConfig().isAnnotationCollectionEnabled();
    }

    private boolean hasUnevaluatedItemsValidator() {
        if (this.hasUnevaluatedItemsValidator == null) {
            this.hasUnevaluatedItemsValidator = hasAdjacentKeywordInEvaluationPath("unevaluatedItems");
        }
        return this.hasUnevaluatedItemsValidator;
    }
}