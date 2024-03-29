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
import com.networknt.schema.utils.JsonSchemaRefs;

import org.slf4j.Logger;

import java.util.Collection;
import java.util.Set;
import java.util.function.Consumer;

public abstract class BaseJsonValidator extends ValidationMessageHandler implements JsonValidator {
    protected final boolean suppressSubSchemaRetrieval;

    protected final JsonNode schemaNode;

    protected ValidationContext validationContext;

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
                        ? validationContext.getConfig().isCustomMessageSupported()
                        : true,
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
     * Copy constructor.
     * 
     * @param copy to copy from
     */
    protected BaseJsonValidator(BaseJsonValidator copy) {
        super(copy);
        this.suppressSubSchemaRetrieval = copy.suppressSubSchemaRetrieval;
        this.schemaNode = copy.schemaNode;
        this.validationContext = copy.validationContext;
    }

    private static JsonSchema obtainSubSchemaNode(final JsonNode schemaNode,
            final ValidationContext validationContext) {
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

    protected static void debug(Logger logger, JsonNode node, JsonNode rootNode, JsonNodePath instanceLocation) {
        logger.debug("validate( {}, {}, {})", node, rootNode, instanceLocation);
    }

   
    protected static void checkDiscriminatorMatch(final DiscriminatorContext currentDiscriminatorContext,
            final ObjectNode discriminator,
            final String discriminatorPropertyValue,
            final JsonSchema jsonSchema) {
      
        DiscriminatorHandler.checkDiscriminatorMatch(currentDiscriminatorContext, discriminator, discriminatorPropertyValue, jsonSchema);                
      
    }

    protected static void registerAndMergeDiscriminator(final DiscriminatorContext currentDiscriminatorContext,
            final ObjectNode discriminator,
            final JsonSchema schema,
            final JsonNodePath instanceLocation) {
        DiscriminatorHandler.registerAndMergeDiscriminator(currentDiscriminatorContext, discriminator, schema,
                instanceLocation);
    }

    @SuppressWarnings("unused")
    private static void checkForImplicitDiscriminatorMappingMatch(
            final DiscriminatorContext currentDiscriminatorContext,
            final String discriminatorPropertyValue,
            final JsonSchema schema) {

        DiscriminatorHandler.checkForImplicitDiscriminatorMappingMatch(currentDiscriminatorContext,
                discriminatorPropertyValue, schema);
    }

    @SuppressWarnings("unused")
    private static void checkForExplicitDiscriminatorMappingMatch(
            final DiscriminatorContext currentDiscriminatorContext,
            final String discriminatorPropertyValue,
            final JsonNode discriminatorMapping,
            final JsonSchema schema) {

        DiscriminatorHandler.checkForExplicitDiscriminatorMappingMatch(currentDiscriminatorContext,
                discriminatorPropertyValue, discriminatorMapping, schema);

    }

    @SuppressWarnings("unused")
    private static boolean noExplicitDiscriminatorKeyOverride(final JsonNode discriminatorMapping,
            final JsonSchema parentSchema) {
        return DiscriminatorHandler.noExplicitDiscriminatorKeyOverride(discriminatorMapping, parentSchema);

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

    public JsonNode getDefaultNode(JsonSchema schema) {
        JsonNode result = schema.getSchemaNode().get("default");
        if (result == null) {
            JsonSchemaRef schemaRef = JsonSchemaRefs.from(schema);
            if (schemaRef != null) {
                result = getDefaultNode(schemaRef.getSchema());
            }
        }
        return result;
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
        boolean hasValidator = false;
        JsonSchema schema = getEvaluationParentSchema();
        while (schema != null) {
            for (JsonValidator validator : schema.getValidators()) {
                if (keyword.equals(validator.getKeyword())) {
                    hasValidator = true;
                    break;
                }
            }
            if (hasValidator) {
                break;
            }
            schema = schema.getEvaluationParentSchema();
        }
        return hasValidator;
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
     * @param customizer       to customize the annotation
     */
    protected void putAnnotation(ExecutionContext executionContext, Consumer<JsonNodeAnnotation.Builder> customizer) {
        JsonNodeAnnotation.Builder builder = JsonNodeAnnotation.builder().evaluationPath(this.evaluationPath)
                .schemaLocation(this.schemaLocation).keyword(getKeyword());
        customizer.accept(builder);
        executionContext.getAnnotations().put(builder.build());
    }
}
