package com.networknt.schema;

import java.util.Collections;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;

public class WriteOnlyValidator extends BaseJsonValidator {
    private static final Logger logger = LoggerFactory.getLogger(WriteOnlyValidator.class);

    private final boolean writeOnly;

    public WriteOnlyValidator(JsonNodePath schemaLocation, JsonNodePath evaluationPath, JsonNode schemaNode, JsonSchema parentSchema, ValidationContext validationContext) {
        super(schemaLocation, evaluationPath, schemaNode, parentSchema, ValidatorTypeCode.WRITE_ONLY, validationContext);

        this.writeOnly = validationContext.getConfig().isWriteOnly();
        logger.debug("Loaded WriteOnlyValidator for property {} as {}", parentSchema, "write mode");
        parseErrorCode(getValidatorType().getErrorCodeKey());
    }

    @Override
    public Set<ValidationMessage> validate(ExecutionContext executionContext, JsonNode node, JsonNode rootNode, JsonNodePath instanceLocation) {
        debug(logger, node, rootNode, instanceLocation);
        if (this.writeOnly) {
            return Collections.singleton(message().instanceLocation(instanceLocation)
                    .locale(executionContext.getExecutionConfig().getLocale()).build());
        } 
        return Collections.emptySet();
    }

}
