package com.networknt.schema.walk;

import com.fasterxml.jackson.databind.JsonNode;
import com.networknt.schema.ExecutionContext;
import com.networknt.schema.JsonNodePath;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonValidator;

/**
 * Encapsulation of Walk data that is passed into the {@link JsonSchemaWalkListener}.
 */
public class WalkEvent {

    private ExecutionContext executionContext;
    private JsonSchema schema;
    private String keyword;
    private JsonNode rootNode;
    private JsonNode instanceNode;
    private JsonNodePath instanceLocation;
    private JsonValidator validator;

    /**
     * Gets the execution context.
     * <p>
     * As the listeners should be state-less, this allows listeners to store data in
     * the collector context.
     * 
     * @return the execution context
     */
    public ExecutionContext getExecutionContext() {
        return executionContext;
    }

    /**
     * Gets the schema that will be used to evaluate the instance node.
     * <p>
     * For the keyword listener, this will allow getting the validator for the given keyword.
     *
     * @return the schema
     */
    public JsonSchema getSchema() {
        return schema;
    }

    /**
     * Gets the keyword.
     * 
     * @return the keyword
     */
    public String getKeyword() {
        return keyword;
    }

    /**
     * Gets the root instance node.
     * <p>
     * This makes it possible to get the parent node, for instance by getting the
     * instance location parent and using the root node.
     * 
     * @return the root node
     */
    public JsonNode getRootNode() {
        return rootNode;
    }

    /**
     * Gets the instance node.
     * 
     * @return the instance node
     */
    public JsonNode getInstanceNode() {
        return instanceNode;
    }

    /**
     * Gets the instance location of the instance node.
     * 
     * @return the instance location of the instance node
     */
    public JsonNodePath getInstanceLocation() {
        return instanceLocation;
    }

    /**
     * Gets the validator that corresponds with the keyword.
     * @param <T> the type of the validator
     * @return the validator
     */
    @SuppressWarnings("unchecked")
    public <T extends JsonValidator> T getValidator() {
        return (T) this.validator;
    }

    @Override
    public String toString() {
        return "WalkEvent [evaluationPath=" + getSchema().getEvaluationPath() + ", schemaLocation="
                + getSchema().getSchemaLocation() + ", instanceLocation=" + instanceLocation + "]";
    }

    static class WalkEventBuilder {

        private final WalkEvent walkEvent;

        WalkEventBuilder() {
            walkEvent = new WalkEvent();
        }

        public WalkEventBuilder executionContext(ExecutionContext executionContext) {
            walkEvent.executionContext = executionContext;
            return this;
        }

        public WalkEventBuilder schema(JsonSchema schema) {
            walkEvent.schema = schema;
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

        public WalkEventBuilder rootNode(JsonNode rootNode) {
            walkEvent.rootNode = rootNode;
            return this;
        }

        public WalkEventBuilder instanceLocation(JsonNodePath instanceLocation) {
            walkEvent.instanceLocation = instanceLocation;
            return this;
        }

        public WalkEventBuilder validator(JsonValidator validator) {
            walkEvent.validator = validator;
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
