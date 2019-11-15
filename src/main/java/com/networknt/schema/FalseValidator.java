package com.networknt.schema;

import com.fasterxml.jackson.databind.JsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

public class FalseValidator extends BaseJsonValidator implements JsonValidator {
    private static final Logger logger = LoggerFactory.getLogger(FalseValidator.class);

    public FalseValidator(String schemaPath, final JsonNode schemaNode, JsonSchema parentSchema, ValidationContext validationContext) {
        super(schemaPath, schemaNode, parentSchema, ValidatorTypeCode.FALSE, validationContext);
    }

    public Set<ValidationMessage> validate(JsonNode node, JsonNode rootNode, String at) {
        debug(logger, node, rootNode, at);
        // For the false validator, it is always not valid
        Set<ValidationMessage> errors = new LinkedHashSet<ValidationMessage>();
        errors.add(buildValidationMessage(at));
        return Collections.unmodifiableSet(errors);
    }
}
