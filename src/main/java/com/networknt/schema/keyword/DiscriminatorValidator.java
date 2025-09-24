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
 * {@link KeywordValidator} for discriminator.
 * <p>
 * Note that discriminator MUST NOT change the validation outcome of the schema.
 * <p>
 * <a href=
 * "https://spec.openapis.org/oas/v3.1.2#discriminator-object">Discriminator
 * Object</>
 */
public class DiscriminatorValidator extends BaseKeywordValidator {
    /**
     * The name of the property in the payload that will hold the discriminating
     * value. This property SHOULD be required in the payload schema, as the
     * behavior when the property is absent is undefined.
     */
    private final String propertyName;
    /**
     * An object to hold mappings between payload values and schema names or URI
     * references.
     */
    private final Map<String, String> mapping;

    /**
     * The schema name or URI reference to a schema that is expected to validate the
     * structure of the model when the discriminating property is not present in the
     * payload or contains a value for which there is no explicit or implicit
     * mapping.
     * <p>
     * Since OpenAPI 3.2.0
     */
    private final String defaultMapping;

    public DiscriminatorValidator(SchemaLocation schemaLocation, NodePath evaluationPath, JsonNode schemaNode,
            Schema parentSchema, SchemaContext schemaContext) {
        super(KeywordType.DISCRIMINATOR, schemaNode, schemaLocation, parentSchema, schemaContext, evaluationPath);
        ObjectNode discriminator = schemaNode.isObject() ? (ObjectNode) schemaNode : null;
        if (discriminator != null) {
            JsonNode propertyName = discriminator.get("propertyName");
            /*
             * There really should be a parse error if propertyName is not defined in the
             * schema but there is non-specification compliant behavior if there are
             * multiple discriminators on the same path if the propertyName is not defined.
             */
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

            // Check if OpenAPI 3.2.0
            JsonNode defaultMapping = discriminator.get("defaultMapping");
            if (defaultMapping != null) {
                this.defaultMapping = defaultMapping.asText();
            } else {
                this.defaultMapping = null;
            }

        } else {
            this.propertyName = "";
            this.mapping = Collections.emptyMap();
            this.defaultMapping = null;
        }
    }

