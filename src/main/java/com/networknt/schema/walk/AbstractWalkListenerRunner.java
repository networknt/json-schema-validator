package com.networknt.schema.walk;

import java.util.List;
import java.util.Set;

import com.fasterxml.jackson.databind.JsonNode;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.ValidationMessage;

public abstract class AbstractWalkListenerRunner implements WalkListenerRunner {

	protected String getKeywordName(String keyWordPath) {
		return keyWordPath.substring(keyWordPath.lastIndexOf('/') + 1);
	}

	protected WalkEvent constructWalkEvent(String keyWordName, JsonNode node, JsonNode rootNode, String at,
			String schemaPath, JsonNode schemaNode, JsonSchema parentSchema,
			JsonSchemaFactory currentJsonSchemaFactory) {
		return WalkEvent.builder().at(at).keyWordName(keyWordName).node(node).parentSchema(parentSchema)
				.rootNode(rootNode).schemaNode(schemaNode).schemaPath(schemaPath)
				.currentJsonSchemaFactory(currentJsonSchemaFactory).build();
	}

	protected boolean runPreWalkListeners(List<JsonSchemaWalkListener> walkListeners, WalkEvent walkEvent) {
		boolean continueRunningListenersAndWalk = true;
		if (walkListeners != null) {
			for (JsonSchemaWalkListener walkListener : walkListeners) {
				if (WalkFlow.SKIP.equals(walkListener.onWalkStart(walkEvent))) {
					continueRunningListenersAndWalk = false;
					break;
				}
			}
		}
		return continueRunningListenersAndWalk;
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
