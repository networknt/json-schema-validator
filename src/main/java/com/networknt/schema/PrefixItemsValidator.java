/*
 * Copyright (c) 2022 Network New Technologies Inc.
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
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.networknt.schema.annotation.JsonNodeAnnotation;
import com.networknt.schema.walk.DefaultItemWalkListenerRunner;
import com.networknt.schema.walk.WalkListenerRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * {@link JsonValidator} for prefixItems.
 */
public class PrefixItemsValidator extends BaseJsonValidator {
    private static final Logger logger = LoggerFactory.getLogger(PrefixItemsValidator.class);

    private final List<JsonSchema> tupleSchema;
    private WalkListenerRunner arrayItemWalkListenerRunner;
    
    private Boolean hasUnevaluatedItemsValidator = null;

    public PrefixItemsValidator(SchemaLocation schemaLocation, JsonNodePath evaluationPath, JsonNode schemaNode, JsonSchema parentSchema, ValidationContext validationContext) {
        super(schemaLocation, evaluationPath, schemaNode, parentSchema, ValidatorTypeCode.PREFIX_ITEMS, validationContext);

        this.tupleSchema = new ArrayList<>();

        if (schemaNode instanceof ArrayNode && 0 < schemaNode.size()) {
            for (JsonNode s : schemaNode) {
                this.tupleSchema.add(validationContext.newSchema(schemaLocation, evaluationPath, s, parentSchema));
            }
        } else {
            throw new IllegalArgumentException("The value of 'prefixItems' MUST be a non-empty array of valid JSON Schemas.");
        }

        this.arrayItemWalkListenerRunner = new DefaultItemWalkListenerRunner(validationContext.getConfig().getArrayItemWalkListeners());
    }

    @Override
    public Set<ValidationMessage> validate(ExecutionContext executionContext, JsonNode node, JsonNode rootNode, JsonNodePath instanceLocation) {
        debug(logger, node, rootNode, instanceLocation);
        // ignores non-arrays
        if (node.isArray()) {
            Set<ValidationMessage> errors = new LinkedHashSet<>();
            int count = Math.min(node.size(), this.tupleSchema.size());
            for (int i = 0; i < count; ++i) {
                JsonNodePath path = instanceLocation.append(i);
                Set<ValidationMessage> results = this.tupleSchema.get(i).validate(executionContext, node.get(i), rootNode, path);
                if (!results.isEmpty()) {
                    errors.addAll(results);
                }
            }

            // Add annotation
            if (collectAnnotations() || collectAnnotations(executionContext)) {
                // Tuples
                int items = node.isArray() ? node.size() : 1;
                int schemas = this.tupleSchema.size();
                if (items > schemas) {
                    // More items than schemas so the keyword only applied to the number of schemas
                    executionContext.getAnnotations()
                            .put(JsonNodeAnnotation.builder().instanceLocation(instanceLocation)
                                    .evaluationPath(this.evaluationPath).schemaLocation(this.schemaLocation)
                                    .keyword(getKeyword()).value(schemas).build());
                } else {
                    // Applies to all
                    executionContext.getAnnotations()
                            .put(JsonNodeAnnotation.builder().instanceLocation(instanceLocation)
                                    .evaluationPath(this.evaluationPath).schemaLocation(this.schemaLocation)
                                    .keyword(getKeyword()).value(true).build());
                }
            }
            return errors.isEmpty() ? Collections.emptySet() : Collections.unmodifiableSet(errors);
        } else {
            return Collections.emptySet();
        }
    }

    @Override
    public Set<ValidationMessage> walk(ExecutionContext executionContext, JsonNode node, JsonNode rootNode, JsonNodePath instanceLocation, boolean shouldValidateSchema) {
        Set<ValidationMessage> validationMessages = new LinkedHashSet<>();

        if (this.applyDefaultsStrategy.shouldApplyArrayDefaults() && node.isArray()) {
            ArrayNode array = (ArrayNode) node;
            int count = Math.min(node.size(), this.tupleSchema.size());
            for (int i = 0; i < count; ++i) {
                JsonNode n = node.get(i);
                JsonNode defaultNode = this.tupleSchema.get(i).getSchemaNode().get("default");
                if (n.isNull() && defaultNode != null) {
                    array.set(i, defaultNode);
                    n = defaultNode;
                }
                doWalk(executionContext, validationMessages, i, n, rootNode, instanceLocation, shouldValidateSchema);
            }
        }

        return validationMessages;
    }

    private void doWalk(ExecutionContext executionContext, Set<ValidationMessage> validationMessages, int i,
            JsonNode node, JsonNode rootNode, JsonNodePath instanceLocation, boolean shouldValidateSchema) {
        walkSchema(executionContext, this.tupleSchema.get(i), node, rootNode, instanceLocation.append(i),
                shouldValidateSchema, validationMessages);
    }

    private void walkSchema(ExecutionContext executionContext, JsonSchema walkSchema, JsonNode node, JsonNode rootNode,
            JsonNodePath instanceLocation, boolean shouldValidateSchema, Set<ValidationMessage> validationMessages) {
        //@formatter:off
        boolean executeWalk = this.arrayItemWalkListenerRunner.runPreWalkListeners(
            executionContext,
            ValidatorTypeCode.PREFIX_ITEMS.getValue(),
            node,
            rootNode,
            instanceLocation,
            walkSchema.getEvaluationPath(),
            walkSchema.getSchemaLocation(),
            walkSchema.getSchemaNode(),
            walkSchema.getParentSchema(), this.validationContext, this.validationContext.getJsonSchemaFactory()
        );
        if (executeWalk) {
            validationMessages.addAll(walkSchema.walk(executionContext, node, rootNode, instanceLocation, shouldValidateSchema));
        }
        this.arrayItemWalkListenerRunner.runPostWalkListeners(
            executionContext,
            ValidatorTypeCode.PREFIX_ITEMS.getValue(),
            node,
            rootNode,
            instanceLocation,
            this.evaluationPath,
            walkSchema.getSchemaLocation(),
            walkSchema.getSchemaNode(),
            walkSchema.getParentSchema(),
            this.validationContext, this.validationContext.getJsonSchemaFactory(), validationMessages
        );
        //@formatter:on
    }

    public List<JsonSchema> getTupleSchema() {
        return this.tupleSchema;
    }

    private boolean collectAnnotations() {
        return hasUnevaluatedItemsValidator();
    }

    private boolean hasUnevaluatedItemsValidator() {
        if (this.hasUnevaluatedItemsValidator == null) {
            this.hasUnevaluatedItemsValidator = hasAdjacentKeywordInEvaluationPath("unevaluatedItems");
        }
        return hasUnevaluatedItemsValidator;
    }

    @Override
    public void preloadJsonSchema() {
        preloadJsonSchemas(this.tupleSchema);
        collectAnnotations(); // cache the flag
    }

}
