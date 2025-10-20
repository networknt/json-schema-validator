package com.networknt.schema.walk;

import com.fasterxml.jackson.databind.JsonNode;
import com.networknt.schema.ExecutionContext;
import com.networknt.schema.Schema;
import com.networknt.schema.keyword.KeywordValidator;
import com.networknt.schema.path.NodePath;
import com.networknt.schema.Error;

import java.util.List;

/**
 * Walk handler that is called before and after visiting.
 */
public interface WalkHandler {

    boolean preWalk(ExecutionContext executionContext, String keyword, JsonNode instanceNode, JsonNode rootNode,
            NodePath instanceLocation, Schema schema, KeywordValidator validator);

    void postWalk(ExecutionContext executionContext, String keyword, JsonNode instanceNode, JsonNode rootNode,
            NodePath instanceLocation, Schema schema, KeywordValidator validator, List<Error> errors);

}
