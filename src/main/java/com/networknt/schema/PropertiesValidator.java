package com.networknt.schema;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class PropertiesValidator extends BaseJsonValidator implements JsonValidator {
    public static final String PROPERTY = "properties";
    private static final Logger logger = LoggerFactory.getLogger(PropertiesValidator.class);
    private Map<String, JsonSchema> schemas;

    public PropertiesValidator(String schemaPath, JsonNode schemaNode, JsonSchema parentSchema, ObjectMapper mapper) {
        super(schemaPath, schemaNode, parentSchema, ValidatorTypeCode.PROPERTIES);
        schemas = new HashMap<String, JsonSchema>();
        for (Iterator<String> it = schemaNode.fieldNames(); it.hasNext(); ) {
            String pname = it.next();
            schemas.put(pname, new JsonSchema(mapper, schemaPath + "/" + pname, schemaNode.get(pname), parentSchema));
        }
    }

    public Set<ValidationMessage> validate(JsonNode node, JsonNode rootNode, String at) {
        debug(logger, node, rootNode, at);

        Set<ValidationMessage> errors = new HashSet<ValidationMessage>();

        for (String key : schemas.keySet()) {
            JsonSchema propertySchema = schemas.get(key);
            JsonNode propertyNode = node.get(key);

            if (propertyNode != null) {
                errors.addAll(propertySchema.validate(propertyNode, rootNode, at + "." + key));
            }
        }

        return errors;
    }

}
