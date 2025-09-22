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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * {@link KeywordValidator} that resolves discriminator.
 */
public class DiscriminatorValidator extends BaseKeywordValidator {
    private final String propertyName;
    private final Map<String, String> mapping;

    public DiscriminatorValidator(SchemaLocation schemaLocation, JsonNodePath evaluationPath, JsonNode schemaNode,
            JsonSchema parentSchema, ValidationContext validationContext) {
        super(ValidatorTypeCode.DISCRIMINATOR, schemaNode, schemaLocation, parentSchema, validationContext,
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
            JsonNodePath instanceLocation) {
        // Do nothing
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
     * Checks based on the current {@link DiscriminatorContext} whether the provided {@link JsonSchema} a match against
     * the current discriminator.
     *
     * @param currentDiscriminatorContext the currently active {@link DiscriminatorContext}
     * @param discriminator               the discriminator to use for the check
     * @param discriminatorPropertyValue  the value of the <code>discriminator/propertyName</code> field
     * @param jsonSchema                  the {@link JsonSchema} to check
     */
    public static void checkDiscriminatorMatch(final DiscriminatorContext currentDiscriminatorContext,
                                                  final ObjectNode discriminator,
                                                  final String discriminatorPropertyValue,
                                                  final JsonSchema jsonSchema) {
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
                                                        final JsonSchema schema,
                                                        final JsonNodePath instanceLocation) {
        final JsonNode discriminatorOnSchema = schema.schemaNode.get("discriminator");
        if (null != discriminatorOnSchema && null != currentDiscriminatorContext
                .getDiscriminatorForPath(schema.schemaLocation)) {
            // this is where A -> B -> C inheritance exists, A has the root discriminator and B adds to the mapping
            final JsonNode propertyName = discriminatorOnSchema.get("propertyName");
            if (null != propertyName) {
                throw new JsonSchemaException(instanceLocation + " schema " + schema + " attempts redefining the discriminator property");
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
                    if (null != currentMappingValue && currentMappingValue != mappingValueToAdd) {
                        throw new JsonSchemaException(instanceLocation + "discriminator mapping redefinition from " + mappingKeyToAdd
                                + "/" + currentMappingValue + " to " + mappingValueToAdd);
                    } else if (null == currentMappingValue) {
                        mappingOnContextDiscriminator.set(mappingKeyToAdd, mappingValueToAdd);
                    }
                }
            }
        }
        currentDiscriminatorContext.registerDiscriminator(schema.schemaLocation, discriminator);
    }

    private static void checkForImplicitDiscriminatorMappingMatch(final DiscriminatorContext currentDiscriminatorContext,
                                                                  final String discriminatorPropertyValue,
                                                                  final JsonSchema schema) {
        if (schema.schemaLocation.getFragment().getName(-1).equals(discriminatorPropertyValue)) {
            currentDiscriminatorContext.markMatch();
        }
    }

    private static void checkForExplicitDiscriminatorMappingMatch(final DiscriminatorContext currentDiscriminatorContext,
                                                                  final String discriminatorPropertyValue,
                                                                  final JsonNode discriminatorMapping,
                                                                  final JsonSchema schema) {
        final Iterator<Map.Entry<String, JsonNode>> explicitMappings = discriminatorMapping.fields();
        while (explicitMappings.hasNext()) {
            final Map.Entry<String, JsonNode> candidateExplicitMapping = explicitMappings.next();
            if (candidateExplicitMapping.getKey().equals(discriminatorPropertyValue)
                    && ("#" + schema.schemaLocation.getFragment().toString())
                            .equals(candidateExplicitMapping.getValue().asText())) {
                currentDiscriminatorContext.markMatch();
                break;
            }
        }
    }

    private static boolean noExplicitDiscriminatorKeyOverride(final JsonNode discriminatorMapping,
                                                              final JsonSchema parentSchema) {
        final Iterator<Map.Entry<String, JsonNode>> explicitMappings = discriminatorMapping.fields();
        while (explicitMappings.hasNext()) {
            final Map.Entry<String, JsonNode> candidateExplicitMapping = explicitMappings.next();
            if (candidateExplicitMapping.getValue().asText()
                    .equals(parentSchema.schemaLocation.getFragment().toString())) {
                return false;
            }
        }
        return true;
    }    
}
