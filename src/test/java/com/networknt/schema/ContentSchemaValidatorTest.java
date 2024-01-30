/*
 * Copyright (c) 2023 the original author or authors.
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

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.networknt.schema.SpecVersion.VersionFlag;
import com.networknt.schema.output.OutputUnit;
import com.networknt.schema.serialization.JsonMapperFactory;

/**
 * ContentSchemaValidatorTest.
 */
public class ContentSchemaValidatorTest {
    @Test
    void annotationCollection() throws JsonProcessingException {
        String schemaData = "{\r\n"
                + "    \"type\": \"string\",\r\n"
                + "    \"contentMediaType\": \"application/jwt\",\r\n"
                + "    \"contentSchema\": {\r\n"
                + "        \"type\": \"array\",\r\n"
                + "        \"minItems\": 2,\r\n"
                + "        \"prefixItems\": [\r\n"
                + "            {\r\n"
                + "                \"const\": {\r\n"
                + "                    \"typ\": \"JWT\",\r\n"
                + "                    \"alg\": \"HS256\"\r\n"
                + "                }\r\n"
                + "            },\r\n"
                + "            {\r\n"
                + "                \"type\": \"object\",\r\n"
                + "                \"required\": [\"iss\", \"exp\"],\r\n"
                + "                \"properties\": {\r\n"
                + "                    \"iss\": {\"type\": \"string\"},\r\n"
                + "                    \"exp\": {\"type\": \"integer\"}\r\n"
                + "                }\r\n"
                + "            }\r\n"
                + "        ]\r\n"
                + "    }\r\n"
                + "}";
        JsonSchemaFactory factory = JsonSchemaFactory.getInstance(VersionFlag.V202012);
        SchemaValidatorsConfig config = new SchemaValidatorsConfig();
        config.setPathType(PathType.JSON_POINTER);
        JsonSchema schema = factory.getSchema(schemaData, config);
        
        String inputData = "\"helloworld\"";
        
        OutputUnit outputUnit = schema.validate(inputData, InputFormat.JSON, OutputFormat.LIST, executionConfiguration -> {
            executionConfiguration.getExecutionConfig().setAnnotationCollectionEnabled(true);
            executionConfiguration.getExecutionConfig().setAnnotationCollectionPredicate(keyword -> true);
        });
        String output = JsonMapperFactory.getInstance().writerWithDefaultPrettyPrinter().writeValueAsString(outputUnit);
        String expected = "{\r\n"
                + "  \"valid\" : true,\r\n"
                + "  \"details\" : [ {\r\n"
                + "    \"valid\" : true,\r\n"
                + "    \"evaluationPath\" : \"\",\r\n"
                + "    \"schemaLocation\" : \"#\",\r\n"
                + "    \"instanceLocation\" : \"\",\r\n"
                + "    \"annotations\" : {\r\n"
                + "      \"contentMediaType\" : \"application/jwt\",\r\n"
                + "      \"contentSchema\" : {\r\n"
                + "        \"type\" : \"array\",\r\n"
                + "        \"minItems\" : 2,\r\n"
                + "        \"prefixItems\" : [ {\r\n"
                + "          \"const\" : {\r\n"
                + "            \"typ\" : \"JWT\",\r\n"
                + "            \"alg\" : \"HS256\"\r\n"
                + "          }\r\n"
                + "        }, {\r\n"
                + "          \"type\" : \"object\",\r\n"
                + "          \"required\" : [ \"iss\", \"exp\" ],\r\n"
                + "          \"properties\" : {\r\n"
                + "            \"iss\" : {\r\n"
                + "              \"type\" : \"string\"\r\n"
                + "            },\r\n"
                + "            \"exp\" : {\r\n"
                + "              \"type\" : \"integer\"\r\n"
                + "            }\r\n"
                + "          }\r\n"
                + "        } ]\r\n"
                + "      }\r\n"
                + "    }\r\n"
                + "  } ]\r\n"
                + "}";
        assertEquals(expected, output);
    }
}
