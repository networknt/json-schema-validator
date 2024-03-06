package com.networknt.schema.walk;

import com.fasterxml.jackson.databind.JsonNode;
import com.networknt.schema.ExecutionContext;
import com.networknt.schema.JsonNodePath;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.SchemaLocation;
import com.networknt.schema.ValidationContext;
import com.networknt.schema.ValidationMessage;

import java.util.Set;

public interface WalkListenerRunner {

    public boolean runPreWalkListeners(ExecutionContext executionContext, String keyword, JsonNode instanceNode,
            JsonNode rootNode, JsonNodePath instanceLocation, JsonNodePath evaluationPath, SchemaLocation schemaLocation,
            JsonNode schemaNode, JsonSchema schema, JsonSchema parentSchema, ValidationContext validationContext);

    public void runPostWalkListeners(ExecutionContext executionContext, String keyword, JsonNode instanceNode,
            JsonNode rootNode, JsonNodePath instanceLocation, JsonNodePath evaluationPath, SchemaLocation schemaLocation,
            JsonNode schemaNode, JsonSchema schema, JsonSchema parentSchema,
            ValidationContext validationContext, Set<ValidationMessage> validationMessages);

}
