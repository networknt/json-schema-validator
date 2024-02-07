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

package com.networknt.schema;

import java.util.Collections;
import java.util.Set;
import java.util.function.Supplier;

import com.fasterxml.jackson.databind.JsonNode;

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
     * Gets the error message description.
     *
     * @return the error message description.
     */
    String getErrorMessageDescription();


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
     * @param validationContext the validation context
     * @param value             to match
     * @return true if matches
     */
    default boolean matches(ExecutionContext executionContext, ValidationContext validationContext, String value) {
        return matches(executionContext, value);
    }

    /**
     * Determines if the value matches the format.
     * <p>
     * This can be implemented for non-string node types.
     *
     * @param executionContext the execution context
     * @param validationContext the validation context
     * @param node the node
     * @param rootNode the root node
     * @param instanceLocation the instance location
     * @param assertionsEnabled if assertions are enabled
     * @param formatValidator the format validator
     * @return true if matches
     */
    default boolean matches(ExecutionContext executionContext, ValidationContext validationContext, JsonNode node,
            JsonNode rootNode, JsonNodePath instanceLocation, boolean assertionsEnabled, FormatValidator formatValidator) {
        JsonType nodeType = TypeFactory.getValueNodeType(node, validationContext.getConfig());
        if (nodeType != JsonType.STRING) {
            return true;
        }
        return matches(executionContext, validationContext, node.asText());
    }

    /**
     * Validates the format.
     * <p>
     * This is the most flexible method to implement.
     *
     * @param executionContext the execution context
     * @param validationContext the validation context
     * @param node the node
     * @param rootNode the root node
     * @param instanceLocation the instance locaiton
     * @param assertionsEnabled if assertions are enabled
     * @param message the message builder
     * @param formatValidator the format validator
     * @return the messages
     */
    default Set<ValidationMessage> validate(ExecutionContext executionContext, ValidationContext validationContext,
            JsonNode node, JsonNode rootNode, JsonNodePath instanceLocation, boolean assertionsEnabled,
            Supplier<MessageSourceValidationMessage.Builder> message,
            FormatValidator formatValidator) {
        if (assertionsEnabled) {
            if (!matches(executionContext, validationContext, node, rootNode, instanceLocation, assertionsEnabled,
                    formatValidator)) {
                return Collections.singleton(message.get().instanceNode(node).instanceLocation(instanceLocation)
                        .locale(executionContext.getExecutionConfig().getLocale())
                        .failFast(executionContext.isFailFast())
                        .arguments(this.getName(), this.getErrorMessageDescription()).build());
            }
        }
        return Collections.emptySet();
    }
}
