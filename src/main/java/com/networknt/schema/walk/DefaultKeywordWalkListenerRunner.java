package com.networknt.schema.walk;

import com.fasterxml.jackson.databind.JsonNode;
import com.networknt.schema.*;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class DefaultKeywordWalkListenerRunner extends AbstractWalkListenerRunner {

    private Map<String, List<JsonSchemaWalkListener>> keywordWalkListenersMap;

    public DefaultKeywordWalkListenerRunner(Map<String, List<JsonSchemaWalkListener>> keywordWalkListenersMap) {
        this.keywordWalkListenersMap = keywordWalkListenersMap;
    }

    @Override
    public boolean runPreWalkListeners(ExecutionContext executionContext, String keyword, JsonNode node, JsonNode rootNode,
            JsonNodePath instanceLocation, JsonNodePath evaluationPath, JsonNodePath schemaLocation,
                                       JsonNode schemaNode,
                                       JsonSchema parentSchema, ValidationContext validationContext, JsonSchemaFactory currentJsonSchemaFactory) {
        boolean continueRunningListenersAndWalk = true;
        WalkEvent keywordWalkEvent = constructWalkEvent(executionContext, keyword, node, rootNode, instanceLocation, evaluationPath,
                schemaLocation, schemaNode, parentSchema, validationContext, currentJsonSchemaFactory);
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
    public void runPostWalkListeners(ExecutionContext executionContext, String keyword, JsonNode node, JsonNode rootNode, JsonNodePath instanceLocation,
            JsonNodePath evaluationPath, JsonNodePath schemaLocation,
                                     JsonNode schemaNode,
                                     JsonSchema parentSchema,
                                     ValidationContext validationContext, JsonSchemaFactory currentJsonSchemaFactory, Set<ValidationMessage> validationMessages) {
        WalkEvent keywordWalkEvent = constructWalkEvent(executionContext, keyword, node, rootNode, instanceLocation, evaluationPath,
                schemaLocation, schemaNode, parentSchema, validationContext, currentJsonSchemaFactory);
        // Run Listeners that are setup only for this keyword.
        List<JsonSchemaWalkListener> currentKeywordListeners = keywordWalkListenersMap.get(keyword);
        runPostWalkListeners(currentKeywordListeners, keywordWalkEvent, validationMessages);
        // Run Listeners that are setup for all keywords.
        List<JsonSchemaWalkListener> allKeywordListeners = keywordWalkListenersMap
                .get(SchemaValidatorsConfig.ALL_KEYWORD_WALK_LISTENER_KEY);
        runPostWalkListeners(allKeywordListeners, keywordWalkEvent, validationMessages);
    }

}
