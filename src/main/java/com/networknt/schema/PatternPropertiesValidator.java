package com.networknt.schema;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PatternPropertiesValidator extends BaseJsonValidator implements JsonValidator {
    public static final String PROPERTY = "patternProperties";
    private static final Logger logger = LoggerFactory.getLogger(PatternPropertiesValidator.class);
    private Map<Pattern, JsonSchema> schemas = new HashMap<Pattern, JsonSchema>();

    public PatternPropertiesValidator(String schemaPath, JsonNode schemaNode, JsonSchema parentSchema,
                                      ObjectMapper mapper) {
        super(schemaPath, schemaNode, parentSchema, ValidatorTypeCode.PATTERN_PROPERTIES);
        if (!schemaNode.isObject()) {
            throw new JsonSchemaException("patternProperties must be an object node");
        }
        Iterator<String> names = schemaNode.fieldNames();
        while (names.hasNext()) {
            String name = names.next();
            schemas.put(Pattern.compile(name), new JsonSchema(mapper, name, schemaNode.get(name), parentSchema));
        }
    }

    public Set<ValidationMessage> validate(JsonNode node, JsonNode rootNode, String at) {
        debug(logger, node, rootNode, at);

        Set<ValidationMessage> errors = new HashSet<ValidationMessage>();

        if (!node.isObject()) {
            return errors;
        }

        Iterator<String> names = node.fieldNames();
        while (names.hasNext()) {
            String name = names.next();
            JsonNode n = node.get(name);
            for (Pattern pattern : schemas.keySet()) {
                Matcher m = pattern.matcher(name);
                if (m.find()) {
                    errors.addAll(schemas.get(pattern).validate(n, rootNode, at + "." + name));
                }
            }
        }
        return errors;
    }

}
