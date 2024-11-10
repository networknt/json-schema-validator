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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.JsonLocation;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.networknt.schema.InputFormat;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SchemaValidatorsConfig;
import com.networknt.schema.SpecVersion.VersionFlag;
import com.networknt.schema.ValidationMessage;
import com.networknt.schema.serialization.JsonMapperFactory;
import com.networknt.schema.serialization.JsonNodeReader;
import com.networknt.schema.serialization.node.LocationJsonNodeFactoryFactory;
/**
 * Tests for JsonNodes.
 */
class JsonNodesTest {
    @Test
    void location() throws JsonParseException, IOException {
        String schemaData = "{\r\n"
                + "  \"$id\": \"https://schema/myschema\",\r\n"
                + "  \"properties\": {\r\n"
                + "    \"startDate\": {\r\n"
                + "      \"format\": \"date\",\r\n"
                + "      \"minLength\": 6\r\n"
                + "    }\r\n"
                + "  }\r\n"
                + "}";
        JsonNode jsonNode = JsonNodes.readTree(JsonMapperFactory.getInstance(), schemaData,
                LocationJsonNodeFactoryFactory.getInstance());
        JsonNode idNode = jsonNode.at("/$id");
        JsonLocation location = JsonNodes.tokenLocationOf(idNode);
        assertEquals(2, location.getLineNr());
        assertEquals(10, location.getColumnNr());

        JsonNode formatNode = jsonNode.at("/properties/startDate/format");
        location = JsonNodes.tokenLocationOf(formatNode);
        assertEquals(5, location.getLineNr());
        assertEquals(17, location.getColumnNr());

        JsonNode minLengthNode = jsonNode.at("/properties/startDate/minLength");
        location = JsonNodes.tokenLocationOf(minLengthNode);
        assertEquals(6, location.getLineNr());
        assertEquals(20, location.getColumnNr());
    }

    @Test
    void jsonLocation() {
        String schemaData = "{\r\n"
                + "  \"$id\": \"https://schema/myschema\",\r\n"
                + "  \"properties\": {\r\n"
                + "    \"startDate\": {\r\n"
                + "      \"format\": \"date\",\r\n"
                + "      \"minLength\": 6\r\n"
                + "    }\r\n"
                + "  }\r\n"
                + "}";
        String inputData = "{\r\n"
                + "  \"startDate\": \"1\"\r\n"
                + "}";
        JsonSchemaFactory factory = JsonSchemaFactory.getInstance(VersionFlag.V202012,
                builder -> builder.jsonNodeReader(JsonNodeReader.builder().locationAware().build()));
        SchemaValidatorsConfig config = SchemaValidatorsConfig.builder().build();
        JsonSchema schema = factory.getSchema(schemaData, InputFormat.JSON, config);
        Set<ValidationMessage> messages = schema.validate(inputData, InputFormat.JSON, executionContext -> {
            executionContext.getExecutionConfig().setFormatAssertionsEnabled(true);
        });
        List<ValidationMessage> list = messages.stream().collect(Collectors.toList());
        ValidationMessage format = list.get(0);
        JsonLocation formatInstanceNodeTokenLocation = JsonNodes.tokenLocationOf(format.getInstanceNode());
        JsonLocation formatSchemaNodeTokenLocation = JsonNodes.tokenLocationOf(format.getSchemaNode());
        ValidationMessage minLength = list.get(1);
        JsonLocation minLengthInstanceNodeTokenLocation = JsonNodes.tokenLocationOf(minLength.getInstanceNode());
        JsonLocation minLengthSchemaNodeTokenLocation = JsonNodes.tokenLocationOf(minLength.getSchemaNode());

        assertEquals("format", format.getType());

        assertEquals("date", format.getSchemaNode().asText());
        assertEquals(5, formatSchemaNodeTokenLocation.getLineNr());
        assertEquals(17, formatSchemaNodeTokenLocation.getColumnNr());

        assertEquals("1", format.getInstanceNode().asText());
        assertEquals(2, formatInstanceNodeTokenLocation.getLineNr());
        assertEquals(16, formatInstanceNodeTokenLocation.getColumnNr());

        assertEquals("minLength", minLength.getType());

        assertEquals("6", minLength.getSchemaNode().asText());
        assertEquals(6, minLengthSchemaNodeTokenLocation.getLineNr());
        assertEquals(20, minLengthSchemaNodeTokenLocation.getColumnNr());

        assertEquals("1", minLength.getInstanceNode().asText());
        assertEquals(2, minLengthInstanceNodeTokenLocation.getLineNr());
        assertEquals(16, minLengthInstanceNodeTokenLocation.getColumnNr());
    }

