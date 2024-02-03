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
import com.networknt.schema.annotation.JsonNodeAnnotation;
import com.networknt.schema.walk.DefaultItemWalkListenerRunner;
import com.networknt.schema.walk.WalkListenerRunner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * {@link JsonValidator} for items V4 to V2019-09.
 */
public class ItemsValidator extends BaseJsonValidator {
    private static final Logger logger = LoggerFactory.getLogger(ItemsValidator.class);
    private static final String PROPERTY_ADDITIONAL_ITEMS = "additionalItems";

    private final JsonSchema schema;
    private final List<JsonSchema> tupleSchema;
    private Boolean additionalItems;
    private final JsonSchema additionalSchema;
    private WalkListenerRunner arrayItemWalkListenerRunner;

    private Boolean hasUnevaluatedItemsValidator = null;

    private final JsonNodePath additionalItemsEvaluationPath;
    private final SchemaLocation additionalItemsSchemaLocation;
    private final JsonNode additionalItemsSchemaNode;

    public ItemsValidator(SchemaLocation schemaLocation, JsonNodePath evaluationPath, JsonNode schemaNode, JsonSchema parentSchema, ValidationContext validationContext) {
        super(schemaLocation, evaluationPath, schemaNode, parentSchema, ValidatorTypeCode.ITEMS, validationContext);

        this.tupleSchema = new ArrayList<>();
        JsonSchema foundSchema = null;
        JsonSchema foundAdditionalSchema = null;
        JsonNode additionalItemsSchemaNode = null;

        if (schemaNode.isObject() || schemaNode.isBoolean()) {
            foundSchema = validationContext.newSchema(schemaLocation, evaluationPath, schemaNode, parentSchema);
        } else {
            int i = 0;
            for (JsonNode s : schemaNode) {
                this.tupleSchema.add(validationContext.newSchema(schemaLocation.append(i), evaluationPath.append(i),
                        s, parentSchema));
                i++;
            }

            JsonNode addItemNode = getParentSchema().getSchemaNode().get(PROPERTY_ADDITIONAL_ITEMS);
            if (addItemNode != null) {
                additionalItemsSchemaNode = addItemNode;
                if (addItemNode.isBoolean()) {
                    this.additionalItems = addItemNode.asBoolean();
                } else if (addItemNode.isObject()) {
                    foundAdditionalSchema = validationContext.newSchema(
                            parentSchema.schemaLocation.append(PROPERTY_ADDITIONAL_ITEMS),
                            parentSchema.evaluationPath.append(PROPERTY_ADDITIONAL_ITEMS), addItemNode, parentSchema);
                }
            }
        }
        this.arrayItemWalkListenerRunner = new DefaultItemWalkListenerRunner(validationContext.getConfig().getArrayItemWalkListeners());

        this.schema = foundSchema;
        this.additionalSchema = foundAdditionalSchema;
        this.additionalItemsEvaluationPath = parentSchema.evaluationPath.append(PROPERTY_ADDITIONAL_ITEMS);
        this.additionalItemsSchemaLocation = parentSchema.schemaLocation.append(PROPERTY_ADDITIONAL_ITEMS);
        this.additionalItemsSchemaNode = additionalItemsSchemaNode;
    }

