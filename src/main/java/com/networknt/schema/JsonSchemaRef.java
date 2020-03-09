package com.networknt.schema;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.Set;

/**
 * Use this object instead a JsonSchema for references.
 * <p>
 * This reference may be empty (if the reference is being parsed) or with data (after the reference has been parsed),
 * helping to prevent recursive reference to cause an infinite loop.
 */

public class JsonSchemaRef {

    private JsonSchema schema;
    private ValidationContext validationContext;
    private String refValue;

    public JsonSchemaRef(ValidationContext validationContext, String refValue) {
        this.validationContext = validationContext;
        this.refValue = refValue;
    }

    public JsonSchemaRef(JsonSchema schema) {
        this.schema = schema;
    }

    public void set(JsonSchema schema) {
        this.schema = schema;
    }

    public Set<ValidationMessage> validate(JsonNode node, JsonNode rootNode, String at) {
        return schema.validate(node, rootNode, at);
    }

    public JsonSchema getSchema() {
        return schema;
    }
}
