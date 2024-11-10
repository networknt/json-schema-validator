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
package com.networknt.schema.serialization;

import static org.junit.jupiter.api.Assertions.assertEquals;
import java.io.IOException;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.JsonLocation;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonNode;
import com.networknt.schema.InputFormat;
import com.networknt.schema.utils.JsonNodes;

/**
 * Test for Default Object Reader.
 */
class DefaultJsonNodeReaderTest {
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
        JsonNode jsonNode = JsonNodeReader.builder().locationAware().build().readTree(schemaData, InputFormat.JSON);
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
    void jsonLocation() throws IOException {
        String schemaData = "{\r\n"
                + "  \"$id\": \"https://schema/myschema\",\r\n"
                + "  \"properties\": {\r\n"
                + "    \"startDate\": {\r\n"
                + "      \"format\": \"date\",\r\n"
                + "      \"minLength\": 6\r\n"
                + "    }\r\n"
                + "  }\r\n"
                + "}";
        JsonNode jsonNode = JsonNodeReader.builder().locationAware().build().readTree(schemaData, InputFormat.JSON);

        JsonLocation formatSchemaNodeTokenLocation = JsonNodes.tokenLocationOf(jsonNode.at("/properties/startDate/format"));
        JsonLocation minLengthSchemaNodeTokenLocation = JsonNodes.tokenLocationOf(jsonNode.at("/properties/startDate/minLength"));

        assertEquals(5, formatSchemaNodeTokenLocation.getLineNr());
        assertEquals(17, formatSchemaNodeTokenLocation.getColumnNr());

        assertEquals(6, minLengthSchemaNodeTokenLocation.getLineNr());
        assertEquals(20, minLengthSchemaNodeTokenLocation.getColumnNr());
    }

    @Test
    void yamlLocation() throws IOException {
        String schemaData = "---\r\n"
                + "\"$id\": 'https://schema/myschema'\r\n"
                + "properties:\r\n"
                + "  startDate:\r\n"
                + "    format: 'date'\r\n"
                + "    minLength: 6\r\n";
        JsonNode jsonNode = JsonNodeReader.builder().locationAware().build().readTree(schemaData, InputFormat.YAML);

        JsonLocation formatSchemaNodeTokenLocation = JsonNodes.tokenLocationOf(jsonNode.at("/properties/startDate/format"));
        JsonLocation minLengthSchemaNodeTokenLocation = JsonNodes.tokenLocationOf(jsonNode.at("/properties/startDate/minLength"));

        assertEquals(5, formatSchemaNodeTokenLocation.getLineNr());
        assertEquals(13, formatSchemaNodeTokenLocation.getColumnNr());

        assertEquals(6, minLengthSchemaNodeTokenLocation.getLineNr());
        assertEquals(16, minLengthSchemaNodeTokenLocation.getColumnNr());
    }
}
