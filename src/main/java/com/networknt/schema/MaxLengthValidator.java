package com.networknt.schema;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Set;

public class MaxLengthValidator extends BaseJsonValidator implements JsonValidator {
    private static final Logger logger = LoggerFactory.getLogger(MaxLengthValidator.class);

    private int maxLength;

    public MaxLengthValidator(String schemaPath, JsonNode schemaNode, JsonSchema parentSchema, ObjectMapper mapper) {
        super(schemaPath, schemaNode, parentSchema, ValidatorTypeCode.MAX_LENGTH);
        maxLength = Integer.MAX_VALUE;
        if (schemaNode != null && schemaNode.isIntegralNumber()) {
            maxLength = schemaNode.intValue();
        }

        parseErrorCode(getValidatorType().getErrorCodeKey());
    }

    public Set<ValidationMessage> validate(JsonNode node, JsonNode rootNode, String at) {
        debug(logger, node, rootNode, at);

        JsonType nodeType = TypeFactory.getValueNodeType(node);
        Set<ValidationMessage> errors = new HashSet<ValidationMessage>();
        if (nodeType != JsonType.STRING) {
            // ignore no-string typs
            return errors;
        }

        if (node.textValue().length() > maxLength) {
            errors.add(buildValidationMessage(at, "" + maxLength));
        }

        return errors;
    }

}
