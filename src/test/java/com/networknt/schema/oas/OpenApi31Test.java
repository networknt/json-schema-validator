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
 * OpenApi31Test.
 */
class OpenApi31Test {
    /**
     * Test using vocabulary.
     */
    @Test
    void validateVocabulary() {
        JsonSchemaFactory factory = JsonSchemaFactory.getInstance(VersionFlag.V202012,
                builder -> builder.schemaMappers(schemaMappers -> schemaMappers
                        .mapPrefix("https://spec.openapis.org/oas/3.1", "classpath:oas/3.1")));
        JsonSchema schema = factory
                .getSchema(SchemaLocation.of("classpath:schema/oas/3.1/petstore.yaml#/components/schemas/PetResponse"));
        String input = "{\r\n"
                + "  \"petType\": \"dog\",\r\n"
                + "  \"bark\": \"woof\"\r\n"
                + "}";
        List<ValidationMessage> messages = schema.validate(input, InputFormat.JSON);
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
     * Test with the explicitly configured OpenApi31 instance.
     */
    @Test
    void validateMetaSchema() {
        JsonSchemaFactory factory = JsonSchemaFactory.getInstance(VersionFlag.V202012,
                builder -> builder.metaSchema(OpenApi31.getInstance())
                        .metaSchemaFactory(DisallowUnknownJsonMetaSchemaFactory.getInstance()));
        JsonSchema schema = factory
                .getSchema(SchemaLocation.of("classpath:schema/oas/3.1/petstore.yaml#/components/schemas/PetResponse"));
        String input = "{\r\n"
                + "  \"petType\": \"dog\",\r\n"
                + "  \"bark\": \"woof\"\r\n"
                + "}";
        List<ValidationMessage> messages = schema.validate(input, InputFormat.JSON);
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
     * Test oneOf with multiple matches should fail. Note that the discriminator
     * does not affect the validation outcome.
     */
    @Test
    void discriminatorOneOfMultipleMatchShouldFail() {
        JsonSchemaFactory factory = JsonSchemaFactory.getInstance(VersionFlag.V202012,
                builder -> builder.metaSchema(OpenApi31.getInstance())
                        .metaSchemaFactory(DisallowUnknownJsonMetaSchemaFactory.getInstance()));
        JsonSchema schema = factory
                .getSchema(SchemaLocation.of("classpath:schema/oas/3.1/petstore.yaml#/components/schemas/PetResponse"));
        String input = "{\r\n"
                + "  \"petType\": \"dog\",\r\n"
                + "  \"bark\": \"woof\",\r\n"
                + "  \"lovesRocks\": true\r\n"
                + "}";
        List<ValidationMessage> messages = schema.validate(input, InputFormat.JSON);
        List<ValidationMessage> list = messages.stream().collect(Collectors.toList());
        assertEquals("oneOf", list.get(0).getType());
    }

    /**
     * Test oneOf with no matches should fail with only the errors from the discriminator.
     */
    @Test
    void discriminatorOneOfNoMatchShouldFail() {
        JsonSchemaFactory factory = JsonSchemaFactory.getInstance(VersionFlag.V202012,
                builder -> builder.metaSchema(OpenApi31.getInstance())
                        .metaSchemaFactory(DisallowUnknownJsonMetaSchemaFactory.getInstance()));
        JsonSchema schema = factory
                .getSchema(SchemaLocation.of("classpath:schema/oas/3.1/petstore.yaml#/components/schemas/PetResponse"));
        String input = "{\r\n"
                + "  \"petType\": \"lizard\",\r\n"
                + "  \"none\": true\r\n"
                + "}";
        List<ValidationMessage> messages = schema.validate(input, InputFormat.JSON);
        List<ValidationMessage> list = messages.stream().collect(Collectors.toList());
        assertEquals("oneOf", list.get(0).getType());
        assertEquals("required", list.get(1).getType());
        assertEquals("lovesRocks", list.get(1).getProperty());
    }

    /**
     * Test oneOf with one match but incorrect discriminator should succeed. Note
     * that the discriminator does not affect the validation outcome.
     */
    @Test
    void discriminatorOneOfOneMatchWrongDiscriminatorShouldSucceed() {
        JsonSchemaFactory factory = JsonSchemaFactory.getInstance(VersionFlag.V202012,
                builder -> builder.metaSchema(OpenApi31.getInstance())
                        .metaSchemaFactory(DisallowUnknownJsonMetaSchemaFactory.getInstance()));
        JsonSchema schema = factory
                .getSchema(SchemaLocation.of("classpath:schema/oas/3.1/petstore.yaml#/components/schemas/PetResponse"));
        String input = "{\r\n"
                + "  \"petType\": \"dog\",\r\n"
                + "  \"lovesRocks\": true\r\n"
                + "}";
        List<ValidationMessage> messages = schema.validate(input, InputFormat.JSON);
        assertEquals(0, messages.size());
    }

}
