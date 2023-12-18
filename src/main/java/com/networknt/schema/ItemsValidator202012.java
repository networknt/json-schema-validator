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
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.networknt.schema.walk.DefaultItemWalkListenerRunner;
import com.networknt.schema.walk.WalkListenerRunner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class ItemsValidator202012 extends BaseJsonValidator {
    private static final Logger logger = LoggerFactory.getLogger(ItemsValidator202012.class);

    private final JsonSchema schema;
    private final WalkListenerRunner arrayItemWalkListenerRunner;
    private final int prefixCount;

    public ItemsValidator202012(JsonNodePath schemaLocation, JsonNodePath evaluationPath, JsonNode schemaNode, JsonSchema parentSchema, ValidationContext validationContext) {
        super(schemaLocation, evaluationPath, schemaNode, parentSchema, ValidatorTypeCode.ITEMS_202012, validationContext);

        JsonNode prefixItems = parentSchema.getSchemaNode().get("prefixItems");
        if (prefixItems instanceof ArrayNode) {
            this.prefixCount = prefixItems.size();
        } else if (null == prefixItems) {
            this.prefixCount = 0;
        } else {
            throw new IllegalArgumentException("The value of 'prefixItems' must be an array of JSON Schema.");
        }
    
        if (schemaNode.isObject() || schemaNode.isBoolean()) {
            this.schema = validationContext.newSchema(schemaLocation, evaluationPath, schemaNode, parentSchema);
        } else {
            throw new IllegalArgumentException("The value of 'items' MUST be a valid JSON Schema.");
        }

        this.arrayItemWalkListenerRunner = new DefaultItemWalkListenerRunner(validationContext.getConfig().getArrayItemWalkListeners());

        this.validationContext = validationContext;

        parseErrorCode(getValidatorType().getErrorCodeKey());
    }

    @Override
    public Set<ValidationMessage> validate(ExecutionContext executionContext, JsonNode node, JsonNode rootNode, JsonNodePath instanceLocation) {
        debug(logger, node, rootNode, instanceLocation);

        // ignores non-arrays
        if (node.isArray()) {
            Set<ValidationMessage> errors = new LinkedHashSet<>();
            Collection<JsonNodePath> evaluatedItems = executionContext.getCollectorContext().getEvaluatedItems();
            for (int i = this.prefixCount; i < node.size(); ++i) {
                JsonNodePath path = instanceLocation.resolve(i);
                // validate with item schema (the whole array has the same item schema)
                Set<ValidationMessage> results = this.schema.validate(executionContext, node.get(i), rootNode, path);
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
    public Set<ValidationMessage> walk(ExecutionContext executionContext, JsonNode node, JsonNode rootNode, JsonNodePath instanceLocation, boolean shouldValidateSchema) {
        Set<ValidationMessage> validationMessages = new LinkedHashSet<>();

        if (node instanceof ArrayNode) {
            ArrayNode arrayNode = (ArrayNode) node;
            JsonNode defaultNode = null;
            if (this.applyDefaultsStrategy.shouldApplyArrayDefaults() && this.schema != null) {
                defaultNode = this.schema.getSchemaNode().get("default");
            }
            for (int i = this.prefixCount; i < node.size(); ++i) {
                JsonNode n = node.get(i);
                if (n.isNull() && defaultNode != null) {
                    arrayNode.set(i, defaultNode);
                    n = defaultNode;
                }
                // Walk the schema.
                walkSchema(executionContext, this.schema, n, rootNode, instanceLocation.resolve(i), shouldValidateSchema, validationMessages);
            }
        } else {
            walkSchema(executionContext, this.schema, node, rootNode, instanceLocation, shouldValidateSchema, validationMessages);
        }

        return validationMessages;
    }

    private void walkSchema(ExecutionContext executionContext, JsonSchema walkSchema, JsonNode node, JsonNode rootNode,
            JsonNodePath instanceLocation, boolean shouldValidateSchema, Set<ValidationMessage> validationMessages) {
        //@formatter:off
        boolean executeWalk = this.arrayItemWalkListenerRunner.runPreWalkListeners(
            executionContext,
            ValidatorTypeCode.ITEMS.getValue(),
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
            ValidatorTypeCode.ITEMS.getValue(),
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

    public JsonSchema getSchema() {
        return this.schema;
    }

    @Override
    public void preloadJsonSchema() {
        this.schema.initializeValidators();
    }

}
