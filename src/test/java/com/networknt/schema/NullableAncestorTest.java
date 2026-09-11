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
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;

import java.time.Duration;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.networknt.schema.dialect.Dialect;
import com.networknt.schema.dialect.Dialects;
import com.networknt.schema.keyword.NonValidationKeyword;

/**
 * Tests for resolving the OpenAPI {@code nullable} keyword through ancestors
 * that describe the same value, on a dialect where {@code nullable} is enabled
 * alongside the 2020-12 keyword set.
 */
class NullableAncestorTest {

    private static Schema schema(String schemaData) {
        Dialect dialect = Dialect.builder(Dialects.getDraft202012())
                .keyword(new NonValidationKeyword("nullable"))
                .build();
        return SchemaRegistry.withDialect(dialect).getSchema(schemaData);
    }

    @Test
    void recursiveSchemaWithNullTerminates() {
        String schemaData = "{\r\n"
                + "  \"$ref\": \"#/$defs/X\",\r\n"
                + "  \"$defs\": {\r\n"
                + "    \"T\": { \"title\": \"t\" },\r\n"
                + "    \"X\": {\r\n"
                + "      \"$ref\": \"#/$defs/T\",\r\n"
                + "      \"type\": \"object\",\r\n"
                + "      \"properties\": { \"c\": { \"$ref\": \"#/$defs/X\" } }\r\n"
                + "    }\r\n"
                + "  }\r\n"
                + "}\r\n";
        Schema schema = schema(schemaData);
        List<Error> messages = assertTimeoutPreemptively(Duration.ofSeconds(10),
                () -> schema.validate("{ \"c\": { \"c\": null } }", InputFormat.JSON));
        assertEquals(1, messages.size());
        assertEquals("type", messages.get(0).getKeyword());
    }

    @Test
    void recursiveSchemaThroughAllOfWithNullTerminates() {
        String schemaData = "{\r\n"
                + "  \"$defs\": {\r\n"
                + "    \"LP\": { \"allOf\": [ { \"$ref\": \"#/$defs/T\" } ] },\r\n"
                + "    \"T\": {\r\n"
                + "      \"$ref\": \"#/$defs/Base\",\r\n"
                + "      \"properties\": {\r\n"
                + "        \"child\": { \"$ref\": \"#/$defs/LP\" },\r\n"
                + "        \"v\": { \"type\": \"string\" }\r\n"
                + "      }\r\n"
                + "    },\r\n"
                + "    \"Base\": { \"type\": \"object\" }\r\n"
                + "  },\r\n"
                + "  \"properties\": { \"root\": { \"$ref\": \"#/$defs/LP\" } }\r\n"
                + "}\r\n";
        Schema schema = schema(schemaData);
        List<Error> messages = assertTimeoutPreemptively(Duration.ofSeconds(10),
                () -> schema.validate("{ \"root\": { \"child\": { \"v\": null } } }", InputFormat.JSON));
        assertEquals(1, messages.size());
        assertEquals("type", messages.get(0).getKeyword());
    }

    @Test
    void nullablePropagatesThroughIfThenElse() {
        String schemaData = "{\r\n"
                + "  \"properties\": {\r\n"
                + "    \"v\": {\r\n"
                + "      \"nullable\": true,\r\n"
                + "      \"if\": { \"type\": \"object\" },\r\n"
                + "      \"then\": { \"type\": \"string\" },\r\n"
                + "      \"else\": { \"type\": \"string\" }\r\n"
                + "    }\r\n"
                + "  }\r\n"
                + "}\r\n";
        List<Error> messages = schema(schemaData).validate("{ \"v\": null }", InputFormat.JSON);
        assertEquals(0, messages.size());
    }

    @Test
    void nullablePropagatesThroughDynamicRef() {
        String schemaData = "{\r\n"
                + "  \"$defs\": {\r\n"
                + "    \"T\": { \"$dynamicAnchor\": \"T\", \"type\": \"object\" }\r\n"
                + "  },\r\n"
                + "  \"properties\": {\r\n"
                + "    \"v\": {\r\n"
                + "      \"nullable\": true,\r\n"
                + "      \"allOf\": [ { \"$dynamicRef\": \"#T\" } ]\r\n"
                + "    }\r\n"
                + "  }\r\n"
                + "}\r\n";
        List<Error> messages = schema(schemaData).validate("{ \"v\": null }", InputFormat.JSON);
        assertEquals(0, messages.size());
    }

    @Test
    void nullableDoesNotLeakIntoNotSubschema() {
        // If nullable leaked into the negated subschema, "type": "string" would
        // accept null, the subschema would match, and "not" would then fail.
        String schemaData = "{\r\n"
                + "  \"properties\": {\r\n"
                + "    \"v\": { \"nullable\": true, \"not\": { \"type\": \"string\" } }\r\n"
                + "  }\r\n"
                + "}\r\n";
        List<Error> messages = schema(schemaData).validate("{ \"v\": null }", InputFormat.JSON);
        assertEquals(0, messages.size());
    }

    @Test
    void nullableIsNotHonouredInDialectWithoutNullableKeyword() {
        String schemaData = "{\r\n"
                + "  \"properties\": {\r\n"
                + "    \"v\": {\r\n"
                + "      \"nullable\": true,\r\n"
                + "      \"allOf\": [ { \"$ref\": \"#/$defs/T\" } ]\r\n"
                + "    }\r\n"
                + "  },\r\n"
                + "  \"$defs\": { \"T\": { \"type\": \"object\" } }\r\n"
                + "}\r\n";
        Schema schema = SchemaRegistry.withDialect(Dialects.getDraft202012()).getSchema(schemaData);
        List<Error> messages = schema.validate("{ \"v\": null }", InputFormat.JSON);
        assertEquals(1, messages.size());
        assertEquals("type", messages.get(0).getKeyword());
    }

    @Test
    void nullableBesideRefIsHonouredWhenDialectKeepsSiblings() {
        // Unlike OpenAPI 3.0, 2020-12 keeps members declared alongside $ref, so
        // this nullable applies to the referenced schema's type check too.
        String schemaData = "{\r\n"
                + "  \"properties\": {\r\n"
                + "    \"v\": { \"$ref\": \"#/$defs/M\", \"nullable\": true }\r\n"
                + "  },\r\n"
                + "  \"$defs\": { \"M\": { \"type\": \"object\" } }\r\n"
                + "}\r\n";
        List<Error> messages = schema(schemaData).validate("{ \"v\": null }", InputFormat.JSON);
        assertEquals(0, messages.size());
    }

    @Test
    void refPointingIntoComposingBranchDoesNotInheritNullable() {
        String schemaData = "{\r\n"
                + "  \"nullable\": true,\r\n"
                + "  \"allOf\": [ { \"type\": \"object\", \"required\": [\"zzz\"] } ],\r\n"
                + "  \"properties\": { \"b\": { \"$ref\": \"#/allOf/0\" } }\r\n"
                + "}\r\n";
        List<Error> messages = schema(schemaData).validate("{ \"zzz\": 1, \"b\": null }", InputFormat.JSON);
        assertEquals(1, messages.size());
        assertEquals("type", messages.get(0).getKeyword());
    }
}
