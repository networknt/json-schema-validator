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
    private SchemaLocation schemaLocation;
    private JsonNodePath evaluationPath;
    private JsonNode schemaNode;
    private JsonSchema schema;
    private JsonSchema parentSchema;
    private String keyword;
    private JsonNode instanceNode;
    private JsonNode rootNode;
    private JsonNodePath instanceLocation;
    private ValidationContext validationContext;

    public ExecutionContext getExecutionContext() {
        return executionContext;
    }

    public SchemaLocation getSchemaLocation() {
        return schemaLocation;
    }
    
    public JsonNodePath getEvaluationPath() {
        return evaluationPath;
    }

    public JsonNode getSchemaNode() {
        return schemaNode;
    }

    /**
     * Gets the JsonSchema indicated by the schema node.
     *
     * @return the schema
     */
    public JsonSchema getSchema() {
        return schema;
    }

    public JsonSchema getParentSchema() {
        return parentSchema;
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

    public JsonSchema getRefSchema(SchemaLocation schemaLocation) {
        return this.validationContext.getJsonSchemaFactory().getSchema(schemaLocation, validationContext.getConfig());
    }

    public JsonSchema getRefSchema(SchemaLocation schemaLocation, SchemaValidatorsConfig schemaValidatorsConfig) {
        if (schemaValidatorsConfig != null) {
            return this.validationContext.getJsonSchemaFactory().getSchema(schemaLocation, schemaValidatorsConfig);
        } else {
            return getRefSchema(schemaLocation);
        }
    }

    public JsonSchemaFactory getJsonSchemaFactory() {
        return this.validationContext.getJsonSchemaFactory();
    }

    @Deprecated
    public JsonSchemaFactory getCurrentJsonSchemaFactory() {
        return getJsonSchemaFactory();
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

        public WalkEventBuilder schemaLocation(SchemaLocation schemaLocation) {
            walkEvent.schemaLocation = schemaLocation;
            return this;
        }

        public WalkEventBuilder schemaNode(JsonNode schemaNode) {
            walkEvent.schemaNode = schemaNode;
            return this;
        }

        public WalkEventBuilder schema(JsonSchema schema) {
            walkEvent.schema = schema;
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