    @Override
    public Set<ValidationMessage> validate(ExecutionContext executionContext, JsonNode node, JsonNode rootNode, JsonNodePath instanceLocation) {
        debug(logger, node, rootNode, instanceLocation);

        if (!node.isArray() && !this.validationContext.getConfig().isTypeLoose()) {
            // ignores non-arrays
            return Collections.emptySet();
        }
        boolean collectAnnotations = collectAnnotations();

        // Add items annotation
        if (collectAnnotations || collectAnnotations(executionContext)) {
            if (this.schema != null) {
                // Applies to all
                executionContext.getAnnotations()
                        .put(JsonNodeAnnotation.builder().instanceLocation(instanceLocation)
                                .evaluationPath(this.evaluationPath).schemaLocation(this.schemaLocation)
                                .keyword(getKeyword()).value(true).build());
            } else if (this.tupleSchema != null) {
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
        }

        boolean hasAdditionalItem = false;
        Set<ValidationMessage> errors = new LinkedHashSet<>();
        if (node.isArray()) {
            int i = 0;
            for (JsonNode n : node) {
                if (doValidate(executionContext, errors, i, n, rootNode, instanceLocation)) {
                    hasAdditionalItem = true;
                }
                i++;
            }
        } else {
            if (doValidate(executionContext, errors, 0, node, rootNode, instanceLocation)) {
                hasAdditionalItem = true;
            }
        }

        if (hasAdditionalItem) {
            if (collectAnnotations || collectAnnotations(executionContext, "additionalItems")) {
                executionContext.getAnnotations()
                        .put(JsonNodeAnnotation.builder().instanceLocation(instanceLocation)
                                .evaluationPath(this.additionalItemsEvaluationPath)
                                .schemaLocation(this.additionalItemsSchemaLocation)
                                .keyword("additionalItems").value(true).build());
            }
        }
        return errors.isEmpty() ? Collections.emptySet() : Collections.unmodifiableSet(errors);
    }

    private boolean doValidate(ExecutionContext executionContext, Set<ValidationMessage> errors, int i, JsonNode node,
            JsonNode rootNode, JsonNodePath instanceLocation) {
        boolean isAdditionalItem = false;
        JsonNodePath path = instanceLocation.append(i);

        if (this.schema != null) {
            // validate with item schema (the whole array has the same item
            // schema)
            Set<ValidationMessage> results = this.schema.validate(executionContext, node, rootNode, path);
            if (!results.isEmpty()) {
                errors.addAll(results);
            }
        } else if (this.tupleSchema != null) {
            if (i < this.tupleSchema.size()) {
                // validate against tuple schema
                Set<ValidationMessage> results = this.tupleSchema.get(i).validate(executionContext, node, rootNode, path);
                if (!results.isEmpty()) {
                    errors.addAll(results);
                }
            } else {
                if ((this.additionalItems != null && this.additionalItems) || this.additionalSchema != null) {
                    isAdditionalItem = true;
                }

                if (this.additionalSchema != null) {
                    // validate against additional item schema
                    Set<ValidationMessage> results = this.additionalSchema.validate(executionContext, node, rootNode, path);
                    if (!results.isEmpty()) {
                        errors.addAll(results);
                    }
                } else if (this.additionalItems != null) {
                    if (this.additionalItems) {
//                        evaluatedItems.add(path);
                    } else {
                        // no additional item allowed, return error
                        errors.add(message().instanceNode(rootNode).instanceLocation(instanceLocation)
                                .type("additionalItems")
                                .evaluationPath(this.additionalItemsEvaluationPath)
                                .schemaLocation(this.additionalItemsSchemaLocation)
                                .schemaNode(this.additionalItemsSchemaNode)
                                .locale(executionContext.getExecutionConfig().getLocale())
                                .failFast(executionContext.getExecutionConfig().isFailFast()).arguments(i).build());
                    }
                }
            }
        }
        return isAdditionalItem;
    }

    @Override
    public Set<ValidationMessage> walk(ExecutionContext executionContext, JsonNode node, JsonNode rootNode, JsonNodePath instanceLocation, boolean shouldValidateSchema) {
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
                doWalk(executionContext, validationMessages, i, n, rootNode, instanceLocation, shouldValidateSchema);
                i++;
            }
        } else {
            doWalk(executionContext, validationMessages, 0, node, rootNode, instanceLocation, shouldValidateSchema);
        }
        return validationMessages;
    }

    private void doWalk(ExecutionContext executionContext, HashSet<ValidationMessage> validationMessages, int i, JsonNode node,
            JsonNode rootNode, JsonNodePath instanceLocation, boolean shouldValidateSchema) {
        if (this.schema != null) {
            // Walk the schema.
            walkSchema(executionContext, this.schema, node, rootNode, instanceLocation.append(i), shouldValidateSchema, validationMessages);
        }

        if (this.tupleSchema != null) {
            if (i < this.tupleSchema.size()) {
                // walk tuple schema
                walkSchema(executionContext, this.tupleSchema.get(i), node, rootNode, instanceLocation.append(i),
                        shouldValidateSchema, validationMessages);
            } else {
                if (this.additionalSchema != null) {
                    // walk additional item schema
                    walkSchema(executionContext, this.additionalSchema, node, rootNode, instanceLocation.append(i),
                            shouldValidateSchema, validationMessages);
                }
            }
        }
    }

    private void walkSchema(ExecutionContext executionContext, JsonSchema walkSchema, JsonNode node, JsonNode rootNode,
            JsonNodePath instanceLocation, boolean shouldValidateSchema, Set<ValidationMessage> validationMessages) {
        boolean executeWalk = this.arrayItemWalkListenerRunner.runPreWalkListeners(executionContext, ValidatorTypeCode.ITEMS.getValue(),
                node, rootNode, instanceLocation, walkSchema.getEvaluationPath(), walkSchema.getSchemaLocation(),
                walkSchema.getSchemaNode(), walkSchema.getParentSchema(), this.validationContext, this.validationContext.getJsonSchemaFactory());
        if (executeWalk) {
            validationMessages.addAll(walkSchema.walk(executionContext, node, rootNode, instanceLocation, shouldValidateSchema));
        }
        this.arrayItemWalkListenerRunner.runPostWalkListeners(executionContext, ValidatorTypeCode.ITEMS.getValue(), node, rootNode,
                instanceLocation, this.evaluationPath, walkSchema.getSchemaLocation(),
                walkSchema.getSchemaNode(), walkSchema.getParentSchema(), this.validationContext, this.validationContext.getJsonSchemaFactory(), validationMessages);

    }

    public List<JsonSchema> getTupleSchema() {
        return this.tupleSchema;
    }

    public JsonSchema getSchema() {
        return this.schema;
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
        if (null != this.schema) {
            this.schema.initializeValidators();
        }
        preloadJsonSchemas(this.tupleSchema);
        if (null != this.additionalSchema) {
            this.additionalSchema.initializeValidators();
        }
        collectAnnotations(); // cache the flag
    }
}
