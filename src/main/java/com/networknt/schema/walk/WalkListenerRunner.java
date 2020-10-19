package com.networknt.schema.walk;

import java.util.Set;

import com.fasterxml.jackson.databind.JsonNode;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.ValidationMessage;

public interface WalkListenerRunner {

	public boolean runPreWalkListeners(String keyWordPath, JsonNode node, JsonNode rootNode, String at,
			String schemaPath, JsonNode schemaNode, JsonSchema parentSchema, JsonSchemaFactory jsonSchemaFactory);

	public void runPostWalkListeners(String keyWordPath, JsonNode node, JsonNode rootNode, String at, String schemaPath,
			JsonNode schemaNode, JsonSchema parentSchema, JsonSchemaFactory jsonSchemaFactory,
			Set<ValidationMessage> validationMessages);

}
