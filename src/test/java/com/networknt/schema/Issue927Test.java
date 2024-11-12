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

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Set;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.networknt.schema.SpecVersion.VersionFlag;
import com.networknt.schema.serialization.JsonMapperFactory;

/**
 * Test that the code isn't confused by an anchor in the id.
 */
class Issue927Test {
    @Test
    void test() throws JsonProcessingException {
        String schema = "{\r\n"
                + "  \"$schema\": \"http://json-schema.org/draft-07/schema#\",\r\n"
                + "  \"$id\": \"id\",\r\n"
                + "  \"type\": \"object\",\r\n"
                + "  \"title\": \"title\",\r\n"
                + "  \"anyOf\": [\r\n"
                + "    {\r\n"
                + "      \"required\": [\r\n"
                + "        \"id\",\r\n"
                + "        \"type\",\r\n"
                + "        \"genericSubmission\"\r\n"
                + "      ]\r\n"
                + "    }\r\n"
                + "  ],\r\n"
                + "  \"properties\": {\r\n"
                + "    \"id\": {\r\n"
                + "      \"type\": \"string\",\r\n"
                + "      \"title\": \"title\"\r\n"
                + "    },\r\n"
                + "    \"type\": {\r\n"
                + "      \"type\": \"string\",\r\n"
                + "      \"title\": \"title\"\r\n"
                + "    },\r\n"
                + "    \"genericSubmission\": {\r\n"
                + "      \"$ref\": \"#/definitions/genericSubmission\"\r\n"
                + "    }\r\n"
                + "  },\r\n"
                + "  \"definitions\": {\r\n"
                + "    \"genericSubmission\": {\r\n"
                + "      \"$id\": \"#/definitions/genericSubmission\",\r\n"
                + "      \"type\": \"object\",\r\n"
                + "      \"title\": \"title\",\r\n"
                + "      \"required\": [\r\n"
                + "        \"transactionReference\",\r\n"
                + "        \"title\"\r\n"
                + "      ],\r\n"
                + "      \"properties\": {\r\n"
                + "        \"transactionReference\": {\r\n"
                + "          \"type\": \"string\",\r\n"
                + "          \"title\": \"title\",\r\n"
                + "          \"description\": \"description\"\r\n"
                + "        },\r\n"
                + "        \"title\": {\r\n"
                + "          \"type\": \"array\",\r\n"
                + "          \"minItems\": 1,\r\n"
                + "          \"items\": {\r\n"
                + "            \"type\": \"object\",\r\n"
                + "            \"required\": [\r\n"
                + "              \"value\",\r\n"
                + "              \"locale\"\r\n"
                + "            ],\r\n"
                + "            \"properties\": {\r\n"
                + "              \"value\": {\r\n"
                + "                \"$ref\": \"#/definitions/value\"\r\n"
                + "              },\r\n"
                + "              \"locale\": {\r\n"
                + "                \"$ref\": \"#/definitions/locale\"\r\n"
                + "              }\r\n"
                + "            }\r\n"
                + "          }\r\n"
                + "        }\r\n"
                + "      }\r\n"
                + "    },\r\n"
                + "    \"value\": {\r\n"
                + "      \"$id\": \"#/definitions/value\",\r\n"
                + "      \"type\": \"string\"\r\n"
                + "    },\r\n"
                + "    \"locale\": {\r\n"
                + "      \"$id\": \"#/definitions/locale\",\r\n"
                + "      \"type\": \"string\",\r\n"
                + "      \"default\": \"fr\"\r\n"
                + "    }\r\n"
                + "  }\r\n"
                + "}";
        JsonSchema jsonSchema = JsonSchemaFactory.getInstance(VersionFlag.V7)
                .getSchema(SchemaLocation.of("http://www.example.org"), JsonMapperFactory.getInstance().readTree(schema));
        
        String input = "{\r\n"
                + "  \"$schema\": \"./mySchema.json\",\r\n"
                + "  \"_comment\": \"comment\",\r\n"
                + "  \"id\": \"b34024c4-6103-478c-bad6-83b26d98a892\",\r\n"
                + "  \"type\": \"genericSubmission\",\r\n"
                + "  \"genericSubmission\": {\r\n"
                + "    \"transactionReference\": \"123456\",\r\n"
                + "    \"title\": [\r\n"
                + "      {\r\n"
                + "        \"value\": \"[DE]...\",\r\n"
                + "        \"locale\": \"de\"\r\n"
                + "      },\r\n"
                + "      {\r\n"
                + "        \"value\": \"[EN]...\",\r\n"
                + "        \"locale\": \"en\"\r\n"
                + "      }\r\n"
                + "    ]\r\n"
                + "  }\r\n"
                + "}";
        Set<ValidationMessage> messages = jsonSchema.validate(input, InputFormat.JSON);
        assertEquals(0, messages.size());
    }

}
