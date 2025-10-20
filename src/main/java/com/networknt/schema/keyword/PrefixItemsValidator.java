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

package com.networknt.schema.keyword;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.networknt.schema.ExecutionContext;
import com.networknt.schema.Schema;
import com.networknt.schema.SchemaRef;
import com.networknt.schema.SchemaLocation;
import com.networknt.schema.SchemaContext;
import com.networknt.schema.annotation.Annotation;
import com.networknt.schema.path.NodePath;
import com.networknt.schema.utils.SchemaRefs;

import java.util.ArrayList;
import java.util.List;

/**
 * {@link KeywordValidator} for prefixItems.
 */
public class PrefixItemsValidator extends BaseKeywordValidator {
    private final List<Schema> tupleSchema;
    
    public PrefixItemsValidator(SchemaLocation schemaLocation, JsonNode schemaNode, Schema parentSchema, SchemaContext schemaContext) {
        super(KeywordType.PREFIX_ITEMS, schemaNode, schemaLocation, parentSchema, schemaContext);

        if (schemaNode instanceof ArrayNode && !schemaNode.isEmpty()) {
            int i = 0;
            this.tupleSchema = new ArrayList<>(schemaNode.size());
            for (JsonNode s : schemaNode) {
                this.tupleSchema.add(schemaContext.newSchema(schemaLocation.append(i), s,
                        parentSchema));
                i++;
            }
        } else {
            throw new IllegalArgumentException("The value of 'prefixItems' MUST be a non-empty array of valid JSON Schemas.");
        }
    }

    @Override
    public void validate(ExecutionContext executionContext, JsonNode node, JsonNode rootNode, NodePath instanceLocation) {
        
        // ignores non-arrays
        if (node.isArray()) {
            int count = Math.min(node.size(), this.tupleSchema.size());
            for (int i = 0; i < count; ++i) {
                NodePath path = instanceLocation.append(i);
                executionContext.evaluationPathAddLast(i);
                try {
                    this.tupleSchema.get(i).validate(executionContext, node.get(i), rootNode, path);
                } finally {
                    executionContext.evaluationPathRemoveLast();
                }
            }

            // Add annotation
            if (hasUnevaluatedItemsInEvaluationPath(executionContext) || collectAnnotations(executionContext)) {
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
    }

    @Override
    public void walk(ExecutionContext executionContext, JsonNode node, JsonNode rootNode, NodePath instanceLocation, boolean shouldValidateSchema) {
        if (node instanceof ArrayNode) {
            ArrayNode array = (ArrayNode) node;
            int count = this.tupleSchema.size();
            for (int i = 0; i < count; ++i) {
                JsonNode n = node.get(i);
                if (executionContext.getWalkConfig().getApplyDefaultsStrategy().shouldApplyArrayDefaults()) {
                    JsonNode defaultNode = getDefaultNode(this.tupleSchema.get(i), executionContext);
                    if (n != null) {
                        // Defaults only set if array index is explicitly null
                        if (n.isNull() && defaultNode != null) {
                            array.set(i, defaultNode);
                            n = defaultNode;
                        }
                    }
                }
                doWalk(executionContext, i, n, rootNode, instanceLocation, shouldValidateSchema);
            }

            // Add annotation
            if (hasUnevaluatedItemsInEvaluationPath(executionContext) || collectAnnotations(executionContext)) {
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
        } else {
            int count = this.tupleSchema.size();
            for (int i = 0; i < count; ++i) {
                doWalk(executionContext, i, null, rootNode, instanceLocation, shouldValidateSchema);
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

    private void doWalk(ExecutionContext executionContext, int i,
            JsonNode node, JsonNode rootNode, NodePath instanceLocation, boolean shouldValidateSchema) {
        walkSchema(executionContext, i, this.tupleSchema.get(i), node, rootNode, instanceLocation.append(i),
                shouldValidateSchema);
    }

    private void walkSchema(ExecutionContext executionContext, int schemaIndex, Schema walkSchema, JsonNode node, JsonNode rootNode,
            NodePath instanceLocation, boolean shouldValidateSchema) {
        //@formatter:off
        boolean executeWalk = executionContext.getWalkConfig().getItemWalkHandler().preWalk(
            executionContext,
            KeywordType.PREFIX_ITEMS.getValue(),
            node,
            rootNode,
            instanceLocation,
            walkSchema, this
        );
        int currentErrors = executionContext.getErrors().size();
        if (executeWalk) {
            executionContext.evaluationPathAddLast(schemaIndex);
            try {
                walkSchema.walk(executionContext, node, rootNode, instanceLocation, shouldValidateSchema);
            } finally {
                executionContext.evaluationPathRemoveLast();
            }
        }
        executionContext.getWalkConfig().getItemWalkHandler().postWalk(
            executionContext,
            KeywordType.PREFIX_ITEMS.getValue(),
            node,
            rootNode,
            instanceLocation,
            walkSchema,
            this, executionContext.getErrors().subList(currentErrors, executionContext.getErrors().size())
        );
        //@formatter:on
    }

    public List<Schema> getTupleSchema() {
        return this.tupleSchema;
    }

    @Override
    public void preloadSchema() {
        preloadSchemas(this.tupleSchema);
    }

}
