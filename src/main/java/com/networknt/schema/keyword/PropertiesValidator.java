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
import com.fasterxml.jackson.databind.node.JsonNodeType;
import com.fasterxml.jackson.databind.node.MissingNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.networknt.schema.ExecutionContext;
import com.networknt.schema.Schema;
import com.networknt.schema.SchemaRef;
import com.networknt.schema.SchemaLocation;
import com.networknt.schema.SchemaContext;
import com.networknt.schema.annotation.Annotation;
import com.networknt.schema.path.NodePath;
import com.networknt.schema.utils.SchemaRefs;
import com.networknt.schema.walk.WalkHandler;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * {@link KeywordValidator} for properties.
 */
public class PropertiesValidator extends BaseKeywordValidator {
    public static final String PROPERTY = "properties";
    private final Map<String, Schema> schemas = new LinkedHashMap<>();
    
    public PropertiesValidator(SchemaLocation schemaLocation, JsonNode schemaNode, Schema parentSchema, SchemaContext schemaContext) {
        super(KeywordType.PROPERTIES, schemaNode, schemaLocation, parentSchema, schemaContext);
        for (Iterator<Entry<String, JsonNode>> it = schemaNode.fields(); it.hasNext();) {
            Entry<String, JsonNode> entry = it.next();
            String pname = entry.getKey();
            this.schemas.put(pname, schemaContext.newSchema(schemaLocation.append(pname),
                    entry.getValue(), parentSchema));
        }
    }

    @Override
    public void validate(ExecutionContext executionContext, JsonNode node, JsonNode rootNode,
            NodePath instanceLocation) {
        validate(executionContext, node, rootNode, instanceLocation, false);
    }

    protected void validate(ExecutionContext executionContext, JsonNode node, JsonNode rootNode,
            NodePath instanceLocation, boolean walk) {
        Set<String> matchedInstancePropertyNames = null;
        boolean collectAnnotations =  hasUnevaluatedPropertiesInEvaluationPath(executionContext) || collectAnnotations(executionContext);
        for (Entry<String, Schema> entry : this.schemas.entrySet()) {
            JsonNode propertyNode = node.get(entry.getKey());
            if (propertyNode != null) {
                NodePath path = instanceLocation.append(entry.getKey());
                if (collectAnnotations) {
                    if (matchedInstancePropertyNames == null) {
                        matchedInstancePropertyNames = new LinkedHashSet<>();
                    }
                    matchedInstancePropertyNames.add(entry.getKey());
                }
                executionContext.evaluationPathAddLast(entry.getKey());
                try {
                    if (!walk) {
                        //validate the child element(s)
                        entry.getValue().validate(executionContext, propertyNode, rootNode, path);
                    } else {
                        // check if walker is enabled. If it is enabled it is upto the walker implementation to decide about the validation.
                        walkSchema(executionContext, entry, node, rootNode, instanceLocation, true, executionContext.getWalkConfig().getPropertyWalkHandler());
                    }
                } finally {
                    executionContext.evaluationPathRemoveLast();
                }
            } else {
                if (walk) {
                    // This tries to make the walk listener consistent between when validation is
                    // enabled or disabled as when validation is disabled it will walk where node is
                    // null.
                    // The actual walk needs to be skipped as the validators assume that node is not
                    // null.
                    executionContext.evaluationPathAddLast(entry.getKey());
                    try {
                        walkSchema(executionContext, entry, node, rootNode, instanceLocation, true,
                                executionContext.getWalkConfig().getPropertyWalkHandler());
                    } finally {
                        executionContext.evaluationPathRemoveLast();
                    }
                }
            }
        }
        if (collectAnnotations) {
            executionContext.getAnnotations()
                    .put(Annotation.builder().instanceLocation(instanceLocation)
                            .evaluationPath(executionContext.getEvaluationPath()).schemaLocation(this.schemaLocation)
                            .keyword(getKeyword()).value(matchedInstancePropertyNames == null ? Collections.emptySet()
                                    : matchedInstancePropertyNames)
                            .build());
        }
    }

    @Override
    public void walk(ExecutionContext executionContext, JsonNode node, JsonNode rootNode, NodePath instanceLocation, boolean shouldValidateSchema) {
        if (executionContext.getWalkConfig().getApplyDefaultsStrategy().shouldApplyPropertyDefaults() && null != node
                && node.getNodeType() == JsonNodeType.OBJECT) {
            applyPropertyDefaults((ObjectNode) node, executionContext);
        }
        if (shouldValidateSchema) {
            validate(executionContext, node == null ? MissingNode.getInstance() : node, rootNode,
                    instanceLocation, true);
        } else {
            WalkHandler propertyWalkHandler = executionContext.getWalkConfig().getPropertyWalkHandler();
            for (Map.Entry<String, Schema> entry : this.schemas.entrySet()) {
                executionContext.evaluationPathAddLast(entry.getKey());
                try {
                    walkSchema(executionContext, entry, node, rootNode, instanceLocation, shouldValidateSchema, propertyWalkHandler);
                } finally {
                    executionContext.evaluationPathRemoveLast();
                }
            }
        }
    }

    private void applyPropertyDefaults(ObjectNode node, ExecutionContext executionContext) {
        for (Map.Entry<String, Schema> entry : this.schemas.entrySet()) {
            JsonNode propertyNode = node.get(entry.getKey());

            JsonNode defaultNode = getDefaultNode(entry.getValue(), executionContext);
            if (defaultNode == null) {
                continue;
            }
            boolean applyDefault = propertyNode == null || (propertyNode.isNull() && executionContext.getWalkConfig()
                    .getApplyDefaultsStrategy().shouldApplyPropertyDefaultsIfNull());
            if (applyDefault) {
                node.set(entry.getKey(), defaultNode);
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

    private void walkSchema(ExecutionContext executionContext, Map.Entry<String, Schema> entry, JsonNode node,
            JsonNode rootNode, NodePath instanceLocation, boolean shouldValidateSchema, WalkHandler propertyWalkHandler) {
        Schema propertySchema = entry.getValue();
        JsonNode propertyNode = (node == null ? null : node.get(entry.getKey()));
        NodePath path = instanceLocation.append(entry.getKey());
        boolean executeWalk = propertyWalkHandler.preWalk(executionContext,
                KeywordType.PROPERTIES.getValue(), propertyNode, rootNode, path,
                propertySchema, this);
        if (propertyNode == null && node != null) {
            // Attempt to get the property node again in case the propertyNode was updated
            propertyNode = node.get(entry.getKey());
        }
        int currentErrors = executionContext.getErrors().size();
        if (executeWalk) {
            propertySchema.walk(executionContext, propertyNode, rootNode, path, shouldValidateSchema);
        }
        propertyWalkHandler.postWalk(executionContext, KeywordType.PROPERTIES.getValue(),
                propertyNode, rootNode, path, propertySchema, this,
                executionContext.getErrors().subList(currentErrors, executionContext.getErrors().size()));
    }

    public Map<String, Schema> getSchemas() {
        return this.schemas;
    }

    @Override
    public void preloadSchema() {
        preloadSchemas(this.schemas.values());
    }
}
