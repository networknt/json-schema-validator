package com.networknt.schema;

import com.fasterxml.jackson.databind.JsonNode;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;

import java.util.Set;

public abstract class BaseJsonValidator implements JsonValidator {
    private String schemaPath;
    private JsonNode schemaNode;
    private JsonSchema parentSchema;
    private ValidatorTypeCode validatorType;
    private String errorCode;

    public BaseJsonValidator(String schemaPath, JsonNode schemaNode, JsonSchema parentSchema,
                             ValidatorTypeCode validatorType) {
        this.schemaPath = schemaPath;
        this.schemaNode = schemaNode;
        this.parentSchema = parentSchema;
        this.validatorType = validatorType;
    }

    protected String getSchemaPath() {
        return schemaPath;
    }

    protected JsonNode getSchemaNode() {
        return schemaNode;
    }

    protected JsonSchema getParentSchema() {
        return parentSchema;
    }

    public Set<ValidationMessage> validate(JsonNode node) {
        return validate(node, node, AT_ROOT);
    }

    protected boolean equals(double n1, double n2) {
        return Math.abs(n1 - n2) < 1e-12;
    }

    protected boolean greaterThan(double n1, double n2) {
        return n1 - n2 > 1e-12;
    }

    protected boolean lessThan(double n1, double n2) {
        return n1 - n2 < -1e-12;
    }

    protected void parseErrorCode(String errorCodeKey) {
        JsonNode errorCodeNode = getParentSchema().getSchemaNode().get(errorCodeKey);
        if (errorCodeNode != null && errorCodeNode.isTextual()) {
            errorCode = errorCodeNode.asText();
        }
    }

    private String getErrorCode() {
        return errorCode;
    }

    private boolean isUsingCustomErrorCode() {
        return StringUtils.isNotBlank(errorCode);
    }

    protected ValidationMessage buildValidationMessage(String at, String... arguments) {
        ValidationMessage.Builder builder = new ValidationMessage.Builder();
        if (isUsingCustomErrorCode()) {
            builder.code(getErrorCode()).path(at).arguments(arguments).type(validatorType.getValue());
        } else {
            builder.code(validatorType.getErrorCode()).path(at).arguments(arguments)
                    .format(validatorType.getMessageFormat()).type(validatorType.getValue());
        }
        return builder.build();
    }

    protected void debug(Logger logger, JsonNode node, JsonNode rootNode, String at) {
        if (logger.isDebugEnabled()) {
            logger.debug("validate( " + node + ", " + rootNode + ", " + at + ")");
        }
    }

    protected ValidatorTypeCode getValidatorType() {
        return validatorType;
    }
}
