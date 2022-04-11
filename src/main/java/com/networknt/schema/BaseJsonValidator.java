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

import java.net.URI;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.networknt.schema.ValidationContext.DiscriminatorContext;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;

public abstract class BaseJsonValidator implements JsonValidator {
    protected String schemaPath;
    protected JsonNode schemaNode;
    protected JsonSchema parentSchema;
    private boolean suppressSubSchemaRetrieval;
    private ValidatorTypeCode validatorType;
    private ErrorMessageType errorMessageType;
    protected ValidationContext validationContext;
    protected final boolean failFast;
    protected final ApplyDefaultsStrategy applyDefaultsStrategy;

    public BaseJsonValidator(String schemaPath, JsonNode schemaNode, JsonSchema parentSchema,
                             ValidatorTypeCode validatorType, ValidationContext validationContext) {
        this(schemaPath, schemaNode, parentSchema, validatorType, false,
             validationContext.getConfig() != null && validationContext.getConfig().isFailFast(),
             validationContext.getConfig() != null ? validationContext.getConfig().getApplyDefaultsStrategy() : null);
    }

    // TODO: can this be made package private?
    @Deprecated // use the BaseJsonValidator below
    public BaseJsonValidator(String schemaPath, JsonNode schemaNode, JsonSchema parentSchema,
                             ValidatorTypeCode validatorType, boolean suppressSubSchemaRetrieval, boolean failFast) {
        this(schemaPath, schemaNode, parentSchema, validatorType, false, failFast, null);
    }

    public BaseJsonValidator(String schemaPath,
                             JsonNode schemaNode,
                             JsonSchema parentSchema,
                             ValidatorTypeCode validatorType,
                             boolean suppressSubSchemaRetrieval,
                             boolean failFast,
                             ApplyDefaultsStrategy applyDefaultsStrategy) {
        this.errorMessageType = validatorType;
        this.schemaPath = schemaPath;
        this.schemaNode = schemaNode;
        this.parentSchema = parentSchema;
        this.validatorType = validatorType;
        this.suppressSubSchemaRetrieval = suppressSubSchemaRetrieval;
        this.failFast = failFast;
        this.applyDefaultsStrategy = applyDefaultsStrategy != null ? applyDefaultsStrategy : ApplyDefaultsStrategy.EMPTY_APPLY_DEFAULTS_STRATEGY;
    }

    public String getSchemaPath() {
        return schemaPath;
    }

    public JsonNode getSchemaNode() {
        return schemaNode;
    }

    public JsonSchema getParentSchema() {
        return parentSchema;
    }

