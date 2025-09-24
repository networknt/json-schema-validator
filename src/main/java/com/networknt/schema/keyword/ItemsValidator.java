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

package com.networknt.schema.keyword;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.networknt.schema.ExecutionContext;
import com.networknt.schema.NodePath;
import com.networknt.schema.Schema;
import com.networknt.schema.SchemaRef;
import com.networknt.schema.SchemaLocation;
import com.networknt.schema.SchemaContext;
import com.networknt.schema.annotation.Annotation;
import com.networknt.schema.utils.SchemaRefs;

import java.util.*;

/**
 * {@link KeywordValidator} for items V4 to V2019-09.
 */
public class ItemsValidator extends BaseKeywordValidator {
    private static final String PROPERTY_ADDITIONAL_ITEMS = "additionalItems";

    private final Schema schema;
    private final List<Schema> tupleSchema;
    private final Boolean additionalItems;
    private final Schema additionalSchema;

    private Boolean hasUnevaluatedItemsValidator = null;

    private final NodePath additionalItemsEvaluationPath;
    private final SchemaLocation additionalItemsSchemaLocation;
    private final JsonNode additionalItemsSchemaNode;

    public ItemsValidator(SchemaLocation schemaLocation, NodePath evaluationPath, JsonNode schemaNode, Schema parentSchema, SchemaContext schemaContext) {
        super(Keywords.ITEMS, schemaNode, schemaLocation, parentSchema, schemaContext, evaluationPath);

        Boolean additionalItems = null;

        Schema foundSchema = null;
        Schema foundAdditionalSchema = null;
        JsonNode additionalItemsSchemaNode = null;

        if (schemaNode.isObject() || schemaNode.isBoolean()) {
            foundSchema = schemaContext.newSchema(schemaLocation, evaluationPath, schemaNode, parentSchema);
            this.tupleSchema = Collections.emptyList();
        } else {
            int i = 0;
            this.tupleSchema = new ArrayList<>(schemaNode.size());
            for (JsonNode s : schemaNode) {
                this.tupleSchema.add(schemaContext.newSchema(schemaLocation.append(i), evaluationPath.append(i),
                        s, parentSchema));
                i++;
            }

            JsonNode addItemNode = getParentSchema().getSchemaNode().get(PROPERTY_ADDITIONAL_ITEMS);
            if (addItemNode != null) {
                additionalItemsSchemaNode = addItemNode;
                if (addItemNode.isBoolean()) {
                    additionalItems = addItemNode.asBoolean();
                } else if (addItemNode.isObject()) {
                    foundAdditionalSchema = schemaContext.newSchema(
                            parentSchema.getSchemaLocation().append(PROPERTY_ADDITIONAL_ITEMS),
                            parentSchema.getEvaluationPath().append(PROPERTY_ADDITIONAL_ITEMS), addItemNode, parentSchema);
                }
            }
        }
        this.additionalItems = additionalItems;
        this.schema = foundSchema;
        this.additionalSchema = foundAdditionalSchema;
        this.additionalItemsEvaluationPath = parentSchema.getEvaluationPath().append(PROPERTY_ADDITIONAL_ITEMS);
        this.additionalItemsSchemaLocation = parentSchema.getSchemaLocation().append(PROPERTY_ADDITIONAL_ITEMS);
        this.additionalItemsSchemaNode = additionalItemsSchemaNode;
    }

