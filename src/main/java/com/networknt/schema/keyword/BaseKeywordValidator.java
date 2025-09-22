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

package com.networknt.schema.keyword;

import com.fasterxml.jackson.databind.JsonNode;
import com.networknt.schema.ErrorMessages;
import com.networknt.schema.ExecutionContext;
import com.networknt.schema.JsonNodePath;
import com.networknt.schema.Schema;
import com.networknt.schema.MessageSourceError;
import com.networknt.schema.SchemaLocation;
import com.networknt.schema.ValidationContext;

import org.slf4j.Logger;

import java.util.Collection;
import java.util.Map;

/**
 * Base {@link KeywordValidator}. 
 */
public abstract class BaseKeywordValidator extends AbstractKeywordValidator {
    protected final ValidationContext validationContext;

    protected final Schema parentSchema;
    protected final Map<String, String> errorMessage;

    protected final Schema evaluationParentSchema;

    public BaseKeywordValidator(Keyword keyword, JsonNode schemaNode, SchemaLocation schemaLocation,
            Schema parentSchema, ValidationContext validationContext,
            JsonNodePath evaluationPath) {
        super(keyword, schemaNode, schemaLocation, evaluationPath);
        this.validationContext = validationContext;

        this.parentSchema = parentSchema;
        if (keyword != null && parentSchema != null && validationContext.getConfig().getErrorMessageKeyword() != null) {
            this.errorMessage = ErrorMessages.getErrorMessage(parentSchema,
                    validationContext.getConfig().getErrorMessageKeyword(), keyword.getValue());
        } else {
            this.errorMessage = null;
        }
        this.evaluationParentSchema = null;
    }

    /**
     * Constructor to create a copy using fields.
     * @param keyword the keyword
     * @param schemaNode the schema node
     * @param schemaLocation the schema location
     * @param validationContext the validation context
     * @param parentSchema the parent schema
     * @param evaluationPath the evaluation path
     * @param evaluationParentSchema the evaluation parent schema
     * @param errorMessage the error message
     */
    protected BaseKeywordValidator(
            Keyword keyword,
            JsonNode schemaNode,
            SchemaLocation schemaLocation,
            ValidationContext validationContext,
            Schema parentSchema,
            JsonNodePath evaluationPath,
            Schema evaluationParentSchema,
            Map<String, String> errorMessage) {
        super(keyword, schemaNode, schemaLocation, evaluationPath);
        this.validationContext = validationContext;
        
        this.parentSchema = parentSchema;
        this.errorMessage = errorMessage;

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

    /**
     * Gets the parent schema.
     * <p>
     * This is the lexical parent schema.
     * 
     * @return the parent schema
     */
    public Schema getParentSchema() {
        return this.parentSchema;
    }

    /**
     * Gets the evaluation parent schema.
     * <p>
     * This is the dynamic parent schema when following references.
     * 
     * @see Schema#fromRef(Schema, JsonNodePath)
     * @return the evaluation parent schema
     */
    public Schema getEvaluationParentSchema() {
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

    protected void preloadJsonSchemas(final Collection<Schema> schemas) {
        for (final Schema schema : schemas) {
            schema.initializeValidators();
        }
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
        Schema schema = getEvaluationParentSchema();
        while (schema != null) {
            for (KeywordValidator validator : schema.getValidators()) {
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

    protected MessageSourceError.Builder error() {
        return MessageSourceError
                .builder(this.validationContext.getConfig().getMessageSource(), this.errorMessage)
                .schemaNode(this.schemaNode).schemaLocation(this.schemaLocation).evaluationPath(this.evaluationPath)
                .keyword(this.getKeyword()).messageKey(this.getKeyword());
    }
}
