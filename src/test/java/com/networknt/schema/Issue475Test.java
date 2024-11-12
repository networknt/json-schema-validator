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

import java.util.Set;

import org.junit.jupiter.api.Test;

import com.networknt.schema.SpecVersion.VersionFlag;
import com.networknt.schema.serialization.JsonMapperFactory;

/**
 * Tests for validation of schema against meta schema.
 */
class Issue475Test {
    private static final String VALID_INPUT = "{  \n"
            + "  \"type\": \"object\",  \n"
            + "  \"properties\": {    \n"
            + "    \"key\": { \n"
            + "      \"title\" : \"My key\", \n"
            + "      \"type\": \"array\" \n"
            + "    } \n"
            + "  }\n"
            + "}";
    
    private static final String INVALID_INPUT = "{  \n"
            + "  \"type\": \"object\",  \n"
            + "  \"properties\": {    \n"
            + "    \"key\": { \n"
            + "      \"title\" : \"My key\", \n"
            + "      \"type\": \"blabla\" \n"
            + "    } \n"
            + "  }\n"
            + "}";

    @Test
    void draft4() throws Exception {
        JsonSchemaFactory jsonSchemaFactory = JsonSchemaFactory.getInstance(VersionFlag.V4, builder -> builder
                .schemaMappers(schemaMappers -> schemaMappers.mapPrefix("http://json-schema.org", "classpath:")));
        SchemaValidatorsConfig config = SchemaValidatorsConfig.builder().build();
        JsonSchema schema = jsonSchemaFactory.getSchema(SchemaLocation.of(SchemaId.V4), config);

        Set<ValidationMessage> assertions = schema.validate(JsonMapperFactory.getInstance().readTree(INVALID_INPUT));
        assertEquals(2, assertions.size());
        
        assertions = schema.validate(JsonMapperFactory.getInstance().readTree(VALID_INPUT));
        assertEquals(0, assertions.size());
    }
    
    @Test
    void draft6() throws Exception {
        JsonSchemaFactory jsonSchemaFactory = JsonSchemaFactory.getInstance(VersionFlag.V6, builder -> builder
                .schemaMappers(schemaMappers -> schemaMappers.mapPrefix("http://json-schema.org", "classpath:")));
        SchemaValidatorsConfig config = SchemaValidatorsConfig.builder().build();
        JsonSchema schema = jsonSchemaFactory.getSchema(SchemaLocation.of(SchemaId.V6), config);

        Set<ValidationMessage> assertions = schema.validate(JsonMapperFactory.getInstance().readTree(INVALID_INPUT));
        assertEquals(2, assertions.size());
        
        assertions = schema.validate(JsonMapperFactory.getInstance().readTree(VALID_INPUT));
        assertEquals(0, assertions.size());
    }

    @Test
    void draft7() throws Exception {
        JsonSchemaFactory jsonSchemaFactory = JsonSchemaFactory.getInstance(VersionFlag.V7, builder -> builder
                .schemaMappers(schemaMappers -> schemaMappers.mapPrefix("http://json-schema.org", "classpath:")));
        SchemaValidatorsConfig config = SchemaValidatorsConfig.builder().build();
        JsonSchema schema = jsonSchemaFactory.getSchema(SchemaLocation.of(SchemaId.V7), config);

        Set<ValidationMessage> assertions = schema.validate(JsonMapperFactory.getInstance().readTree(INVALID_INPUT));
        assertEquals(2, assertions.size());
        
        assertions = schema.validate(JsonMapperFactory.getInstance().readTree(VALID_INPUT));
        assertEquals(0, assertions.size());
    }
    
    @Test
    void draft201909() throws Exception {
        JsonSchemaFactory jsonSchemaFactory = JsonSchemaFactory.getInstance(VersionFlag.V201909, builder -> builder
                .schemaMappers(schemaMappers -> schemaMappers.mapPrefix("https://json-schema.org", "classpath:")));
        SchemaValidatorsConfig config = SchemaValidatorsConfig.builder().build();
        JsonSchema schema = jsonSchemaFactory.getSchema(SchemaLocation.of(SchemaId.V201909), config);

        Set<ValidationMessage> assertions = schema.validate(JsonMapperFactory.getInstance().readTree(INVALID_INPUT));
        assertEquals(2, assertions.size());
        
        assertions = schema.validate(JsonMapperFactory.getInstance().readTree(VALID_INPUT));
        assertEquals(0, assertions.size());
    }

    @Test
    void draft202012() throws Exception {
        JsonSchemaFactory jsonSchemaFactory = JsonSchemaFactory.getInstance(VersionFlag.V202012, builder -> builder
                .schemaMappers(schemaMappers -> schemaMappers.mapPrefix("https://json-schema.org", "classpath:")));
        SchemaValidatorsConfig config = SchemaValidatorsConfig.builder().build();
        JsonSchema schema = jsonSchemaFactory.getSchema(SchemaLocation.of(SchemaId.V202012), config);

        Set<ValidationMessage> assertions = schema.validate(JsonMapperFactory.getInstance().readTree(INVALID_INPUT));
        assertEquals(2, assertions.size());
        
        assertions = schema.validate(JsonMapperFactory.getInstance().readTree(VALID_INPUT));
        assertEquals(0, assertions.size());
    }
}
