package com.networknt.schema;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Set;

public class UniqueItemsValidator extends BaseJsonValidator implements JsonValidator {
    private static final Logger logger = LoggerFactory.getLogger(UniqueItemsValidator.class);

    private boolean unique = false;

    public UniqueItemsValidator(String schemaPath, JsonNode schemaNode, JsonSchema parentSchema, ObjectMapper mapper) {
        super(schemaPath, schemaNode, parentSchema, ValidatorTypeCode.UNIQUE_ITEMS);
        if (schemaNode.isBoolean()) {
            unique = schemaNode.booleanValue();
        }

        parseErrorCode(getValidatorType().getErrorCodeKey());
    }

    public Set<ValidationMessage> validate(JsonNode node, JsonNode rootNode, String at) {
        debug(logger, node, rootNode, at);

        Set<ValidationMessage> errors = new HashSet<ValidationMessage>();

        if (unique) {
            Set<JsonNode> set = new HashSet<JsonNode>();
            for (JsonNode n : node) {
                set.add(n);
            }

            if (set.size() < node.size()) {
                errors.add(buildValidationMessage(at));
            }
        }

        return errors;
    }

}
