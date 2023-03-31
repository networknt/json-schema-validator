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

import java.util.Set;

import com.fasterxml.jackson.databind.JsonNode;
import com.networknt.schema.walk.JsonSchemaWalker;

/**
 * Standard json validator interface, implemented by all validators and JsonSchema.
 */
public interface JsonValidator extends JsonSchemaWalker {

    /**
     * Validate the given root JsonNode, starting at the root of the data path.
     *
     * @param rootNode JsonNode
     * @return A list of ValidationMessage if there is any validation error, or an empty
     * list if there is no error.
     */
    Set<ValidationMessage> validate(JsonNode rootNode);

    /**
     * Validate the given JsonNode, the given node is the child node of the root node at given
     * data path.
     *
     * @param node     JsonNode
     * @param rootNode JsonNode
     * @param at       String
     * @return A list of ValidationMessage if there is any validation error, or an empty
     * list if there is no error.
     */
    Set<ValidationMessage> validate(JsonNode node, JsonNode rootNode, String at);

    /**
     * In case the {@link com.networknt.schema.JsonValidator} has a related {@link com.networknt.schema.JsonSchema} or several
     * ones, calling preloadJsonSchema will actually load the schema document(s) eagerly.
     *
     * @throws JsonSchemaException (a {@link java.lang.RuntimeException}) in case the {@link com.networknt.schema.JsonSchema} or nested schemas
     * are invalid (like <code>$ref</code> not resolving)
     * @since 1.0.54
     */
    default void preloadJsonSchema() throws JsonSchemaException {
        // do nothing by default - to be overridden in subclasses
    }
}
