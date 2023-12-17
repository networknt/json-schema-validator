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
import com.networknt.schema.walk.DefaultItemWalkListenerRunner;
import com.networknt.schema.walk.WalkListenerRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class PrefixItemsValidator extends BaseJsonValidator {
    private static final Logger logger = LoggerFactory.getLogger(PrefixItemsValidator.class);

    private final List<JsonSchema> tupleSchema;
    private WalkListenerRunner arrayItemWalkListenerRunner;

    public PrefixItemsValidator(JsonNodePath schemaPath, JsonNodePath evaluationPath, JsonNode schemaNode, JsonSchema parentSchema, ValidationContext validationContext) {
        super(schemaPath, evaluationPath, schemaNode, parentSchema, ValidatorTypeCode.PREFIX_ITEMS, validationContext);

        this.tupleSchema = new ArrayList<>();

        if (schemaNode instanceof ArrayNode && 0 < schemaNode.size()) {
            for (JsonNode s : schemaNode) {
                this.tupleSchema.add(validationContext.newSchema(schemaPath, evaluationPath, s, parentSchema));
            }
        } else {
            throw new IllegalArgumentException("The value of 'prefixItems' MUST be a non-empty array of valid JSON Schemas.");
        }

        this.arrayItemWalkListenerRunner = new DefaultItemWalkListenerRunner(validationContext.getConfig().getArrayItemWalkListeners());

        this.validationContext = validationContext;

        parseErrorCode(getValidatorType().getErrorCodeKey());
    }

    @Override
    public Set<ValidationMessage> validate(ExecutionContext executionContext, JsonNode node, JsonNode rootNode, JsonNodePath at) {
        debug(logger, node, rootNode, at);
        // ignores non-arrays
        if (node.isArray()) {
            Set<ValidationMessage> errors = new LinkedHashSet<>();
            Collection<JsonNodePath> evaluatedItems = executionContext.getCollectorContext().getEvaluatedItems();
            int count = Math.min(node.size(), this.tupleSchema.size());
            for (int i = 0; i < count; ++i) {
                JsonNodePath path = atPath(at, i);
                Set<ValidationMessage> results = this.tupleSchema.get(i).validate(executionContext, node.get(i), rootNode, path);
                if (results.isEmpty()) {
                    evaluatedItems.add(path);
                } else {
                    errors.addAll(results);
                }
            }
            return errors.isEmpty() ? Collections.emptySet() : Collections.unmodifiableSet(errors);
        } else {
            return Collections.emptySet();
        }
    }

    @Override
    public Set<ValidationMessage> walk(ExecutionContext executionContext, JsonNode node, JsonNode rootNode, JsonNodePath at, boolean shouldValidateSchema) {
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
                doWalk(executionContext, validationMessages, i, n, rootNode, at, shouldValidateSchema);
            }
        }

        return validationMessages;
    }

    private void doWalk(ExecutionContext executionContext, Set<ValidationMessage> validationMessages, int i, JsonNode node, JsonNode rootNode, JsonNodePath at, boolean shouldValidateSchema) {
        walkSchema(executionContext, this.tupleSchema.get(i), node, rootNode, atPath(at, i), shouldValidateSchema, validationMessages);
    }

    private void walkSchema(ExecutionContext executionContext, JsonSchema walkSchema, JsonNode node, JsonNode rootNode, JsonNodePath at, boolean shouldValidateSchema, Set<ValidationMessage> validationMessages) {
        //@formatter:off
        boolean executeWalk = this.arrayItemWalkListenerRunner.runPreWalkListeners(
            executionContext,
            ValidatorTypeCode.PREFIX_ITEMS.getValue(),
            node,
            rootNode,
            at,
            walkSchema.getSchemaPath(),
            walkSchema.getSchemaNode(),
            walkSchema.getParentSchema(),
            this.validationContext, this.validationContext.getJsonSchemaFactory()
        );
        if (executeWalk) {
            validationMessages.addAll(walkSchema.walk(executionContext, node, rootNode, at, shouldValidateSchema));
        }
        this.arrayItemWalkListenerRunner.runPostWalkListeners(
            executionContext,
            ValidatorTypeCode.PREFIX_ITEMS.getValue(),
            node,
            rootNode,
            at,
            walkSchema.getSchemaPath(),
            walkSchema.getSchemaNode(),
            walkSchema.getParentSchema(),
            this.validationContext,
            this.validationContext.getJsonSchemaFactory(), validationMessages
        );
        //@formatter:on
    }

    public List<JsonSchema> getTupleSchema() {
        return this.tupleSchema;
    }

    @Override
    public void preloadJsonSchema() {
        preloadJsonSchemas(this.tupleSchema);
    }

}
