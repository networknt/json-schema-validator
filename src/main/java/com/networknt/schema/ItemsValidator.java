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
import com.networknt.schema.utils.JsonSchemaRefs;
import com.networknt.schema.utils.SetView;

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
    private final Boolean additionalItems;
    private final JsonSchema additionalSchema;

    private Boolean hasUnevaluatedItemsValidator = null;

    private final JsonNodePath additionalItemsEvaluationPath;
    private final SchemaLocation additionalItemsSchemaLocation;
    private final JsonNode additionalItemsSchemaNode;

    public ItemsValidator(SchemaLocation schemaLocation, JsonNodePath evaluationPath, JsonNode schemaNode, JsonSchema parentSchema, ValidationContext validationContext) {
        super(schemaLocation, evaluationPath, schemaNode, parentSchema, ValidatorTypeCode.ITEMS, validationContext);

        Boolean additionalItems = null;

        JsonSchema foundSchema = null;
        JsonSchema foundAdditionalSchema = null;
        JsonNode additionalItemsSchemaNode = null;

        if (schemaNode.isObject() || schemaNode.isBoolean()) {
            foundSchema = validationContext.newSchema(schemaLocation, evaluationPath, schemaNode, parentSchema);
            this.tupleSchema = Collections.emptyList();
        } else {
            int i = 0;
            this.tupleSchema = new ArrayList<>(schemaNode.size());
            for (JsonNode s : schemaNode) {
                this.tupleSchema.add(validationContext.newSchema(schemaLocation.append(i), evaluationPath.append(i),
                        s, parentSchema));
                i++;
            }

            JsonNode addItemNode = getParentSchema().getSchemaNode().get(PROPERTY_ADDITIONAL_ITEMS);
            if (addItemNode != null) {
                additionalItemsSchemaNode = addItemNode;
                if (addItemNode.isBoolean()) {
                    additionalItems = addItemNode.asBoolean();
                } else if (addItemNode.isObject()) {
                    foundAdditionalSchema = validationContext.newSchema(
                            parentSchema.schemaLocation.append(PROPERTY_ADDITIONAL_ITEMS),
                            parentSchema.evaluationPath.append(PROPERTY_ADDITIONAL_ITEMS), addItemNode, parentSchema);
                }
            }
        }
        this.additionalItems = additionalItems;
        this.schema = foundSchema;
        this.additionalSchema = foundAdditionalSchema;
        this.additionalItemsEvaluationPath = parentSchema.evaluationPath.append(PROPERTY_ADDITIONAL_ITEMS);
        this.additionalItemsSchemaLocation = parentSchema.schemaLocation.append(PROPERTY_ADDITIONAL_ITEMS);
        this.additionalItemsSchemaNode = additionalItemsSchemaNode;
    }

    @Override
    public Set<ValidationMessage> validate(ExecutionContext executionContext, JsonNode node, JsonNode rootNode, JsonNodePath instanceLocation) {
        debug(logger, executionContext, node, rootNode, instanceLocation);

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
        SetView<ValidationMessage> errors = new SetView<>();
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
        return errors.isEmpty() ? Collections.emptySet() : errors;
    }

    private boolean doValidate(ExecutionContext executionContext, SetView<ValidationMessage> errors, int i, JsonNode node,
            JsonNode rootNode, JsonNodePath instanceLocation) {
        boolean isAdditionalItem = false;
        JsonNodePath path = instanceLocation.append(i);

        if (this.schema != null) {
            // validate with item schema (the whole array has the same item
            // schema)
            Set<ValidationMessage> results = this.schema.validate(executionContext, node, rootNode, path);
            if (!results.isEmpty()) {
                errors.union(results);
            }
        } else if (this.tupleSchema != null) {
            if (i < this.tupleSchema.size()) {
                // validate against tuple schema
                Set<ValidationMessage> results = this.tupleSchema.get(i).validate(executionContext, node, rootNode, path);
                if (!results.isEmpty()) {
                    errors.union(results);
                }
            } else {
                if ((this.additionalItems != null && this.additionalItems) || this.additionalSchema != null) {
                    isAdditionalItem = true;
                }

                if (this.additionalSchema != null) {
                    // validate against additional item schema
                    Set<ValidationMessage> results = this.additionalSchema.validate(executionContext, node, rootNode, path);
                    if (!results.isEmpty()) {
                        errors.union(results);
                    }
                } else if (this.additionalItems != null) {
                    if (this.additionalItems) {
//                        evaluatedItems.add(path);
                    } else {
                        // no additional item allowed, return error
                        errors.union(Collections.singleton(message().instanceNode(rootNode).instanceLocation(instanceLocation)
                                .type("additionalItems")
                                .messageKey("additionalItems")
                                .evaluationPath(this.additionalItemsEvaluationPath)
                                .schemaLocation(this.additionalItemsSchemaLocation)
                                .schemaNode(this.additionalItemsSchemaNode)
                                .locale(executionContext.getExecutionConfig().getLocale())
                                .failFast(executionContext.isFailFast()).arguments(i).build()));
                    }
                }
            }
        }
        return isAdditionalItem;
    }

    @Override
    public Set<ValidationMessage> walk(ExecutionContext executionContext, JsonNode node, JsonNode rootNode, JsonNodePath instanceLocation, boolean shouldValidateSchema) {
        Set<ValidationMessage> validationMessages = new LinkedHashSet<>();
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

        if (this.schema != null) {
            // Walk the schema.
            if (node instanceof ArrayNode) {
                int count = Math.max(1, node.size());
                ArrayNode arrayNode = (ArrayNode) node;
                JsonNode defaultNode = null;
                if (this.validationContext.getConfig().getApplyDefaultsStrategy().shouldApplyArrayDefaults()) {
                    defaultNode = getDefaultNode(this.schema);
                }
                for (int i = 0; i < count; i++) {
                    JsonNode n = arrayNode.get(i);
                    if (n != null) {
                        if (n.isNull() && defaultNode != null) {
                            arrayNode.set(i, defaultNode);
                            n = defaultNode;
                        }
                    }
                    walkSchema(executionContext, this.schema, n, rootNode, instanceLocation.append(i), shouldValidateSchema, validationMessages, ValidatorTypeCode.ITEMS.getValue());
                }
            } else {
                walkSchema(executionContext, this.schema, null, rootNode, instanceLocation.append(0), shouldValidateSchema, validationMessages, ValidatorTypeCode.ITEMS.getValue());
            }
        }
        else if (this.tupleSchema != null) {
            int prefixItems = this.tupleSchema.size();
            for (int i = 0; i < prefixItems; i++) {
                // walk tuple schema
                if (node instanceof ArrayNode) {
                    ArrayNode arrayNode = (ArrayNode) node;
                    JsonNode defaultNode = null;
                    JsonNode n = arrayNode.get(i);
                    if (this.validationContext.getConfig().getApplyDefaultsStrategy().shouldApplyArrayDefaults()) {
                        defaultNode = getDefaultNode(this.tupleSchema.get(i));
                    }
                    if (n != null) {
                        if (n.isNull() && defaultNode != null) {
                            arrayNode.set(i, defaultNode);
                            n = defaultNode;
                        }
                    }
                    walkSchema(executionContext, this.tupleSchema.get(i), n, rootNode, instanceLocation.append(i),
                            shouldValidateSchema, validationMessages, ValidatorTypeCode.ITEMS.getValue());
                } else {
                    walkSchema(executionContext, this.tupleSchema.get(i), null, rootNode, instanceLocation.append(i),
                            shouldValidateSchema, validationMessages, ValidatorTypeCode.ITEMS.getValue());
                }
            }
            if (this.additionalSchema != null) {
                boolean hasAdditionalItem = false;

                int additionalItems = Math.max(1, (node != null ? node.size() : 0) - prefixItems);
                for (int x = 0; x < additionalItems; x++) {
                    int i = x + prefixItems;
                    // walk additional item schema
                    if (node instanceof ArrayNode) {
                        ArrayNode arrayNode = (ArrayNode) node;
                        JsonNode defaultNode = null;
                        JsonNode n = arrayNode.get(i);
                        if (this.validationContext.getConfig().getApplyDefaultsStrategy().shouldApplyArrayDefaults()) {
                            defaultNode = getDefaultNode(this.additionalSchema);
                        }
                        if (n != null) {
                            if (n.isNull() && defaultNode != null) {
                                arrayNode.set(i, defaultNode);
                                n = defaultNode;
                            }
                        }
                        walkSchema(executionContext, this.additionalSchema, n, rootNode, instanceLocation.append(i),
                                shouldValidateSchema, validationMessages, PROPERTY_ADDITIONAL_ITEMS);
                        if (n != null) {
                            hasAdditionalItem = true;
                        }
                    } else {
                        walkSchema(executionContext, this.additionalSchema, null, rootNode, instanceLocation.append(i),
                                shouldValidateSchema, validationMessages, PROPERTY_ADDITIONAL_ITEMS);
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
            }
        }
        return validationMessages;
    }

    private static JsonNode getDefaultNode(JsonSchema schema) {
        JsonNode result = schema.getSchemaNode().get("default");
        if (result == null) {
            JsonSchemaRef schemaRef = JsonSchemaRefs.from(schema);
            if (schemaRef != null) {
                result = getDefaultNode(schemaRef.getSchema());
            }
        }
        return result;
    }

    private void walkSchema(ExecutionContext executionContext, JsonSchema walkSchema, JsonNode node, JsonNode rootNode,
            JsonNodePath instanceLocation, boolean shouldValidateSchema, Set<ValidationMessage> validationMessages, String keyword) {
        boolean executeWalk = this.validationContext.getConfig().getItemWalkListenerRunner().runPreWalkListeners(executionContext, keyword,
                node, rootNode, instanceLocation, walkSchema, this);
        if (executeWalk) {
            validationMessages.addAll(walkSchema.walk(executionContext, node, rootNode, instanceLocation, shouldValidateSchema));
        }
        this.validationContext.getConfig().getItemWalkListenerRunner().runPostWalkListeners(executionContext, keyword, node, rootNode,
                instanceLocation, walkSchema, this, validationMessages);

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
