/*
 * Copyright (c) 2024 the original author or authors.
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
package com.networknt.schema.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaRef;

/**
 * The 'Defaults' class provides utility methods for retrieving default values
 * from a JSON schema.
 * 
 * This class contains a single static method, 'getDefaultNode', which takes a
 * 'JsonSchema' object as input
 * and returns the default value specified in the schema. If the schema does not
 * have a default value,
 * it checks if the schema has a reference to another schema and recursively
 * calls itself with the referenced schema.
 * 
 * Usage:
 * JsonSchema schema = ...; // create or obtain a JSON schema
 * JsonNode defaultNode = Defaults.getDefaultNode(schema); // retrieve the
 * default value from the schema
 * 
 * Note: This class requires the 'com.networknt.schema.JsonSchema' and
 * 'com.networknt.schema.JsonSchemaRef' classes
 * from the 'networknt/json-schema-validator' library.
 */
public class Defaults {
    /**
     * Retrieves the default value specified in the JSON schema.
     * 
     * This method takes a 'JsonSchema' object as input and returns the default
     * value specified in the schema.
     * If the schema does not have a default value, it checks if the schema has a
     * reference to another schema
     * and recursively calls itself with the referenced schema.
     * 
     * @param schema the JSON schema from which to retrieve the default value
     * @return the default value specified in the schema, or null if no default
     *         value is found
     */
    public static JsonNode getDefaultNode(JsonSchema schema) {
        JsonNode result = schema.getSchemaNode().get("default");
        if (result == null) {
            JsonSchemaRef schemaRef = JsonSchemaRefs.from(schema);
            if (schemaRef != null) {
                result = getDefaultNode(schemaRef.getSchema());
            }
        }
        return result;
    }
}
