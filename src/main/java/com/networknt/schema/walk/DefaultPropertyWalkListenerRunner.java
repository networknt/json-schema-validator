package com.networknt.schema.walk;

import java.util.List;
import java.util.Set;

import com.fasterxml.jackson.databind.JsonNode;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.ValidationMessage;

public class DefaultPropertyWalkListenerRunner extends AbstractWalkListenerRunner {

	private List<JsonSchemaWalkListener> propertyWalkListeners;

	public DefaultPropertyWalkListenerRunner(List<JsonSchemaWalkListener> propertyWalkListeners) {
		this.propertyWalkListeners = propertyWalkListeners;
	}

	@Override
	public boolean runPreWalkListeners(String keyWordPath, JsonNode node, JsonNode rootNode, String at,
			String schemaPath, JsonNode schemaNode, JsonSchema parentSchema,
			JsonSchemaFactory currentJsonSchemaFactory) {
		WalkEvent walkEvent = constructWalkEvent(keyWordPath, node, rootNode, at, schemaPath, schemaNode, parentSchema,
				currentJsonSchemaFactory);
		return runPreWalkListeners(propertyWalkListeners, walkEvent);
	}

	@Override
	public void runPostWalkListeners(String keyWordPath, JsonNode node, JsonNode rootNode, String at, String schemaPath,
			JsonNode schemaNode, JsonSchema parentSchema, JsonSchemaFactory currentJsonSchemaFactory,
			Set<ValidationMessage> validationMessages) {
		WalkEvent walkEvent = constructWalkEvent(keyWordPath, node, rootNode, at, schemaPath, schemaNode, parentSchema,
				currentJsonSchemaFactory);
		runPostWalkListeners(propertyWalkListeners, walkEvent, validationMessages);

	}

}