    @Override
    public void validate(ExecutionContext executionContext, JsonNode node, JsonNode rootNode,
            NodePath instanceLocation) {
        DiscriminatorState state = null;
        DiscriminatorState existing = executionContext.getDiscriminatorMapping().get(instanceLocation);
        if (existing != null) {
            /*
             * By default this does not throw an exception unless strict for discriminator
             * is set to true.
             * 
             * This default is in line with the fact that the discriminator keyword doesn't
             * affect validation but just helps to filter the messages.
             */
            if (this.schemaContext.getSchemaRegistryConfig().isStrict("discriminator", Boolean.FALSE)) {
                throw new SchemaException("Schema at " + this.schemaLocation
                        + " has a discriminator keyword for which another discriminator keyword has already been set for at "
                        + instanceLocation);
            }
            /*
             * This allows a new discriminator keyword if the propertyName is empty or if
             * the propertyName value is the same as the existing one.
             * 
             * In the specification the behavior of this is undefined. There shouldn't be
             * multiple matching discriminator keywords for the same instance.
             * 
             * Also propertyName for the discriminator keyword should not be empty according
             * to the specification.
             */
            if (!"".equals(this.propertyName) && !existing.getPropertyName().equals(this.propertyName)) {
                throw new SchemaException("Schema at " + this.schemaLocation
                        + " is redefining the discriminator property that has already been set for at "
                        + instanceLocation);
            }
            state = existing;
        } else {
            state = new DiscriminatorState();
            state.setPropertyName(this.propertyName);
            executionContext.getDiscriminatorMapping().put(instanceLocation, state);
        }
        JsonNode discriminatingValueNode = node.get(state.getPropertyName());
        if (discriminatingValueNode != null && discriminatingValueNode.isTextual()) {
            String discriminatingValue = discriminatingValueNode.asText();
            state.setDiscriminatingValue(discriminatingValue);
            // Check for explicit mapping
            String mappedSchema = mapping.get(discriminatingValue);
            if (existing != null && mappedSchema != null) {
                /*
                 * If the existing already has an explicit mapping and this doesn't tally with
                 * this one this is an issue as well.
                 */
                if (existing.isExplicitMapping() && !existing.getMappedSchema().equals(mappedSchema)) {
                    throw new SchemaException(
                            "Schema at " + this.schemaLocation + " is mapping that has already been set for "
                                    + instanceLocation + " from " + existing.getMappedSchema() + " to " + mappedSchema);
                }
            }
            if (mappedSchema != null) {
                // Explicit mapping found
                state.setMappedSchema(mappedSchema);
                state.setExplicitMapping(true);
            } else {
                if (!state.isExplicitMapping()) { // only sets implicit if an explicit mapping was not previously set
                    // If explicit mapping not found use implicit value
                    state.setMappedSchema(discriminatingValue);
                    state.setExplicitMapping(false);
                }
            }
        } else {
            /*
             * Since OpenAPI 3.2.0 if defaultMapping is set, then the property is optional.
             */
            if (this.defaultMapping != null) {
                state.setMappedSchema(defaultMapping);
                state.setExplicitMapping(true);
                return;
            }

            /*
             * The property is not present in the payload. This property SHOULD be required
             * in the payload schema, as the behavior when the property is absent is
             * undefined.
             */
            /*
             * By default this does not generate an assertion unless strict for
             * discriminator is set to true.
             * 
             * This default is in line with the intent that discriminator should be an
             * annotation and not an assertion and shouldn't change the result which was why
             * the specification changed from MUST to SHOULD.
             */
            if (this.schemaContext.getSchemaRegistryConfig().isStrict("discriminator", Boolean.FALSE)) {
                executionContext.addError(error().instanceNode(node).instanceLocation(instanceLocation)
                        .locale(executionContext.getExecutionConfig().getLocale())
                        .messageKey("discriminator.missing_discriminating_value").arguments(this.propertyName).build());
            }
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
     * Checks based on the current {@link DiscriminatorContext} whether the provided
     * {@link Schema} a match against the current discriminator.
     *
     * @param currentDiscriminatorContext the currently active
     *                                    {@link DiscriminatorContext}
     * @param discriminator               the discriminator to use for the check
     * @param discriminatorPropertyValue  the value of the
     *                                    <code>discriminator/propertyName</code>
     *                                    field
     * @param jsonSchema                  the {@link Schema} to check
     */
    public static void checkDiscriminatorMatch(final DiscriminatorContext currentDiscriminatorContext,
            final ObjectNode discriminator, final String discriminatorPropertyValue, final Schema jsonSchema) {
        if (discriminatorPropertyValue == null) {
            currentDiscriminatorContext.markIgnore();
            return;
        }

        final JsonNode discriminatorMapping = discriminator.get("mapping");
        if (null == discriminatorMapping) {
            checkForImplicitDiscriminatorMappingMatch(currentDiscriminatorContext, discriminatorPropertyValue,
                    jsonSchema);
        } else {
            checkForExplicitDiscriminatorMappingMatch(currentDiscriminatorContext, discriminatorPropertyValue,
                    discriminatorMapping, jsonSchema);
            if (!currentDiscriminatorContext.isDiscriminatorMatchFound()
                    && noExplicitDiscriminatorKeyOverride(discriminatorMapping, jsonSchema)) {
                checkForImplicitDiscriminatorMappingMatch(currentDiscriminatorContext, discriminatorPropertyValue,
                        jsonSchema);
            }
        }
    }

    /**
     * Rolls up all nested and compatible discriminators to the root discriminator
     * of the type. Detects attempts to redefine the <code>propertyName</code> or
     * mappings.
     *
     * @param currentDiscriminatorContext the currently active
     *                                    {@link DiscriminatorContext}
     * @param discriminator               the discriminator to use for the check
     * @param schema                      the value of the
     *                                    <code>discriminator/propertyName</code>
     *                                    field
     * @param instanceLocation            the logging prefix
     */
    public static void registerAndMergeDiscriminator(final DiscriminatorContext currentDiscriminatorContext,
            final ObjectNode discriminator, final Schema schema, final NodePath instanceLocation) {
        final JsonNode discriminatorOnSchema = schema.getSchemaNode().get("discriminator");
        if (null != discriminatorOnSchema
                && null != currentDiscriminatorContext.getDiscriminatorForPath(schema.getSchemaLocation())) {
            // this is where A -> B -> C inheritance exists, A has the root discriminator
            // and B adds to the mapping
            final JsonNode propertyName = discriminatorOnSchema.get("propertyName");
            if (null != propertyName) {
                throw new SchemaException(
                        instanceLocation + " schema " + schema + " attempts redefining the discriminator property");
            }
            final ObjectNode mappingOnContextDiscriminator = (ObjectNode) discriminator.get("mapping");
            final ObjectNode mappingOnCurrentSchemaDiscriminator = (ObjectNode) discriminatorOnSchema.get("mapping");
            if (null == mappingOnContextDiscriminator && null != mappingOnCurrentSchemaDiscriminator) {
                // here we have a mapping on a nested discriminator and none on the root
                // discriminator, so we can simply
                // make it the root's
                discriminator.set("mapping", discriminatorOnSchema);
            } else if (null != mappingOnContextDiscriminator && null != mappingOnCurrentSchemaDiscriminator) {
                // here we have to merge. The spec doesn't specify anything on this, but here we
                // don't accept redefinition of
                // mappings that already exist
                final Iterator<Map.Entry<String, JsonNode>> fieldsToAdd = mappingOnCurrentSchemaDiscriminator.fields();
                while (fieldsToAdd.hasNext()) {
                    final Map.Entry<String, JsonNode> fieldToAdd = fieldsToAdd.next();
                    final String mappingKeyToAdd = fieldToAdd.getKey();
                    final JsonNode mappingValueToAdd = fieldToAdd.getValue();

                    final JsonNode currentMappingValue = mappingOnContextDiscriminator.get(mappingKeyToAdd);
                    if (null != currentMappingValue && !currentMappingValue.equals(mappingValueToAdd)) {
                        throw new SchemaException(instanceLocation + "discriminator mapping redefinition from "
                                + mappingKeyToAdd + "/" + currentMappingValue + " to " + mappingValueToAdd);
                    } else if (null == currentMappingValue) {
                        mappingOnContextDiscriminator.set(mappingKeyToAdd, mappingValueToAdd);
                    }
                }
            }
        }
        currentDiscriminatorContext.registerDiscriminator(schema.getSchemaLocation(), discriminator);
    }

    private static void checkForImplicitDiscriminatorMappingMatch(
            final DiscriminatorContext currentDiscriminatorContext, final String discriminatorPropertyValue,
            final Schema schema) {
        if (schema.getSchemaLocation().getFragment().getName(-1).equals(discriminatorPropertyValue)) {
            currentDiscriminatorContext.markMatch();
        }
    }

    private static void checkForExplicitDiscriminatorMappingMatch(
            final DiscriminatorContext currentDiscriminatorContext, final String discriminatorPropertyValue,
            final JsonNode discriminatorMapping, final Schema schema) {
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
