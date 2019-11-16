package com.networknt.schema;

import com.fasterxml.jackson.databind.JsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

public class ConstValidator extends BaseJsonValidator implements JsonValidator {
    private static final Logger logger = LoggerFactory.getLogger(ConstValidator.class);
    JsonNode schemaNode;

    public ConstValidator(String schemaPath, JsonNode schemaNode, JsonSchema parentSchema, ValidationContext validationContext) {
        super(schemaPath, schemaNode, parentSchema, ValidatorTypeCode.CONST, validationContext);
        this.schemaNode = schemaNode;
    }

    public Set<ValidationMessage> validate(JsonNode node, JsonNode rootNode, String at) {
        debug(logger, node, rootNode, at);

        Set<ValidationMessage> errors = new LinkedHashSet<ValidationMessage>();
        if(schemaNode.isNumber() && node.isNumber()) {
            if(schemaNode.decimalValue().compareTo(node.decimalValue()) != 0) {
                errors.add(buildValidationMessage(at, schemaNode.asText()));
            }
        } else if (!schemaNode.equals(node)) {
            errors.add(buildValidationMessage(at, schemaNode.asText()));
        }
        return Collections.unmodifiableSet(errors);
    }
}
