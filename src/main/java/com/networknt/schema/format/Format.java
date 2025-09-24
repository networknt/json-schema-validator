/*
 * Copyright (c) 2016 Network New Technologies Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.networknt.schema.format;

import java.util.function.Supplier;

import com.fasterxml.jackson.databind.JsonNode;
import com.networknt.schema.ExecutionContext;
import com.networknt.schema.JsonType;
import com.networknt.schema.MessageSourceError;
import com.networknt.schema.NodePath;
import com.networknt.schema.SchemaContext;
import com.networknt.schema.TypeFactory;
import com.networknt.schema.keyword.FormatValidator;

/**
 * Used to implement the various formats for the format keyword.
 * <p>
 * Simple implementations need only override {@link #matches(ExecutionContext, String)}.
 */
public interface Format {
    /**
     * Gets the format name.
     *
     * @return the format name as referred to in a json schema format node.
     */
    String getName();
    
    /**
     * Gets the message key to use for the message.
     * <p>
     * See jsv-messages.properties.
     * <p>
     * The following are the arguments.<br>
     * {0} The format name<br>
     * {1} The input value
     * <p>
     * Note that the default localized messages do not use the input value.
     * 
     * @return the message key
     */
    default String getMessageKey() {
        return "format";
    }
    
    /**
     * Determines if the value matches the format.
     * <p>
     * This should be implemented for string node types.
     *
     * @param executionContext the execution context
     * @param value            to match
     * @return true if matches
     */
    default boolean matches(ExecutionContext executionContext, String value) {
        return true;
    }

    /**
     * Determines if the value matches the format.
     *
     * @param executionContext  the execution context
     * @param schemaContext the schema context
     * @param value             to match
     * @return true if matches
     */
    default boolean matches(ExecutionContext executionContext, SchemaContext schemaContext, String value) {
        return matches(executionContext, value);
    }
    
    /**
     * Determines if the value matches the format.
     * 
     * @param executionContext  the execution context
     * @param schemaContext the schema context
     * @param value             to match
     * @return true if matches
     */
    default boolean matches(ExecutionContext executionContext, SchemaContext schemaContext, JsonNode value) {
        JsonType nodeType = TypeFactory.getValueNodeType(value, schemaContext.getSchemaRegistryConfig());
        if (nodeType != JsonType.STRING) {
            return true;
        }
        return matches(executionContext, schemaContext, value.textValue());
    }

    /**
     * Determines if the value matches the format.
     * <p>
     * This can be implemented for non-string node types.
     *
     * @param executionContext the execution context
     * @param schemaContext the schema context
     * @param node the node
     * @param rootNode the root node
     * @param instanceLocation the instance location
     * @param assertionsEnabled if assertions are enabled
     * @param formatValidator the format validator
     * @return true if matches
     */
    default boolean matches(ExecutionContext executionContext, SchemaContext schemaContext, JsonNode node,
            JsonNode rootNode, NodePath instanceLocation, boolean assertionsEnabled, FormatValidator formatValidator) {
        return matches(executionContext, schemaContext, node);
    }

    /**
     * Validates the format.
     * <p>
     * This is the most flexible method to implement.
     *
     * @param executionContext the execution context
     * @param schemaContext the schema context
     * @param node the node
     * @param rootNode the root node
     * @param instanceLocation the instance locaiton
     * @param assertionsEnabled if assertions are enabled
     * @param message the message builder
     * @param formatValidator the format validator
     */
    default void validate(ExecutionContext executionContext, SchemaContext schemaContext,
            JsonNode node, JsonNode rootNode, NodePath instanceLocation, boolean assertionsEnabled,
            Supplier<MessageSourceError.Builder> message,
            FormatValidator formatValidator) {
        if (assertionsEnabled) {
            if (!matches(executionContext, schemaContext, node, rootNode, instanceLocation, assertionsEnabled,
                    formatValidator)) {
                executionContext.addError(message.get()
                                .arguments(this.getName(), node.asText()).build());
            }
        }
    }
}
