/*
 * Copyright (c) 2024 the original author or authors.
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

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * {@link JsonValidator} that resolves discriminator.
 */
public class DiscriminatorValidator extends BaseJsonValidator {
    private final String propertyName;
    private final Map<String, String> mapping;

    public DiscriminatorValidator(SchemaLocation schemaLocation, JsonNodePath evaluationPath, JsonNode schemaNode,
            JsonSchema parentSchema, ValidationContext validationContext) {
        super(schemaLocation, evaluationPath, schemaNode, parentSchema, ValidatorTypeCode.DISCRIMINATOR,
                validationContext);
        ObjectNode discriminator = schemaNode.isObject() ? (ObjectNode) schemaNode : null;
        if (discriminator != null) {
            JsonNode propertyName = discriminator.get("propertyName");
            this.propertyName = propertyName != null ? propertyName.asText() : "";
            JsonNode mappingNode = discriminator.get("mapping");
            ObjectNode mapping = mappingNode != null && mappingNode.isObject() ? (ObjectNode) mappingNode : null;
            if (mapping != null) {
                this.mapping = new HashMap<>();
                for (Iterator<Entry<String, JsonNode>> iter = mapping.fields(); iter.hasNext();) {
                    Entry<String, JsonNode> entry = iter.next();
                    this.mapping.put(entry.getKey(), entry.getValue().asText());
                }
            } else {
                this.mapping = Collections.emptyMap();
            }
        } else {
            this.propertyName = "";
            this.mapping = Collections.emptyMap();
        }
    }

    @Override
    public Set<ValidationMessage> validate(ExecutionContext executionContext, JsonNode node, JsonNode rootNode,
            JsonNodePath instanceLocation) {
        return Collections.emptySet();
    }

    /**
     * Gets the property name of the discriminator.
     * 
     * @return the property name
     */
    public String getPropertyName() {
        return propertyName;
    }

    /**
     * Gets the mapping to map the property name value to the schema name.
     * 
     * @return the discriminator mappings
     */
    public Map<String, String> getMapping() {
        return mapping;
    }
}
