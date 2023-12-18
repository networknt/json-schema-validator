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

public class ItemsValidator extends BaseJsonValidator {
    private static final Logger logger = LoggerFactory.getLogger(ItemsValidator.class);
    private static final String PROPERTY_ADDITIONAL_ITEMS = "additionalItems";

    private final JsonSchema schema;
    private final List<JsonSchema> tupleSchema;
    private Boolean additionalItems;
    private final JsonSchema additionalSchema;
    private WalkListenerRunner arrayItemWalkListenerRunner;

    public ItemsValidator(JsonNodePath schemaPath, JsonNodePath evaluationPath, JsonNode schemaNode, JsonSchema parentSchema, ValidationContext validationContext) {
        super(schemaPath, evaluationPath, schemaNode, parentSchema, ValidatorTypeCode.ITEMS, validationContext);

        this.tupleSchema = new ArrayList<>();
        JsonSchema foundSchema = null;
        JsonSchema foundAdditionalSchema = null;

        if (schemaNode.isObject() || schemaNode.isBoolean()) {
            foundSchema = validationContext.newSchema(schemaPath, evaluationPath, schemaNode, parentSchema);
        } else {
            for (JsonNode s : schemaNode) {
                this.tupleSchema.add(validationContext.newSchema(schemaPath, evaluationPath, s, parentSchema));
            }

            JsonNode addItemNode = getParentSchema().getSchemaNode().get(PROPERTY_ADDITIONAL_ITEMS);
            if (addItemNode != null) {
                if (addItemNode.isBoolean()) {
                    this.additionalItems = addItemNode.asBoolean();
                } else if (addItemNode.isObject()) {
                    foundAdditionalSchema = validationContext.newSchema(
                            schemaPath.resolve(PROPERTY_ADDITIONAL_ITEMS), evaluationPath, addItemNode, parentSchema);
                }
            }
        }
        this.arrayItemWalkListenerRunner = new DefaultItemWalkListenerRunner(validationContext.getConfig().getArrayItemWalkListeners());

        this.validationContext = validationContext;

        parseErrorCode(getValidatorType().getErrorCodeKey());

        this.schema = foundSchema;
        this.additionalSchema = foundAdditionalSchema;
    }

    @Override
    public Set<ValidationMessage> validate(ExecutionContext executionContext, JsonNode node, JsonNode rootNode, JsonNodePath at) {
        debug(logger, node, rootNode, at);

        if (!node.isArray() && !this.validationContext.getConfig().isTypeLoose()) {
            // ignores non-arrays
            return Collections.emptySet();
        }
        Set<ValidationMessage> errors = new LinkedHashSet<>();
        if (node.isArray()) {
            int i = 0;
            for (JsonNode n : node) {
                doValidate(executionContext, errors, i, n, rootNode, at);
                i++;
            }
        } else {
            doValidate(executionContext, errors, 0, node, rootNode, at);
        }
        return errors.isEmpty() ? Collections.emptySet() : Collections.unmodifiableSet(errors);
    }

    private void doValidate(ExecutionContext executionContext, Set<ValidationMessage> errors, int i, JsonNode node, JsonNode rootNode, JsonNodePath at) {
        Collection<JsonNodePath> evaluatedItems = executionContext.getCollectorContext().getEvaluatedItems();
        JsonNodePath path = at.resolve(i);

        if (this.schema != null) {
            // validate with item schema (the whole array has the same item
            // schema)
            Set<ValidationMessage> results = this.schema.validate(executionContext, node, rootNode, path);
            if (results.isEmpty()) {
                evaluatedItems.add(path);
            } else {
                errors.addAll(results);
            }
        } else if (this.tupleSchema != null) {
            if (i < this.tupleSchema.size()) {
                // validate against tuple schema
                Set<ValidationMessage> results = this.tupleSchema.get(i).validate(executionContext, node, rootNode, path);
                if (results.isEmpty()) {
                    evaluatedItems.add(path);
                } else {
                    errors.addAll(results);
                }
            } else {
                if (this.additionalSchema != null) {
                    // validate against additional item schema
                    Set<ValidationMessage> results = this.additionalSchema.validate(executionContext, node, rootNode, path);
                    if (results.isEmpty()) {
                        evaluatedItems.add(path);
                    } else {
                        errors.addAll(results);
                    }
                } else if (this.additionalItems != null) {
                    if (this.additionalItems) {
                        evaluatedItems.add(path);
                    } else {
                        // no additional item allowed, return error
                        errors.add(
                                buildValidationMessage(null, path, executionContext.getExecutionConfig().getLocale(), "" + i));
                    }
                }
            }
//        } else {
//            evaluatedItems.add(path);
        }
    }

