package com.networknt.schema;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class EnumValidator extends BaseJsonValidator implements JsonValidator {
    private static final Logger logger = LoggerFactory.getLogger(EnumValidator.class);

    private List<JsonNode> nodes;
    private String error;

    public EnumValidator(String schemaPath, JsonNode schemaNode, JsonSchema parentSchema, ObjectMapper mapper) {
        super(schemaPath, schemaNode, parentSchema, ValidatorTypeCode.ENUM);
        nodes = new ArrayList<JsonNode>();
        error = "[none]";

        if (schemaNode != null && schemaNode.isArray()) {
            error = "[";
            int i = 0;
            for (JsonNode n : schemaNode) {
                nodes.add(n);

                String v = n.asText();
                error = error + (i == 0 ? "" : ", ") + v;
                i++;

            }
            error = error + "]";
        }

        parseErrorCode(getValidatorType().getErrorCodeKey());
    }

    public Set<ValidationMessage> validate(JsonNode node, JsonNode rootNode, String at) {
        debug(logger, node, rootNode, at);

        Set<ValidationMessage> errors = new HashSet<ValidationMessage>();

        if (!nodes.contains(node)) {
            errors.add(buildValidationMessage(at, error));
        }

        return errors;
    }

}
