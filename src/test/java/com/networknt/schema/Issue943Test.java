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
package com.networknt.schema;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.networknt.schema.SpecVersion.VersionFlag;

class Issue943Test {
    @Test
    void test() {
        Map<String, String> external = new HashMap<>();

        String externalSchemaData = "{\r\n"
                + "  \"$schema\": \"http://json-schema.org/draft-07/schema#\",\r\n"
                + "  \"$id\": \"https://www.example.org/point.json\",\r\n"
                + "  \"type\": \"object\",\r\n"
                + "  \"required\": [\r\n"
                + "    \"type\",\r\n"
                + "    \"coordinates\"\r\n"
                + "  ],\r\n"
                + "  \"properties\": {\r\n"
                + "    \"type\": {\r\n"
                + "      \"type\": \"string\",\r\n"
                + "      \"enum\": [\r\n"
                + "        \"Point\"\r\n"
                + "      ]\r\n"
                + "    },\r\n"
                + "    \"coordinates\": {\r\n"
                + "      \"type\": \"array\",\r\n"
                + "      \"minItems\": 2,\r\n"
                + "      \"items\": {\r\n"
                + "        \"type\": \"number\"\r\n"
                + "      }\r\n"
                + "    }\r\n"
                + "  }\r\n"
                + "}";

        external.put("https://www.example.org/point.json", externalSchemaData);

        String schemaData = "{\r\n"
                + "  \"$schema\": \"https://json-schema.org/draft/2020-12/schema\",\r\n"
                + "  \"$ref\": \"https://www.example.org/point.json\",\r\n"
                + "  \"unevaluatedProperties\": false\r\n"
                + "}";

        String inputData = "{\r\n"
                + "  \"type\": \"Point\",\r\n"
                + "  \"coordinates\": [1, 1]\r\n"
                + "}";
        JsonSchemaFactory factory = JsonSchemaFactory.getInstance(VersionFlag.V202012,
                builder -> builder.schemaLoaders(schemaLoaders -> schemaLoaders.schemas(external)));
        SchemaValidatorsConfig config = SchemaValidatorsConfig.builder().build();
        JsonSchema schema = factory.getSchema(schemaData, config);
        assertTrue(schema.validate(inputData, InputFormat.JSON).isEmpty());

        String badData = "{\r\n"
                + "  \"type\": \"Point\",\r\n"
                + "  \"hello\": \"Point\",\r\n"
                + "  \"coordinates\": [1, 1]\r\n"
                + "}";
        assertFalse(schema.validate(badData, InputFormat.JSON).isEmpty());
    }
}
