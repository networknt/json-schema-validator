/*
 * Copyright (c) 2025 the original author or authors.
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

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.fasterxml.jackson.databind.JsonNode;
import com.networknt.schema.ExecutionContext;
import com.networknt.schema.Schema;
import com.networknt.schema.SchemaContext;
import com.networknt.schema.SchemaLocation;
import com.networknt.schema.path.NodePath;

/**
 * {@link KeywordValidator} for propertyDependencies.
 */
public class PropertyDependenciesValidator extends BaseKeywordValidator implements KeywordValidator {
    /*
     * Property Name -> Property Value -> Schema
     */
    private final Map<String, Map<String, Schema>> propertyDependencies;

    public PropertyDependenciesValidator(SchemaLocation schemaLocation, JsonNode schemaNode,
            Schema parentSchema, SchemaContext schemaContext) {
        super(KeywordType.PROPERTY_DEPENDENCIES, schemaNode, schemaLocation, parentSchema, schemaContext);
        Set<Entry<String, JsonNode>> properties = schemaNode.properties();
        this.propertyDependencies = new LinkedHashMap<>(properties.size());
        for (Entry<String, JsonNode> property : properties) {
            String propertyName = property.getKey();
            SchemaLocation propertySchemaLocation = schemaLocation.append(propertyName);

            Set<Entry<String, JsonNode>> propertyValues = property.getValue().properties();
            for (Entry<String, JsonNode> propertyValue : propertyValues) {
                Map<String, Schema> valueSchemas = this.propertyDependencies.computeIfAbsent(propertyName,
                        key -> new LinkedHashMap<>());
                valueSchemas.put(propertyValue.getKey(),
                        schemaContext.newSchema(propertySchemaLocation.append(propertyValue.getKey()),
                                propertyValue.getValue(),
                                parentSchema));
            }
        }
    }

    @Override
    public void validate(ExecutionContext executionContext, JsonNode node, JsonNode rootNode,
            NodePath instanceLocation) {
        validate(executionContext, node, rootNode, instanceLocation, false);
    }

    protected void validate(ExecutionContext executionContext, JsonNode node, JsonNode rootNode,
            NodePath instanceLocation, boolean walk) {
        Set<Entry<String, JsonNode>> properties = node.properties();
        for (Entry<String, JsonNode> property : properties) {
            String propertyName = property.getKey();
            String propertyValue = property.getValue().asText();
            if (propertyValue != null) {
                Map<String, Schema> propertySchemas = this.propertyDependencies.get(propertyName);
                if (propertySchemas != null) {
                    executionContext.evaluationPathAddLast(propertyName);
                    try {
                        Schema schema = propertySchemas.get(propertyValue);
                        if (schema != null) {
                            executionContext.evaluationPathAddLast(propertyValue);
                            try {
                                if (!walk) {
                                    schema.validate(executionContext, node, rootNode, instanceLocation);
                                } else {
                                    schema.walk(executionContext, node, rootNode, instanceLocation, true);
                                }
                            } finally {
                                executionContext.evaluationPathRemoveLast();
                            }
                        }
                    } finally {
                        executionContext.evaluationPathRemoveLast();
                    }
                }
            }
        }
    }

    @Override
    public void walk(ExecutionContext executionContext, JsonNode node, JsonNode rootNode, NodePath instanceLocation,
            boolean shouldValidateSchema) {
        if (shouldValidateSchema) {
            validate(executionContext, node, rootNode, instanceLocation, true);
            return;
        }

        for (Entry<String, Map<String, Schema>> property : this.propertyDependencies.entrySet()) {
            String propertyName = property.getKey();
            executionContext.evaluationPathAddLast(propertyName);
            try {
                for (Entry<String, Schema> propertyValue : property.getValue().entrySet()) {
                    executionContext.evaluationPathAddLast(propertyValue.getKey());
                    try {
                        propertyValue.getValue().walk(executionContext, node, rootNode, instanceLocation, false);
                    } finally {
                        executionContext.evaluationPathRemoveLast();
                    }
                }
            } finally {
                executionContext.evaluationPathRemoveLast();
            }
        }
    }

    @Override
    public void preloadSchema() {
        for (Map<String, Schema> properties : propertyDependencies.values()) {

            for (Schema schema : properties.values()) {
                schema.initializeValidators();
            }
        }
    }
}
