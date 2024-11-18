package com.networknt.schema.walk;

import com.fasterxml.jackson.databind.JsonNode;
import com.networknt.schema.ExecutionContext;
import com.networknt.schema.JsonNodePath;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonValidator;
import com.networknt.schema.ValidationMessage;

import java.util.Set;

public interface WalkListenerRunner {

    boolean runPreWalkListeners(ExecutionContext executionContext, String keyword, JsonNode instanceNode,
                                JsonNode rootNode, JsonNodePath instanceLocation, JsonSchema schema, JsonValidator validator);

    void runPostWalkListeners(ExecutionContext executionContext, String keyword, JsonNode instanceNode,
                              JsonNode rootNode, JsonNodePath instanceLocation, JsonSchema schema, JsonValidator validator, Set<ValidationMessage> validationMessages);

}
