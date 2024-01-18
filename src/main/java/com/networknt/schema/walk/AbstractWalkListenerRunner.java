package com.networknt.schema.walk;

import com.fasterxml.jackson.databind.JsonNode;
import com.networknt.schema.ExecutionContext;
import com.networknt.schema.JsonNodePath;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SchemaLocation;
import com.networknt.schema.ValidationContext;
import com.networknt.schema.ValidationMessage;

import java.util.List;
import java.util.Set;

public abstract class AbstractWalkListenerRunner implements WalkListenerRunner {

    protected WalkEvent constructWalkEvent(ExecutionContext executionContext, String keyword, JsonNode node,
            JsonNode rootNode, JsonNodePath instanceLocation, JsonNodePath evaluationPath, SchemaLocation schemaLocation,
            JsonNode schemaNode, JsonSchema parentSchema, ValidationContext validationContext,
            JsonSchemaFactory currentJsonSchemaFactory) {
        return WalkEvent.builder().executionContext(executionContext).instanceLocation(instanceLocation)
                .evaluationPath(evaluationPath).keyword(keyword).node(node).parentSchema(parentSchema)
                .rootNode(rootNode).schemaNode(schemaNode).schemaLocation(schemaLocation)
                .currentJsonSchemaFactory(currentJsonSchemaFactory).validationContext(validationContext).build();
    }

    protected boolean runPreWalkListeners(List<JsonSchemaWalkListener> walkListeners, WalkEvent walkEvent) {
        boolean continueToWalkMethod = true;
        if (walkListeners != null) {
            for (JsonSchemaWalkListener walkListener : walkListeners) {
                WalkFlow walkFlow = walkListener.onWalkStart(walkEvent);
                if (WalkFlow.SKIP.equals(walkFlow) || WalkFlow.ABORT.equals(walkFlow)) {
                    continueToWalkMethod = false;
                    if (WalkFlow.ABORT.equals(walkFlow)) {
                        break;
                    }
                }
            }
        }
        return continueToWalkMethod;
    }

    protected void runPostWalkListeners(List<JsonSchemaWalkListener> walkListeners, WalkEvent walkEvent,
                                        Set<ValidationMessage> validationMessages) {
        if (walkListeners != null) {
            for (JsonSchemaWalkListener walkListener : walkListeners) {
                walkListener.onWalkEnd(walkEvent, validationMessages);
            }
        }
    }
}
