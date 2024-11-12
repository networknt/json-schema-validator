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
package com.networknt.schema.oas;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

import com.networknt.schema.DisallowUnknownJsonMetaSchemaFactory;
import com.networknt.schema.InputFormat;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.OutputFormat;
import com.networknt.schema.PathType;
import com.networknt.schema.SchemaLocation;
import com.networknt.schema.SchemaValidatorsConfig;
import com.networknt.schema.SpecVersion.VersionFlag;
import com.networknt.schema.ValidationMessage;

/**
 * OpenApi30Test.
 */
class OpenApi30Test {
    /**
     * Test with the explicitly configured OpenApi30 instance.
     */
    @Test
    void validateMetaSchema() {
        JsonSchemaFactory factory = JsonSchemaFactory.getInstance(VersionFlag.V7,
                builder -> builder.metaSchema(OpenApi30.getInstance())
                        .defaultMetaSchemaIri(OpenApi30.getInstance().getIri())
                        .metaSchemaFactory(DisallowUnknownJsonMetaSchemaFactory.getInstance()));
        JsonSchema schema = factory.getSchema(SchemaLocation.of(
                "classpath:schema/oas/3.0/petstore.yaml#/paths/~1pet/post/requestBody/content/application~1json/schema"));
        String input = "{\r\n"
                + "  \"petType\": \"dog\",\r\n"
                + "  \"bark\": \"woof\"\r\n"
                + "}";
        Set<ValidationMessage> messages = schema.validate(input, InputFormat.JSON);
        assertEquals(0, messages.size());

        String invalid = "{\r\n"
                + "  \"petType\": \"dog\",\r\n"
                + "  \"meow\": \"meeeooow\"\r\n"
                + "}";
        messages = schema.validate(invalid, InputFormat.JSON);
        assertEquals(2, messages.size());
        List<ValidationMessage> list = messages.stream().collect(Collectors.toList());
        assertEquals("oneOf", list.get(0).getType());
        assertEquals("required", list.get(1).getType());
        assertEquals("bark", list.get(1).getProperty());
    }

    /**
     * Tests that schema location with number in fragment can resolve.
     */
    @Test
    void jsonPointerWithNumberInFragment() {
        JsonSchemaFactory factory = JsonSchemaFactory.getInstance(VersionFlag.V7, builder -> builder
                .metaSchema(OpenApi30.getInstance()).defaultMetaSchemaIri(OpenApi30.getInstance().getIri()));
        JsonSchema schema = factory.getSchema(SchemaLocation.of(
                "classpath:schema/oas/3.0/petstore.yaml#/paths/~1pet/post/responses/200/content/application~1json/schema"),
                SchemaValidatorsConfig.builder().pathType(PathType.JSON_PATH).build());
        assertNotNull(schema);
        assertEquals("$.paths['/pet'].post.responses['200'].content['application/json'].schema",
                schema.getEvaluationPath().toString());
    }

    /**
     * Exclusive maximum true.
     */
    @Test
    void exclusiveMaximum() {
        String schemaData = "{\r\n"
                + "  \"type\": \"number\",\r\n"
                + "  \"minimum\": 0,\r\n"
                + "  \"maximum\": 100,\r\n"
                + "  \"exclusiveMaximum\": true\r\n"
                + "}\r\n";
        JsonSchemaFactory factory = JsonSchemaFactory.getInstance(VersionFlag.V7, builder -> builder
                .metaSchema(OpenApi30.getInstance()).defaultMetaSchemaIri(OpenApi30.getInstance().getIri()));
        JsonSchema schema = factory.getSchema(schemaData);
        assertFalse(schema.validate("100", InputFormat.JSON, OutputFormat.BOOLEAN));
    }

    /**
     * Exclusive minimum true.
     */
    @Test
    void exclusiveMinimum() {
        String schemaData = "{\r\n"
                + "  \"type\": \"number\",\r\n"
                + "  \"minimum\": 0,\r\n"
                + "  \"maximum\": 100,\r\n"
                + "  \"exclusiveMinimum\": true\r\n"
                + "}\r\n";
        JsonSchemaFactory factory = JsonSchemaFactory.getInstance(VersionFlag.V7, builder -> builder
                .metaSchema(OpenApi30.getInstance()).defaultMetaSchemaIri(OpenApi30.getInstance().getIri()));
        JsonSchema schema = factory.getSchema(schemaData);
        assertFalse(schema.validate("0", InputFormat.JSON, OutputFormat.BOOLEAN));
    }
}
