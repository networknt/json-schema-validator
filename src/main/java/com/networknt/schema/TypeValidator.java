package com.networknt.schema;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Set;

public class TypeValidator extends BaseJsonValidator implements JsonValidator {
    private static final Logger logger = LoggerFactory.getLogger(TypeValidator.class);

    private JsonType schemaType;
    private UnionTypeValidator unionTypeValidator;

    public TypeValidator(String schemaPath, JsonNode schemaNode, JsonSchema parentSchema, ObjectMapper mapper) {
        super(schemaPath, schemaNode, parentSchema, ValidatorTypeCode.TYPE);
        schemaType = TypeFactory.getSchemaNodeType(schemaNode);

        if (schemaType == JsonType.UNION) {
            unionTypeValidator = new UnionTypeValidator(schemaPath, schemaNode, parentSchema, mapper);
        }

        parseErrorCode(getValidatorType().getErrorCodeKey());
    }

    public Set<ValidationMessage> validate(JsonNode node, JsonNode rootNode, String at) {
        debug(logger, node, rootNode, at);

        Set<ValidationMessage> errors = new HashSet<ValidationMessage>();

        if (schemaType == JsonType.UNION) {
            errors.addAll(unionTypeValidator.validate(node, rootNode, at));
            return errors;
        }

        JsonType nodeType = TypeFactory.getValueNodeType(node);
        if (nodeType != schemaType) {
            if (schemaType == JsonType.ANY) {
                return errors;
            }

            if (schemaType == JsonType.NUMBER && nodeType == JsonType.INTEGER) {
                return errors;
            }

            errors.add(buildValidationMessage(at, nodeType.toString(), schemaType.toString()));
        }

        return errors;
    }

}
