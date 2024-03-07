package com.networknt.schema.walk;

import com.fasterxml.jackson.databind.JsonNode;
import com.networknt.schema.ExecutionContext;
import com.networknt.schema.JsonNodePath;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.ValidationMessage;

import java.util.List;
import java.util.Set;

public class DefaultItemWalkListenerRunner extends AbstractWalkListenerRunner {

    private List<JsonSchemaWalkListener> itemWalkListeners;

    public DefaultItemWalkListenerRunner(List<JsonSchemaWalkListener> itemWalkListeners) {
        this.itemWalkListeners = itemWalkListeners;
    }

    @Override
    public boolean runPreWalkListeners(ExecutionContext executionContext, String keyword, JsonNode instanceNode,
            JsonNode rootNode, JsonNodePath instanceLocation, JsonSchema schema) {
        WalkEvent walkEvent = constructWalkEvent(executionContext, keyword, instanceNode, rootNode, instanceLocation,
                schema);
        return runPreWalkListeners(itemWalkListeners, walkEvent);
    }

    @Override
    public void runPostWalkListeners(ExecutionContext executionContext, String keyword, JsonNode instanceNode,
            JsonNode rootNode, JsonNodePath instanceLocation, JsonSchema schema, Set<ValidationMessage> validationMessages) {
        WalkEvent walkEvent = constructWalkEvent(executionContext, keyword, instanceNode, rootNode, instanceLocation,
                schema);
        runPostWalkListeners(itemWalkListeners, walkEvent, validationMessages);
    }

}