package com.networknt.schema.walk;

import com.fasterxml.jackson.databind.JsonNode;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.ValidationContext;
import com.networknt.schema.ValidationMessage;

import java.util.Set;

public interface WalkListenerRunner {

    public boolean runPreWalkListeners(String keyWordPath, JsonNode node, JsonNode rootNode, String at,
                                       String schemaPath, JsonNode schemaNode, JsonSchema parentSchema, ValidationContext validationContext, JsonSchemaFactory jsonSchemaFactory);

    public void runPostWalkListeners(String keyWordPath, JsonNode node, JsonNode rootNode, String at, String schemaPath,
                                     JsonNode schemaNode, JsonSchema parentSchema, ValidationContext validationContext, JsonSchemaFactory jsonSchemaFactory,
                                     Set<ValidationMessage> validationMessages);

}