    @Override
    public Set<ValidationMessage> walk(ExecutionContext executionContext, JsonNode node, JsonNode rootNode, JsonNodePath at, boolean shouldValidateSchema) {
        HashSet<ValidationMessage> validationMessages = new LinkedHashSet<>();
        if (node instanceof ArrayNode) {
            ArrayNode arrayNode = (ArrayNode) node;
            JsonNode defaultNode = null;
            if (this.applyDefaultsStrategy.shouldApplyArrayDefaults() && this.schema != null) {
                defaultNode = this.schema.getSchemaNode().get("default");
            }
            int i = 0;
            for (JsonNode n : arrayNode) {
                if (n.isNull() && defaultNode != null) {
                    arrayNode.set(i, defaultNode);
                    n = defaultNode;
                }
                doWalk(executionContext, validationMessages, i, n, rootNode, at, shouldValidateSchema);
                i++;
            }
        } else {
            doWalk(executionContext, validationMessages, 0, node, rootNode, at, shouldValidateSchema);
        }
        return validationMessages;
    }

    private void doWalk(ExecutionContext executionContext, HashSet<ValidationMessage> validationMessages, int i, JsonNode node,
            JsonNode rootNode, JsonNodePath at, boolean shouldValidateSchema) {
        if (this.schema != null) {
            // Walk the schema.
            walkSchema(executionContext, this.schema, node, rootNode, at.resolve(i), shouldValidateSchema, validationMessages);
        }

        if (this.tupleSchema != null) {
            if (i < this.tupleSchema.size()) {
                // walk tuple schema
                walkSchema(executionContext, this.tupleSchema.get(i), node, rootNode, at.resolve(i),
                        shouldValidateSchema, validationMessages);
            } else {
                if (this.additionalSchema != null) {
                    // walk additional item schema
                    walkSchema(executionContext, this.additionalSchema, node, rootNode, at.resolve(i),
                            shouldValidateSchema, validationMessages);
                }
            }
        }
    }

    private void walkSchema(ExecutionContext executionContext, JsonSchema walkSchema, JsonNode node, JsonNode rootNode,
            JsonNodePath at, boolean shouldValidateSchema, Set<ValidationMessage> validationMessages) {
        boolean executeWalk = this.arrayItemWalkListenerRunner.runPreWalkListeners(executionContext, ValidatorTypeCode.ITEMS.getValue(),
                node, rootNode, at, this.evaluationPath, walkSchema.getSchemaPath(),
                walkSchema.getSchemaNode(), walkSchema.getParentSchema(), this.validationContext, this.validationContext.getJsonSchemaFactory());
        if (executeWalk) {
            validationMessages.addAll(walkSchema.walk(executionContext, node, rootNode, at, shouldValidateSchema));
        }
        this.arrayItemWalkListenerRunner.runPostWalkListeners(executionContext, ValidatorTypeCode.ITEMS.getValue(), node, rootNode,
                at, this.evaluationPath, walkSchema.getSchemaPath(),
                walkSchema.getSchemaNode(), walkSchema.getParentSchema(), this.validationContext, this.validationContext.getJsonSchemaFactory(), validationMessages);

    }

    public List<JsonSchema> getTupleSchema() {
        return this.tupleSchema;
    }

    public JsonSchema getSchema() {
        return this.schema;
    }

    @Override
    public void preloadJsonSchema() {
        if (null != this.schema) {
            this.schema.initializeValidators();
        }
        preloadJsonSchemas(this.tupleSchema);
        if (null != this.additionalSchema) {
            this.additionalSchema.initializeValidators();
        }
    }
}
