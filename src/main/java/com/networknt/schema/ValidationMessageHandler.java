package com.networknt.schema;

import com.fasterxml.jackson.databind.JsonNode;
import com.networknt.schema.i18n.MessageSource;
import com.networknt.schema.utils.StringUtils;

import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public abstract class ValidationMessageHandler {
    protected final boolean failFast;
    protected final Map<String, String> customMessage;
    protected final MessageSource messageSource;
    protected ValidatorTypeCode validatorType;
    protected ErrorMessageType errorMessageType;

    protected JsonNodePath schemaPath;
    protected JsonNodePath evaluationPath;

    protected JsonSchema parentSchema;

    protected ValidationMessageHandler(boolean failFast, ErrorMessageType errorMessageType, Map<String, String> customMessage, MessageSource messageSource, ValidatorTypeCode validatorType, JsonSchema parentSchema, JsonNodePath schemaPath, JsonNodePath evaluationPath) {
        this.failFast = failFast;
        this.errorMessageType = errorMessageType;
        this.customMessage = customMessage;
        this.messageSource = messageSource;
        this.validatorType = validatorType;
        this.schemaPath = Objects.requireNonNull(schemaPath);
        this.evaluationPath = Objects.requireNonNull(evaluationPath);
        this.parentSchema = parentSchema;
    }

    protected ValidationMessage buildValidationMessage(String propertyName, JsonNodePath instanceLocation,
            Locale locale, Object... arguments) {
        return buildValidationMessage(propertyName, instanceLocation, getErrorMessageType().getErrorCodeValue(), locale,
                arguments);
    }

    protected ValidationMessage buildValidationMessage(String propertyName, JsonNodePath instanceLocation,
            String messageKey, Locale locale, Object... arguments) {
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
                .instanceLocation(Objects.requireNonNull(instanceLocation))
                .schemaLocation(this.schemaPath)
                .evaluationPath(this.evaluationPath)
                .property(propertyName)
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
        return schemaPathContains(ValidatorTypeCode.ANY_OF.getValue());
    }

    private boolean isPartOfIfMultipleType() {
        return schemaPathContains(ValidatorTypeCode.IF_THEN_ELSE.getValue());
    }

    private boolean isPartOfNotMultipleType() {
        return schemaPathContains(ValidatorTypeCode.NOT.getValue());
    }
    
    protected boolean schemaPathContains(String match) {
        int count = this.parentSchema.schemaPath.getNameCount();
        for (int x = 0; x < count; x++) {
            String name = this.parentSchema.schemaPath.getName(x);
            if (match.equals(name)) {
                return true;
            }
        }
        return false;
    }

    /* ********************** START OF OpenAPI 3.0.x DISCRIMINATOR METHODS ********************************* */

    protected boolean isPartOfOneOfMultipleType() {
        return schemaPathContains(ValidatorTypeCode.ONE_OF.getValue());
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
