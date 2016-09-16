package com.networknt.schema;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ItemsValidator extends BaseJsonValidator implements JsonValidator {
    private static final Logger logger = LoggerFactory.getLogger(ItemsValidator.class);
    private static final String PROPERTY_ADDITIONAL_ITEMS = "additionalItems";

    private JsonSchema schema;
    private List<JsonSchema> tupleSchema;
    private boolean additionalItems = true;
    private JsonSchema additionalSchema;

    public ItemsValidator(String schemaPath, JsonNode schemaNode, JsonSchema parentSchema, ObjectMapper mapper) {
        super(schemaPath, schemaNode, parentSchema, ValidatorTypeCode.ITEMS);
        if (schemaNode.isObject()) {
            schema = new JsonSchema(mapper, getValidatorType().getValue(), schemaNode, parentSchema);
        } else {
            tupleSchema = new ArrayList<JsonSchema>();
            for (JsonNode s : schemaNode) {
                tupleSchema.add(new JsonSchema(mapper, getValidatorType().getValue(), s, parentSchema));
            }

            JsonNode addItemNode = getParentSchema().getSchemaNode().get(PROPERTY_ADDITIONAL_ITEMS);
            if (addItemNode != null) {
                if (addItemNode.isBoolean()) {
                    additionalItems = addItemNode.asBoolean();
                } else if (addItemNode.isObject()) {
                    additionalSchema = new JsonSchema(mapper, addItemNode);
                }
            }
        }

        parseErrorCode(getValidatorType().getErrorCodeKey());
    }

    public Set<ValidationMessage> validate(JsonNode node, JsonNode rootNode, String at) {
        debug(logger, node, rootNode, at);

        Set<ValidationMessage> errors = new HashSet<ValidationMessage>();

        if (!node.isArray()) {
            // ignores non-arrays
            return errors;
        }

        int i = 0;
        for (JsonNode n : node) {
            if (schema != null) {
                // validate with item schema (the whole array has the same item
                // schema)
                errors.addAll(schema.validate(n, rootNode, at + "[" + i + "]"));
            }

            if (tupleSchema != null) {
                if (i < tupleSchema.size()) {
                    // validate against tuple schema
                    errors.addAll(tupleSchema.get(i).validate(n, rootNode, at + "[" + i + "]"));
                } else {
                    if (additionalSchema != null) {
                        // validate against additional item schema
                        errors.addAll(additionalSchema.validate(n, rootNode, at + "[" + i + "]"));
                    } else if (!additionalItems) {
                        // no additional item allowed, return error
                        errors.add(buildValidationMessage(at, "" + i));
                    }
                }
            }

            i++;
        }
        return errors;
    }

}
