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
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.networknt.schema.ValidationContext.DiscriminatorContext;
import com.networknt.schema.i18n.DefaultMessageSource;

import org.slf4j.Logger;

import java.net.URI;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public abstract class BaseJsonValidator extends ValidationMessageHandler implements JsonValidator {
    protected final boolean suppressSubSchemaRetrieval;
    protected final ApplyDefaultsStrategy applyDefaultsStrategy;
    private final PathType pathType;

    protected JsonNode schemaNode;

    protected ValidationContext validationContext;

    public BaseJsonValidator(JsonNodePath schemaLocation, JsonNodePath evaluationPath, JsonNode schemaNode,
            JsonSchema parentSchema, ValidatorTypeCode validatorType, ValidationContext validationContext) {
        this(schemaLocation, evaluationPath, schemaNode, parentSchema, validatorType, validatorType, validationContext,
                false);
    }

    public BaseJsonValidator(JsonNodePath schemaLocation, JsonNodePath evaluationPath, JsonNode schemaNode,
            JsonSchema parentSchema, ErrorMessageType errorMessageType, Keyword keyword,
            ValidationContext validationContext, boolean suppressSubSchemaRetrieval) {
        super(validationContext != null
                && validationContext.getConfig() != null && validationContext.getConfig().isFailFast(),
                errorMessageType,
                (validationContext != null && validationContext.getConfig() != null)
                        ? validationContext.getConfig().isCustomMessageSupported()
                        : true,
                (validationContext != null && validationContext.getConfig() != null)
                        ? validationContext.getConfig().getMessageSource()
                        : DefaultMessageSource.getInstance(),
                keyword, parentSchema, schemaLocation, evaluationPath);
        this.validationContext = validationContext;
        this.schemaNode = schemaNode;
        this.suppressSubSchemaRetrieval = suppressSubSchemaRetrieval;
        this.applyDefaultsStrategy = (validationContext != null && validationContext.getConfig() != null
                && validationContext.getConfig().getApplyDefaultsStrategy() != null)
                        ? validationContext.getConfig().getApplyDefaultsStrategy()
                        : ApplyDefaultsStrategy.EMPTY_APPLY_DEFAULTS_STRATEGY;
        this.pathType = (validationContext != null && validationContext.getConfig() != null
                && validationContext.getConfig().getPathType() != null) ? validationContext.getConfig().getPathType()
                        : PathType.DEFAULT;
    }

    private static JsonSchema obtainSubSchemaNode(final JsonNode schemaNode, final ValidationContext validationContext) {
        final JsonNode node = schemaNode.get("id");

        if (node == null) {
            return null;
        }

        if (node.equals(schemaNode.get("$schema"))) {
            return null;
        }

        final String text = node.textValue();
        if (text == null) {
            return null;
        }

        final URI uri;
        try {
            uri = validationContext.getURIFactory().create(node.textValue());
        } catch (IllegalArgumentException e) {
            return null;
        }

        return validationContext.getJsonSchemaFactory().getSchema(uri, validationContext.getConfig());
    }

    protected static boolean equals(double n1, double n2) {
        return Math.abs(n1 - n2) < 1e-12;
    }

    protected static void debug(Logger logger, JsonNode node, JsonNode rootNode, JsonNodePath instanceLocation) {
        logger.debug("validate( {}, {}, {})", node, rootNode, instanceLocation);
    }

    /**
     * Checks based on the current {@link DiscriminatorContext} whether the provided {@link JsonSchema} a match against
     * against the current discriminator.
     *
     * @param currentDiscriminatorContext the currently active {@link DiscriminatorContext}
     * @param discriminator               the discriminator to use for the check
     * @param discriminatorPropertyValue  the value of the <code>discriminator/propertyName</code> field
     * @param jsonSchema                  the {@link JsonSchema} to check
     */
    protected static void checkDiscriminatorMatch(final ValidationContext.DiscriminatorContext currentDiscriminatorContext,
                                                  final ObjectNode discriminator,
                                                  final String discriminatorPropertyValue,
                                                  final JsonSchema jsonSchema) {
        if (discriminatorPropertyValue == null) {
            currentDiscriminatorContext.markMatch();
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
    protected static void registerAndMergeDiscriminator(final DiscriminatorContext currentDiscriminatorContext,
                                                        final ObjectNode discriminator,
                                                        final JsonSchema schema,
                                                        final JsonNodePath instanceLocation) {
        final JsonNode discriminatorOnSchema = schema.schemaNode.get("discriminator");
        if (null != discriminatorOnSchema && null != currentDiscriminatorContext
                .getDiscriminatorForPath(schema.schemaLocation.toString())) {
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
        currentDiscriminatorContext.registerDiscriminator(schema.schemaLocation.toString(), discriminator);
    }

    private static void checkForImplicitDiscriminatorMappingMatch(final DiscriminatorContext currentDiscriminatorContext,
                                                                  final String discriminatorPropertyValue,
                                                                  final JsonSchema schema) {
        if (schema.schemaLocation.getName(-1).equals(discriminatorPropertyValue)) {
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
                    && schema.schemaLocation.toString().equals(candidateExplicitMapping.getValue().asText())) {
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
            if (candidateExplicitMapping.getValue().asText().equals(parentSchema.schemaLocation.toString())) {
                return false;
            }
        }
        return true;
    }

    @Override
    public JsonNodePath getSchemaLocation() {
        return this.schemaLocation;
    }

    @Override
    public JsonNodePath getEvaluationPath() {
        return this.evaluationPath;
    }

    @Override
    public String getKeyword() {
        return this.keyword.getValue();
    }

    public JsonNode getSchemaNode() {
        return this.schemaNode;
    }

    public JsonSchema getParentSchema() {
        return this.parentSchema;
    }

    protected JsonSchema fetchSubSchemaNode(ValidationContext validationContext) {
        return this.suppressSubSchemaRetrieval ? null : obtainSubSchemaNode(this.schemaNode, validationContext);
    }

    public Set<ValidationMessage> validate(ExecutionContext executionContext, JsonNode node) {
        return validate(executionContext, node, node, atRoot());
    }

    /**
     * Validates to a format.
     * 
     * @param <T>              the result type
     * @param executionContext the execution context
     * @param node             the node
     * @param format           the format
     * @return the result
     */
    public <T> T validate(ExecutionContext executionContext, JsonNode node, OutputFormat<T> format) {
        return validate(executionContext, node, format, null);
    }

    /**
     * Validates to a format.
     * 
     * @param <T>                 the result type
     * @param executionContext    the execution context
     * @param node                the node
     * @param format              the format
     * @param executionCustomizer the customizer
     * @return the result
     */
    public <T> T validate(ExecutionContext executionContext, JsonNode node, OutputFormat<T> format,
            ExecutionCustomizer executionCustomizer) {
        format.customize(executionContext, this.validationContext);
        if (executionCustomizer != null) {
            executionCustomizer.customize(executionContext, validationContext);
        }
        Set<ValidationMessage> validationMessages = validate(executionContext, node);
        return format.format(validationMessages, executionContext, this.validationContext);
    }

    protected String getNodeFieldType() {
        JsonNode typeField = this.getParentSchema().getSchemaNode().get("type");
        if (typeField != null) {
            return typeField.asText();
        }
        return null;
    }

    protected void preloadJsonSchemas(final Collection<JsonSchema> schemas) {
        for (final JsonSchema schema : schemas) {
            schema.initializeValidators();
        }
    }

    /**
     * Get the root path.
     *
     * @return The path.
     */
    protected JsonNodePath atRoot() {
        return new JsonNodePath(this.pathType);
    }
}
