package com.networknt.schema;

import com.fasterxml.jackson.databind.JsonNode;
import com.networknt.schema.i18n.MessageSource;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Validation message handler.
 */
public abstract class ValidationMessageHandler {
    protected final ErrorMessageType errorMessageType;
    protected final String errorMessageKeyword;
    protected final MessageSource messageSource;
    protected final Keyword keyword;
    protected final JsonSchema parentSchema;
    protected final SchemaLocation schemaLocation;
    protected final JsonNodePath evaluationPath;
    protected final JsonSchema evaluationParentSchema;
    protected final Map<String, String> errorMessage;

    protected ValidationMessageHandler(ErrorMessageType errorMessageType, String errorMessageKeyword,
            MessageSource messageSource, Keyword keyword, JsonSchema parentSchema, SchemaLocation schemaLocation,
            JsonNodePath evaluationPath) {
        this.errorMessageType = errorMessageType;
        this.messageSource = messageSource;
        this.schemaLocation = Objects.requireNonNull(schemaLocation);
        this.evaluationPath = Objects.requireNonNull(evaluationPath);
        this.parentSchema = parentSchema;
        this.evaluationParentSchema = null;
        this.errorMessageKeyword = errorMessageKeyword;
        this.keyword = keyword;

        Map<String, String> currentErrorMessage = null;
        if (this.keyword != null) {
            if (this.errorMessageKeyword != null && keyword != null && parentSchema != null) {
                currentErrorMessage = getErrorMessage(this.errorMessageKeyword, parentSchema.getSchemaNode(),
                        keyword.getValue());
            }
        }
        this.errorMessage = currentErrorMessage;
    }

    /**
     * Constructor to create a copy using fields.
     * 
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
    protected ValidationMessageHandler(ErrorMessageType errorMessageType, String errorMessageKeyword,
            MessageSource messageSource, Keyword keyword, JsonSchema parentSchema, SchemaLocation schemaLocation,
            JsonNodePath evaluationPath, JsonSchema evaluationParentSchema, Map<String, String> errorMessage) {
        this.errorMessageType = errorMessageType;
        this.errorMessageKeyword = errorMessageKeyword;
        this.messageSource = messageSource;
        this.keyword = keyword;
        this.parentSchema = parentSchema;
        this.schemaLocation = schemaLocation;
        this.evaluationPath = evaluationPath;
        this.evaluationParentSchema = evaluationParentSchema;
        this.errorMessage = errorMessage;
    }

    protected MessageSourceValidationMessage.Builder message() {
        return MessageSourceValidationMessage.builder(this.messageSource, this.errorMessage, (message, failFast) -> {
            if (failFast) {
                throw new FailFastAssertionException(message);
            }
        }).code(getErrorMessageType().getErrorCode()).schemaLocation(this.schemaLocation)
                .evaluationPath(this.evaluationPath).type(this.keyword != null ? this.keyword.getValue() : null)
                .messageKey(getErrorMessageType().getErrorCodeValue());
    }

    protected ErrorMessageType getErrorMessageType() {
        return this.errorMessageType;
    }

    /**
     * Gets the custom error message to use.
     * @param errorMessageKeyword the error message keyword
     * @param schemaNode the schema node
     * @param keyword    the keyword
     * @return the custom error message
     */
    protected Map<String, String> getErrorMessage(String errorMessageKeyword, JsonNode schemaNode, String keyword) {
        final JsonSchema parentSchema = this.parentSchema;
        final JsonNode message = getMessageNode(errorMessageKeyword, schemaNode, parentSchema, keyword);
        if (message != null) {
            JsonNode messageNode = message.get(keyword);
            if (messageNode != null) {
                if (messageNode.isTextual()) {
                    return Collections.singletonMap("", messageNode.asText());
                } else if (messageNode.isObject()) {
                    Map<String, String> result = new LinkedHashMap<>();
                    messageNode.fields().forEachRemaining(entry -> result.put(entry.getKey(), entry.getValue().textValue()));
                    if (!result.isEmpty()) {
                        return result;
                    }
                }
            }
        }
        return Collections.emptyMap();
    }

    protected JsonNode getMessageNode(String errorMessageKeyword, JsonNode schemaNode, JsonSchema parentSchema,
            String pname) {
        if (schemaNode.get(errorMessageKeyword) != null && schemaNode.get(errorMessageKeyword).get(pname) != null) {
            return schemaNode.get(errorMessageKeyword);
        }
        JsonNode messageNode;
        messageNode = schemaNode.get(errorMessageKeyword);
        if (messageNode == null && parentSchema != null) {
            messageNode = parentSchema.schemaNode.get(errorMessageKeyword);
            if (messageNode == null) {
                return getMessageNode(errorMessageKeyword, parentSchema.schemaNode, parentSchema.getParentSchema(),
                        pname);
            }
        }
        return messageNode;
    }
}
