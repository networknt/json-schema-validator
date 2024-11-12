/*
 * Copyright (c) 2020 Network New Technologies Inc.
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

import com.fasterxml.jackson.databind.JsonNode;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static com.networknt.schema.BaseJsonSchemaValidatorTest.getJsonNodeFromStringContent;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class Issue619Test extends HTTPServiceSupport {

    private JsonSchemaFactory factory;
    private JsonNode one;
    private JsonNode two;
    private JsonNode three;

    @BeforeEach
    void setup() throws Exception {
        factory = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V4);
        one = getJsonNodeFromStringContent("1");
        two = getJsonNodeFromStringContent("2");
        three = getJsonNodeFromStringContent("3");
    }

    @Test
    void bundledSchemaLoadsAndValidatesCorrectly_Ref() {
        JsonSchema referencingRootSchema = factory.getSchema("{ \"$ref\": \"resource:schema/issue619.json\" }");

        assertTrue(referencingRootSchema.validate(one).isEmpty());
        assertTrue(referencingRootSchema.validate(two).isEmpty());
        assertFalse(referencingRootSchema.validate(three).isEmpty());
    }

    @Test
    void bundledSchemaLoadsAndValidatesCorrectly_Uri() throws Exception {
        JsonSchema rootSchema = factory.getSchema(SchemaLocation.of("resource:schema/issue619.json"));

        assertTrue(rootSchema.validate(one).isEmpty());
        assertTrue(rootSchema.validate(two).isEmpty());
        assertFalse(rootSchema.validate(three).isEmpty());
    }

    @Test
    void uriWithEmptyFragment_Ref() {
        JsonSchema referencingRootSchema = factory.getSchema("{ \"$ref\": \"resource:schema/issue619.json#\" }");

        assertTrue(referencingRootSchema.validate(one).isEmpty());
        assertTrue(referencingRootSchema.validate(two).isEmpty());
        assertFalse(referencingRootSchema.validate(three).isEmpty());
    }

    @Test
    void uriWithEmptyFragment_Uri() throws Exception {
        JsonSchema rootSchema = factory.getSchema(SchemaLocation.of("resource:schema/issue619.json#"));

        assertTrue(rootSchema.validate(one).isEmpty());
        assertTrue(rootSchema.validate(two).isEmpty());
        assertFalse(rootSchema.validate(three).isEmpty());
    }

    @Test
    void uriThatPointsToTwoShouldOnlyValidateTwo_Ref() {
        JsonSchema referencingTwoSchema = factory.getSchema("{ \"$ref\": \"resource:schema/issue619.json#/definitions/two\" }");

        assertFalse(referencingTwoSchema.validate(one).isEmpty());
        assertTrue(referencingTwoSchema.validate(two).isEmpty());
        assertFalse(referencingTwoSchema.validate(three).isEmpty());
    }

    @Test
    void uriThatPointsToOneShouldOnlyValidateOne_Uri() throws Exception {
        JsonSchema oneSchema = factory.getSchema(SchemaLocation.of("resource:schema/issue619.json#/definitions/one"));

        assertTrue(oneSchema.validate(one).isEmpty());
        assertFalse(oneSchema.validate(two).isEmpty());
        assertFalse(oneSchema.validate(three).isEmpty());
    }

    @Test
    void uriThatPointsToNodeThatInTurnReferencesOneShouldOnlyValidateOne_Ref() {
        JsonSchema referencingTwoSchema = factory.getSchema("{ \"$ref\": \"resource:schema/issue619.json#/definitions/refToOne\" }");

        assertTrue(referencingTwoSchema.validate(one).isEmpty());
        assertFalse(referencingTwoSchema.validate(two).isEmpty());
        assertFalse(referencingTwoSchema.validate(three).isEmpty());
    }

    @Test
    void uriThatPointsToNodeThatInTurnReferencesOneShouldOnlyValidateOne_Uri() throws Exception {
        JsonSchema oneSchema = factory.getSchema(SchemaLocation.of("resource:schema/issue619.json#/definitions/refToOne"));

        assertTrue(oneSchema.validate(one).isEmpty());
        assertFalse(oneSchema.validate(two).isEmpty());
        assertFalse(oneSchema.validate(three).isEmpty());
    }
    
    @Test
    void uriThatPointsToSchemaWithIdThatHasDifferentUri_Ref() throws Exception {
        JsonNode oneArray = getJsonNodeFromStringContent("[[1]]");
        JsonNode textArray = getJsonNodeFromStringContent("[[\"a\"]]");

        JsonSchema schemaWithIdFromRef = factory.getSchema("{ \"$ref\": \"resource:tests/draft4/refRemote.json#/3/schema\" }");
        assertTrue(schemaWithIdFromRef.validate(oneArray).isEmpty());
        assertFalse(schemaWithIdFromRef.validate(textArray).isEmpty());
    }

    @Test
    void uriThatPointsToSchemaWithIdThatHasDifferentUri_Uri() throws Exception {
        JsonNode oneArray = getJsonNodeFromStringContent("[[1]]");
        JsonNode textArray = getJsonNodeFromStringContent("[[\"a\"]]");

        JsonSchema schemaWithIdFromUri = factory.getSchema(SchemaLocation.of("resource:tests/draft4/refRemote.json#/3/schema"));
        assertTrue(schemaWithIdFromUri.validate(oneArray).isEmpty());
        assertFalse(schemaWithIdFromUri.validate(textArray).isEmpty());
    }

    @Test
    void uriThatPointsToSchemaThatDoesNotExistShouldFail_Ref() {
        JsonSchema referencingNonexistentSchema = factory.getSchema("{ \"$ref\": \"resource:data/schema-that-does-not-exist.json#/definitions/something\" }");

        assertThrows(JsonSchemaException.class, () -> referencingNonexistentSchema.validate(one));
    }

    @Test
    void uriThatPointsToSchemaThatDoesNotExistShouldFail_Uri() {
        assertThrows(JsonSchemaException.class, () -> factory.getSchema(SchemaLocation.of("resource:data/schema-that-does-not-exist.json#/definitions/something")));
    }

    @Test
    void uriThatPointsToNodeThatDoesNotExistShouldFail_Ref() {
        JsonSchema referencingNonexistentSchema = factory.getSchema("{ \"$ref\": \"resource:schema/issue619.json#/definitions/node-that-does-not-exist\" }");

        assertThrows(JsonSchemaException.class, () -> referencingNonexistentSchema.validate(one));
    }

    @Test
    void uriThatPointsToNodeThatDoesNotExistShouldFail_Uri() {
        // This test failed before adding the 10-millisecond sleep. IllegalStateException is returned with recursive update error. This only happens on my faster desktop
        // computer during 'maven clean install'. It passes within the IDE on the same computer. It passes on my slower laptop. It passes on Travis CI.
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        assertThrows(JsonSchemaException.class, () -> factory.getSchema(SchemaLocation.of("resource:schema/issue619.json#/definitions/node-that-does-not-exist")));
    }
}
