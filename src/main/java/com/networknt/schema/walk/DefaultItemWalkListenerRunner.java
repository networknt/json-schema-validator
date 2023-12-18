package com.networknt.schema.walk;

import com.fasterxml.jackson.databind.JsonNode;
import com.networknt.schema.ExecutionContext;
import com.networknt.schema.JsonNodePath;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.ValidationContext;
import com.networknt.schema.ValidationMessage;

import java.util.List;
import java.util.Set;

public class DefaultItemWalkListenerRunner extends AbstractWalkListenerRunner {

    private List<JsonSchemaWalkListener> itemWalkListeners;

    public DefaultItemWalkListenerRunner(List<JsonSchemaWalkListener> itemWalkListeners) {
        this.itemWalkListeners = itemWalkListeners;
    }

    @Override
    public boolean runPreWalkListeners(ExecutionContext executionContext, String keyWordPath, JsonNode node, JsonNode rootNode,
            JsonNodePath at, JsonNodePath evaluationPath, JsonNodePath schemaLocation, JsonNode schemaNode,
                                       JsonSchema parentSchema, ValidationContext validationContext, JsonSchemaFactory currentJsonSchemaFactory) {
        WalkEvent walkEvent = constructWalkEvent(executionContext, keyWordPath, node, rootNode, at, evaluationPath, schemaLocation, schemaNode,
                parentSchema, validationContext, currentJsonSchemaFactory);
        return runPreWalkListeners(itemWalkListeners, walkEvent);
    }

    @Override
    public void runPostWalkListeners(ExecutionContext executionContext, String keyWordPath, JsonNode node, JsonNode rootNode, JsonNodePath at,
            JsonNodePath evaluationPath, JsonNodePath schemaLocation, JsonNode schemaNode, JsonSchema parentSchema,
                                     ValidationContext validationContext, JsonSchemaFactory currentJsonSchemaFactory, Set<ValidationMessage> validationMessages) {
        WalkEvent walkEvent = constructWalkEvent(executionContext, keyWordPath, node, rootNode, at, evaluationPath, schemaLocation,
                schemaNode, parentSchema, validationContext, currentJsonSchemaFactory);
        runPostWalkListeners(itemWalkListeners, walkEvent, validationMessages);
    }

}