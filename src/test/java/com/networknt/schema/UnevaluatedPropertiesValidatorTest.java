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
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

import com.networknt.schema.SpecVersion.VersionFlag;
import com.networknt.schema.output.OutputUnit;

/**
 * UnevaluatedPropertiesValidatorTest.
 */
class UnevaluatedPropertiesValidatorTest {
    /**
     * Issue 962.
     */
    @Test
    void annotationsOnlyDroppedAtTheEndOfSchemaProcessing() {
        String schemaData = "{\r\n"
                + "  \"type\": \"object\",\r\n"
                + "  \"required\": [\r\n"
                + "    \"key1\"\r\n"
                + "  ],\r\n"
                + "  \"properties\": {\r\n"
                + "    \"key1\": {\r\n"
                + "      \"type\": \"string\"\r\n"
                + "    },\r\n"
                + "    \"key2\": {\r\n"
                + "      \"type\": \"string\"\r\n"
                + "    },\r\n"
                + "    \"key3\": {\r\n"
                + "      \"type\": \"string\"\r\n"
                + "    }\r\n"
                + "  },\r\n"
                + "  \"unevaluatedProperties\": false\r\n"
                + "}";
        String inputData = "{\r\n"
                + "    \"key2\": \"value2\",\r\n"
                + "    \"key3\": \"value3\",\r\n"
                + "    \"key4\": \"value4\"\r\n"
                + "}";
        JsonSchema schema = JsonSchemaFactory.getInstance(VersionFlag.V202012).getSchema(schemaData);
        Set<ValidationMessage> messages = schema.validate(inputData, InputFormat.JSON);
        assertEquals(2, messages.size());
        List<ValidationMessage> assertions = messages.stream().collect(Collectors.toList());
        assertEquals("required", assertions.get(0).getType());
        assertEquals("key1", assertions.get(0).getProperty());
        assertEquals("unevaluatedProperties", assertions.get(1).getType());
        assertEquals("key4", assertions.get(1).getProperty());
    }

    /**
     * Issue 967.
     */
    @Test
    void subschemaProcessing() {
        String schemaData = "{\r\n"
                + "  \"$schema\": \"https://json-schema.org/draft/2019-09/schema\",\r\n"
                + "  \"$defs\" : {\r\n"
                + "    \"subschema\": {\r\n"
                + "      \"type\": \"object\",\r\n"
                + "      \"required\": [\"group\"],\r\n"
                + "      \"properties\": {\r\n"
                + "        \"group\": {\r\n"
                + "          \"type\": \"object\",\r\n"
                + "          \"additionalProperties\": false,\r\n"
                + "          \"required\": [\"parentprop\"],\r\n"
                + "          \"properties\": {\r\n"
                + "            \"parentprop\": {\r\n"
                + "              \"type\": \"string\"\r\n"
                + "            }\r\n"
                + "          }\r\n"
                + "        }\r\n"
                + "      }\r\n"
                + "    }\r\n"
                + "  },\r\n"
                + "  \"type\": \"object\",\r\n"
                + "  \"unevaluatedProperties\": false,\r\n"
                + "  \"allOf\": [\r\n"
                + "    {\"properties\": { \"group\" : {\"type\":\"object\"} } },\r\n"
                + "    {\"$ref\": \"#/$defs/subschema\"}\r\n"
                + "  ],\r\n"
                + "  \"required\": [\"childprop\"],\r\n"
                + "  \"properties\": {\r\n"
                + "    \"childprop\": {\r\n"
                + "      \"type\": \"string\"\r\n"
                + "    }\r\n"
                + "  }\r\n"
                + "}";
        String inputData = "{\r\n"
                + "  \"childprop\": \"something\",\r\n"
                + "  \"group\": {\r\n"
                + "     \"parentprop\":\"something\",\r\n"
                + "     \"notallowed\": false\r\n"
                + "  }\r\n"
                + "}";
        JsonSchema schema = JsonSchemaFactory.getInstance(VersionFlag.V201909).getSchema(schemaData);
        Set<ValidationMessage> messages = schema.validate(inputData, InputFormat.JSON);
        assertEquals(1, messages.size());
        List<ValidationMessage> assertions = messages.stream().collect(Collectors.toList());
        assertEquals("additionalProperties", assertions.get(0).getType());
        assertEquals("notallowed", assertions.get(0).getProperty());
    }

    @Test
    void unevaluatedPropertiesSchema() {
        String schemaData = "{\r\n"
                + "  \"oneOf\": [\r\n"
                + "    { \r\n"
                + "      \"type\" : \"object\" ,\r\n"
                + "      \"properties\" : {\r\n"
                + "        \"prop\" : { \"type\" : \"integer\" }\r\n"
                + "      }\r\n"
                + "    }\r\n"
                + "  ],\r\n"
                + "  \"unevaluatedProperties\" : { \"type\" : \"string\" }\r\n"
                + "}";
        String inputData = "{\r\n"
                + "  \"prop\": 1,\r\n"
                + "  \"group\": {\r\n"
                + "     \"parentprop\":\"something\",\r\n"
                + "     \"notallowed\": false\r\n"
                + "  }\r\n"
                + "}";
        JsonSchema schema = JsonSchemaFactory.getInstance(VersionFlag.V201909).getSchema(schemaData);
        Set<ValidationMessage> messages = schema.validate(inputData, InputFormat.JSON);
        assertEquals(1, messages.size());
        List<ValidationMessage> assertions = messages.stream().collect(Collectors.toList());
        assertEquals("type", assertions.get(0).getType());
        assertEquals("$.unevaluatedProperties.type", assertions.get(0).getEvaluationPath().toString());
    }

