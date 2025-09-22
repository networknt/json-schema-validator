package com.networknt.schema.walk;

import com.fasterxml.jackson.databind.JsonNode;
import com.networknt.schema.ExecutionContext;
import com.networknt.schema.JsonNodePath;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.keyword.KeywordValidator;
import com.networknt.schema.Error;

import java.util.List;

public interface WalkListenerRunner {

    boolean runPreWalkListeners(ExecutionContext executionContext, String keyword, JsonNode instanceNode,
                                JsonNode rootNode, JsonNodePath instanceLocation, JsonSchema schema, KeywordValidator validator);

    void runPostWalkListeners(ExecutionContext executionContext, String keyword, JsonNode instanceNode,
                              JsonNode rootNode, JsonNodePath instanceLocation, JsonSchema schema, KeywordValidator validator, List<Error> errors);

}
