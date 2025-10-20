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
import com.networknt.schema.Schema;
import com.networknt.schema.SchemaRef;
import com.networknt.schema.SchemaLocation;
import com.networknt.schema.SchemaContext;
import com.networknt.schema.annotation.Annotation;
import com.networknt.schema.path.NodePath;
import com.networknt.schema.utils.SchemaRefs;

/**
 * {@link KeywordValidator} for items from Draft 2012-12.
 */
public class ItemsValidator extends BaseKeywordValidator {
    private final Schema schema;
    private final int prefixCount;
    private final boolean additionalItems;
    
    public ItemsValidator(SchemaLocation schemaLocation, JsonNode schemaNode,
            Schema parentSchema, SchemaContext schemaContext) {
        super(KeywordType.ITEMS, schemaNode, schemaLocation, parentSchema, schemaContext);

        JsonNode prefixItems = parentSchema.getSchemaNode().get("prefixItems");
        if (prefixItems instanceof ArrayNode) {
            this.prefixCount = prefixItems.size();
        } else if (null == prefixItems) {
            this.prefixCount = 0;
        } else {
            throw new IllegalArgumentException("The value of 'prefixItems' must be an array of JSON Schema.");
        }

        if (schemaNode.isObject() || schemaNode.isBoolean()) {
            this.schema = schemaContext.newSchema(schemaLocation, schemaNode, parentSchema);
        } else {
            throw new IllegalArgumentException("The value of 'items' MUST be a valid JSON Schema.");
        }

        this.additionalItems = schemaNode.isBoolean() ? schemaNode.booleanValue() : true;
    }

    @Override
    public void validate(ExecutionContext executionContext, JsonNode node, JsonNode rootNode,
            NodePath instanceLocation) {
        

        // ignores non-arrays
        if (node.isArray()) {
            boolean evaluated = false;
            for (int i = this.prefixCount; i < node.size(); ++i) {
                NodePath path = instanceLocation.append(i);
                // validate with item schema (the whole array has the same item schema)
                if (additionalItems) {
                    this.schema.validate(executionContext, node.get(i), rootNode, path);
                } else {
                    // This handles the case where "items": false as the boolean false schema doesn't
                    // generate a helpful message
                    int x = i;
                    executionContext.addError(error().instanceNode(node).instanceLocation(instanceLocation)
                            .evaluationPath(executionContext.getEvaluationPath()).locale(executionContext.getExecutionConfig().getLocale())
                            .index(x).arguments(x).build());
                }
                evaluated = true;
            }
            if (evaluated) {
                if (hasUnevaluatedItemsInEvaluationPath(executionContext) || collectAnnotations(executionContext)) {
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
    public void walk(ExecutionContext executionContext, JsonNode node, JsonNode rootNode,
            NodePath instanceLocation, boolean shouldValidateSchema) {
        if (node instanceof ArrayNode) {
            ArrayNode arrayNode = (ArrayNode) node;
            JsonNode defaultNode = null;
            if (executionContext.getWalkConfig().getApplyDefaultsStrategy().shouldApplyArrayDefaults()
                    && this.schema != null) {
                defaultNode = getDefaultNode(this.schema, executionContext);
            }
            boolean evaluated = false;
            for (int i = this.prefixCount; i < node.size(); ++i) {
                JsonNode n = node.get(i);
                if (n.isNull() && defaultNode != null) {
                    arrayNode.set(i, defaultNode);
                    n = defaultNode;
                }
                // Walk the schema.
                walkSchema(executionContext, this.schema, n, rootNode, instanceLocation.append(i), shouldValidateSchema);
                if (n != null) {
                    evaluated = true;
                }
            }
            if (evaluated) {
                if (hasUnevaluatedItemsInEvaluationPath(executionContext) || collectAnnotations(executionContext)) {
                    // Applies to all
                    executionContext.getAnnotations()
                            .put(Annotation.builder().instanceLocation(instanceLocation)
                                    .evaluationPath(executionContext.getEvaluationPath()).schemaLocation(this.schemaLocation)
                                    .keyword(getKeyword()).value(true).build());
                }
            }
        } else {
            // If the node is not an ArrayNode, e.g. ObjectNode or null then the instance is null.
            // The instance location starts at the end of the prefix count.
            walkSchema(executionContext, this.schema, null, rootNode, instanceLocation.append(this.prefixCount),
                    shouldValidateSchema);
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
            NodePath instanceLocation, boolean shouldValidateSchema) {
        //@formatter:off
        boolean executeWalk = executionContext.getWalkConfig().getItemWalkHandler().preWalk(
            executionContext,
            KeywordType.ITEMS.getValue(),
            node,
            rootNode,
            instanceLocation,
            walkSchema, this
        );
        int currentErrors = executionContext.getErrors().size();
        if (executeWalk) {
            walkSchema.walk(executionContext, node, rootNode, instanceLocation, shouldValidateSchema);
        }
        executionContext.getWalkConfig().getItemWalkHandler().postWalk(
            executionContext,
            KeywordType.ITEMS.getValue(),
            node,
            rootNode,
            instanceLocation,
            walkSchema,
            this, executionContext.getErrors().subList(currentErrors, executionContext.getErrors().size())
        );
        //@formatter:on
    }

    public Schema getSchema() {
        return this.schema;
    }

    @Override
    public void preloadSchema() {
        this.schema.initializeValidators();
    }
}
