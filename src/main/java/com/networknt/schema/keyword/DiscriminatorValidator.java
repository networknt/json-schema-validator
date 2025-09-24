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

package com.networknt.schema.keyword;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.networknt.schema.DiscriminatorContext;
import com.networknt.schema.ExecutionContext;
import com.networknt.schema.Schema;
import com.networknt.schema.SchemaException;
import com.networknt.schema.SchemaLocation;
import com.networknt.schema.path.NodePath;
import com.networknt.schema.SchemaContext;

/**
 * {@link KeywordValidator} that resolves discriminator.
 */
public class DiscriminatorValidator extends BaseKeywordValidator {
    private final String propertyName;
    private final Map<String, String> mapping;

    public DiscriminatorValidator(SchemaLocation schemaLocation, NodePath evaluationPath, JsonNode schemaNode,
            Schema parentSchema, SchemaContext schemaContext) {
        super(KeywordType.DISCRIMINATOR, schemaNode, schemaLocation, parentSchema, schemaContext,
                evaluationPath);
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
    public void validate(ExecutionContext executionContext, JsonNode node, JsonNode rootNode,
            NodePath instanceLocation) {
        // Check for discriminator mapping
    	if ("".equals(this.propertyName)) {
    		// Invalid discriminator as propertyName cannot be empty
    		return;
    	}
    	JsonNode propertyValue = node.get(this.propertyName);
    	DiscriminatorState state = new DiscriminatorState();
    	DiscriminatorState existing = executionContext.getDiscriminatorMapping().put(instanceLocation, state);
    	state.setPropertyName(this.propertyName);
    	if (propertyValue != null && propertyValue.isTextual()) {
    		String value = propertyValue.asText();
    		state.setPropertyValue(value);
    		// Check for explicit mapping
    		String mapped = mapping.get(value);
    		if (mapped == null) {
    			// If explicit mapping not found use implicit value
    			state.setDiscriminatingValue(value);
    			state.setExplicitMapping(false);
    		} else {
    			state.setDiscriminatingValue(mapped);
    			state.setExplicitMapping(true);
    		}
    	} else {
    		// discriminator property name is missing so the property value in the state is null
    	}
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

    /**
     * Checks based on the current {@link DiscriminatorContext} whether the provided {@link Schema} a match against
     * the current discriminator.
     *
     * @param currentDiscriminatorContext the currently active {@link DiscriminatorContext}
     * @param discriminator               the discriminator to use for the check
     * @param discriminatorPropertyValue  the value of the <code>discriminator/propertyName</code> field
     * @param jsonSchema                  the {@link Schema} to check
     */
    public static void checkDiscriminatorMatch(final DiscriminatorContext currentDiscriminatorContext,
                                                  final ObjectNode discriminator,
                                                  final String discriminatorPropertyValue,
                                                  final Schema jsonSchema) {
        if (discriminatorPropertyValue == null) {
            currentDiscriminatorContext.markIgnore();
            return;
        }

        final JsonNode discriminatorMapping = discriminator.get("mapping");
        if (null == discriminatorMapping) {
            checkForImplicitDiscriminatorMappingMatch(currentDiscriminatorContext,
                    discriminatorPropertyValue,
                    jsonSchema);
        } else {
            checkForExplicitDiscriminatorMappingMatch(currentDiscriminatorContext,
                    discriminatorPropertyValue,
                    discriminatorMapping,
                    jsonSchema);
            if (!currentDiscriminatorContext.isDiscriminatorMatchFound()
                    && noExplicitDiscriminatorKeyOverride(discriminatorMapping, jsonSchema)) {
                checkForImplicitDiscriminatorMappingMatch(currentDiscriminatorContext,
                        discriminatorPropertyValue,
                        jsonSchema);
            }
        }
    }

    /**
     * Rolls up all nested and compatible discriminators to the root discriminator of the type. Detects attempts to redefine
     * the <code>propertyName</code> or mappings.
     *
     * @param currentDiscriminatorContext the currently active {@link DiscriminatorContext}
     * @param discriminator               the discriminator to use for the check
     * @param schema                      the value of the <code>discriminator/propertyName</code> field
     * @param instanceLocation                          the logging prefix
     */
    public static void registerAndMergeDiscriminator(final DiscriminatorContext currentDiscriminatorContext,
                                                        final ObjectNode discriminator,
                                                        final Schema schema,
                                                        final NodePath instanceLocation) {
        final JsonNode discriminatorOnSchema = schema.getSchemaNode().get("discriminator");
        if (null != discriminatorOnSchema && null != currentDiscriminatorContext
                .getDiscriminatorForPath(schema.getSchemaLocation())) {
            // this is where A -> B -> C inheritance exists, A has the root discriminator and B adds to the mapping
            final JsonNode propertyName = discriminatorOnSchema.get("propertyName");
            if (null != propertyName) {
                throw new SchemaException(instanceLocation + " schema " + schema + " attempts redefining the discriminator property");
            }
            final ObjectNode mappingOnContextDiscriminator = (ObjectNode) discriminator.get("mapping");
            final ObjectNode mappingOnCurrentSchemaDiscriminator = (ObjectNode) discriminatorOnSchema.get("mapping");
            if (null == mappingOnContextDiscriminator && null != mappingOnCurrentSchemaDiscriminator) {
                // here we have a mapping on a nested discriminator and none on the root discriminator, so we can simply
                // make it the root's
                discriminator.set("mapping", discriminatorOnSchema);
            } else if (null != mappingOnContextDiscriminator && null != mappingOnCurrentSchemaDiscriminator) {
                // here we have to merge. The spec doesn't specify anything on this, but here we don't accept redefinition of
                // mappings that already exist
                final Iterator<Map.Entry<String, JsonNode>> fieldsToAdd = mappingOnCurrentSchemaDiscriminator.fields();
                while (fieldsToAdd.hasNext()) {
                    final Map.Entry<String, JsonNode> fieldToAdd = fieldsToAdd.next();
                    final String mappingKeyToAdd = fieldToAdd.getKey();
                    final JsonNode mappingValueToAdd = fieldToAdd.getValue();

                    final JsonNode currentMappingValue = mappingOnContextDiscriminator.get(mappingKeyToAdd);
                    if (null != currentMappingValue && !currentMappingValue.equals(mappingValueToAdd)) {
                        throw new SchemaException(instanceLocation + "discriminator mapping redefinition from " + mappingKeyToAdd
                                + "/" + currentMappingValue + " to " + mappingValueToAdd);
                    } else if (null == currentMappingValue) {
                        mappingOnContextDiscriminator.set(mappingKeyToAdd, mappingValueToAdd);
                    }
                }
            }
        }
        currentDiscriminatorContext.registerDiscriminator(schema.getSchemaLocation(), discriminator);
    }

    private static void checkForImplicitDiscriminatorMappingMatch(final DiscriminatorContext currentDiscriminatorContext,
                                                                  final String discriminatorPropertyValue,
                                                                  final Schema schema) {
        if (schema.getSchemaLocation().getFragment().getName(-1).equals(discriminatorPropertyValue)) {
            currentDiscriminatorContext.markMatch();
        }
    }

    private static void checkForExplicitDiscriminatorMappingMatch(final DiscriminatorContext currentDiscriminatorContext,
                                                                  final String discriminatorPropertyValue,
                                                                  final JsonNode discriminatorMapping,
                                                                  final Schema schema) {
        final Iterator<Map.Entry<String, JsonNode>> explicitMappings = discriminatorMapping.fields();
        while (explicitMappings.hasNext()) {
            final Map.Entry<String, JsonNode> candidateExplicitMapping = explicitMappings.next();
            if (candidateExplicitMapping.getKey().equals(discriminatorPropertyValue)
                    && ("#" + schema.getSchemaLocation().getFragment().toString())
                            .equals(candidateExplicitMapping.getValue().asText())) {
                currentDiscriminatorContext.markMatch();
                break;
            }
        }
    }

    private static boolean noExplicitDiscriminatorKeyOverride(final JsonNode discriminatorMapping,
                                                              final Schema parentSchema) {
        final Iterator<Map.Entry<String, JsonNode>> explicitMappings = discriminatorMapping.fields();
        while (explicitMappings.hasNext()) {
            final Map.Entry<String, JsonNode> candidateExplicitMapping = explicitMappings.next();
            if (candidateExplicitMapping.getValue().asText()
                    .equals(parentSchema.getSchemaLocation().getFragment().toString())) {
                return false;
            }
        }
        return true;
    }    
}
