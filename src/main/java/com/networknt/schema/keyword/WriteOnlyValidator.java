package com.networknt.schema.keyword;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.networknt.schema.ExecutionContext;
import com.networknt.schema.NodePath;
import com.networknt.schema.Schema;
import com.networknt.schema.SchemaLocation;
import com.networknt.schema.SchemaContext;

/**
 * {@link KeywordValidator} for writeOnly.
 */
public class WriteOnlyValidator extends BaseKeywordValidator {
    private static final Logger logger = LoggerFactory.getLogger(WriteOnlyValidator.class);

    public WriteOnlyValidator(SchemaLocation schemaLocation, NodePath evaluationPath, JsonNode schemaNode, Schema parentSchema, SchemaContext schemaContext) {
        super(Keywords.WRITE_ONLY, schemaNode, schemaLocation, parentSchema, schemaContext, evaluationPath);
        logger.debug("Loaded WriteOnlyValidator for property {} as {}", parentSchema, "write mode");
    }

    @Override
    public void validate(ExecutionContext executionContext, JsonNode node, JsonNode rootNode, NodePath instanceLocation) {
        
        if (Boolean.TRUE.equals(executionContext.getExecutionConfig().getWriteOnly())) {
            executionContext.addError(error().instanceNode(node).instanceLocation(instanceLocation)
                    .locale(executionContext.getExecutionConfig().getLocale())
                    .build());
        } 
    }

}
