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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.networknt.schema.ExecutionContext;
import com.networknt.schema.Schema;
import com.networknt.schema.SchemaContext;
import com.networknt.schema.SchemaLocation;
import com.networknt.schema.SchemaRef;
import com.networknt.schema.annotation.Annotation;
import com.networknt.schema.path.NodePath;
import com.networknt.schema.utils.SchemaRefs;

/**
 * {@link KeywordValidator} for items Draft 4 to Draft 2019-09.
 */
public class ItemsLegacyValidator extends BaseKeywordValidator {
    private static final String PROPERTY_ADDITIONAL_ITEMS = "additionalItems";

    private final Schema schema;
    private final List<Schema> tupleSchema;
    private final Boolean additionalItems;
    private final Schema additionalSchema;

    private final SchemaLocation additionalItemsSchemaLocation;
    private final JsonNode additionalItemsSchemaNode;

    public ItemsLegacyValidator(SchemaLocation schemaLocation, JsonNode schemaNode, Schema parentSchema, SchemaContext schemaContext) {
        super(KeywordType.ITEMS_LEGACY, schemaNode, schemaLocation, parentSchema, schemaContext);

        Boolean additionalItems = null;

        Schema foundSchema = null;
        Schema foundAdditionalSchema = null;
        JsonNode additionalItemsSchemaNode = null;

        if (schemaNode.isObject() || schemaNode.isBoolean()) {
            foundSchema = schemaContext.newSchema(schemaLocation, schemaNode, parentSchema);
            this.tupleSchema = Collections.emptyList();
        } else {
            int i = 0;
            this.tupleSchema = new ArrayList<>(schemaNode.size());
            for (JsonNode s : schemaNode) {
                this.tupleSchema.add(schemaContext.newSchema(schemaLocation.append(i),
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
                            addItemNode, parentSchema);
                }
            }
        }
        this.additionalItems = additionalItems;
        this.schema = foundSchema;
        this.additionalSchema = foundAdditionalSchema;
        this.additionalItemsSchemaLocation = parentSchema.getSchemaLocation().append(PROPERTY_ADDITIONAL_ITEMS);
        this.additionalItemsSchemaNode = additionalItemsSchemaNode;
    }

