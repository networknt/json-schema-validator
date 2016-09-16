package com.networknt.schema;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Set;

public class MultipleOfValidator extends BaseJsonValidator implements JsonValidator {
    private static final Logger logger = LoggerFactory.getLogger(MultipleOfValidator.class);

    private double divisor = 0;

    public MultipleOfValidator(String schemaPath, JsonNode schemaNode, JsonSchema parentSchema, ObjectMapper mapper) {

        super(schemaPath, schemaNode, parentSchema, ValidatorTypeCode.MULTIPLE_OF);
        if (schemaNode.isNumber()) {
            divisor = schemaNode.doubleValue();
        }

        parseErrorCode(getValidatorType().getErrorCodeKey());
    }

    public Set<ValidationMessage> validate(JsonNode node, JsonNode rootNode, String at) {
        debug(logger, node, rootNode, at);

        Set<ValidationMessage> errors = new HashSet<ValidationMessage>();

        if (node.isNumber()) {
            double nodeValue = node.doubleValue();
            if (divisor != 0) {
                long multiples = Math.round(nodeValue / divisor);
                if (Math.abs(multiples * divisor - nodeValue) > 1e-12) {
                    errors.add(buildValidationMessage(at, "" + divisor));
                }
            }
        }

        return errors;
    }

}
