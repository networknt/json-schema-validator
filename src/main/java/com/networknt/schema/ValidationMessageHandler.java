package com.networknt.schema;

import com.fasterxml.jackson.databind.JsonNode;
import com.networknt.schema.i18n.MessageSource;
import com.networknt.schema.utils.StringUtils;

import java.util.Locale;
import java.util.Map;

public abstract class ValidationMessageHandler {
    protected final boolean failFast;
    protected final Map<String, String> customMessage;
    protected final MessageSource messageSource;
    protected ValidatorTypeCode validatorType;
    protected ErrorMessageType errorMessageType;

    protected String schemaPath;

    protected JsonSchema parentSchema;

    protected ValidationMessageHandler(boolean failFast, ErrorMessageType errorMessageType, Map<String, String> customMessage, MessageSource messageSource, ValidatorTypeCode validatorType, JsonSchema parentSchema, String schemaPath) {
        this.failFast = failFast;
        this.errorMessageType = errorMessageType;
        this.customMessage = customMessage;
        this.messageSource = messageSource;
        this.validatorType = validatorType;
        this.schemaPath = schemaPath;
        this.parentSchema = parentSchema;
    }

    protected ValidationMessage buildValidationMessage(String propertyName, String at, Locale locale, Object... arguments) {
        return buildValidationMessage(propertyName, at, getErrorMessageType().getErrorCodeValue(), locale, arguments);
    }

    protected ValidationMessage buildValidationMessage(String propertyName, String at, String messageKey, Locale locale, Object... arguments) {
        String messagePattern = null;
        if (this.customMessage != null) {
            messagePattern = this.customMessage.get("");
            if (propertyName != null) {
                String specificMessagePattern = this.customMessage.get(propertyName);
                if (specificMessagePattern != null) {
                   messagePattern = specificMessagePattern; 
                }
            }
        }
        final ValidationMessage message = ValidationMessage.builder()
                .code(getErrorMessageType().getErrorCode())
                .path(at)
                .schemaPath(this.schemaPath)
                .arguments(arguments)
                .messageKey(messageKey)
                .messageFormatter(args -> this.messageSource.getMessage(messageKey, locale, args))
                .type(getValidatorType().getValue())
                .message(messagePattern)
                .build();
        if (this.failFast && isApplicator()) {
            throw new JsonSchemaException(message);
        }
        return message;
    }

    protected ValidatorTypeCode getValidatorType() {
        return this.validatorType;
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
        return this.parentSchema.schemaPath.contains("/" + ValidatorTypeCode.ANY_OF.getValue() + "/");
    }

    private boolean isPartOfIfMultipleType() {
        return this.parentSchema.schemaPath.contains("/" + ValidatorTypeCode.IF_THEN_ELSE.getValue() + "/");
    }

    private boolean isPartOfNotMultipleType() {
        return this.parentSchema.schemaPath.contains("/" + ValidatorTypeCode.NOT.getValue() + "/");
    }

    /* ********************** START OF OpenAPI 3.0.x DISCRIMINATOR METHODS ********************************* */

    protected boolean isPartOfOneOfMultipleType() {
        return this.parentSchema.schemaPath.contains("/" + ValidatorTypeCode.ONE_OF.getValue() + "/");
    }

    protected void parseErrorCode(String errorCodeKey) {
        JsonNode errorCodeNode = this.parentSchema.getSchemaNode().get(errorCodeKey);
        if (errorCodeNode != null && errorCodeNode.isTextual()) {
            String errorCodeText = errorCodeNode.asText();
            if (StringUtils.isNotBlank(errorCodeText)) {
                this.errorMessageType = CustomErrorMessageType.of(errorCodeText);
            }
        }
    }

    protected void updateValidatorType(ValidatorTypeCode validatorTypeCode) {
        this.validatorType = validatorTypeCode;
        this.errorMessageType = validatorTypeCode;
        parseErrorCode(validatorTypeCode.getErrorCodeKey());
    }
}
