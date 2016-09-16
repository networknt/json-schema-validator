package com.networknt.schema;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Set;

public class MinLengthValidator extends BaseJsonValidator implements JsonValidator {
    private static final Logger logger = LoggerFactory.getLogger(MinLengthValidator.class);

    private int minLength;

    public MinLengthValidator(String schemaPath, JsonNode schemaNode, JsonSchema parentSchema, ObjectMapper mapper) {
        super(schemaPath, schemaNode, parentSchema, ValidatorTypeCode.MIN_LENGTH);
        minLength = Integer.MIN_VALUE;
        if (schemaNode != null && schemaNode.isIntegralNumber()) {
            minLength = schemaNode.intValue();
        }

        parseErrorCode(getValidatorType().getErrorCodeKey());
    }

    public Set<ValidationMessage> validate(JsonNode node, JsonNode rootNode, String at) {
        debug(logger, node, rootNode, at);

        JsonType nodeType = TypeFactory.getValueNodeType(node);
        Set<ValidationMessage> errors = new HashSet<ValidationMessage>();
        if (nodeType != JsonType.STRING) {
            // ignore non-string types
            return errors;
        }

        if (node.textValue().length() < minLength) {
            errors.add(buildValidationMessage(at, "" + minLength));
        }

        return errors;
    }

}
