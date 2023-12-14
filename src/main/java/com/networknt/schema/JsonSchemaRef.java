/*
 * Copyright (c) 2020 Network New Technologies Inc.
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

import com.fasterxml.jackson.databind.JsonNode;

import java.util.Set;

/**
 * Use this object instead a JsonSchema for references.
 * <p>
 * This reference may be empty (if the reference is being parsed) or with data (after the reference has been parsed),
 * helping to prevent recursive reference to cause an infinite loop.
 */

public class JsonSchemaRef {

    private final JsonSchema schema;

    public JsonSchemaRef(JsonSchema schema) {
        this.schema = schema;
    }

    public Set<ValidationMessage> validate(ExecutionContext executionContext, JsonNode node, JsonNode rootNode, JsonNodePath at) {
        return schema.validate(executionContext, node, rootNode, at);
    }

    public JsonSchema getSchema() {
        return schema;
    }

	public Set<ValidationMessage> walk(ExecutionContext executionContext, JsonNode node, JsonNode rootNode, JsonNodePath at, boolean shouldValidateSchema) {
		return schema.walk(executionContext, node, rootNode, at, shouldValidateSchema);
	}
}
