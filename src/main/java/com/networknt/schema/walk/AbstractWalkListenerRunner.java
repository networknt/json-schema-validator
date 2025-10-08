package com.networknt.schema.walk;

import com.fasterxml.jackson.databind.JsonNode;
import com.networknt.schema.ExecutionContext;
import com.networknt.schema.Schema;
import com.networknt.schema.keyword.KeywordValidator;
import com.networknt.schema.path.NodePath;
import com.networknt.schema.Error;

import java.util.List;

public abstract class AbstractWalkListenerRunner implements WalkListenerRunner {

    protected WalkEvent constructWalkEvent(ExecutionContext executionContext, String keyword, JsonNode instanceNode,
            JsonNode rootNode, NodePath instanceLocation, Schema schema, KeywordValidator validator) {
        return WalkEvent.builder().executionContext(executionContext).instanceLocation(instanceLocation)
                .keyword(keyword).instanceNode(instanceNode)
                .rootNode(rootNode).schema(schema).validator(validator).build();
    }

    protected boolean runPreWalkListeners(List<WalkListener> walkListeners, WalkEvent walkEvent) {
        boolean continueToWalkMethod = true;
        if (walkListeners != null) {
            for (WalkListener walkListener : walkListeners) {
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

    protected void runPostWalkListeners(List<WalkListener> walkListeners, WalkEvent walkEvent,
                                        List<Error> errors) {
        if (walkListeners != null) {
            for (WalkListener walkListener : walkListeners) {
                walkListener.onWalkEnd(walkEvent, errors);
            }
        }
    }
}
