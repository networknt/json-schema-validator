/*
 * Copyright (c) 2026 the original author or authors.
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

package com.networknt.schema.keyword;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import tools.jackson.databind.JsonNode;
import com.networknt.schema.AnnotationCapture;
import com.networknt.schema.ExecutionContext;
import com.networknt.schema.InputFormat;
import com.networknt.schema.Schema;
import com.networknt.schema.SchemaRegistry;
import com.networknt.schema.dialect.Dialect;
import com.networknt.schema.dialect.Dialects;
import com.networknt.schema.path.NodePath;
import com.networknt.schema.path.PathType;

/**
 * The value reported for the keywords that only contribute an annotation.
 * <p>
 * The value mapping lives in {@link AnnotationKeywordValues}, which {@link AnnotationKeyword}
 * and {@link AbstractKeywordValidator#putSchemaNodeAnnotation(ExecutionContext, NodePath)}
 * share, and which {@link ReadOnlyValidator} and {@link WriteOnlyValidator} use for their
 * execution policy. The keywords that use it are spread over the meta-data, content and
 * OpenAPI vocabularies, so the values are not all strings. The tests go through the public
 * API, which is how the values reach the caller, and assert both the Java type and the node
 * identity of the value.
 */
class AnnotationKeywordTest {
    private static final NodePath ROOT = new NodePath(PathType.JSON_POINTER);

    @Test
    void stringValueIsUnwrapped() {
        assertEquals("t", value("{\"title\":\"t\"}", "title", Dialects.getDraft202012()));
    }

    @Test
    void numberValueIsUnwrapped() {
        assertEquals(42, value("{\"default\":42}", "default", Dialects.getDraft202012()));
        assertEquals(1.5, value("{\"default\":1.5}", "default", Dialects.getDraft202012()));
    }

    /**
     * A boolean is the value of both readOnly and writeOnly, which are the keywords
     * that started this, and of deprecated, which is a plain annotation keyword.
     */
    @Test
    void booleanValueIsUnwrapped() {
        assertEquals(Boolean.TRUE,
                value("{\"deprecated\":true,\"readOnly\":true}", "deprecated", Dialects.getDraft202012()));
        assertEquals(Boolean.FALSE,
                value("{\"deprecated\":false,\"readOnly\":false}", "deprecated", Dialects.getDraft202012()));
    }

    /**
     * A boolean default is reported as the Java Boolean, so the value itself is
     * false while the annotation exists.
     */
    @Test
    void booleanDefaultIsReportedAsTheJavaBoolean() {
        Schema schema = schema(Dialects.getDraft202012(), "{\"default\":false}");
        assertEquals(Boolean.FALSE, value(schema, "default"));
    }

    /**
     * A meta-data keyword is allowed to be null. The value is the schema's own
     * NullNode, which is not the Java null that means there is no value.
     */
    @Test
    void nullValueIsReportedAsTheNode() {
        Object value = value("{\"default\":null}", "default", Dialects.getDraft202012());
        assertInstanceOf(JsonNode.class, value);
        assertTrue(((JsonNode) value).isNull());
    }

    /**
     * The array of examples is reported as a copy of the schema's node, so a caller
     * that keeps or mutates the value cannot change the schema.
     */
    @Test
    void arrayValueIsReportedAsACopy() {
        Schema schema = schema(Dialects.getDraft202012(), "{\"examples\":[1,2]}");
        JsonNode reported = assertInstanceOf(JsonNode.class, value(schema, "examples"));
        assertEquals(schema.getSchemaNode().get("examples"), reported);
        assertNotSame(schema.getSchemaNode().get("examples"), reported);

        reported.asArray().add(3);
        assertEquals(2, schema.getSchemaNode().get("examples").size());
    }

    /**
     * An object valued keyword such as an object default is reported as a copy as
     * well, and the copy is deep.
     */
    @Test
    void objectValueIsReportedAsACopy() {
        Schema schema = schema(Dialects.getDraft202012(), "{\"default\":{\"a\":[1]}}");
        JsonNode reported = assertInstanceOf(JsonNode.class, value(schema, "default"));
        assertEquals(schema.getSchemaNode().get("default"), reported);
        assertNotSame(schema.getSchemaNode().get("default"), reported);

        reported.get("a").asArray().add(2);
        assertEquals(1, schema.getSchemaNode().get("default").get("a").size());
    }

    /**
     * contentSchema is an annotation keyword of the content vocabulary and is a
     * schema, so its value may be a boolean.
     */
    @Test
    void booleanContentSchemaIsReported() {
        assertEquals(Boolean.TRUE,
                value("{\"contentSchema\":true}", "contentSchema", Dialects.getDraft202012()));
        assertEquals(Boolean.FALSE,
                value("{\"contentSchema\":false}", "contentSchema", Dialects.getDraft202012()));
    }

