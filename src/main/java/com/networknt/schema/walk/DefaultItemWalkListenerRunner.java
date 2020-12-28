package com.networknt.schema.walk;

import com.fasterxml.jackson.databind.JsonNode;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.ValidationMessage;

import java.util.List;
import java.util.Set;

public class DefaultItemWalkListenerRunner extends AbstractWalkListenerRunner {

	private List<JsonSchemaWalkListener> itemWalkListeners;

	public DefaultItemWalkListenerRunner(List<JsonSchemaWalkListener> itemWalkListeners) {
		this.itemWalkListeners = itemWalkListeners;
	}

	@Override
	public boolean runPreWalkListeners(String keyWordPath, JsonNode node, JsonNode rootNode, String at,
			String schemaPath, JsonNode schemaNode, JsonSchema parentSchema,
			JsonSchemaFactory currentJsonSchemaFactory) {
		WalkEvent walkEvent = constructWalkEvent(keyWordPath, node, rootNode, at, schemaPath, schemaNode, parentSchema,
				currentJsonSchemaFactory);
		return runPreWalkListeners(itemWalkListeners, walkEvent);
	}

	@Override
	public void runPostWalkListeners(String keyWordPath, JsonNode node, JsonNode rootNode, String at, String schemaPath,
			JsonNode schemaNode, JsonSchema parentSchema, JsonSchemaFactory currentJsonSchemaFactory,
			Set<ValidationMessage> validationMessages) {
		WalkEvent walkEvent = constructWalkEvent(keyWordPath, node, rootNode, at, schemaPath, schemaNode, parentSchema,
				currentJsonSchemaFactory);
		runPostWalkListeners(itemWalkListeners, walkEvent, validationMessages);
	}

}