    @Test
    void ref() {
        String schemaData = "{\r\n"
                + "    \"definitions\": {\r\n"
                + "        \"other\": {\r\n"
                + "            \"type\": \"object\",\r\n"
                + "            \"properties\": {\r\n"
                + "                \"surfboard\": {\r\n"
                + "                    \"type\": \"string\"\r\n"
                + "                }\r\n"
                + "            }\r\n"
                + "        }\r\n"
                + "    },\r\n"
                + "    \"allOf\": [\r\n"
                + "        {\r\n"
                + "            \"$ref\": \"#/definitions/other\"\r\n"
                + "        },\r\n"
                + "        {\r\n"
                + "            \"properties\": {\r\n"
                + "                \"wheels\": {},\r\n"
                + "                \"headlights\": {}\r\n"
                + "            }\r\n"
                + "        },\r\n"
                + "        {\r\n"
                + "            \"properties\": {\r\n"
                + "                \"pontoons\": {}\r\n"
                + "            }\r\n"
                + "        },\r\n"
                + "        {\r\n"
                + "            \"properties\": {\r\n"
                + "                \"wings\": {}\r\n"
                + "            }\r\n"
                + "        }\r\n"
                + "    ],\r\n"
                + "    \"unevaluatedProperties\": false\r\n"
                + "}";
        String inputData = "{ \"pontoons\": {}, \"wheels\": {}, \"surfboard\": \"2\" }";
        JsonSchema schema = JsonSchemaFactory.getInstance(VersionFlag.V201909).getSchema(schemaData);
        Set<ValidationMessage> messages = schema.validate(inputData, InputFormat.JSON);
        assertEquals(0, messages.size());
    }

    @Test
    void nestedRef() {
        String schemaData = "{\r\n"
                + "  \"$schema\": \"https://json-schema.org/draft/2019-09/schema\",\r\n"
                + "  \"type\": \"object\",\r\n"
                + "  \"allOf\": [ { \"$ref\": \"https://www.example.org/PrimaryDeviceConfiguration.json#PrimaryDeviceConfiguration\" } ],\r\n"
                + "  \"properties\": {\r\n"
                + "    \"__type\": { \"const\": \"dk.cachet.carp.common.application.devices.Smartphone\" }\r\n"
                + "  },\r\n"
                + "  \"unevaluatedProperties\": false\r\n"
                + "}";
        String primaryDeviceConfiguration =  "{\r\n"
                + "  \"$schema\": \"https://json-schema.org/draft/2019-09/schema\",\r\n"
                + "    \"PrimaryDeviceConfiguration\": {\r\n"
                + "      \"$anchor\": \"PrimaryDeviceConfiguration\",\r\n"
                + "      \"allOf\": [ { \"$ref\": \"DeviceConfiguration.json#DeviceConfiguration\" } ],\r\n"
                + "      \"properties\": {\r\n"
                + "        \"isPrimaryDevice\": { \"const\": true }\r\n"
                + "      },\r\n"
                + "      \"required\": [ \"isPrimaryDevice\" ]\r\n"
                + "    }\r\n"
                + "  }";
        String deviceConfiguration = "{\r\n"
                + "  \"$schema\": \"https://json-schema.org/draft/2019-09/schema\",\r\n"
                + "    \"DeviceConfiguration\": {\r\n"
                + "      \"properties\": {\r\n"
                + "        \"roleName\": { \"type\": \"string\" }\r\n"
                + "      }\r\n"
                + "    }\r\n"
                + "}";
        Map<String, String> schemas = new HashMap<>();
        schemas.put("https://www.example.org/PrimaryDeviceConfiguration.json", primaryDeviceConfiguration);
        schemas.put("https://www.example.org/DeviceConfiguration.json", deviceConfiguration);
        JsonSchema schema = JsonSchemaFactory
                .getInstance(VersionFlag.V201909,
                        builder -> builder.schemaLoaders(schemaLoaders -> schemaLoaders.schemas(schemas)))
                .getSchema(schemaData);
        String inputData = "{ \"isPrimaryDevice\": true, \"roleName\": \"hello\" }";
        OutputUnit outputUnit = schema.validate(inputData, InputFormat.JSON, OutputFormat.HIERARCHICAL,
                executionContext -> {
                    executionContext.getExecutionConfig().setAnnotationCollectionEnabled(false);
                    executionContext.getExecutionConfig().setAnnotationCollectionFilter(keyword -> true);
                });
        assertTrue(outputUnit.isValid());
    }
}
