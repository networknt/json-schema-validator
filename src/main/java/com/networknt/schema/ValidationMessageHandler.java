package com.networknt.schema;

import com.fasterxml.jackson.databind.JsonNode;
import com.networknt.schema.i18n.MessageSource;
import com.networknt.schema.utils.StringUtils;

import java.util.Map;
import java.util.Objects;

public abstract class ValidationMessageHandler {
    protected final boolean failFast;
    protected final MessageSource messageSource;
    protected ErrorMessageType errorMessageType;

    protected JsonNodePath schemaLocation;
    protected JsonNodePath evaluationPath;

    protected JsonSchema parentSchema;

    protected Map<String, String> customMessage;

    protected Keyword keyword;
    protected String errorCodeKey;

    protected ValidationMessageHandler(boolean failFast, ErrorMessageType errorMessageType,
            Map<String, String> customMessage, MessageSource messageSource, ValidatorTypeCode validatorType,
            JsonSchema parentSchema, JsonNodePath schemaLocation, JsonNodePath evaluationPath) {
        this(failFast, errorMessageType, customMessage, messageSource, validatorType,
                validatorType != null ? validatorType.getErrorCodeKey() : null, parentSchema, schemaLocation,
                evaluationPath);
    }
    
    protected ValidationMessageHandler(boolean failFast, ErrorMessageType errorMessageType,
            Map<String, String> customMessage, MessageSource messageSource, Keyword keyword, String errorCodeKey,
            JsonSchema parentSchema, JsonNodePath schemaLocation, JsonNodePath evaluationPath) {
        this.failFast = failFast;
        this.errorMessageType = errorMessageType;
        this.customMessage = customMessage;
        this.messageSource = messageSource;
        this.errorCodeKey = errorCodeKey;
        this.schemaLocation = Objects.requireNonNull(schemaLocation);
        this.evaluationPath = Objects.requireNonNull(evaluationPath);
        this.parentSchema = parentSchema;
        this.keyword = keyword;
        if (errorCodeKey != null) {
            parseErrorCode(errorCodeKey);
        }
    }

    protected MessageSourceValidationMessage.Builder message() {
        return MessageSourceValidationMessage.builder(this.messageSource, this.customMessage, message -> {
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
        int count = this.parentSchema.schemaLocation.getNameCount();
        for (int x = 0; x < count; x++) {
            String name = this.parentSchema.schemaLocation.getName(x);
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
        if (this.parentSchema != null) {
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
        this.errorMessageType = validatorTypeCode;
        this.keyword = validatorTypeCode;
        this.customMessage = validatorTypeCode.getCustomMessage();
        parseErrorCode(validatorTypeCode.getErrorCodeKey());
    }
    

}
