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

    protected String getKeywordName(String keyWordPath) {
        return keyWordPath.substring(keyWordPath.lastIndexOf('/') + 1);
    }

    @Override
    public boolean runPreWalkListeners(ExecutionContext executionContext, String keyWordPath, JsonNode node, JsonNode rootNode,
            JsonNodePath at, JsonNodePath schemaPath, JsonNode schemaNode,
                                       JsonSchema parentSchema,
                                       ValidationContext validationContext, JsonSchemaFactory currentJsonSchemaFactory) {
        String keyword = getKeywordName(keyWordPath);
        boolean continueRunningListenersAndWalk = true;
        WalkEvent keywordWalkEvent = constructWalkEvent(executionContext, keyword, node, rootNode, at, schemaPath,
                schemaNode, parentSchema, validationContext, currentJsonSchemaFactory);
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
    public void runPostWalkListeners(ExecutionContext executionContext, String keyWordPath, JsonNode node, JsonNode rootNode, JsonNodePath at,
            JsonNodePath schemaPath, JsonNode schemaNode,
                                     JsonSchema parentSchema,
                                     ValidationContext validationContext,
                                     JsonSchemaFactory currentJsonSchemaFactory, Set<ValidationMessage> validationMessages) {
        String keyword = getKeywordName(keyWordPath);
        WalkEvent keywordWalkEvent = constructWalkEvent(executionContext, keyword, node, rootNode, at, schemaPath,
                schemaNode, parentSchema, validationContext, currentJsonSchemaFactory);
        // Run Listeners that are setup only for this keyword.
        List<JsonSchemaWalkListener> currentKeywordListeners = keywordWalkListenersMap.get(keyword);
        runPostWalkListeners(currentKeywordListeners, keywordWalkEvent, validationMessages);
        // Run Listeners that are setup for all keywords.
        List<JsonSchemaWalkListener> allKeywordListeners = keywordWalkListenersMap
                .get(SchemaValidatorsConfig.ALL_KEYWORD_WALK_LISTENER_KEY);
        runPostWalkListeners(allKeywordListeners, keywordWalkEvent, validationMessages);
    }

}
