package com.networknt.schema;


import com.fasterxml.jackson.databind.JsonNode;

public interface Keyword {
    String getValue();
    
    JsonValidator newValidator(String schemaPath, JsonNode schemaNode, JsonSchema parentSchema, ValidationContext validationContext) throws JsonSchemaException, Exception;
}