    protected JsonSchema fetchSubSchemaNode(ValidationContext validationContext) {
        return suppressSubSchemaRetrieval ? null : obtainSubSchemaNode(schemaNode, validationContext);
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
        } else {
            final URI uri;
            try {
                uri = validationContext.getURIFactory().create(node.textValue());
            } catch (IllegalArgumentException e) {
                return null;
            }
            return validationContext.getJsonSchemaFactory().getSchema(uri, validationContext.getConfig());
        }
    }

    public Set<ValidationMessage> validate(JsonNode node) {
        return validate(node, node, AT_ROOT);
    }

    protected boolean equals(double n1, double n2) {
        return Math.abs(n1 - n2) < 1e-12;
    }

    protected boolean greaterThan(double n1, double n2) {
        return n1 - n2 > 1e-12;
    }

    protected boolean lessThan(double n1, double n2) {
        return n1 - n2 < -1e-12;
    }

    protected void parseErrorCode(String errorCodeKey) {
        JsonNode errorCodeNode = getParentSchema().getSchemaNode().get(errorCodeKey);
        if (errorCodeNode != null && errorCodeNode.isTextual()) {
            String errorCodeText = errorCodeNode.asText();
            if (StringUtils.isNotBlank(errorCodeText)) {
                errorMessageType = CustomErrorMessageType.of(errorCodeText);
            }
        }
    }

    protected ValidationMessage buildValidationMessage(String at, String... arguments) {
        final ValidationMessage message = ValidationMessage.of(getValidatorType().getValue(), errorMessageType, at, schemaPath, arguments);
        if (failFast && !isPartOfOneOfMultipleType()) {
            throw new JsonSchemaException(message);
        }
        return message;
    }

    protected void debug(Logger logger, JsonNode node, JsonNode rootNode, String at) {
        if (logger.isDebugEnabled()) {
            logger.debug("validate( " + node + ", " + rootNode + ", " + at + ")");
        }
    }

    protected ValidatorTypeCode getValidatorType() {
        return validatorType;
    }

    protected String getNodeFieldType() {
        JsonNode typeField = this.getParentSchema().getSchemaNode().get("type");
        if (typeField != null) {
            return typeField.asText();
        }
        return null;
    }

    /**
     * This is default implementation of walk method. Its job is to call the
     * validate method if shouldValidateSchema is enabled.
     */
    @Override
    public Set<ValidationMessage> walk(JsonNode node, JsonNode rootNode, String at, boolean shouldValidateSchema) {
        Set<ValidationMessage> validationMessages = new LinkedHashSet<ValidationMessage>();
        if (shouldValidateSchema) {
            validationMessages = validate(node, rootNode, at);
        }
        return validationMessages;
    }

    protected void preloadJsonSchemas(final Collection<JsonSchema> schemas) {
        for (final JsonSchema schema: schemas) {
            schema.initializeValidators();
        }
    }

    protected boolean isPartOfOneOfMultipleType() {
        return parentSchema.schemaPath.equals(ValidatorTypeCode.ONE_OF.getValue());
    }

    /* ********************** START OF OpenAPI 3.0.x DISCRIMINATOR METHODS ********************************* */

    /**
     * Checks based on the current {@link DiscriminatorContext} whether the provided {@link JsonSchema} a match against
     * against the current discriminator.
     *
     * @param currentDiscriminatorContext the currently active {@link DiscriminatorContext}
     * @param discriminator the discriminator to use for the check
     * @param discriminatorPropertyValue the value of the <code>discriminator/propertyName</code> field
     * @param jsonSchema the {@link JsonSchema} to check
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
     * @param discriminator the discriminator to use for the check
     * @param schema the value of the <code>discriminator/propertyName</code> field
     * @param at the logging prefix
     */
    protected static void registerAndMergeDiscriminator(final DiscriminatorContext currentDiscriminatorContext,
                                                        final ObjectNode discriminator,
                                                        final JsonSchema schema,
                                                        final String at) {
        final JsonNode discriminatorOnSchema = schema.schemaNode.get("discriminator");
        if (null != discriminatorOnSchema && null != currentDiscriminatorContext
                .getDiscriminatorForPath(schema.schemaPath)) {
            // this is where A -> B -> C inheritance exists, A has the root discriminator and B adds to the mapping
            final JsonNode propertyName = discriminatorOnSchema.get("propertyName");
            if (null != propertyName) {
                throw new JsonSchemaException(at + " schema " + schema + " attempts redefining the discriminator property");
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
                        throw new JsonSchemaException(at + "discriminator mapping redefinition from " + mappingKeyToAdd
                                                              + "/" + currentMappingValue + " to " + mappingValueToAdd);
                    } else if (null == currentMappingValue) {
                        mappingOnContextDiscriminator.set(mappingKeyToAdd, mappingValueToAdd);
                    }
                }
            }
        }
        currentDiscriminatorContext.registerDiscriminator(schema.schemaPath, discriminator);
    }

    private static void checkForImplicitDiscriminatorMappingMatch(final DiscriminatorContext currentDiscriminatorContext,
                                                                  final String discriminatorPropertyValue,
                                                                  final JsonSchema schema) {
        if (schema.schemaPath.endsWith("/" + discriminatorPropertyValue)) {
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
                    && schema.schemaPath.equals(candidateExplicitMapping.getValue().asText())) {
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
            if (candidateExplicitMapping.getValue().asText().equals(parentSchema.schemaPath)) {
                return false;
            }
        }
        return true;
    }
}
