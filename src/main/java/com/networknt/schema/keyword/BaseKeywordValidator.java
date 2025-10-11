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
import com.networknt.schema.Schema;
import com.networknt.schema.MessageSourceError;
import com.networknt.schema.SchemaLocation;
import com.networknt.schema.SchemaContext;

import java.util.Collection;
import java.util.Map;

/**
 * Base {@link KeywordValidator}. 
 */
public abstract class BaseKeywordValidator extends AbstractKeywordValidator {
    protected final SchemaContext schemaContext;

    protected final Schema parentSchema;
    protected final Map<String, String> errorMessage;

    public BaseKeywordValidator(Keyword keyword, JsonNode schemaNode, SchemaLocation schemaLocation,
            Schema parentSchema, SchemaContext schemaContext) {
        super(keyword, schemaNode, schemaLocation);
        this.schemaContext = schemaContext;

        this.parentSchema = parentSchema;
        if (keyword != null && parentSchema != null && schemaContext.getSchemaRegistryConfig().getErrorMessageKeyword() != null) {
            this.errorMessage = ErrorMessages.getErrorMessage(parentSchema,
                    schemaContext.getSchemaRegistryConfig().getErrorMessageKeyword(), keyword.getValue());
        } else {
            this.errorMessage = null;
        }
    }

    /**
     * Constructor to create a copy using fields.
     * @param keyword the keyword
     * @param schemaNode the schema node
     * @param schemaLocation the schema location
     * @param schemaContext the schema context
     * @param parentSchema the parent schema
     * @param errorMessage the error message
     */
    protected BaseKeywordValidator(
            Keyword keyword,
            JsonNode schemaNode,
            SchemaLocation schemaLocation,
            SchemaContext schemaContext,
            Schema parentSchema,
            Map<String, String> errorMessage) {
        super(keyword, schemaNode, schemaLocation);
        this.schemaContext = schemaContext;
        
        this.parentSchema = parentSchema;
        this.errorMessage = errorMessage;
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

    protected String getNodeFieldType() {
        JsonNode typeField = this.getParentSchema().getSchemaNode().get("type");
        if (typeField != null) {
            return typeField.asText();
        }
        return null;
    }

    protected void preloadSchemas(final Collection<Schema> schemas) {
        for (final Schema schema : schemas) {
            schema.initializeValidators();
        }
    }

    protected MessageSourceError.Builder error() {
        return MessageSourceError
                .builder(this.schemaContext.getSchemaRegistryConfig().getMessageSource(), this.errorMessage)
                .schemaNode(this.schemaNode).schemaLocation(this.schemaLocation)
                .keyword(this.getKeyword()).messageKey(this.getKeyword());
    }
}
