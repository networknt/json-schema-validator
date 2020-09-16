package com.networknt.schema.walk;

import java.util.List;
import java.util.Set;

import com.fasterxml.jackson.databind.JsonNode;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.ValidationMessage;

public class DefaultPropertyWalkListenerRunner extends AbstractWalkListenerRunner {

	private List<WalkListener> propertyWalkListeners;

	public DefaultPropertyWalkListenerRunner(List<WalkListener> propertyWalkListeners) {
		this.propertyWalkListeners = propertyWalkListeners;
	}

	@Override
	public boolean runPreWalkListeners(String keyWordPath, JsonNode node, JsonNode rootNode, String at,
			String schemaPath, JsonNode schemaNode, JsonSchema parentSchema) {
		WalkEvent walkEvent = constructWalkEvent(keyWordPath, node, rootNode, at, schemaPath, schemaNode, parentSchema);
		return runPreWalkListeners(propertyWalkListeners, walkEvent);
	}

	@Override
	public void runPostWalkListeners(String keyWordPath, JsonNode node, JsonNode rootNode, String at, String schemaPath,
			JsonNode schemaNode, JsonSchema parentSchema, Set<ValidationMessage> validationMessages) {
		WalkEvent walkEvent = constructWalkEvent(keyWordPath, node, rootNode, at, schemaPath, schemaNode, parentSchema);
		runPostWalkListeners(propertyWalkListeners, walkEvent, validationMessages);

	}

}
