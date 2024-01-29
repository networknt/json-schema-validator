package com.networknt.schema;

import com.fasterxml.jackson.databind.JsonNode;
import com.networknt.schema.i18n.MessageSource;
import com.networknt.schema.utils.StringUtils;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

public abstract class ValidationMessageHandler {
    protected final MessageSource messageSource;
    protected ErrorMessageType errorMessageType;

    protected SchemaLocation schemaLocation;
    protected JsonNodePath evaluationPath;
    protected JsonSchema evaluationParentSchema;

    protected JsonSchema parentSchema;

    protected boolean customErrorMessagesEnabled;
    protected Map<String, String> errorMessage;

    protected Keyword keyword;

    protected ValidationMessageHandler(ErrorMessageType errorMessageType, boolean customErrorMessagesEnabled,
            MessageSource messageSource, Keyword keyword, JsonSchema parentSchema, SchemaLocation schemaLocation,
            JsonNodePath evaluationPath) {
        this.errorMessageType = errorMessageType;
        this.messageSource = messageSource;
        this.schemaLocation = Objects.requireNonNull(schemaLocation);
        this.evaluationPath = Objects.requireNonNull(evaluationPath);
        this.parentSchema = parentSchema;
        this.customErrorMessagesEnabled = customErrorMessagesEnabled;
        updateKeyword(keyword);
    }

    /**
     * Copy constructor.
     *
     * @param copy to copy from
     */
    protected ValidationMessageHandler(ValidationMessageHandler copy) {
        this.messageSource = copy.messageSource;
        this.errorMessageType = copy.errorMessageType;
        this.schemaLocation = copy.schemaLocation;
        this.evaluationPath = copy.evaluationPath;
        this.parentSchema = copy.parentSchema;
        this.evaluationParentSchema = copy.evaluationParentSchema;
        this.customErrorMessagesEnabled = copy.customErrorMessagesEnabled;
        this.errorMessage = copy.errorMessage;
        this.keyword = copy.keyword;
    }

    protected MessageSourceValidationMessage.Builder message() {
        return MessageSourceValidationMessage.builder(this.messageSource, this.errorMessage, (message, failFast) -> {
            if (failFast && canFailFast()) {
                throw new JsonSchemaException(message);
            }
        }).code(getErrorMessageType().getErrorCode()).schemaLocation(this.schemaLocation)
                .evaluationPath(this.evaluationPath).type(this.keyword != null ? this.keyword.getValue() : null)
                .messageKey(getErrorMessageType().getErrorCodeValue());
    }

    protected ErrorMessageType getErrorMessageType() {
        return this.errorMessageType;
    }

    /**
     * Determines if the evaluation can fast fail.
     *
     * @return true if it can fast fail
     */
    protected boolean canFailFast() {
        return !hasApplicatorInEvaluationPath();
    }

    /**
     * Determines if there is an applicator in the evaluation path for determining
     * if it is possible to fast fail.
     * <p>
     * For instance if there is a not keyword in the evaluation path this can change
     * the overall result.
     * 
     * @return true if there is an applicator in the evaluation path
     */
    private boolean hasApplicatorInEvaluationPath() {
        return hasAnyOfInEvaluationPath() || hasIfInEvaluationPath() || hasNotInEvaluationPath()
                || hasOneOfInEvaluationPath();
    }

    /**
     * Determines if anyOf is in the evaluation path.
     * 
     * @return true if anyOf is in the evaluation path
     */
    private boolean hasAnyOfInEvaluationPath() {
        return this.evaluationPath.contains(ValidatorTypeCode.ANY_OF.getValue());
    }

    /**
     * Determines if if is in the evaluation path.
     * 
     * @return true if if is in the evaluation path
     */
    private boolean hasIfInEvaluationPath() {
        return this.evaluationPath.contains(ValidatorTypeCode.IF_THEN_ELSE.getValue());
    }

    /**
     * Determines if not is in the evaluation path.
     * 
     * @return true if not is in the evaluation path
     */
    private boolean hasNotInEvaluationPath() {
        return this.evaluationPath.contains(ValidatorTypeCode.NOT.getValue());
    }

    /**
     * Determines if oneOf is in the evaluation path.
     * 
     * @return true if oneOf is in the evaluation path
     */
    protected boolean hasOneOfInEvaluationPath() {
        return this.evaluationPath.contains(ValidatorTypeCode.ONE_OF.getValue());
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
