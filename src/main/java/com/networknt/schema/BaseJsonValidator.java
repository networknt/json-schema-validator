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
import com.networknt.schema.annotation.JsonNodeAnnotation;
import com.networknt.schema.i18n.DefaultMessageSource;
import com.networknt.schema.i18n.MessageSource;

import org.slf4j.Logger;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

/**
 * Base {@link JsonValidator}. 
 */
public abstract class BaseJsonValidator extends ValidationMessageHandler implements JsonValidator {
    protected final boolean suppressSubSchemaRetrieval;

    protected final JsonNode schemaNode;

    protected final ValidationContext validationContext;

    public BaseJsonValidator(SchemaLocation schemaLocation, JsonNodePath evaluationPath, JsonNode schemaNode,
            JsonSchema parentSchema, ValidatorTypeCode validatorType, ValidationContext validationContext) {
        this(schemaLocation, evaluationPath, schemaNode, parentSchema, validatorType, validatorType, validationContext,
                false);
    }

    public BaseJsonValidator(SchemaLocation schemaLocation, JsonNodePath evaluationPath, JsonNode schemaNode,
            JsonSchema parentSchema, ErrorMessageType errorMessageType, Keyword keyword,
            ValidationContext validationContext, boolean suppressSubSchemaRetrieval) {
        super(errorMessageType,
                (validationContext != null && validationContext.getConfig() != null)
                        ? validationContext.getConfig().getErrorMessageKeyword()
                        : null,
                (validationContext != null && validationContext.getConfig() != null)
                        ? validationContext.getConfig().getMessageSource()
                        : DefaultMessageSource.getInstance(),
                keyword,
                parentSchema, schemaLocation, evaluationPath);
        this.validationContext = validationContext;
        this.schemaNode = schemaNode;
        this.suppressSubSchemaRetrieval = suppressSubSchemaRetrieval;
    }

    /**
     * Constructor to create a copy using fields.
     *
     * @param suppressSubSchemaRetrieval to suppress sub schema retrieval
     * @param schemaNode the schema node
     * @param validationContext the validation context
     * @param errorMessageType the error message type
     * @param errorMessageKeyword the error message keyword
     * @param messageSource the message source
     * @param keyword the keyword
     * @param parentSchema the parent schema
     * @param schemaLocation the schema location
     * @param evaluationPath the evaluation path
     * @param evaluationParentSchema the evaluation parent schema
     * @param errorMessage the error message
     */
    protected BaseJsonValidator(
            /* Below from BaseJsonValidator */
            boolean suppressSubSchemaRetrieval,
            JsonNode schemaNode,
            ValidationContext validationContext,
            /* Below from ValidationMessageHandler */
            ErrorMessageType errorMessageType,
            String errorMessageKeyword,
            MessageSource messageSource,
            Keyword keyword,
            JsonSchema parentSchema,
            SchemaLocation schemaLocation,
            JsonNodePath evaluationPath,
            JsonSchema evaluationParentSchema,
            Map<String, String> errorMessage) {
        super(errorMessageType, errorMessageKeyword, messageSource, keyword,
                parentSchema, schemaLocation, evaluationPath, evaluationParentSchema, errorMessage);
        this.suppressSubSchemaRetrieval = suppressSubSchemaRetrieval;
        this.schemaNode = schemaNode;
        this.validationContext = validationContext;
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

        final SchemaLocation schemaLocation = SchemaLocation.of(node.textValue());

        return validationContext.getJsonSchemaFactory().getSchema(schemaLocation, validationContext.getConfig());
    }

    protected static boolean equals(double n1, double n2) {
        return Math.abs(n1 - n2) < 1e-12;
    }

