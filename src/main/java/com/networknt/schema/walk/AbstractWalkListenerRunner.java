package com.networknt.schema.walk;

import com.fasterxml.jackson.databind.JsonNode;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.ValidationMessage;

import java.util.List;
import java.util.Set;

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