    @Test
    void yamlLocation() {
        String schemaData = "---\r\n"
                + "\"$id\": 'https://schema/myschema'\r\n"
                + "properties:\r\n"
                + "  startDate:\r\n"
                + "    format: 'date'\r\n"
                + "    minLength: 6\r\n";
        String inputData = "---\r\n"
                + "startDate: '1'\r\n";
        JsonSchemaFactory factory = JsonSchemaFactory.getInstance(VersionFlag.V202012,
                builder -> builder.jsonNodeReader(JsonNodeReader.builder().locationAware().build()));
        SchemaValidatorsConfig config = SchemaValidatorsConfig.builder().build();
        JsonSchema schema = factory.getSchema(schemaData, InputFormat.YAML, config);
        Set<ValidationMessage> messages = schema.validate(inputData, InputFormat.YAML, executionContext -> {
            executionContext.getExecutionConfig().setFormatAssertionsEnabled(true);
        });
        List<ValidationMessage> list = messages.stream().collect(Collectors.toList());
        ValidationMessage format = list.get(0);
        JsonLocation formatInstanceNodeTokenLocation = JsonNodes.tokenLocationOf(format.getInstanceNode());
        JsonLocation formatSchemaNodeTokenLocation = JsonNodes.tokenLocationOf(format.getSchemaNode());
        ValidationMessage minLength = list.get(1);
        JsonLocation minLengthInstanceNodeTokenLocation = JsonNodes.tokenLocationOf(minLength.getInstanceNode());
        JsonLocation minLengthSchemaNodeTokenLocation = JsonNodes.tokenLocationOf(minLength.getSchemaNode());

        assertEquals("format", format.getType());

        assertEquals("date", format.getSchemaNode().asText());
        assertEquals(5, formatSchemaNodeTokenLocation.getLineNr());
        assertEquals(13, formatSchemaNodeTokenLocation.getColumnNr());

        assertEquals("1", format.getInstanceNode().asText());
        assertEquals(2, formatInstanceNodeTokenLocation.getLineNr());
        assertEquals(12, formatInstanceNodeTokenLocation.getColumnNr());

        assertEquals("minLength", minLength.getType());

        assertEquals("6", minLength.getSchemaNode().asText());
        assertEquals(6, minLengthSchemaNodeTokenLocation.getLineNr());
        assertEquals(16, minLengthSchemaNodeTokenLocation.getColumnNr());

        assertEquals("1", minLength.getInstanceNode().asText());
        assertEquals(2, minLengthInstanceNodeTokenLocation.getLineNr());
        assertEquals(12, minLengthInstanceNodeTokenLocation.getColumnNr());
    }

    @Test
    void missingNode() {
        JsonNode missing = JsonNodes.readTree(JsonMapperFactory.getInstance(),
                new ByteArrayInputStream("".getBytes(StandardCharsets.UTF_8)),
                LocationJsonNodeFactoryFactory.getInstance());
        assertTrue(missing.isMissingNode());
    }

    @Test
    void types() {
        String json = "{\r\n"
                + "  \"properties\": {\r\n"
                + "    \"number\": 1234.56789,\r\n"
                + "    \"string\": \"value\",\r\n"
                + "    \"boolean\": true,\r\n"
                + "    \"array\": [],\r\n"
                + "    \"object\": {},\r\n"
                + "    \"null\": null\r\n"
                + "  }\r\n"
                + "}";
        ObjectMapper objectMapper = JsonMapperFactory.getInstance()
                .copy()
                .enable(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS);
        JsonNode root = JsonNodes.readTree(objectMapper, json, LocationJsonNodeFactoryFactory.getInstance());
        JsonNode numberNode = root.at("/properties/number");
        assertEquals(3, JsonNodes.tokenLocationOf(numberNode).getLineNr());
        JsonNode stringNode = root.at("/properties/string");
        assertEquals(4, JsonNodes.tokenLocationOf(stringNode).getLineNr());
        JsonNode booleanNode = root.at("/properties/boolean");
        assertEquals(5, JsonNodes.tokenLocationOf(booleanNode).getLineNr());
        JsonNode arrayNode = root.at("/properties/array");
        assertEquals(6, JsonNodes.tokenLocationOf(arrayNode).getLineNr());
        JsonNode objectNode = root.at("/properties/object");
        assertEquals(7, JsonNodes.tokenLocationOf(objectNode).getLineNr());
        JsonNode nullNode = root.at("/properties/null");
        assertEquals(8, JsonNodes.tokenLocationOf(nullNode).getLineNr());
    }
}