    /**
     * The Draft 4 keywords use the same mapping. exampleSetFlag is its
     * boolean valued annotation keyword.
     */
    @Test
    void draft4AnnotationKeywordIsReported() {
        assertEquals(Boolean.TRUE,
                value("{\"exampleSetFlag\":true}", "exampleSetFlag", Dialects.getDraft4()));
        assertEquals("t", value("{\"title\":\"t\"}", "title", Dialects.getDraft4()));
    }

    /**
     * The Draft 6 examples keyword is an array.
     */
    @Test
    void draft6ExamplesAreReported() {
        Schema schema = schema(Dialects.getDraft6(), "{\"examples\":[1,2]}");
        assertEquals(schema.getSchemaNode().get("examples"), value(schema, "examples"));
    }

    /**
     * The OpenAPI 3.0 dialect adds its own annotation keywords, including a boolean
     * and an object.
     */
    @Test
    void openApi30AnnotationKeywordsAreReported() {
        Schema schema = schema(Dialects.getOpenApi30(), "{\"deprecated\":true,\"xml\":{\"name\":\"a\"}}");
        assertEquals(Boolean.TRUE, value(schema, "deprecated"));
        assertEquals(schema.getSchemaNode().get("xml"), value(schema, "xml"));
    }

    /**
     * The OpenAPI 3.1 base vocabulary adds example, which may hold any value.
     */
    @Test
    void openApi31AnnotationKeywordIsReported() {
        Schema schema = schema(Dialects.getOpenApi31(), "{\"example\":{\"a\":[1]}}");
        assertEquals(schema.getSchemaNode().get("example"), value(schema, "example"));
    }

    /**
     * An unknown keyword is an annotation keyword, so it reports its value the same
     * way the keywords in a dialect do. The value of a keyword that is not in a
     * vocabulary is not restricted to a string, which is the contract change that
     * this mapping makes.
     */
    @Test
    void unknownKeywordValueIsReported() {
        assertEquals("Foo", value("{\"x-unknownKeyword\":\"Foo\"}", "x-unknownKeyword", Dialects.getDraft202012()));
        assertEquals(Boolean.TRUE, value("{\"x-unknownKeyword\":true}", "x-unknownKeyword", Dialects.getDraft202012()));
        assertEquals(42, value("{\"x-unknownKeyword\":42}", "x-unknownKeyword", Dialects.getDraft202012()));
    }

    /**
     * The value that a caller receives is whatever the value of the keyword is, so
     * code that reads it through the generic method of the annotation gets that type.
     * This is the contract that the CHANGELOG describes; it is asserted through
     * getValue() rather than by casting a local, because a cast would only test the
     * compiler.
     */
    @Test
    void theTypeOfTheValueComesFromTheSchema() {
        Schema schema = schema(Dialects.getDraft202012(),
                "{\"x-boolean\":true,\"x-number\":42,\"x-string\":\"s\",\"x-object\":{\"a\":1}}");
        ExecutionContext executionContext = AnnotationCapture.validate(schema, "42", null);

        // The generic method returns the type of the value, which is what a caller of
        // a known boolean or number valued annotation relies on.
        Boolean booleanValue = AnnotationCapture.of(executionContext, ROOT, "x-boolean").getValue();
        assertEquals(Boolean.TRUE, booleanValue);
        Number numberValue = AnnotationCapture.of(executionContext, ROOT, "x-number").getValue();
        assertEquals(42, numberValue.intValue());
        String stringValue = AnnotationCapture.of(executionContext, ROOT, "x-string").getValue();
        assertEquals("s", stringValue);
        JsonNode objectValue = AnnotationCapture.of(executionContext, ROOT, "x-object").getValue();
        assertEquals(1, objectValue.get("a").intValue());

        // The object is a copy, so a caller that mutates the value it was given cannot
        // change the schema.
        objectValue.asObject().put("b", 2);
        assertEquals(2, objectValue.size());
        assertEquals(1, schema.getSchemaNode().get("x-object").size());
    }

    /**
     * The mapping of an unknown keyword and of a keyword in a dialect is the same,
     * so an unknown keyword is reported with the type the value has.
     */
    @Test
    void unknownKeywordAndDialectKeywordAgreeOnTheValueType() {
        assertEquals(value("{\"deprecated\":true}", "deprecated", Dialects.getDraft202012()),
                value("{\"x-unknownKeyword\":true}", "x-unknownKeyword", Dialects.getDraft202012()));
    }

    /**
     * Validates the instance against the schema and returns the value of the
     * annotation that the keyword collected at the root.
     */
    private Object value(String schemaText, String keyword, Dialect dialect) {
        return value(schema(dialect, schemaText), keyword);
    }

    private Object value(Schema schema, String keyword) {
        ExecutionContext executionContext = AnnotationCapture.validate(schema, "42", null);
        return AnnotationCapture.of(executionContext, ROOT, keyword).getValue();
    }

    private Schema schema(Dialect dialect, String schemaText) {
        return SchemaRegistry.withDialect(dialect).getSchema(schemaText, InputFormat.JSON);
    }
}
