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
import com.networknt.schema.annotation.JsonNodeAnnotation;

import org.slf4j.Logger;

import java.util.Collection;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Base {@link JsonValidator}. 
 */
public abstract class BaseJsonValidator implements JsonValidator {
    protected final JsonNode schemaNode;

    protected final ValidationContext validationContext;

    protected final Keyword keyword;
    protected final JsonSchema parentSchema;
    protected final SchemaLocation schemaLocation;
    protected final Map<String, String> errorMessage;

    protected final JsonNodePath evaluationPath;
    protected final JsonSchema evaluationParentSchema;

    public BaseJsonValidator(SchemaLocation schemaLocation, JsonNodePath evaluationPath, JsonNode schemaNode,
            JsonSchema parentSchema, Keyword keyword,
            ValidationContext validationContext) {
        this.validationContext = validationContext;
        this.schemaNode = schemaNode;

        this.keyword = keyword;
        this.parentSchema = parentSchema;
        this.schemaLocation = schemaLocation;
        if (keyword != null && parentSchema != null && validationContext.getConfig().getErrorMessageKeyword() != null) {
            this.errorMessage = ErrorMessages.getErrorMessage(parentSchema,
                    validationContext.getConfig().getErrorMessageKeyword(), keyword.getValue());
        } else {
            this.errorMessage = null;
        }
        this.evaluationPath = evaluationPath;
        this.evaluationParentSchema = null;
    }

    /**
     * Constructor to create a copy using fields.
     *
     * @param schemaNode the schema node
     * @param validationContext the validation context
     * @param keyword the keyword
     * @param parentSchema the parent schema
     * @param schemaLocation the schema location
     * @param evaluationPath the evaluation path
     * @param evaluationParentSchema the evaluation parent schema
     * @param errorMessage the error message
     */
    protected BaseJsonValidator(
            /* Below from BaseJsonValidator */
            JsonNode schemaNode,
            ValidationContext validationContext,
            /* Below from ValidationMessageHandler */
            Keyword keyword,
            JsonSchema parentSchema,
            SchemaLocation schemaLocation,
            JsonNodePath evaluationPath,
            JsonSchema evaluationParentSchema,
            Map<String, String> errorMessage) {
        this.schemaNode = schemaNode;
        this.validationContext = validationContext;
        
        this.keyword = keyword;
        this.parentSchema = parentSchema;
        this.schemaLocation = schemaLocation;
        this.errorMessage = errorMessage;

        this.evaluationPath = evaluationPath;
        this.evaluationParentSchema = evaluationParentSchema;
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

    protected MessageSourceValidationMessage.Builder message() {
        return MessageSourceValidationMessage
                .builder(this.validationContext.getConfig().getMessageSource(), this.errorMessage)
                .schemaNode(this.schemaNode).schemaLocation(this.schemaLocation).evaluationPath(this.evaluationPath)
                .keyword(this.keyword != null ? this.keyword.getValue() : null).messageKey(this.getKeyword());
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
