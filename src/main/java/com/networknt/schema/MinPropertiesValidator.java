package com.networknt.schema;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Set;

public class MinPropertiesValidator extends BaseJsonValidator implements JsonValidator {
    private static final Logger logger = LoggerFactory.getLogger(MinPropertiesValidator.class);

    protected int min;

    public MinPropertiesValidator(String schemaPath, JsonNode schemaNode, JsonSchema parentSchema,
                                  ObjectMapper mapper) {
        super(schemaPath, schemaNode, parentSchema, ValidatorTypeCode.MIN_PROPERTIES);
        if (schemaNode.isIntegralNumber()) {
            min = schemaNode.intValue();
        }

        parseErrorCode(getValidatorType().getErrorCodeKey());
    }

    public Set<ValidationMessage> validate(JsonNode node, JsonNode rootNode, String at) {
        debug(logger, node, rootNode, at);

        Set<ValidationMessage> errors = new HashSet<ValidationMessage>();

        if (node.isObject()) {
            if (node.size() < min) {
                errors.add(buildValidationMessage(at, "" + min));
            }
        }

        return errors;
    }

}
