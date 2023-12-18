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

public class DefaultPropertyWalkListenerRunner extends AbstractWalkListenerRunner {

    private List<JsonSchemaWalkListener> propertyWalkListeners;

    public DefaultPropertyWalkListenerRunner(List<JsonSchemaWalkListener> propertyWalkListeners) {
        this.propertyWalkListeners = propertyWalkListeners;
    }

    @Override
    public boolean runPreWalkListeners(ExecutionContext executionContext, String keyWordPath, JsonNode node, JsonNode rootNode,
            JsonNodePath at, JsonNodePath evaluationPath, JsonNodePath schemaPath, JsonNode schemaNode,
                                       JsonSchema parentSchema, ValidationContext validationContext, JsonSchemaFactory currentJsonSchemaFactory) {
        WalkEvent walkEvent = constructWalkEvent(executionContext, keyWordPath, node, rootNode, at, evaluationPath, schemaPath,
                schemaNode, parentSchema, validationContext, currentJsonSchemaFactory);
        return runPreWalkListeners(propertyWalkListeners, walkEvent);
    }

    @Override
    public void runPostWalkListeners(ExecutionContext executionContext, String keyWordPath, JsonNode node, JsonNode rootNode, JsonNodePath at,
            JsonNodePath evaluationPath, JsonNodePath schemaPath, JsonNode schemaNode, JsonSchema parentSchema,
                                     ValidationContext validationContext, JsonSchemaFactory currentJsonSchemaFactory, Set<ValidationMessage> validationMessages) {
        WalkEvent walkEvent = constructWalkEvent(executionContext, keyWordPath, node, rootNode, at, evaluationPath, schemaPath,
                schemaNode, parentSchema, validationContext, currentJsonSchemaFactory);
        runPostWalkListeners(propertyWalkListeners, walkEvent, validationMessages);

    }

}
