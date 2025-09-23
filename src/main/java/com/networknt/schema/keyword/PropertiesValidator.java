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
import com.networknt.schema.JsonNodePath;
import com.networknt.schema.Schema;
import com.networknt.schema.SchemaRef;
import com.networknt.schema.SchemaLocation;
import com.networknt.schema.SchemaContext;
import com.networknt.schema.annotation.JsonNodeAnnotation;
import com.networknt.schema.utils.SchemaRefs;
import com.networknt.schema.walk.WalkListenerRunner;
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
    
    private Boolean hasUnevaluatedPropertiesValidator;

    public PropertiesValidator(SchemaLocation schemaLocation, JsonNodePath evaluationPath, JsonNode schemaNode, Schema parentSchema, SchemaContext schemaContext) {
        super(ValidatorTypeCode.PROPERTIES, schemaNode, schemaLocation, parentSchema, schemaContext, evaluationPath);
        for (Iterator<Entry<String, JsonNode>> it = schemaNode.fields(); it.hasNext();) {
            Entry<String, JsonNode> entry = it.next();
            String pname = entry.getKey();
            this.schemas.put(pname, schemaContext.newSchema(schemaLocation.append(pname),
                    evaluationPath.append(pname), entry.getValue(), parentSchema));
        }
    }

    @Override
    public void validate(ExecutionContext executionContext, JsonNode node, JsonNode rootNode,
            JsonNodePath instanceLocation) {
        validate(executionContext, node, rootNode, instanceLocation, false);
    }

    protected void validate(ExecutionContext executionContext, JsonNode node, JsonNode rootNode,
            JsonNodePath instanceLocation, boolean walk) {
        

        Set<String> matchedInstancePropertyNames = null;
        boolean collectAnnotations = collectAnnotations() || collectAnnotations(executionContext);
        for (Entry<String, Schema> entry : this.schemas.entrySet()) {
            JsonNode propertyNode = node.get(entry.getKey());
            if (propertyNode != null) {
                JsonNodePath path = instanceLocation.append(entry.getKey());
                if (collectAnnotations) {
                    if (matchedInstancePropertyNames == null) {
                        matchedInstancePropertyNames = new LinkedHashSet<>();
                    }
                    matchedInstancePropertyNames.add(entry.getKey());
                }
                if (!walk) {
                    //validate the child element(s)
                    entry.getValue().validate(executionContext, propertyNode, rootNode, path);
                } else {
                    // check if walker is enabled. If it is enabled it is upto the walker implementation to decide about the validation.
                    walkSchema(executionContext, entry, node, rootNode, instanceLocation, true, executionContext.getWalkConfig().getPropertyWalkListenerRunner());
                }
            } else {
                if (walk) {
                    // This tries to make the walk listener consistent between when validation is
                    // enabled or disabled as when validation is disabled it will walk where node is
                    // null.
                    // The actual walk needs to be skipped as the validators assume that node is not
                    // null.
                    walkSchema(executionContext, entry, node, rootNode, instanceLocation, true, executionContext.getWalkConfig().getPropertyWalkListenerRunner());
                }
            }
        }
        if (collectAnnotations) {
            executionContext.getAnnotations()
                    .put(JsonNodeAnnotation.builder().instanceLocation(instanceLocation)
                            .evaluationPath(this.evaluationPath).schemaLocation(this.schemaLocation)
                            .keyword(getKeyword()).value(matchedInstancePropertyNames == null ? Collections.emptySet()
                                    : matchedInstancePropertyNames)
                            .build());
        }
    }

    @Override
    public void walk(ExecutionContext executionContext, JsonNode node, JsonNode rootNode, JsonNodePath instanceLocation, boolean shouldValidateSchema) {
        if (executionContext.getWalkConfig().getApplyDefaultsStrategy().shouldApplyPropertyDefaults() && null != node
                && node.getNodeType() == JsonNodeType.OBJECT) {
            applyPropertyDefaults((ObjectNode) node, executionContext);
        }
        if (shouldValidateSchema) {
            validate(executionContext, node == null ? MissingNode.getInstance() : node, rootNode,
                    instanceLocation, true);
        } else {
            WalkListenerRunner propertyWalkListenerRunner = executionContext.getWalkConfig().getPropertyWalkListenerRunner();
            for (Map.Entry<String, Schema> entry : this.schemas.entrySet()) {
                walkSchema(executionContext, entry, node, rootNode, instanceLocation, shouldValidateSchema, propertyWalkListenerRunner);
            }
        }
    }

    private boolean collectAnnotations() {
        return hasUnevaluatedPropertiesValidator();
    }

    private boolean hasUnevaluatedPropertiesValidator() {
        if (this.hasUnevaluatedPropertiesValidator == null) {
            this.hasUnevaluatedPropertiesValidator = hasAdjacentKeywordInEvaluationPath("unevaluatedProperties");
        }
        return hasUnevaluatedPropertiesValidator;
    }

    private void applyPropertyDefaults(ObjectNode node, ExecutionContext executionContext) {
        for (Map.Entry<String, Schema> entry : this.schemas.entrySet()) {
            JsonNode propertyNode = node.get(entry.getKey());

            JsonNode defaultNode = getDefaultNode(entry.getValue());
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

    private void walkSchema(ExecutionContext executionContext, Map.Entry<String, Schema> entry, JsonNode node,
            JsonNode rootNode, JsonNodePath instanceLocation, boolean shouldValidateSchema, WalkListenerRunner propertyWalkListenerRunner) {
        Schema propertySchema = entry.getValue();
        JsonNode propertyNode = (node == null ? null : node.get(entry.getKey()));
        JsonNodePath path = instanceLocation.append(entry.getKey());
        boolean executeWalk = propertyWalkListenerRunner.runPreWalkListeners(executionContext,
                ValidatorTypeCode.PROPERTIES.getValue(), propertyNode, rootNode, path,
                propertySchema, this);
        if (propertyNode == null && node != null) {
            // Attempt to get the property node again in case the propertyNode was updated
            propertyNode = node.get(entry.getKey());
        }
        int currentErrors = executionContext.getErrors().size();
        if (executeWalk) {
            propertySchema.walk(executionContext, propertyNode, rootNode, path, shouldValidateSchema);
        }
        propertyWalkListenerRunner.runPostWalkListeners(executionContext, ValidatorTypeCode.PROPERTIES.getValue(),
                propertyNode, rootNode, path, propertySchema, this,
                executionContext.getErrors().subList(currentErrors, executionContext.getErrors().size()));
    }

    public Map<String, Schema> getSchemas() {
        return this.schemas;
    }

    @Override
    public void preloadJsonSchema() {
        preloadJsonSchemas(this.schemas.values());
        collectAnnotations(); // cache the flag
    }
}
