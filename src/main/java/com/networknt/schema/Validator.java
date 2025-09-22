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

import com.fasterxml.jackson.databind.JsonNode;
import com.networknt.schema.walk.Walker;

/**
 * A processor that checks an instance node belonging to an instance document
 * against a schema.
 */
public interface Validator extends Walker {
    /**
     * Validate the instance node which belongs to the instance document at the
     * instance location.
     * 
     * @param executionContext the execution context
     * @param instanceNode     the instance node being processed
     * @param instance         the instance document that the instance node belongs
     *                         to
     * @param instanceLocation the location of the instance node being processed
     */
    void validate(ExecutionContext executionContext, JsonNode instanceNode, JsonNode instance,
            JsonNodePath instanceLocation);

    /**
     * This is default implementation of walk method. Its job is to call the
     * validate method if shouldValidateSchema is enabled.
     */
    @Override
    default void walk(ExecutionContext executionContext, JsonNode instanceNode, JsonNode instance,
            JsonNodePath instanceLocation, boolean shouldValidateSchema) {
        if (instanceNode == null) {
            // Note that null is not the same as NullNode
            return;
        }
        if (shouldValidateSchema) {
            validate(executionContext, instanceNode, instance, instanceLocation);
        }
    }

    /**
     * The schema location is the canonical URI of the schema object plus a JSON
     * Pointer fragment indicating the subschema that produced a result. In contrast
     * with the evaluation path, the schema location MUST NOT include by-reference
     * applicators such as $ref or $dynamicRef.
     * 
     * @return the schema location
     */
    SchemaLocation getSchemaLocation();

    /**
     * The evaluation path is the set of keys, starting from the schema root,
     * through which evaluation passes to reach the schema object that produced a
     * specific result.
     * 
     * @return the evaluation path
     */
    JsonNodePath getEvaluationPath();
}
