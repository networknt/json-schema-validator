package com.networknt.schema.walk;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import com.networknt.schema.Error;
import com.networknt.schema.ExecutionContext;
import com.networknt.schema.JsonNodePath;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonValidator;
import com.networknt.schema.SchemaValidatorsConfig;

public class DefaultKeywordWalkListenerRunner extends AbstractWalkListenerRunner {

    private final Map<String, List<JsonSchemaWalkListener>> keywordWalkListenersMap;

    public DefaultKeywordWalkListenerRunner(Map<String, List<JsonSchemaWalkListener>> keywordWalkListenersMap) {
        this.keywordWalkListenersMap = keywordWalkListenersMap;
    }

    @Override
    public boolean runPreWalkListeners(ExecutionContext executionContext, String keyword, JsonNode instanceNode, JsonNode rootNode,
            JsonNodePath instanceLocation, JsonSchema schema, JsonValidator validator) {
        boolean continueRunningListenersAndWalk = true;
        WalkEvent keywordWalkEvent = constructWalkEvent(executionContext, keyword, instanceNode, rootNode, instanceLocation, schema, validator);
        // Run Listeners that are setup only for this keyword.
        List<JsonSchemaWalkListener> currentKeywordListeners = keywordWalkListenersMap.get(keyword);
        continueRunningListenersAndWalk = runPreWalkListeners(currentKeywordListeners, keywordWalkEvent);
        if (continueRunningListenersAndWalk) {
            // Run Listeners that are setup for all keywords.
            List<JsonSchemaWalkListener> allKeywordListeners = keywordWalkListenersMap
                    .get(SchemaValidatorsConfig.ALL_KEYWORD_WALK_LISTENER_KEY);
            runPreWalkListeners(allKeywordListeners, keywordWalkEvent);
        }
        return continueRunningListenersAndWalk;
    }

    @Override
    public void runPostWalkListeners(ExecutionContext executionContext, String keyword, JsonNode instanceNode,
            JsonNode rootNode, JsonNodePath instanceLocation, JsonSchema schema, JsonValidator validator,
            List<Error> errors) {
        WalkEvent keywordWalkEvent = constructWalkEvent(executionContext, keyword, instanceNode, rootNode, instanceLocation, schema, validator);
        // Run Listeners that are setup only for this keyword.
        List<JsonSchemaWalkListener> currentKeywordListeners = keywordWalkListenersMap.get(keyword);
        runPostWalkListeners(currentKeywordListeners, keywordWalkEvent, errors);
        // Run Listeners that are setup for all keywords.
        List<JsonSchemaWalkListener> allKeywordListeners = keywordWalkListenersMap
                .get(SchemaValidatorsConfig.ALL_KEYWORD_WALK_LISTENER_KEY);
        runPostWalkListeners(allKeywordListeners, keywordWalkEvent, errors);
    }
}
