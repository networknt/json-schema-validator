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
    void ofDocument() {
        assertEquals(SchemaLocation.DOCUMENT, SchemaLocation.of("#"));
    }

    @Test
    void document() {
        assertEquals(SchemaLocation.DOCUMENT.toString(), "#");
    }

    @Test
    void schemaLocationResolveDocument() {
        SchemaLocation schemaLocation = SchemaLocation.of("https://example.com/schemas/address#street_address");
        assertEquals("https://example.com/schemas/address#", SchemaLocation.resolve(schemaLocation, "#"));
    }

    @Test
    void schemaLocationResolveDocumentPointer() {
        SchemaLocation schemaLocation = SchemaLocation.of("https://example.com/schemas/address#street_address");
        assertEquals("https://example.com/schemas/address#/allOf/12/properties",
                SchemaLocation.resolve(schemaLocation, "#/allOf/12/properties"));
    }

    @Test
    void schemaLocationResolveEmptyString() {
        SchemaLocation schemaLocation = SchemaLocation.of("https://example.com/schemas/address#street_address");
        assertEquals("https://example.com/schemas/address#", SchemaLocation.resolve(schemaLocation, ""));
    }

    @Test
    void schemaLocationResolveRelative() {
        SchemaLocation schemaLocation = SchemaLocation.of("https://example.com/schemas/address#street_address");
        assertEquals("https://example.com/schemas/test#", SchemaLocation.resolve(schemaLocation, "test"));
    }

    @Test
    void schemaLocationResolveRelativeIndex() {
        SchemaLocation schemaLocation = SchemaLocation.of("https://example.com/schemas/address/#street_address");
        assertEquals("https://example.com/schemas/address/test#", SchemaLocation.resolve(schemaLocation, "test"));
    }

    @Test
    void resolveDocument() {
        SchemaLocation schemaLocation = SchemaLocation.of("https://example.com/schemas/address#street_address");
        assertEquals("https://example.com/schemas/address#", schemaLocation.resolve("#").toString());
    }

    @Test
    void resolveDocumentPointer() {
        SchemaLocation schemaLocation = SchemaLocation.of("https://example.com/schemas/address#street_address");
        assertEquals("https://example.com/schemas/address#/allOf/10/properties",
                schemaLocation.resolve("#/allOf/10/properties").toString());
    }

    @Test
    void resolveEmptyString() {
        SchemaLocation schemaLocation = SchemaLocation.of("https://example.com/schemas/address#street_address");
        assertEquals("https://example.com/schemas/address#", schemaLocation.resolve("").toString());
    }

    @Test
    void resolveRelative() {
        SchemaLocation schemaLocation = SchemaLocation.of("https://example.com/schemas/address#street_address");
        assertEquals("https://example.com/schemas/test#", schemaLocation.resolve("test").toString());
    }

    @Test
    void resolveRelativeIndex() {
        SchemaLocation schemaLocation = SchemaLocation.of("https://example.com/schemas/address/#street_address");
        assertEquals("https://example.com/schemas/address/test#", schemaLocation.resolve("test").toString());
    }

    @Test
    void resolveNull() {
        SchemaLocation schemaLocation = new SchemaLocation(null);
        assertEquals("test#", schemaLocation.resolve("test").toString());
    }

    @Test
    void build() {
        SchemaLocation schemaLocation = SchemaLocation.builder().absoluteIri("https://example.com/schemas/address/")
                .fragment("/allOf/10/properties").build();
        assertEquals("https://example.com/schemas/address/#/allOf/10/properties", schemaLocation.toString());
        assertEquals("https://example.com/schemas/address/", schemaLocation.getAbsoluteIri().toString());
        assertEquals("/allOf/10/properties", schemaLocation.getFragment().toString());
    }

    @Test
    void append() {
        SchemaLocation schemaLocation = SchemaLocation.builder().absoluteIri("https://example.com/schemas/address/")
                .build().append("allOf").append(10).append("properties");
        assertEquals("https://example.com/schemas/address/#/allOf/10/properties", schemaLocation.toString());
        assertEquals("https://example.com/schemas/address/", schemaLocation.getAbsoluteIri().toString());
        assertEquals("/allOf/10/properties", schemaLocation.getFragment().toString());
    }

    @Test
    void anchorFragment() {
        assertTrue(SchemaLocation.Fragment.isAnchorFragment("#test"));
        assertFalse(SchemaLocation.Fragment.isAnchorFragment("#"));
        assertFalse(SchemaLocation.Fragment.isAnchorFragment("#/allOf/10/properties"));
        assertFalse(SchemaLocation.Fragment.isAnchorFragment(""));
    }

    @Test
    void jsonPointerFragment() {
        assertTrue(SchemaLocation.Fragment.isJsonPointerFragment("#/allOf/10/properties"));
        assertFalse(SchemaLocation.Fragment.isJsonPointerFragment("#"));
        assertFalse(SchemaLocation.Fragment.isJsonPointerFragment("#test"));
    }

    @Test
    void fragment() {
        assertTrue(SchemaLocation.Fragment.isFragment("#/allOf/10/properties"));
        assertTrue(SchemaLocation.Fragment.isFragment("#test"));
        assertFalse(SchemaLocation.Fragment.isFragment("test"));
    }

    @Test
    void documentFragment() {
        assertFalse(SchemaLocation.Fragment.isDocumentFragment("#/allOf/10/properties"));
        assertFalse(SchemaLocation.Fragment.isDocumentFragment("#test"));
        assertFalse(SchemaLocation.Fragment.isDocumentFragment("test"));
        assertTrue(SchemaLocation.Fragment.isDocumentFragment("#"));
    }

    @Test
    void ofNull() {
        assertNull(SchemaLocation.of(null));
    }

    @Test
    void ofEmptyString() {
        SchemaLocation schemaLocation = SchemaLocation.of("");
        assertEquals("", schemaLocation.getAbsoluteIri().toString());
        assertEquals("#", schemaLocation.toString());
    }

    @Test
    void newNull() {
        SchemaLocation schemaLocation = new SchemaLocation(null);
        assertEquals("#", schemaLocation.toString());
    }

    @Test
    void equalsEquals() {
        assertEquals(SchemaLocation.of("https://example.com/schemas/address/#street_address"),
                SchemaLocation.of("https://example.com/schemas/address/#street_address"));
    }

    @Test
    void hashCodeEquals() {
        assertEquals(SchemaLocation.of("https://example.com/schemas/address/#street_address").hashCode(),
                SchemaLocation.of("https://example.com/schemas/address/#street_address").hashCode());
    }

}
