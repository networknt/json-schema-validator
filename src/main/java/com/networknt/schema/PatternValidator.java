package com.networknt.schema;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class PatternValidator extends BaseJsonValidator implements JsonValidator {
    private static final Logger logger = LoggerFactory.getLogger(PatternValidator.class);

    private String pattern;
    private Pattern p;

    public PatternValidator(String schemaPath, JsonNode schemaNode, JsonSchema parentSchema, ObjectMapper mapper) {

        super(schemaPath, schemaNode, parentSchema, ValidatorTypeCode.PATTERN);
        pattern = "";
        if (schemaNode != null && schemaNode.isTextual()) {
            pattern = schemaNode.textValue();
            p = Pattern.compile(pattern);
        }

        parseErrorCode(getValidatorType().getErrorCodeKey());
    }

    public Set<ValidationMessage> validate(JsonNode node, JsonNode rootNode, String at) {
        debug(logger, node, rootNode, at);

        Set<ValidationMessage> errors = new HashSet<ValidationMessage>();

        JsonType nodeType = TypeFactory.getValueNodeType(node);
        if (nodeType != JsonType.STRING && nodeType != JsonType.NUMBER && nodeType != JsonType.INTEGER) {
            return errors;
        }

        if (p != null) {
            try {
                Matcher m = p.matcher(node.asText());
                if (!m.matches()) {
                    errors.add(buildValidationMessage(at, pattern));
                }
            } catch (PatternSyntaxException pse) {
                logger.error("Failed to apply pattern on " + at + ": Invalid syntax [" + pattern + "]", pse);
            }
        }

        return errors;
    }

}
