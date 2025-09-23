package com.networknt.schema.walk;

import com.fasterxml.jackson.databind.JsonNode;
import com.networknt.schema.ExecutionContext;
import com.networknt.schema.NodePath;
import com.networknt.schema.Schema;
import com.networknt.schema.keyword.KeywordValidator;
import com.networknt.schema.Error;

import java.util.List;

public interface WalkListenerRunner {

    boolean runPreWalkListeners(ExecutionContext executionContext, String keyword, JsonNode instanceNode,
                                JsonNode rootNode, NodePath instanceLocation, Schema schema, KeywordValidator validator);

    void runPostWalkListeners(ExecutionContext executionContext, String keyword, JsonNode instanceNode,
                              JsonNode rootNode, NodePath instanceLocation, Schema schema, KeywordValidator validator, List<Error> errors);

}
