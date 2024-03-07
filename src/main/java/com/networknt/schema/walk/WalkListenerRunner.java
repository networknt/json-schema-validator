package com.networknt.schema.walk;

import com.fasterxml.jackson.databind.JsonNode;
import com.networknt.schema.ExecutionContext;
import com.networknt.schema.JsonNodePath;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.ValidationMessage;

import java.util.Set;

public interface WalkListenerRunner {

    public boolean runPreWalkListeners(ExecutionContext executionContext, String keyword, JsonNode instanceNode,
            JsonNode rootNode, JsonNodePath instanceLocation, JsonSchema schema);

    public void runPostWalkListeners(ExecutionContext executionContext, String keyword, JsonNode instanceNode,
            JsonNode rootNode, JsonNodePath instanceLocation, JsonSchema schema, Set<ValidationMessage> validationMessages);

}
