package com.networknt.schema;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Set;

public class NotValidator extends BaseJsonValidator implements JsonValidator {
    private static final Logger logger = LoggerFactory.getLogger(RequiredValidator.class);

    private JsonSchema schema;

    public NotValidator(String schemaPath, JsonNode schemaNode, JsonSchema parentSchema, ObjectMapper mapper) {
        super(schemaPath, schemaNode, parentSchema, ValidatorTypeCode.NOT);
        schema = new JsonSchema(mapper, getValidatorType().getValue(), schemaNode, parentSchema);

        parseErrorCode(getValidatorType().getErrorCodeKey());
    }

    public Set<ValidationMessage> validate(JsonNode node, JsonNode rootNode, String at) {
        debug(logger, node, rootNode, at);

        Set<ValidationMessage> notValidationError = new HashSet<ValidationMessage>();
        Set<ValidationMessage> errors = schema.validate(node, rootNode, at);
        if (errors.isEmpty()) {
            notValidationError.add(buildValidationMessage(at, schema.toString()));
        }
        return notValidationError;
    }

}
