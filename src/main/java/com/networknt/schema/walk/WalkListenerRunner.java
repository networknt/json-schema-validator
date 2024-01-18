package com.networknt.schema.walk;

import com.fasterxml.jackson.databind.JsonNode;
import com.networknt.schema.ExecutionContext;
import com.networknt.schema.JsonNodePath;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SchemaLocation;
import com.networknt.schema.ValidationContext;
import com.networknt.schema.ValidationMessage;

import java.util.Set;

public interface WalkListenerRunner {

    public boolean runPreWalkListeners(ExecutionContext executionContext, String keyword, JsonNode node,
            JsonNode rootNode, JsonNodePath instanceLocation, JsonNodePath evaluationPath, SchemaLocation schemaLocation,
            JsonNode schemaNode, JsonSchema parentSchema, ValidationContext validationContext,
            JsonSchemaFactory jsonSchemaFactory);

    public void runPostWalkListeners(ExecutionContext executionContext, String keyword, JsonNode node,
            JsonNode rootNode, JsonNodePath instanceLocation, JsonNodePath evaluationPath, SchemaLocation schemaLocation,
            JsonNode schemaNode, JsonSchema parentSchema, ValidationContext validationContext,
            JsonSchemaFactory jsonSchemaFactory, Set<ValidationMessage> validationMessages);

}
