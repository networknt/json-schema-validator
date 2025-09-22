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

import com.networknt.schema.DynamicRefValidator;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaRef;
import com.networknt.schema.KeywordValidator;
import com.networknt.schema.RecursiveRefValidator;
import com.networknt.schema.RefValidator;

/**
 * Utility methods for JsonSchemaRef.
 */
public class JsonSchemaRefs {

    /**
     * Gets the ref.
     *
     * @param schema the schema
     * @return the ref
     */
    public static JsonSchemaRef from(JsonSchema schema) {
        for (KeywordValidator validator : schema.getValidators()) {
            if (validator instanceof RefValidator) {
                return ((RefValidator) validator).getSchemaRef();
            } else if (validator instanceof DynamicRefValidator) {
                return ((DynamicRefValidator) validator).getSchemaRef();
            } else if (validator instanceof RecursiveRefValidator) {
                return ((RecursiveRefValidator) validator).getSchemaRef();
            }
        }
        return null;
    }

}
