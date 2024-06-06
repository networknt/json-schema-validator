package com.networknt.schema;

import java.util.Collections;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * {@link JsonValidator} for writeOnly.
 */
public class WriteOnlyValidator extends BaseJsonValidator {
    private static final Logger logger = LoggerFactory.getLogger(WriteOnlyValidator.class);

    private final boolean writeOnly;

    public WriteOnlyValidator(SchemaLocation schemaLocation, JsonNodePath evaluationPath, JsonNode schemaNode, JsonSchema parentSchema, ValidationContext validationContext) {
        super(schemaLocation, evaluationPath, schemaNode, parentSchema, ValidatorTypeCode.WRITE_ONLY, validationContext);

        this.writeOnly = validationContext.getConfig().isWriteOnly();
        logger.debug("Loaded WriteOnlyValidator for property {} as {}", parentSchema, "write mode");
    }

    @Override
    public Set<ValidationMessage> validate(ExecutionContext executionContext, JsonNode node, JsonNode rootNode, JsonNodePath instanceLocation) {
        debug(logger, executionContext, node, rootNode, instanceLocation);
        if (this.writeOnly) {
            return Collections.singleton(message().instanceNode(node).instanceLocation(instanceLocation)
                    .locale(executionContext.getExecutionConfig().getLocale())
                    .failFast(executionContext.isFailFast()).build());
        } 
        return Collections.emptySet();
    }

}
