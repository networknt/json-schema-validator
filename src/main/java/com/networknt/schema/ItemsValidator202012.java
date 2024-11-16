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
 * {@link JsonValidator} for items from V2012-12.
 */
public class ItemsValidator202012 extends BaseJsonValidator {
    private static final Logger logger = LoggerFactory.getLogger(ItemsValidator202012.class);

    private final JsonSchema schema;
    private final int prefixCount;
    private final boolean additionalItems;
    
    private Boolean hasUnevaluatedItemsValidator = null;

    public ItemsValidator202012(SchemaLocation schemaLocation, JsonNodePath evaluationPath, JsonNode schemaNode,
            JsonSchema parentSchema, ValidationContext validationContext) {
        super(schemaLocation, evaluationPath, schemaNode, parentSchema, ValidatorTypeCode.ITEMS_202012,
                validationContext);

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

        this.additionalItems = schemaNode.isBoolean() ? schemaNode.booleanValue() : true;
    }

    @Override
    public Set<ValidationMessage> validate(ExecutionContext executionContext, JsonNode node, JsonNode rootNode,
            JsonNodePath instanceLocation) {
        debug(logger, executionContext, node, rootNode, instanceLocation);

        // ignores non-arrays
        if (node.isArray()) {
            SetView<ValidationMessage> errors = null;
            boolean evaluated = false;
            for (int i = this.prefixCount; i < node.size(); ++i) {
                JsonNodePath path = instanceLocation.append(i);
                // validate with item schema (the whole array has the same item schema)
                Set<ValidationMessage> results = null;
                if (additionalItems) {
                    results = this.schema.validate(executionContext, node.get(i), rootNode, path);
                } else {
                    // This handles the case where "items": false as the boolean false schema doesn't
                    // generate a helpful message
                    int x = i;
                    results = Collections.singleton(message().instanceNode(node).instanceLocation(instanceLocation)
                            .locale(executionContext.getExecutionConfig().getLocale())
                            .failFast(executionContext.isFailFast()).arguments(x).build());
                }
                if (results.isEmpty()) {
//                    evaluatedItems.add(path);
                } else {
                    if (errors == null) {
                        errors = new SetView<>();
                    }
                    errors.union(results);
                }
                evaluated = true;
            }
            if (evaluated) {
                if (collectAnnotations() || collectAnnotations(executionContext)) {
                    // Applies to all
                    executionContext.getAnnotations()
                            .put(JsonNodeAnnotation.builder().instanceLocation(instanceLocation)
                                    .evaluationPath(this.evaluationPath).schemaLocation(this.schemaLocation)
                                    .keyword(getKeyword()).value(true).build());
                }
            }
            return errors == null || errors.isEmpty() ? Collections.emptySet() : errors;
        } else {
            return Collections.emptySet();
        }
    }

    @Override
    public Set<ValidationMessage> walk(ExecutionContext executionContext, JsonNode node, JsonNode rootNode,
            JsonNodePath instanceLocation, boolean shouldValidateSchema) {
        Set<ValidationMessage> validationMessages = new LinkedHashSet<>();

        if (node instanceof ArrayNode) {
            ArrayNode arrayNode = (ArrayNode) node;
            JsonNode defaultNode = null;
            if (this.validationContext.getConfig().getApplyDefaultsStrategy().shouldApplyArrayDefaults()
                    && this.schema != null) {
                defaultNode = getDefaultNode(this.schema);
            }
            boolean evaluated = false;
            for (int i = this.prefixCount; i < node.size(); ++i) {
                JsonNode n = node.get(i);
                if (n.isNull() && defaultNode != null) {
                    arrayNode.set(i, defaultNode);
                    n = defaultNode;
                }
                // Walk the schema.
                walkSchema(executionContext, this.schema, n, rootNode, instanceLocation.append(i), shouldValidateSchema,
                        validationMessages);
                if (n != null) {
                    evaluated = true;
                }
            }
            if (evaluated) {
                if (collectAnnotations() || collectAnnotations(executionContext)) {
                    // Applies to all
                    executionContext.getAnnotations()
                            .put(JsonNodeAnnotation.builder().instanceLocation(instanceLocation)
                                    .evaluationPath(this.evaluationPath).schemaLocation(this.schemaLocation)
                                    .keyword(getKeyword()).value(true).build());
                }
            }
        } else {
            // If the node is not an ArrayNode, e.g. ObjectNode or null then the instance is null.
            // The instance location starts at the end of the prefix count.
            walkSchema(executionContext, this.schema, null, rootNode, instanceLocation.append(this.prefixCount),
                    shouldValidateSchema, validationMessages);
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
            JsonNodePath instanceLocation, boolean shouldValidateSchema, Set<ValidationMessage> validationMessages) {
        //@formatter:off
        boolean executeWalk = this.validationContext.getConfig().getItemWalkListenerRunner().runPreWalkListeners(
            executionContext,
            ValidatorTypeCode.ITEMS.getValue(),
            node,
            rootNode,
            instanceLocation,
            walkSchema, this
        );
        if (executeWalk) {
            validationMessages.addAll(walkSchema.walk(executionContext, node, rootNode, instanceLocation, shouldValidateSchema));
        }
        this.validationContext.getConfig().getItemWalkListenerRunner().runPostWalkListeners(
            executionContext,
            ValidatorTypeCode.ITEMS.getValue(),
            node,
            rootNode,
            instanceLocation,
            walkSchema,
            this, validationMessages
        );
        //@formatter:on
    }

    public JsonSchema getSchema() {
        return this.schema;
    }

    @Override
    public void preloadJsonSchema() {
        this.schema.initializeValidators();
        collectAnnotations(); // cache the flag
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

}
