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
        String schemaData = """
                {
                  "$ref": "#/$defs/X",
                  "$defs": {
                    "T": { "title": "t" },
                    "X": {
                      "$ref": "#/$defs/T",
                      "type": "object",
                      "properties": { "c": { "$ref": "#/$defs/X" } }
                    }
                  }
                }
                """;
        Schema schema = schema(schemaData);
        List<Error> messages = assertTimeoutPreemptively(Duration.ofSeconds(10),
                () -> schema.validate("{ \"c\": { \"c\": null } }", InputFormat.JSON));
        assertEquals(1, messages.size());
        assertEquals("type", messages.get(0).getKeyword());
    }

    @Test
    void recursiveSchemaThroughAllOfWithNullTerminates() {
        String schemaData = """
                {
                  "$defs": {
                    "LP": { "allOf": [ { "$ref": "#/$defs/T" } ] },
                    "T": {
                      "$ref": "#/$defs/Base",
                      "properties": {
                        "child": { "$ref": "#/$defs/LP" },
                        "v": { "type": "string" }
                      }
                    },
                    "Base": { "type": "object" }
                  },
                  "properties": { "root": { "$ref": "#/$defs/LP" } }
                }
                """;
        Schema schema = schema(schemaData);
        List<Error> messages = assertTimeoutPreemptively(Duration.ofSeconds(10),
                () -> schema.validate("{ \"root\": { \"child\": { \"v\": null } } }", InputFormat.JSON));
        assertEquals(1, messages.size());
        assertEquals("type", messages.get(0).getKeyword());
    }

    @Test
    void nullablePropagatesThroughIfThenElse() {
        String schemaData = """
                {
                  "properties": {
                    "v": {
                      "nullable": true,
                      "if": { "type": "object" },
                      "then": { "type": "string" },
                      "else": { "type": "string" }
                    }
                  }
                }
                """;
        List<Error> messages = schema(schemaData).validate("{ \"v\": null }", InputFormat.JSON);
        assertEquals(0, messages.size());
    }

    @Test
    void nullablePropagatesThroughDynamicRef() {
        String schemaData = """
                {
                  "$defs": {
                    "T": { "$dynamicAnchor": "T", "type": "object" }
                  },
                  "properties": {
                    "v": {
                      "nullable": true,
                      "allOf": [ { "$dynamicRef": "#T" } ]
                    }
                  }
                }
                """;
        List<Error> messages = schema(schemaData).validate("{ \"v\": null }", InputFormat.JSON);
        assertEquals(0, messages.size());
    }

    @Test
    void nullableDoesNotLeakIntoNotSubschema() {
        // If nullable leaked into the negated subschema, "type": "string" would
        // accept null, the subschema would match, and "not" would then fail.
        String schemaData = """
                {
                  "properties": {
                    "v": { "nullable": true, "not": { "type": "string" } }
                  }
                }
                """;
        List<Error> messages = schema(schemaData).validate("{ \"v\": null }", InputFormat.JSON);
        assertEquals(0, messages.size());
    }

    @Test
    void nullableIsNotHonouredInDialectWithoutNullableKeyword() {
        String schemaData = """
                {
                  "properties": {
                    "v": {
                      "nullable": true,
                      "allOf": [ { "$ref": "#/$defs/T" } ]
                    }
                  },
                  "$defs": { "T": { "type": "object" } }
                }
                """;
        Schema schema = SchemaRegistry.withDialect(Dialects.getDraft202012()).getSchema(schemaData);
        List<Error> messages = schema.validate("{ \"v\": null }", InputFormat.JSON);
        assertEquals(1, messages.size());
        assertEquals("type", messages.get(0).getKeyword());
    }

    @Test
    void nullableBesideRefIsHonouredWhenDialectKeepsSiblings() {
        // Unlike OpenAPI 3.0, 2020-12 keeps members declared alongside $ref, so
        // this nullable applies to the referenced schema's type check too.
        String schemaData = """
                {
                  "properties": {
                    "v": { "$ref": "#/$defs/M", "nullable": true }
                  },
                  "$defs": { "M": { "type": "object" } }
                }
                """;
        List<Error> messages = schema(schemaData).validate("{ \"v\": null }", InputFormat.JSON);
        assertEquals(0, messages.size());
    }

    @Test
    void refPointingIntoComposingBranchDoesNotInheritNullable() {
        String schemaData = """
                {
                  "nullable": true,
                  "allOf": [ { "type": "object", "required": ["zzz"] } ],
                  "properties": { "b": { "$ref": "#/allOf/0" } }
                }
                """;
        List<Error> messages = schema(schemaData).validate("{ \"zzz\": 1, \"b\": null }", InputFormat.JSON);
        assertEquals(1, messages.size());
        assertEquals("type", messages.get(0).getKeyword());
    }
}
