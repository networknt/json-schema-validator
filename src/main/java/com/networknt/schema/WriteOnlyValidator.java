package com.networknt.schema;

import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;

public class WriteOnlyValidator extends BaseJsonValidator {
    private static final Logger logger = LoggerFactory.getLogger(WriteOnlyValidator.class);

    private final boolean writeOnly;

    public WriteOnlyValidator(String schemaPath, JsonNode schemaNode, JsonSchema parentSchema, ValidationContext validationContext) {
        super(schemaPath, schemaNode, parentSchema, ValidatorTypeCode.WRITE_ONLY, validationContext);

        this.writeOnly = validationContext.getConfig().isWriteOnly();
        logger.debug("Loaded WriteOnlyValidator for property {} as {}", parentSchema, "write mode");
        parseErrorCode(getValidatorType().getErrorCodeKey());
    }

    @Override
    public Set<ValidationMessage> validate(JsonNode node, JsonNode rootNode, String at) {
        debug(logger, node, rootNode, at);
        Set<ValidationMessage> errors= new HashSet<>();
        if (this.writeOnly) {
            errors.add(buildValidationMessage(at));
        } 
        return errors;
    }

}
