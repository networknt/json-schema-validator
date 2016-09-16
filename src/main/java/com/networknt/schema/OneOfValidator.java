package com.networknt.schema;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class OneOfValidator extends BaseJsonValidator implements JsonValidator {
    private static final Logger logger = LoggerFactory.getLogger(RequiredValidator.class);

    private List<JsonSchema> schemas = new ArrayList<JsonSchema>();

    public OneOfValidator(String schemaPath, JsonNode schemaNode, JsonSchema parentSchema, ObjectMapper mapper) {
        super(schemaPath, schemaNode, parentSchema, ValidatorTypeCode.ONE_OF);
        int size = schemaNode.size();
        for (int i = 0; i < size; i++) {
            schemas.add(new JsonSchema(mapper, getValidatorType().getValue(), schemaNode.get(i), parentSchema));
        }

        parseErrorCode(getValidatorType().getErrorCodeKey());
    }

    public Set<ValidationMessage> validate(JsonNode node, JsonNode rootNode, String at) {
        debug(logger, node, rootNode, at);

        int numberOfValidSchema = 0;

        for (JsonSchema schema : schemas) {
            Set<ValidationMessage> errors = schema.validate(node, rootNode, at);
            if (errors == null || errors.isEmpty()) {
                numberOfValidSchema++;
            }
            if (numberOfValidSchema > 1) {
                break;
            }
        }

        Set<ValidationMessage> errors = new HashSet<ValidationMessage>();
        if (numberOfValidSchema != 1) {
            errors.add(buildValidationMessage(at, ""));
        }
        return errors;
    }

}
