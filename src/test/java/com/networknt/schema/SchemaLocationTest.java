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

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class SchemaLocationTest {

    @Test
    void ofAbsoluteIri() {
        SchemaLocation schemaLocation = SchemaLocation.of("https://json-schema.org/draft/2020-12/schema");
        assertEquals("https://json-schema.org/draft/2020-12/schema", schemaLocation.getAbsoluteIri().toString());
        assertEquals(0, schemaLocation.getFragment().getNameCount());
        assertEquals("https://json-schema.org/draft/2020-12/schema#", schemaLocation.toString());
    }

    @Test
    void ofAbsoluteIriWithJsonPointer() {
        SchemaLocation schemaLocation = SchemaLocation.of("https://json-schema.org/draft/2020-12/schema#/properties/0");
        assertEquals("https://json-schema.org/draft/2020-12/schema", schemaLocation.getAbsoluteIri().toString());
        assertEquals(2, schemaLocation.getFragment().getNameCount());
        assertEquals("https://json-schema.org/draft/2020-12/schema#/properties/0", schemaLocation.toString());
        assertEquals("/properties/0", schemaLocation.getFragment().toString());
    }

    @Test
    void ofAbsoluteIriWithAnchor() {
        SchemaLocation schemaLocation = SchemaLocation.of("https://example.com/schemas/address#street_address");
        assertEquals("https://example.com/schemas/address", schemaLocation.getAbsoluteIri().toString());
        assertEquals(1, schemaLocation.getFragment().getNameCount());
        assertEquals("https://example.com/schemas/address#street_address", schemaLocation.toString());
        assertEquals("street_address", schemaLocation.getFragment().toString());
    }

    @Test
    void document() {
        assertEquals(SchemaLocation.DOCUMENT.toString(), "#");
    }

}
