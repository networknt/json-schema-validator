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

import java.util.List;

import org.junit.jupiter.api.Test;

import com.networknt.schema.Specification.Version;
import com.networknt.schema.dialect.DialectId;
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
        JsonSchemaFactory jsonSchemaFactory = JsonSchemaFactory.getInstance(Version.DRAFT_4, builder -> builder
                .schemaMappers(schemaMappers -> schemaMappers.mapPrefix("http://json-schema.org", "classpath:")));
        SchemaValidatorsConfig config = SchemaValidatorsConfig.builder().build();
        JsonSchema schema = jsonSchemaFactory.getSchema(SchemaLocation.of(DialectId.DRAFT_4), config);

        List<Error> assertions = schema.validate(JsonMapperFactory.getInstance().readTree(INVALID_INPUT));
        assertEquals(2, assertions.size());
        
        assertions = schema.validate(JsonMapperFactory.getInstance().readTree(VALID_INPUT));
        assertEquals(0, assertions.size());
    }
    
    @Test
    void draft6() throws Exception {
        JsonSchemaFactory jsonSchemaFactory = JsonSchemaFactory.getInstance(Version.DRAFT_6, builder -> builder
                .schemaMappers(schemaMappers -> schemaMappers.mapPrefix("http://json-schema.org", "classpath:")));
        SchemaValidatorsConfig config = SchemaValidatorsConfig.builder().build();
        JsonSchema schema = jsonSchemaFactory.getSchema(SchemaLocation.of(DialectId.DRAFT_6), config);

        List<Error> assertions = schema.validate(JsonMapperFactory.getInstance().readTree(INVALID_INPUT));
        assertEquals(2, assertions.size());
        
        assertions = schema.validate(JsonMapperFactory.getInstance().readTree(VALID_INPUT));
        assertEquals(0, assertions.size());
    }

    @Test
    void draft7() throws Exception {
        JsonSchemaFactory jsonSchemaFactory = JsonSchemaFactory.getInstance(Version.DRAFT_7, builder -> builder
                .schemaMappers(schemaMappers -> schemaMappers.mapPrefix("http://json-schema.org", "classpath:")));
        SchemaValidatorsConfig config = SchemaValidatorsConfig.builder().build();
        JsonSchema schema = jsonSchemaFactory.getSchema(SchemaLocation.of(DialectId.DRAFT_7), config);

        List<Error> assertions = schema.validate(JsonMapperFactory.getInstance().readTree(INVALID_INPUT));
        assertEquals(2, assertions.size());
        
        assertions = schema.validate(JsonMapperFactory.getInstance().readTree(VALID_INPUT));
        assertEquals(0, assertions.size());
    }
    
    @Test
    void draft201909() throws Exception {
        JsonSchemaFactory jsonSchemaFactory = JsonSchemaFactory.getInstance(Version.DRAFT_2019_09, builder -> builder
                .schemaMappers(schemaMappers -> schemaMappers.mapPrefix("https://json-schema.org", "classpath:")));
        SchemaValidatorsConfig config = SchemaValidatorsConfig.builder().build();
        JsonSchema schema = jsonSchemaFactory.getSchema(SchemaLocation.of(DialectId.DRAFT_2019_09), config);

        List<Error> assertions = schema.validate(JsonMapperFactory.getInstance().readTree(INVALID_INPUT));
        assertEquals(2, assertions.size());
        
        assertions = schema.validate(JsonMapperFactory.getInstance().readTree(VALID_INPUT));
        assertEquals(0, assertions.size());
    }

    @Test
    void draft202012() throws Exception {
        JsonSchemaFactory jsonSchemaFactory = JsonSchemaFactory.getInstance(Version.DRAFT_2020_12, builder -> builder
                .schemaMappers(schemaMappers -> schemaMappers.mapPrefix("https://json-schema.org", "classpath:")));
        SchemaValidatorsConfig config = SchemaValidatorsConfig.builder().build();
        JsonSchema schema = jsonSchemaFactory.getSchema(SchemaLocation.of(DialectId.DRAFT_2020_12), config);

        List<Error> assertions = schema.validate(JsonMapperFactory.getInstance().readTree(INVALID_INPUT));
        assertEquals(2, assertions.size());
        
        assertions = schema.validate(JsonMapperFactory.getInstance().readTree(VALID_INPUT));
        assertEquals(0, assertions.size());
    }
}
