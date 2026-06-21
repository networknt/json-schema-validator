/*
 * Copyright (c) 2026 the original author or authors.
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.util.List;

import org.junit.jupiter.api.Test;

class Issue1252Test {
    @Test
    void additionalPropertiesSchemaValidatesHashPrefixedProperties() {
        String schemaData = "{\r\n"
                + "  \"$schema\": \"http://json-schema.org/draft-07/schema#\",\r\n"
                + "  \"type\": \"object\",\r\n"
                + "  \"properties\": {\r\n"
                + "    \"compilerOptions\": {\r\n"
                + "      \"type\": \"object\",\r\n"
                + "      \"properties\": {\r\n"
                + "        \"paths\": {\r\n"
                + "          \"type\": \"object\",\r\n"
                + "          \"additionalProperties\": {\r\n"
                + "            \"type\": \"array\",\r\n"
                + "            \"items\": {\r\n"
                + "              \"type\": \"string\"\r\n"
                + "            }\r\n"
                + "          }\r\n"
                + "        }\r\n"
                + "      }\r\n"
                + "    }\r\n"
                + "  }\r\n"
                + "}";
        String inputData = "{\r\n"
                + "  \"compilerOptions\": {\r\n"
                + "    \"paths\": {\r\n"
                + "      \"#routes/*\": [\r\n"
                + "        {\r\n"
                + "          \"invalid\": null\r\n"
                + "        }\r\n"
                + "      ]\r\n"
                + "    }\r\n"
                + "  }\r\n"
                + "}";

        Schema schema = SchemaRegistry.withDefaultDialect(SpecificationVersion.DRAFT_7).getSchema(schemaData);
        List<Error> errors = schema.validate(inputData, InputFormat.JSON);

        assertFalse(errors.isEmpty());
        Error error = errors.get(0);
        assertEquals("/compilerOptions/paths/#routes~1*/0", error.getInstanceLocation().toString());
        assertEquals("/properties/compilerOptions/properties/paths/additionalProperties/items/type",
                error.getEvaluationPath().toString());
        assertEquals("/compilerOptions/paths/#routes~1*/0: object found, string expected", error.toString());
    }

    @Test
    void additionalPropertiesFalseRejectsHashPrefixedProperties() {
        String schemaData = "{\r\n"
                + "  \"$schema\": \"http://json-schema.org/draft-07/schema#\",\r\n"
                + "  \"type\": \"object\",\r\n"
                + "  \"additionalProperties\": false\r\n"
                + "}";
        String inputData = "{\r\n"
                + "  \"#plugins/\": [\r\n"
                + "    \"plugin\"\r\n"
                + "  ]\r\n"
                + "}";

        Schema schema = SchemaRegistry.withDefaultDialect(SpecificationVersion.DRAFT_7).getSchema(schemaData);
        List<Error> errors = schema.validate(inputData, InputFormat.JSON);

        assertFalse(errors.isEmpty());
        Error error = errors.get(0);
        assertEquals("", error.getInstanceLocation().toString());
        assertEquals("/additionalProperties", error.getEvaluationPath().toString());
        assertEquals("#plugins/", error.getProperty());
    }
}