    @Override
    public void validate(ExecutionContext executionContext, JsonNode node, JsonNode rootNode, NodePath instanceLocation) {
        

        if (!node.isArray() && !this.schemaContext.getSchemaRegistryConfig().isTypeLoose()) {
            // ignores non-arrays
            return;
        }
        boolean collectAnnotations = hasUnevaluatedItemsInEvaluationPath(executionContext);

        // Add items annotation
        if (collectAnnotations || collectAnnotations(executionContext)) {
            if (this.schema != null) {
                // Applies to all
                executionContext.getAnnotations()
                        .put(Annotation.builder().instanceLocation(instanceLocation)
                                .evaluationPath(executionContext.getEvaluationPath()).schemaLocation(this.schemaLocation)
                                .keyword(getKeyword()).value(true).build());
            } else if (this.tupleSchema != null) {
                // Tuples
                int items = node.isArray() ? node.size() : 1;
                int schemas = this.tupleSchema.size();
                if (items > schemas) {
                    // More items than schemas so the keyword only applied to the number of schemas
                    executionContext.getAnnotations()
                            .put(Annotation.builder().instanceLocation(instanceLocation)
                                    .evaluationPath(executionContext.getEvaluationPath()).schemaLocation(this.schemaLocation)
                                    .keyword(getKeyword()).value(schemas).build());
                } else {
                    // Applies to all
                    executionContext.getAnnotations()
                            .put(Annotation.builder().instanceLocation(instanceLocation)
                                    .evaluationPath(executionContext.getEvaluationPath()).schemaLocation(this.schemaLocation)
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
                executionContext.evaluationPathAddLast("additionalItems");
                executionContext.getAnnotations()
                        .put(Annotation.builder().instanceLocation(instanceLocation)
                                .evaluationPath(executionContext.getEvaluationPath())
                                .schemaLocation(this.additionalItemsSchemaLocation)
                                .keyword("additionalItems").value(true).build());
                executionContext.evaluationPathRemoveLast();
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
                executionContext.evaluationPathAddLast(i);
                try {
                    this.tupleSchema.get(i).validate(executionContext, node, rootNode, path);
                } finally {
                    executionContext.evaluationPathRemoveLast();
                }
                
            } else {
                if ((this.additionalItems != null && this.additionalItems) || this.additionalSchema != null) {
                    isAdditionalItem = true;
                }

                if (this.additionalSchema != null) {
                    // validate against additional item schema
                    executionContext.evaluationPathRemoveLast(); // remove items
                    executionContext.evaluationPathAddLast("additionalItems");
                    try {
                        this.additionalSchema.validate(executionContext, node, rootNode, path);
                    } finally {
                        executionContext.evaluationPathRemoveLast();
                        executionContext.evaluationPathAddLast("items");
                    }
                } else if (this.additionalItems != null) {
                    if (this.additionalItems) {
//                        evaluatedItems.add(path);
                    } else {
                        // no additional item allowed, return error
                        executionContext.evaluationPathRemoveLast(); // remove items
                        executionContext.evaluationPathAddLast("additionalItems");
                        try  {
                            executionContext.addError(error().instanceNode(rootNode).instanceLocation(instanceLocation)
                                    .keyword("additionalItems")
                                    .messageKey("additionalItems")
                                    .evaluationPath(executionContext.getEvaluationPath())
                                    .schemaLocation(this.additionalItemsSchemaLocation)
                                    .schemaNode(this.additionalItemsSchemaNode)
                                    .locale(executionContext.getExecutionConfig().getLocale())
                                    .index(i)
                                    .arguments(i).build());
                        } finally {
                            executionContext.evaluationPathRemoveLast();
                            executionContext.evaluationPathAddLast("items");
                        }
                    }
                }
            }
        }
        return isAdditionalItem;
    }

    @Override
    public void walk(ExecutionContext executionContext, JsonNode node, JsonNode rootNode, NodePath instanceLocation, boolean shouldValidateSchema) {
        boolean collectAnnotations = hasUnevaluatedItemsInEvaluationPath(executionContext);

        // Add items annotation
        if (collectAnnotations || collectAnnotations(executionContext)) {
            if (this.schema != null) {
                // Applies to all
                executionContext.getAnnotations()
                        .put(Annotation.builder().instanceLocation(instanceLocation)
                                .evaluationPath(executionContext.getEvaluationPath()).schemaLocation(this.schemaLocation)
                                .keyword(getKeyword()).value(true).build());
            } else if (this.tupleSchema != null) {
                // Tuples
                int items = node.isArray() ? node.size() : 1;
                int schemas = this.tupleSchema.size();
                if (items > schemas) {
                    // More items than schemas so the keyword only applied to the number of schemas
                    executionContext.getAnnotations()
                            .put(Annotation.builder().instanceLocation(instanceLocation)
                                    .evaluationPath(executionContext.getEvaluationPath()).schemaLocation(this.schemaLocation)
                                    .keyword(getKeyword()).value(schemas).build());
                } else {
                    // Applies to all
                    executionContext.getAnnotations()
                            .put(Annotation.builder().instanceLocation(instanceLocation)
                                    .evaluationPath(executionContext.getEvaluationPath()).schemaLocation(this.schemaLocation)
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
                    defaultNode = getDefaultNode(this.schema, executionContext);
                }
                for (int i = 0; i < count; i++) {
                    JsonNode n = arrayNode.get(i);
                    if (n != null) {
                        if (n.isNull() && defaultNode != null) {
                            arrayNode.set(i, defaultNode);
                            n = defaultNode;
                        }
                    }
                    walkSchema(executionContext, this.schema, n, rootNode, instanceLocation.append(i), shouldValidateSchema, KeywordType.ITEMS_LEGACY.getValue());
                }
            } else {
                walkSchema(executionContext, this.schema, null, rootNode, instanceLocation.append(0), shouldValidateSchema, KeywordType.ITEMS_LEGACY.getValue());
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
                        defaultNode = getDefaultNode(this.tupleSchema.get(i), executionContext);
                    }
                    if (n != null) {
                        if (n.isNull() && defaultNode != null) {
                            arrayNode.set(i, defaultNode);
                            n = defaultNode;
                        }
                    }
                    executionContext.evaluationPathAddLast(i);
                    try {
                        walkSchema(executionContext, this.tupleSchema.get(i), n, rootNode, instanceLocation.append(i),
                                shouldValidateSchema, KeywordType.ITEMS_LEGACY.getValue());
                    } finally {
                        executionContext.evaluationPathRemoveLast();
                    }
                } else {
                    executionContext.evaluationPathAddLast(i);
                    try {
                        walkSchema(executionContext, this.tupleSchema.get(i), null, rootNode,
                                instanceLocation.append(i), shouldValidateSchema, KeywordType.ITEMS_LEGACY.getValue());
                    } finally {
                        executionContext.evaluationPathRemoveLast();
                    }
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
                            defaultNode = getDefaultNode(this.additionalSchema, executionContext);
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
                        executionContext.evaluationPathAddLast("additionalItems");
                        executionContext.getAnnotations()
                                .put(Annotation.builder().instanceLocation(instanceLocation)
                                        .evaluationPath(executionContext.getEvaluationPath())
                                        .schemaLocation(this.additionalItemsSchemaLocation)
                                        .keyword("additionalItems").value(true).build());
                        executionContext.evaluationPathRemoveLast();
                    }
                }
            }
        }
    }

    private static JsonNode getDefaultNode(Schema schema, ExecutionContext executionContext) {
        JsonNode result = schema.getSchemaNode().get("default");
        if (result == null) {
            SchemaRef schemaRef = SchemaRefs.from(schema, executionContext);
            if (schemaRef != null) {
                result = getDefaultNode(schemaRef.getSchema(), executionContext);
            }
        }
        return result;
    }

    private void walkSchema(ExecutionContext executionContext, Schema walkSchema, JsonNode node, JsonNode rootNode,
            NodePath instanceLocation, boolean shouldValidateSchema, String keyword) {
        boolean additionalItems = "additionalItems".equals(keyword);
        if (additionalItems) {
            executionContext.evaluationPathRemoveLast(); // remove items
            executionContext.evaluationPathAddLast(keyword);
        }
        try {
            boolean executeWalk = executionContext.getWalkConfig().getItemWalkHandler()
                    .preWalk(executionContext, keyword, node, rootNode, instanceLocation, walkSchema, this);
            int currentErrors = executionContext.getErrors().size();
            if (executeWalk) {
                walkSchema.walk(executionContext, node, rootNode, instanceLocation, shouldValidateSchema);
            }
            executionContext.getWalkConfig().getItemWalkHandler().postWalk(executionContext, keyword,
                    node, rootNode, instanceLocation, walkSchema, this,
                    executionContext.getErrors().subList(currentErrors, executionContext.getErrors().size()));
        } finally {
            if (additionalItems) {
                executionContext.evaluationPathRemoveLast();
                executionContext.evaluationPathAddLast("items");
            }
        }
    }

    public List<Schema> getTupleSchema() {
        return this.tupleSchema;
    }

    public Schema getSchema() {
        return this.schema;
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
    }
}