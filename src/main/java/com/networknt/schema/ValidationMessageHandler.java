package com.networknt.schema;

import com.fasterxml.jackson.databind.JsonNode;
import com.networknt.schema.utils.StringUtils;

import java.text.MessageFormat;
import java.util.ResourceBundle;

public abstract class ValidationMessageHandler {
    protected final boolean failFast;
    protected final String customMessage;
    protected final ResourceBundle resourceBundle;
    protected ValidatorTypeCode validatorType;
    protected ErrorMessageType errorMessageType;

    protected String schemaPath;

    protected JsonSchema parentSchema;

    protected ValidationMessageHandler(boolean failFast, ErrorMessageType errorMessageType, String customMessage, ResourceBundle resourceBundle, ValidatorTypeCode validatorType, JsonSchema parentSchema, String schemaPath) {
        this.failFast = failFast;
        this.errorMessageType = errorMessageType;
        this.customMessage = customMessage;
        this.resourceBundle = resourceBundle;
        this.validatorType = validatorType;
        this.schemaPath = schemaPath;
        this.parentSchema = parentSchema;
    }


    protected ValidationMessage buildValidationMessage(String at, String... arguments) {
        MessageFormat messageFormat = new MessageFormat(this.resourceBundle.getString(getErrorMessageType().getErrorCodeValue()));
        final ValidationMessage message = ValidationMessage.ofWithCustom(getValidatorType().getValue(), getErrorMessageType(), messageFormat, this.customMessage, at, this.schemaPath, arguments);
        if (this.failFast && isApplicator()) {
            throw new JsonSchemaException(message);
        }
        return message;
    }

    protected ValidationMessage constructValidationMessage(String messageKey, String at, String... arguments) {
        MessageFormat messageFormat = new MessageFormat(this.resourceBundle.getString(messageKey));
        final ValidationMessage message = new ValidationMessage.Builder()
                .code(getErrorMessageType().getErrorCode())
                .path(at)
                .schemaPath(this.schemaPath)
                .arguments(arguments)
                .format(messageFormat)
                .type(getValidatorType().getValue())
                .customMessage(this.customMessage)
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
