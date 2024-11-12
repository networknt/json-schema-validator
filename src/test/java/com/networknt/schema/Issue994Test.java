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

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.networknt.schema.SpecVersion.VersionFlag;
import com.networknt.schema.serialization.JsonMapperFactory;

class Issue994Test {
    @Test
    void test() throws JsonProcessingException {
        String schemaData = "{\r\n"
                + "    \"$schema\": \"https://json-schema.org/draft/2020-12/schema\",\r\n"
                + "    \"type\": \"object\",\r\n"
                + "    \"properties\": {\r\n"
                + "        \"textValue\": {\r\n"
                + "            \"type\": [\r\n"
                + "                \"string\",\r\n"
                + "                \"null\"\r\n"
                + "            ],\r\n"
                + "            \"isMandatory\": true\r\n"
                + "        }\r\n"
                + "    }\r\n"
                + "}";
        JsonMetaSchema metaSchema = JsonMetaSchema.builder(JsonMetaSchema.getV202012()).vocabularies(vocabularies -> {
            vocabularies.remove(Vocabulary.V202012_VALIDATION.getIri());
        }).build();
        JsonNode schemaNode = JsonMapperFactory.getInstance().readTree(schemaData);
        JsonSchema schema = JsonSchemaFactory
                .getInstance(VersionFlag.V202012, builder -> builder.metaSchema(metaSchema)).getSchema(schemaNode);
        String inputData = "{\r\n"
                + "  \"textValue\": \"hello\"\r\n"
                + "}";
        assertTrue(schema.validate(inputData, InputFormat.JSON, OutputFormat.BOOLEAN));
    }
}
