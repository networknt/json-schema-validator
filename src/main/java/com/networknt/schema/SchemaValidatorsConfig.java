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

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class SchemaValidatorsConfig {
    /**
     * when validate type, if TYPE_LOOSE = true, will try to convert string to different types to match the type defined in schema.
     */
    private boolean typeLoose;

    /**
     * When set to true, validator process is stop immediately when a very first validation error is discovered.
     */
    private boolean failFast;

    /**
     * When set to true, use ECMA-262 compatible validator
     */
    private boolean ecma262Validator;

    /**
     * Map of public, normally internet accessible schema URLs to alternate locations; this allows for offline
     * validation of schemas that refer to public URLs. This is merged with any mappings the {@link JsonSchemaFactory}
     * may have been built with.
     */
    private Map<String, String> uriMappings = new HashMap<String, String>();

    /**
     * When a field is set as nullable in the OpenAPI specification, the schema validator validates that it is nullable
     * however continues with validation against the nullable field
     * <p>
     * If handleNullableField is set to true && incoming field is nullable && value is field: null --> succeed
     * If handleNullableField is set to false && incoming field is nullable && value is field: null --> it is up to the type
     * validator using the SchemaValidator to handle it.
     */
    private boolean handleNullableField = true;

    public boolean isTypeLoose() {
        return typeLoose;
    }

    public void setTypeLoose(boolean typeLoose) {
        this.typeLoose = typeLoose;
    }

    /**
     * When enabled, {@link JsonValidator#validate(JsonNode, JsonNode, String)}
     * or {@link JsonValidator#validate(JsonNode)} doesn't return any {@link Set}&lt;{@link ValidationMessage}&gt;,
     * instead a {@link JsonSchemaException} is thrown as soon as a validation errors is discovered.
     *
     * @param failFast boolean
     */
    public void setFailFast(final boolean failFast) {
        this.failFast = failFast;
    }

    public boolean isFailFast() {
        return this.failFast;
    }

    public Map<String, String> getUriMappings() {
        // return a copy of the mappings
        return new HashMap<String, String>(uriMappings);
    }

    public void setUriMappings(Map<String, String> uriMappings) {
        this.uriMappings = uriMappings;
    }

    public boolean isHandleNullableField() {
        return handleNullableField;
    }

    public void setHandleNullableField(boolean handleNullableField) {
        this.handleNullableField = handleNullableField;
    }

    public boolean isEcma262Validator() {
        return ecma262Validator;
    }

    public void setEcma262Validator(boolean ecma262Validator) {
        this.ecma262Validator = ecma262Validator;
    }

    public SchemaValidatorsConfig() {
    }
}
