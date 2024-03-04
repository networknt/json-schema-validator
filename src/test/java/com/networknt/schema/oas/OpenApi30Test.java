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

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

import com.networknt.schema.DisallowUnknownJsonMetaSchemaFactory;
import com.networknt.schema.InputFormat;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SchemaLocation;
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
}
