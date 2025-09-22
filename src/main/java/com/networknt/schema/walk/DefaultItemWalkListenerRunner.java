package com.networknt.schema.walk;

import com.fasterxml.jackson.databind.JsonNode;
import com.networknt.schema.ExecutionContext;
import com.networknt.schema.JsonNodePath;
import com.networknt.schema.Schema;
import com.networknt.schema.keyword.KeywordValidator;
import com.networknt.schema.Error;

import java.util.List;

public class DefaultItemWalkListenerRunner extends AbstractWalkListenerRunner {

    private final List<JsonSchemaWalkListener> itemWalkListeners;

    public DefaultItemWalkListenerRunner(List<JsonSchemaWalkListener> itemWalkListeners) {
        this.itemWalkListeners = itemWalkListeners;
    }

    @Override
    public boolean runPreWalkListeners(ExecutionContext executionContext, String keyword, JsonNode instanceNode,
            JsonNode rootNode, JsonNodePath instanceLocation, Schema schema, KeywordValidator validator) {
        WalkEvent walkEvent = constructWalkEvent(executionContext, keyword, instanceNode, rootNode, instanceLocation,
                schema, validator);
        return runPreWalkListeners(itemWalkListeners, walkEvent);
    }

    @Override
    public void runPostWalkListeners(ExecutionContext executionContext, String keyword, JsonNode instanceNode,
            JsonNode rootNode, JsonNodePath instanceLocation, Schema schema, KeywordValidator validator, List<Error> errors) {
        WalkEvent walkEvent = constructWalkEvent(executionContext, keyword, instanceNode, rootNode, instanceLocation,
                schema, validator);
        runPostWalkListeners(itemWalkListeners, walkEvent, errors);
    }

}