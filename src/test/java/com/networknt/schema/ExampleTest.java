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

import java.util.Set;

import org.junit.jupiter.api.Test;

import com.networknt.schema.SpecVersion.VersionFlag;

class ExampleTest {
    @Test
    void exampleSchemaLocation() {
        // This creates a schema factory that will use Draft 2012-12 as the default if $schema is not specified in the initial schema
        JsonSchemaFactory jsonSchemaFactory = JsonSchemaFactory.getInstance(VersionFlag.V202012, builder -> 
            builder.schemaMappers(schemaMappers -> schemaMappers.mapPrefix("https://www.example.org/", "classpath:schema/"))
        );
        SchemaValidatorsConfig config = SchemaValidatorsConfig.builder().build();
        JsonSchema schema = jsonSchemaFactory.getSchema(SchemaLocation.of("https://www.example.org/example-main.json"), config);
        String input = "{\r\n"
                + "  \"DriverProperties\": {\r\n"
                + "    \"CommonProperties\": {\r\n"
                + "      \"field2\": \"abc-def-xyz\"\r\n"
                + "    }\r\n"
                + "  }\r\n"
                + "}";
        // The example-main.json schema defines $schema with Draft 07
        assertEquals(SchemaId.V7, schema.getValidationContext().getMetaSchema().getIri());
        Set<ValidationMessage> assertions = schema.validate(input, InputFormat.JSON);
        assertEquals(1, assertions.size());
        
        // The example-ref.json schema defines $schema with Draft 2019-09
        JsonSchema refSchema = schema.getValidationContext().getSchemaResources().get("https://www.example.org/example-ref.json#");
        assertEquals(SchemaId.V201909, refSchema.getValidationContext().getMetaSchema().getIri());
    }

    @Test
    void exampleClasspath() {
        // This creates a schema factory that will use Draft 2012-12 as the default if $schema is not specified in the initial schema
        JsonSchemaFactory jsonSchemaFactory = JsonSchemaFactory.getInstance(VersionFlag.V202012);
        SchemaValidatorsConfig config = SchemaValidatorsConfig.builder().build();
        JsonSchema schema = jsonSchemaFactory.getSchema(SchemaLocation.of("classpath:schema/example-main.json"), config);
        String input = "{\r\n"
                + "  \"DriverProperties\": {\r\n"
                + "    \"CommonProperties\": {\r\n"
                + "      \"field2\": \"abc-def-xyz\"\r\n"
                + "    }\r\n"
                + "  }\r\n"
                + "}";
        // The example-main.json schema defines $schema with Draft 07
        assertEquals(SchemaId.V7, schema.getValidationContext().getMetaSchema().getIri());
        Set<ValidationMessage> assertions = schema.validate(input, InputFormat.JSON);
        assertEquals(1, assertions.size());
        
        // The example-ref.json schema defines $schema with Draft 2019-09
        JsonSchema refSchema = schema.getValidationContext().getSchemaResources().get("classpath:schema/example-ref.json#");
        assertEquals(SchemaId.V201909, refSchema.getValidationContext().getMetaSchema().getIri());
    }
}