    @Override
    public void validate(ExecutionContext executionContext, JsonNode node, JsonNode rootNode, NodePath instanceLocation) {
        

        if (!node.isArray() && !this.schemaContext.getSchemaRegistryConfig().isTypeLoose()) {
            // ignores non-arrays
            return;
        }
        boolean collectAnnotations = collectAnnotations();

        // Add items annotation
        if (collectAnnotations || collectAnnotations(executionContext)) {
            if (this.schema != null) {
                // Applies to all
                executionContext.getAnnotations()
                        .put(Annotation.builder().instanceLocation(instanceLocation)
                                .evaluationPath(this.evaluationPath).schemaLocation(this.schemaLocation)
                                .keyword(getKeyword()).value(true).build());
            } else if (this.tupleSchema != null) {
                // Tuples
                int items = node.isArray() ? node.size() : 1;
                int schemas = this.tupleSchema.size();
                if (items > schemas) {
                    // More items than schemas so the keyword only applied to the number of schemas
                    executionContext.getAnnotations()
                            .put(Annotation.builder().instanceLocation(instanceLocation)
                                    .evaluationPath(this.evaluationPath).schemaLocation(this.schemaLocation)
                                    .keyword(getKeyword()).value(schemas).build());
                } else {
                    // Applies to all
                    executionContext.getAnnotations()
                            .put(Annotation.builder().instanceLocation(instanceLocation)
                                    .evaluationPath(this.evaluationPath).schemaLocation(this.schemaLocation)
                                    .keyword(getKeyword()).value(true).build());
                }
            }
        }

        boolean hasAdditionalItem = false;
        if (node.isArray()) {
            int i = 0;
            for (JsonNode n : node) {
                if (doValidate(executionContext, i, n, rootNode, instanceLocation)) {
                    hasAdditionalItem = true;
                }
                i++;
            }
        } else {
            if (doValidate(executionContext, 0, node, rootNode, instanceLocation)) {
                hasAdditionalItem = true;
            }
        }

        if (hasAdditionalItem) {
            if (collectAnnotations || collectAnnotations(executionContext, "additionalItems")) {
                executionContext.getAnnotations()
                        .put(Annotation.builder().instanceLocation(instanceLocation)
                                .evaluationPath(this.additionalItemsEvaluationPath)
                                .schemaLocation(this.additionalItemsSchemaLocation)
                                .keyword("additionalItems").value(true).build());
            }
        }
    }

    private boolean doValidate(ExecutionContext executionContext, int i, JsonNode node,
            JsonNode rootNode, NodePath instanceLocation) {
        boolean isAdditionalItem = false;
        NodePath path = instanceLocation.append(i);

        if (this.schema != null) {
            // validate with item schema (the whole array has the same item
            // schema)
            this.schema.validate(executionContext, node, rootNode, path);
        } else if (this.tupleSchema != null) {
            if (i < this.tupleSchema.size()) {
                // validate against tuple schema
                this.tupleSchema.get(i).validate(executionContext, node, rootNode, path);
            } else {
                if ((this.additionalItems != null && this.additionalItems) || this.additionalSchema != null) {
                    isAdditionalItem = true;
                }

                if (this.additionalSchema != null) {
                    // validate against additional item schema
                    this.additionalSchema.validate(executionContext, node, rootNode, path);
                } else if (this.additionalItems != null) {
                    if (this.additionalItems) {
//                        evaluatedItems.add(path);
                    } else {
                        // no additional item allowed, return error
                        executionContext.addError(error().instanceNode(rootNode).instanceLocation(instanceLocation)
                                .keyword("additionalItems")
                                .messageKey("additionalItems")
                                .evaluationPath(this.additionalItemsEvaluationPath)
                                .schemaLocation(this.additionalItemsSchemaLocation)
                                .schemaNode(this.additionalItemsSchemaNode)
                                .locale(executionContext.getExecutionConfig().getLocale())
                                .index(i)
                                .arguments(i).build());
                    }
                }
            }
        }
        return isAdditionalItem;
    }

