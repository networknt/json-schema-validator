package com.networknt.schema.walk;

import com.fasterxml.jackson.databind.JsonNode;
import com.networknt.schema.ExecutionContext;
import com.networknt.schema.JsonNodePath;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SchemaLocation;
import com.networknt.schema.SchemaValidatorsConfig;
import com.networknt.schema.ValidationContext;

/**
 * Encapsulation of Walk data that is passed into the {@link JsonSchemaWalkListener}.
 */
public class WalkEvent {

    private ExecutionContext executionContext;
    private JsonSchema schema;
    private String keyword;
    private JsonNode instanceNode;
    private JsonNode rootNode;
    private JsonNodePath instanceLocation;

    public ExecutionContext getExecutionContext() {
        return executionContext;
    }

    @Deprecated
    public SchemaLocation getSchemaLocation() {
        return getSchema().getSchemaLocation();
    }

    @Deprecated
    public JsonNodePath getEvaluationPath() {
        return getSchema().getEvaluationPath();
    }

    @Deprecated
    public JsonNode getSchemaNode() {
        return getSchema().getSchemaNode();
    }

    /**
     * Gets the JsonSchema indicated by the schema node.
     *
     * @return the schema
     */
    public JsonSchema getSchema() {
        return schema;
    }

    @Deprecated
    public JsonSchema getParentSchema() {
        return getSchema().getParentSchema();
    }

    public String getKeyword() {
        return keyword;
    }

    public JsonNode getInstanceNode() {
        return instanceNode;
    }

    @Deprecated
    public JsonNode getNode() {
        return getInstanceNode();
    }

    public JsonNode getRootNode() {
        return rootNode;
    }

    public JsonNodePath getInstanceLocation() {
        return instanceLocation;
    }

    @Deprecated
    public JsonSchema getRefSchema(SchemaLocation schemaLocation) {
        return getCurrentJsonSchemaFactory().getSchema(schemaLocation, getSchema().getValidationContext().getConfig());
    }

    @Deprecated
    public JsonSchema getRefSchema(SchemaLocation schemaLocation, SchemaValidatorsConfig schemaValidatorsConfig) {
        if (schemaValidatorsConfig != null) {
            return getCurrentJsonSchemaFactory().getSchema(schemaLocation, schemaValidatorsConfig);
        } else {
            return getRefSchema(schemaLocation);
        }
    }

    @Deprecated
    public JsonSchemaFactory getCurrentJsonSchemaFactory() {
        return getSchema().getValidationContext().getJsonSchemaFactory();
    }

    @Override
    public String toString() {
        return "WalkEvent [evaluationPath=" + getSchema().getEvaluationPath() + ", schemaLocation="
                + getSchema().getSchemaLocation() + ", instanceLocation=" + instanceLocation + "]";
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

        @Deprecated
        public WalkEventBuilder evaluationPath(JsonNodePath evaluationPath) {
            return this;
        }

        @Deprecated
        public WalkEventBuilder schemaLocation(SchemaLocation schemaLocation) {
            return this;
        }

        @Deprecated
        public WalkEventBuilder schemaNode(JsonNode schemaNode) {
            return this;
        }

        public WalkEventBuilder schema(JsonSchema schema) {
            walkEvent.schema = schema;
            return this;
        }

        @Deprecated
        public WalkEventBuilder parentSchema(JsonSchema parentSchema) {
            return this;
        }

        public WalkEventBuilder keyword(String keyword) {
            walkEvent.keyword = keyword;
            return this;
        }

        public WalkEventBuilder instanceNode(JsonNode node) {
            walkEvent.instanceNode = node;
            return this;
        }

        @Deprecated
        public WalkEventBuilder node(JsonNode node) {
            return instanceNode(node);
        }

        public WalkEventBuilder rootNode(JsonNode rootNode) {
            walkEvent.rootNode = rootNode;
            return this;
        }

        public WalkEventBuilder instanceLocation(JsonNodePath instanceLocation) {
            walkEvent.instanceLocation = instanceLocation;
            return this;
        }

        @Deprecated
        public WalkEventBuilder currentJsonSchemaFactory(JsonSchemaFactory currentJsonSchemaFactory) {
            return this;
        }

        @Deprecated
        public WalkEventBuilder validationContext(ValidationContext validationContext) {
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
