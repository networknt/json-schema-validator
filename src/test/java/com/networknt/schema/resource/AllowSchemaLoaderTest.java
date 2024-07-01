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
package com.networknt.schema.resource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import com.networknt.schema.InvalidSchemaException;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SchemaLocation;
import com.networknt.schema.SpecVersion.VersionFlag;

/**
 * Test for AllowSchemaLoader.
 */
class AllowSchemaLoaderTest {

    @Test
    void integration() {
        JsonSchemaFactory factory = JsonSchemaFactory.getInstance(VersionFlag.V202012,
                builder -> builder.schemaLoaders(schemaLoaders -> schemaLoaders
                        .add(new AllowSchemaLoader(iri -> iri.toString().startsWith("classpath:")))));
        InvalidSchemaException invalidSchemaException = assertThrows(InvalidSchemaException.class,
                () -> factory.getSchema(SchemaLocation.of("http://www.example.org/schema")));
        assertEquals("http://www.example.org/schema",
                invalidSchemaException.getValidationMessage().getArguments()[0].toString());
        JsonSchema schema = factory.getSchema(SchemaLocation.of("classpath:schema/example-main.json"));
        assertNotNull(schema);
    }

}