    public static void debug(Logger logger, ExecutionContext executionContext, JsonNode node, JsonNode rootNode,
            JsonNodePath instanceLocation) {
        //logger.debug("validate( {}, {}, {})", node, rootNode, instanceLocation);
        // The below is equivalent to the above but as there are more than 2 arguments
        // the var-arg method is used and an array needs to be allocated even if debug
        // is not enabled
        if (executionContext.getExecutionConfig().isDebugEnabled() && logger.isDebugEnabled()) {
            StringBuilder builder = new StringBuilder();
            builder.append("validate( ");
            builder.append(node.toString());
            builder.append(", ");
            builder.append(rootNode.toString());
            builder.append(", ");
            builder.append(instanceLocation.toString());
            builder.append(")");
            logger.debug(builder.toString());
        }
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
    protected static void checkDiscriminatorMatch(final DiscriminatorContext currentDiscriminatorContext,
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
    protected static void registerAndMergeDiscriminator(final DiscriminatorContext currentDiscriminatorContext,
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

    @Override
    public SchemaLocation getSchemaLocation() {
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

    /**
     * Gets the parent schema.
     * <p>
     * This is the lexical parent schema.
     * 
     * @return the parent schema
     */
    public JsonSchema getParentSchema() {
        return this.parentSchema;
    }

    /**
     * Gets the evaluation parent schema.
     * <p>
     * This is the dynamic parent schema when following references.
     * 
     * @see JsonSchema#fromRef(JsonSchema, JsonNodePath)
     * @return the evaluation parent schema
     */
    public JsonSchema getEvaluationParentSchema() {
        if (this.evaluationParentSchema != null) {
            return this.evaluationParentSchema;
        }
        return getParentSchema();
    }

    protected JsonSchema fetchSubSchemaNode(ValidationContext validationContext) {
        return this.suppressSubSchemaRetrieval ? null : obtainSubSchemaNode(this.schemaNode, validationContext);
    }

    public Set<ValidationMessage> validate(ExecutionContext executionContext, JsonNode node) {
        return validate(executionContext, node, node, atRoot());
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

    public static class JsonNodePathLegacy {
        private static final JsonNodePath INSTANCE = new JsonNodePath(PathType.LEGACY);
        public static JsonNodePath getInstance() {
            return INSTANCE;
        }
    }

    public static class JsonNodePathJsonPointer {
        private static final JsonNodePath INSTANCE = new JsonNodePath(PathType.JSON_POINTER);
        public static JsonNodePath getInstance() {
            return INSTANCE;
        }
    }

    public static class JsonNodePathJsonPath {
        private static final JsonNodePath INSTANCE = new JsonNodePath(PathType.JSON_PATH);
        public static JsonNodePath getInstance() {
            return INSTANCE;
        }
    }

    /**
     * Get the root path.
     *
     * @return The path.
     */
    protected JsonNodePath atRoot() {
        if (this.validationContext.getConfig().getPathType().equals(PathType.JSON_POINTER)) {
            return JsonNodePathJsonPointer.getInstance();
        } else if (this.validationContext.getConfig().getPathType().equals(PathType.LEGACY)) {
            return JsonNodePathLegacy.getInstance();
        } else if (this.validationContext.getConfig().getPathType().equals(PathType.JSON_PATH)) {
            return JsonNodePathJsonPath.getInstance();
        }
        return new JsonNodePath(this.validationContext.getConfig().getPathType());
    }

    @Override
    public String toString() {
        return getEvaluationPath().getName(-1);
    }

    /**
     * Determines if the keyword exists adjacent in the evaluation path.
     * <p>
     * This does not check if the keyword exists in the current meta schema as this
     * can be a cross-draft case where the properties keyword is in a Draft 7 schema
     * and the unevaluatedProperties keyword is in an outer Draft 2020-12 schema.
     * <p>
     * The fact that the validator exists in the evaluation path implies that the
     * keyword was valid in whatever meta schema for that schema it was created for.
     * 
     * @param keyword the keyword to check
     * @return true if found
     */
    protected boolean hasAdjacentKeywordInEvaluationPath(String keyword) {
        JsonSchema schema = getEvaluationParentSchema();
        while (schema != null) {
            for (JsonValidator validator : schema.getValidators()) {
                if (keyword.equals(validator.getKeyword())) {
                    return true;
                }
            }
            Object element = schema.getEvaluationPath().getElement(-1);
            if ("properties".equals(element) || "items".equals(element)) {
                // If there is a change in instance location then return false
                return false;
            }
            schema = schema.getEvaluationParentSchema();
        }
        return false;
    }

    @Override
    protected MessageSourceValidationMessage.Builder message() {
        return super.message().schemaNode(this.schemaNode);
    }

    /**
     * Determine if annotations should be reported.
     * 
     * @param executionContext the execution context
     * @return true if annotations should be reported
     */
    protected boolean collectAnnotations(ExecutionContext executionContext) {
        return collectAnnotations(executionContext, getKeyword());
    }

    /**
     * Determine if annotations should be reported.
     * 
     * @param executionContext the execution context
     * @param keyword          the keyword
     * @return true if annotations should be reported
     */
    protected boolean collectAnnotations(ExecutionContext executionContext, String keyword) {
        return executionContext.getExecutionConfig().isAnnotationCollectionEnabled()
                && executionContext.getExecutionConfig().getAnnotationCollectionFilter().test(keyword);
    }

    /**
     * Puts an annotation.
     * 
     * @param executionContext the execution context
     * @param customizer to customize the annotation
     */
    protected void putAnnotation(ExecutionContext executionContext, Consumer<JsonNodeAnnotation.Builder> customizer) {
        JsonNodeAnnotation.Builder builder = JsonNodeAnnotation.builder().evaluationPath(this.evaluationPath)
                .schemaLocation(this.schemaLocation).keyword(getKeyword());
        customizer.accept(builder);
        executionContext.getAnnotations().put(builder.build());
    }
}