    @Override
    public void walk(ExecutionContext executionContext, JsonNode node, JsonNode rootNode, NodePath instanceLocation, boolean shouldValidateSchema) {
        boolean collectAnnotations = collectAnnotations();

        // Add items annotation
        if (collectAnnotations || collectAnnotations(executionContext)) {
            if (this.schema != null) {
                // Applies to all
                executionContext.getAnnotations()
                        .put(Annotation.builder().instanceLocation(instanceLocation)
                                .evaluationPath(this.evaluationPath).schemaLocation(this.schemaLocation)
                                .keyword(getKeyword()).value(true).build());
            } else if (this.tupleSchema != null) {
                // Tuples
                int items = node.isArray() ? node.size() : 1;
                int schemas = this.tupleSchema.size();
                if (items > schemas) {
                    // More items than schemas so the keyword only applied to the number of schemas
                    executionContext.getAnnotations()
                            .put(Annotation.builder().instanceLocation(instanceLocation)
                                    .evaluationPath(this.evaluationPath).schemaLocation(this.schemaLocation)
                                    .keyword(getKeyword()).value(schemas).build());
                } else {
                    // Applies to all
                    executionContext.getAnnotations()
                            .put(Annotation.builder().instanceLocation(instanceLocation)
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
                if (executionContext.getWalkConfig().getApplyDefaultsStrategy().shouldApplyArrayDefaults()) {
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
                    walkSchema(executionContext, this.schema, n, rootNode, instanceLocation.append(i), shouldValidateSchema, Keywords.ITEMS.getValue());
                }
            } else {
                walkSchema(executionContext, this.schema, null, rootNode, instanceLocation.append(0), shouldValidateSchema, Keywords.ITEMS.getValue());
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
                    if (executionContext.getWalkConfig().getApplyDefaultsStrategy().shouldApplyArrayDefaults()) {
                        defaultNode = getDefaultNode(this.tupleSchema.get(i));
                    }
                    if (n != null) {
                        if (n.isNull() && defaultNode != null) {
                            arrayNode.set(i, defaultNode);
                            n = defaultNode;
                        }
                    }
                    walkSchema(executionContext, this.tupleSchema.get(i), n, rootNode, instanceLocation.append(i),
                            shouldValidateSchema, Keywords.ITEMS.getValue());
                } else {
                    walkSchema(executionContext, this.tupleSchema.get(i), null, rootNode, instanceLocation.append(i),
                            shouldValidateSchema, Keywords.ITEMS.getValue());
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
                        if (executionContext.getWalkConfig().getApplyDefaultsStrategy().shouldApplyArrayDefaults()) {
                            defaultNode = getDefaultNode(this.additionalSchema);
                        }
                        if (n != null) {
                            if (n.isNull() && defaultNode != null) {
                                arrayNode.set(i, defaultNode);
                                n = defaultNode;
                            }
                        }
                        walkSchema(executionContext, this.additionalSchema, n, rootNode, instanceLocation.append(i),
                                shouldValidateSchema, PROPERTY_ADDITIONAL_ITEMS);
                        if (n != null) {
                            hasAdditionalItem = true;
                        }
                    } else {
                        walkSchema(executionContext, this.additionalSchema, null, rootNode, instanceLocation.append(i),
                                shouldValidateSchema, PROPERTY_ADDITIONAL_ITEMS);
                    }
                }

                if (hasAdditionalItem) {
                    if (collectAnnotations || collectAnnotations(executionContext, "additionalItems")) {
                        executionContext.getAnnotations()
                                .put(Annotation.builder().instanceLocation(instanceLocation)
                                        .evaluationPath(this.additionalItemsEvaluationPath)
                                        .schemaLocation(this.additionalItemsSchemaLocation)
                                        .keyword("additionalItems").value(true).build());
                    }
                }
            }
        }
    }

    private static JsonNode getDefaultNode(Schema schema) {
        JsonNode result = schema.getSchemaNode().get("default");
        if (result == null) {
            SchemaRef schemaRef = SchemaRefs.from(schema);
            if (schemaRef != null) {
                result = getDefaultNode(schemaRef.getSchema());
            }
        }
        return result;
    }

    private void walkSchema(ExecutionContext executionContext, Schema walkSchema, JsonNode node, JsonNode rootNode,
            NodePath instanceLocation, boolean shouldValidateSchema, String keyword) {
        boolean executeWalk = executionContext.getWalkConfig().getItemWalkListenerRunner().runPreWalkListeners(executionContext, keyword,
                node, rootNode, instanceLocation, walkSchema, this);
        int currentErrors = executionContext.getErrors().size();
        if (executeWalk) {
            walkSchema.walk(executionContext, node, rootNode, instanceLocation, shouldValidateSchema);
        }
        executionContext.getWalkConfig().getItemWalkListenerRunner().runPostWalkListeners(executionContext, keyword, node, rootNode,
                instanceLocation, walkSchema, this, executionContext.getErrors().subList(currentErrors, executionContext.getErrors().size()));

    }

    public List<Schema> getTupleSchema() {
        return this.tupleSchema;
    }

    public Schema getSchema() {
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
    public void preloadSchema() {
        if (null != this.schema) {
            this.schema.initializeValidators();
        }
        preloadSchemas(this.tupleSchema);
        if (null != this.additionalSchema) {
            this.additionalSchema.initializeValidators();
        }
        collectAnnotations(); // cache the flag
    }
}
