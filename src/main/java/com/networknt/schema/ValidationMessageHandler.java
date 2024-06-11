package com.networknt.schema;

import com.fasterxml.jackson.databind.JsonNode;
import com.networknt.schema.i18n.MessageSource;
import com.networknt.schema.utils.StringUtils;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Validation message handler.
 */
public abstract class ValidationMessageHandler {
    protected final ErrorMessageType errorMessageType;
    protected final boolean customErrorMessagesEnabled;
    protected final MessageSource messageSource;
    protected final Keyword keyword;
    protected final JsonSchema parentSchema;
    protected final SchemaLocation schemaLocation;
    protected final JsonNodePath evaluationPath;
    protected final JsonSchema evaluationParentSchema;
    protected final Map<String, String> errorMessage;

    protected ValidationMessageHandler(ErrorMessageType errorMessageType, boolean customErrorMessagesEnabled,
            MessageSource messageSource, Keyword keyword, JsonSchema parentSchema, SchemaLocation schemaLocation,
            JsonNodePath evaluationPath) {
        ErrorMessageType currentErrorMessageType = errorMessageType;
        this.messageSource = messageSource;
        this.schemaLocation = Objects.requireNonNull(schemaLocation);
        this.evaluationPath = Objects.requireNonNull(evaluationPath);
        this.parentSchema = parentSchema;
        this.evaluationParentSchema = null;
        this.customErrorMessagesEnabled = customErrorMessagesEnabled;
        this.keyword = keyword;

        Map<String, String> currentErrorMessage = null;

        if (this.keyword != null) {
            if (this.customErrorMessagesEnabled && keyword != null && parentSchema != null) {
                currentErrorMessage = getErrorMessage(parentSchema.getSchemaNode(), keyword.getValue());
            }
            String errorCodeKey = getErrorCodeKey(keyword.getValue());
            if (errorCodeKey != null && this.parentSchema != null) {
                JsonNode errorCodeNode = this.parentSchema.getSchemaNode().get(errorCodeKey);
                if (errorCodeNode != null && errorCodeNode.isTextual()) {
                    String errorCodeText = errorCodeNode.asText();
                    if (StringUtils.isNotBlank(errorCodeText)) {
                        currentErrorMessageType = CustomErrorMessageType.of(errorCodeText);
                    }
                }
            }
        }
        this.errorMessageType = currentErrorMessageType;
        this.errorMessage = currentErrorMessage;
    }

    /**
     * Constructor to create a copy using fields.
     * 
     * @param errorMessageType the error message type
     * @param customErrorMessagesEnabled whether custom error msessages are enabled
     * @param messageSource the message source
     * @param keyword the keyword
     * @param parentSchema the parent schema
     * @param schemaLocation the schema location
     * @param evaluationPath the evaluation path
     * @param evaluationParentSchema the evaluation parent schema
     * @param errorMessage the error message
     */
    protected ValidationMessageHandler(ErrorMessageType errorMessageType, boolean customErrorMessagesEnabled,
            MessageSource messageSource, Keyword keyword, JsonSchema parentSchema, SchemaLocation schemaLocation,
            JsonNodePath evaluationPath, JsonSchema evaluationParentSchema, Map<String, String> errorMessage) {
        this.errorMessageType = errorMessageType;
        this.customErrorMessagesEnabled = customErrorMessagesEnabled;
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
     * 
     * @param schemaNode the schema node
     * @param keyword    the keyword
     * @return the custom error message
     */
    protected Map<String, String> getErrorMessage(JsonNode schemaNode, String keyword) {
        final JsonSchema parentSchema = this.parentSchema;
        final JsonNode message = getMessageNode(schemaNode, parentSchema, keyword);
        if (message != null) {
            JsonNode messageNode = message.get(keyword);
            if (messageNode != null) {
                if (messageNode.isTextual()) {
                    return Collections.singletonMap("", messageNode.asText());
                } else if (messageNode.isObject()) {
                    Map<String, String> result = new LinkedHashMap<>();
                    messageNode.fields().forEachRemaining(entry -> {
                        result.put(entry.getKey(), entry.getValue().textValue());
                    });
                    if (!result.isEmpty()) {
                        return result;
                    }
                }
            }
        }
        return Collections.emptyMap();
    }

    protected JsonNode getMessageNode(JsonNode schemaNode, JsonSchema parentSchema, String pname) {
        if (schemaNode.get("message") != null && schemaNode.get("message").get(pname) != null) {
            return schemaNode.get("message");
        }
        JsonNode messageNode;
        messageNode = schemaNode.get("message");
        if (messageNode == null && parentSchema != null) {
            messageNode = parentSchema.schemaNode.get("message");
            if (messageNode == null) {
                return getMessageNode(parentSchema.schemaNode, parentSchema.getParentSchema(), pname);
            }
        }
        return messageNode;
    }

    protected String getErrorCodeKey(String keyword) {
        if (keyword != null) {
            return keyword + "ErrorCode";
        }
        return null;
    }
}
