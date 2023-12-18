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
    private JsonNodePath schemaLocation;
    private JsonNodePath evaluationPath;
    private JsonNode schemaNode;
    private JsonSchema parentSchema;
    private String keyword;
    private JsonNode node;
    private JsonNode rootNode;
    private JsonNodePath instanceLocation;
    private JsonSchemaFactory currentJsonSchemaFactory;
    private ValidationContext validationContext;

    public ExecutionContext getExecutionContext() {
        return executionContext;
    }

    public JsonNodePath getSchemaLocation() {
        return schemaLocation;
    }
    
    public JsonNodePath getEvaluationPath() {
        return evaluationPath;
    }

    public JsonNode getSchemaNode() {
        return schemaNode;
    }

    public JsonSchema getParentSchema() {
        return parentSchema;
    }

    public String getKeyword() {
        return keyword;
    }

    public JsonNode getNode() {
        return node;
    }

    public JsonNode getRootNode() {
        return rootNode;
    }

    public JsonNodePath getInstanceLocation() {
        return instanceLocation;
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

    @Override
    public String toString() {
        return "WalkEvent [evaluationPath=" + evaluationPath + ", schemaLocation=" + schemaLocation
                + ", instanceLocation=" + instanceLocation + "]";
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

        public WalkEventBuilder evaluationPath(JsonNodePath evaluationPath) {
            walkEvent.evaluationPath = evaluationPath;
            return this;
        }

        public WalkEventBuilder schemaLocation(JsonNodePath schemaLocation) {
            walkEvent.schemaLocation = schemaLocation;
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

        public WalkEventBuilder keyword(String keyword) {
            walkEvent.keyword = keyword;
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

        public WalkEventBuilder instanceLocation(JsonNodePath instanceLocation) {
            walkEvent.instanceLocation = instanceLocation;
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
