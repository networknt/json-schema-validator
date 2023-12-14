package com.networknt.schema.walk;

import com.fasterxml.jackson.databind.JsonNode;
import com.networknt.schema.ExecutionContext;
import com.networknt.schema.JsonNodePath;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SchemaValidatorsConfig;
import com.networknt.schema.ValidationContext;

import java.net.URI;

/**
 * Encapsulation of Walk data that is passed into the {@link JsonSchemaWalkListener}.
 */
public class WalkEvent {

    private ExecutionContext executionContext;
    private String schemaPath;
    private JsonNode schemaNode;
    private JsonSchema parentSchema;
    private String keyWordName;
    private JsonNode node;
    private JsonNode rootNode;
    private JsonNodePath at;
    private JsonSchemaFactory currentJsonSchemaFactory;
    private ValidationContext validationContext;

    public ExecutionContext getExecutionContext() {
        return executionContext;
    }

    public String getSchemaPath() {
        return schemaPath;
    }

    public JsonNode getSchemaNode() {
        return schemaNode;
    }

    public JsonSchema getParentSchema() {
        return parentSchema;
    }

    public String getKeyWordName() {
        return keyWordName;
    }

    public JsonNode getNode() {
        return node;
    }

    public JsonNode getRootNode() {
        return rootNode;
    }

    public JsonNodePath getAt() {
        return at;
    }

    public JsonSchema getRefSchema(URI schemaUri) {
        return currentJsonSchemaFactory.getSchema(schemaUri, validationContext.getConfig());
    }

    public JsonSchema getRefSchema(URI schemaUri, SchemaValidatorsConfig schemaValidatorsConfig) {
        if (schemaValidatorsConfig != null) {
            return currentJsonSchemaFactory.getSchema(schemaUri, schemaValidatorsConfig);
        } else {
            return getRefSchema(schemaUri);
        }
    }

    public JsonSchemaFactory getCurrentJsonSchemaFactory() {
        return currentJsonSchemaFactory;
    }

    static class WalkEventBuilder {

        private WalkEvent walkEvent;

        WalkEventBuilder() {
            walkEvent = new WalkEvent();
        }

        public WalkEventBuilder executionContext(ExecutionContext executionContext) {
            walkEvent.executionContext = executionContext;
            return this;
        }

        public WalkEventBuilder schemaPath(String schemaPath) {
            walkEvent.schemaPath = schemaPath;
            return this;
        }

        public WalkEventBuilder schemaNode(JsonNode schemaNode) {
            walkEvent.schemaNode = schemaNode;
            return this;
        }

        public WalkEventBuilder parentSchema(JsonSchema parentSchema) {
            walkEvent.parentSchema = parentSchema;
            return this;
        }

        public WalkEventBuilder keyWordName(String keyWordName) {
            walkEvent.keyWordName = keyWordName;
            return this;
        }

        public WalkEventBuilder node(JsonNode node) {
            walkEvent.node = node;
            return this;
        }

        public WalkEventBuilder rootNode(JsonNode rootNode) {
            walkEvent.rootNode = rootNode;
            return this;
        }

        public WalkEventBuilder at(JsonNodePath at) {
            walkEvent.at = at;
            return this;
        }

        public WalkEventBuilder currentJsonSchemaFactory(JsonSchemaFactory currentJsonSchemaFactory) {
            walkEvent.currentJsonSchemaFactory = currentJsonSchemaFactory;
            return this;
        }

        public WalkEventBuilder validationContext(ValidationContext validationContext) {
            walkEvent.validationContext = validationContext;
            return this;
        }

        public WalkEvent build() {
            return walkEvent;
        }

    }

    public static WalkEventBuilder builder() {
        return new WalkEventBuilder();
    }

}
