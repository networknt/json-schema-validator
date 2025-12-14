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
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

import tools.jackson.core.TokenStreamLocation;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import com.networknt.schema.InputFormat;
import com.networknt.schema.Schema;
import com.networknt.schema.SchemaRegistry;
import com.networknt.schema.SpecificationVersion;
import com.networknt.schema.Error;
import com.networknt.schema.serialization.JsonMapperFactory;
import com.networknt.schema.serialization.NodeReader;
import com.networknt.schema.serialization.node.LocationJsonNodeFactoryFactory;
/**
 * Tests for JsonNodes.
 */
class JsonNodesTest {
    @Test
    void location() throws IOException {
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
        TokenStreamLocation location = JsonNodes.tokenStreamLocationOf(idNode);
        assertEquals(2, location.getLineNr());
        assertEquals(10, location.getColumnNr());

        JsonNode formatNode = jsonNode.at("/properties/startDate/format");
        location = JsonNodes.tokenStreamLocationOf(formatNode);
        assertEquals(5, location.getLineNr());
        assertEquals(17, location.getColumnNr());

        JsonNode minLengthNode = jsonNode.at("/properties/startDate/minLength");
        location = JsonNodes.tokenStreamLocationOf(minLengthNode);
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
        SchemaRegistry factory = SchemaRegistry.withDefaultDialect(SpecificationVersion.DRAFT_2020_12,
                builder -> builder.nodeReader(nodeReader -> nodeReader.locationAware()));
        Schema schema = factory.getSchema(schemaData, InputFormat.JSON);
        List<Error> messages = schema.validate(inputData, InputFormat.JSON, executionContext -> {
            executionContext.executionConfig(executionConfig -> executionConfig.formatAssertionsEnabled(true));
        });
        List<Error> list = messages.stream().collect(Collectors.toList());
        Error format = list.get(0);
        TokenStreamLocation formatInstanceNodeTokenLocation = JsonNodes.tokenStreamLocationOf(format.getInstanceNode());
        TokenStreamLocation formatSchemaNodeTokenLocation = JsonNodes.tokenStreamLocationOf(format.getSchemaNode());
        Error minLength = list.get(1);
        TokenStreamLocation minLengthInstanceNodeTokenLocation = JsonNodes.tokenStreamLocationOf(minLength.getInstanceNode());
        TokenStreamLocation minLengthSchemaNodeTokenLocation = JsonNodes.tokenStreamLocationOf(minLength.getSchemaNode());

        assertEquals("format", format.getKeyword());

        assertEquals("date", format.getSchemaNode().asString());
        assertEquals(5, formatSchemaNodeTokenLocation.getLineNr());
        assertEquals(17, formatSchemaNodeTokenLocation.getColumnNr());

        assertEquals("1", format.getInstanceNode().asString());
        assertEquals(2, formatInstanceNodeTokenLocation.getLineNr());
        assertEquals(16, formatInstanceNodeTokenLocation.getColumnNr());

        assertEquals("minLength", minLength.getKeyword());

        assertEquals("6", minLength.getSchemaNode().asString());
        assertEquals(6, minLengthSchemaNodeTokenLocation.getLineNr());
        assertEquals(20, minLengthSchemaNodeTokenLocation.getColumnNr());

        assertEquals("1", minLength.getInstanceNode().asString());
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
        SchemaRegistry factory = SchemaRegistry.withDefaultDialect(SpecificationVersion.DRAFT_2020_12,
                builder -> builder.nodeReader(NodeReader.builder().locationAware().build()));
        Schema schema = factory.getSchema(schemaData, InputFormat.YAML);
        List<Error> messages = schema.validate(inputData, InputFormat.YAML, executionContext -> {
            executionContext.executionConfig(executionConfig -> executionConfig.formatAssertionsEnabled(true));
        });
        List<Error> list = messages.stream().collect(Collectors.toList());
        Error format = list.get(0);
        TokenStreamLocation formatInstanceNodeTokenLocation = JsonNodes.tokenStreamLocationOf(format.getInstanceNode());
        TokenStreamLocation formatSchemaNodeTokenLocation = JsonNodes.tokenStreamLocationOf(format.getSchemaNode());
        Error minLength = list.get(1);
        TokenStreamLocation minLengthInstanceNodeTokenLocation = JsonNodes.tokenStreamLocationOf(minLength.getInstanceNode());
        TokenStreamLocation minLengthSchemaNodeTokenLocation = JsonNodes.tokenStreamLocationOf(minLength.getSchemaNode());

        assertEquals("format", format.getKeyword());

        assertEquals("date", format.getSchemaNode().asString());
        assertEquals(5, formatSchemaNodeTokenLocation.getLineNr());
        assertEquals(13, formatSchemaNodeTokenLocation.getColumnNr());

        assertEquals("1", format.getInstanceNode().asString());
        assertEquals(2, formatInstanceNodeTokenLocation.getLineNr());
        assertEquals(12, formatInstanceNodeTokenLocation.getColumnNr());

        assertEquals("minLength", minLength.getKeyword());

        assertEquals("6", minLength.getSchemaNode().asString());
        assertEquals(6, minLengthSchemaNodeTokenLocation.getLineNr());
        assertEquals(16, minLengthSchemaNodeTokenLocation.getColumnNr());

        assertEquals("1", minLength.getInstanceNode().asString());
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
                .rebuild()
                .enable(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS).build();
        JsonNode root = JsonNodes.readTree(objectMapper, json, LocationJsonNodeFactoryFactory.getInstance());
        JsonNode numberNode = root.at("/properties/number");
        assertEquals(3, JsonNodes.tokenStreamLocationOf(numberNode).getLineNr());
        JsonNode stringNode = root.at("/properties/string");
        assertEquals(4, JsonNodes.tokenStreamLocationOf(stringNode).getLineNr());
        JsonNode booleanNode = root.at("/properties/boolean");
        assertEquals(5, JsonNodes.tokenStreamLocationOf(booleanNode).getLineNr());
        JsonNode arrayNode = root.at("/properties/array");
        assertEquals(6, JsonNodes.tokenStreamLocationOf(arrayNode).getLineNr());
        JsonNode objectNode = root.at("/properties/object");
        assertEquals(7, JsonNodes.tokenStreamLocationOf(objectNode).getLineNr());
        JsonNode nullNode = root.at("/properties/null");
        assertEquals(8, JsonNodes.tokenStreamLocationOf(nullNode).getLineNr());
    }
}
