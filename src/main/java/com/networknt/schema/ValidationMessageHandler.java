package com.networknt.schema;

import com.fasterxml.jackson.databind.JsonNode;
import com.networknt.schema.i18n.MessageSource;
import com.networknt.schema.utils.StringUtils;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

public abstract class ValidationMessageHandler {
    protected boolean failFast;
    protected final MessageSource messageSource;
    protected ErrorMessageType errorMessageType;

    protected SchemaLocation schemaLocation;
    protected JsonNodePath evaluationPath;

    protected JsonSchema parentSchema;

    protected boolean customErrorMessagesEnabled;
    protected Map<String, String> errorMessage;

    protected Keyword keyword;

    protected ValidationMessageHandler(boolean failFast, ErrorMessageType errorMessageType,
            boolean customErrorMessagesEnabled, MessageSource messageSource, Keyword keyword, JsonSchema parentSchema,
            SchemaLocation schemaLocation, JsonNodePath evaluationPath) {
        this.failFast = failFast;
        this.errorMessageType = errorMessageType;
        this.messageSource = messageSource;
        this.schemaLocation = Objects.requireNonNull(schemaLocation);
        this.evaluationPath = Objects.requireNonNull(evaluationPath);
        this.parentSchema = parentSchema;
        this.customErrorMessagesEnabled = customErrorMessagesEnabled;
        updateKeyword(keyword);
    }

    protected MessageSourceValidationMessage.Builder message() {
        return MessageSourceValidationMessage.builder(this.messageSource, this.errorMessage, message -> {
            if (this.failFast && isApplicator()) {
                throw new JsonSchemaException(message);
            }
        }).code(getErrorMessageType().getErrorCode()).schemaLocation(this.schemaLocation)
                .evaluationPath(this.evaluationPath).type(this.keyword != null ? this.keyword.getValue() : null)
                .messageKey(getErrorMessageType().getErrorCodeValue());
    }

    protected ErrorMessageType getErrorMessageType() {
        return this.errorMessageType;
    }

    private boolean isApplicator() {
        return !isPartOfAnyOfMultipleType()
                && !isPartOfIfMultipleType()
                && !isPartOfNotMultipleType()
                && !isPartOfOneOfMultipleType();
    }

    private boolean isPartOfAnyOfMultipleType() {
        return schemaLocationContains(ValidatorTypeCode.ANY_OF.getValue());
    }

    private boolean isPartOfIfMultipleType() {
        return schemaLocationContains(ValidatorTypeCode.IF_THEN_ELSE.getValue());
    }

    private boolean isPartOfNotMultipleType() {
        return schemaLocationContains(ValidatorTypeCode.NOT.getValue());
    }
    
    protected boolean schemaLocationContains(String match) {
        int count = this.parentSchema.schemaLocation.getFragment().getNameCount();
        for (int x = 0; x < count; x++) {
            String name = this.parentSchema.schemaLocation.getFragment().getName(x);
            if (match.equals(name)) {
                return true;
            }
        }
        return false;
    }

    /* ********************** START OF OpenAPI 3.0.x DISCRIMINATOR METHODS ********************************* */

    protected boolean isPartOfOneOfMultipleType() {
        return schemaLocationContains(ValidatorTypeCode.ONE_OF.getValue());
    }

    protected void parseErrorCode(String errorCodeKey) {
        if (errorCodeKey != null && this.parentSchema != null) {
            JsonNode errorCodeNode = this.parentSchema.getSchemaNode().get(errorCodeKey);
            if (errorCodeNode != null && errorCodeNode.isTextual()) {
                String errorCodeText = errorCodeNode.asText();
                if (StringUtils.isNotBlank(errorCodeText)) {
                    this.errorMessageType = CustomErrorMessageType.of(errorCodeText);
                }
            }
        }
    }

    protected void updateValidatorType(ValidatorTypeCode validatorTypeCode) {
        updateKeyword(validatorTypeCode);
        updateErrorMessageType(validatorTypeCode);
    }

    protected void updateErrorMessageType(ErrorMessageType errorMessageType) {
        this.errorMessageType = errorMessageType;
    }

    protected void updateKeyword(Keyword keyword) {
        this.keyword = keyword;
        if (this.keyword != null) {
            if (this.customErrorMessagesEnabled && keyword != null && parentSchema != null) {
                this.errorMessage = getErrorMessage(parentSchema.getSchemaNode(), keyword.getValue());
            }
            parseErrorCode(getErrorCodeKey(keyword.getValue()));
        }
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
