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

package com.networknt.schema;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.function.Consumer;

import org.junit.jupiter.api.Test;

import tools.jackson.databind.JsonNode;
import com.networknt.schema.annotation.Annotation;
import com.networknt.schema.dialect.Dialects;
import com.networknt.schema.path.NodePath;
import com.networknt.schema.path.PathType;

/**
 * readOnly and writeOnly are annotations.
 * <p>
 * These are the "`readOnly` is an annotation" and "`writeOnly` is an annotation"
 * cases from the JSON Schema annotation test suite, which is vendored at
 * src/test/suite/annotations/tests/meta-data.json. Both are marked compatible
 * with draft 7 and later, where the two keywords are pure annotations that do
 * not affect validation.
 * <p>
 * The value mapping that is shared with the other annotation keywords is tested
 * in {@link com.networknt.schema.keyword.AnnotationKeywordTest}.
 */
class ReadOnlyWriteOnlyAnnotationTest {

    private static final String READ_ONLY = "readOnly";
    private static final String WRITE_ONLY = "writeOnly";
    private static final String READ_ONLY_MESSAGE = "is a readonly field, it cannot be changed";
    private static final String WRITE_ONLY_MESSAGE = "is a write-only field, it cannot appear in the data";
    private static final NodePath ROOT = new NodePath(PathType.JSON_POINTER);
    private static final NodePath NESTED = ROOT.append("a");
    private static final String NESTED_READ_ONLY_SCHEMA = "{\"properties\":{\"a\":{\"readOnly\":true}}}";
    private static final String NESTED_WRITE_ONLY_SCHEMA = "{\"properties\":{\"a\":{\"writeOnly\":true}}}";

    @Test
    void readOnlyIsCollectedAsAnAnnotation() {
        assertRootAnnotation("{\"readOnly\":true}", READ_ONLY, Boolean.TRUE);
    }

    @Test
    void writeOnlyIsCollectedAsAnAnnotation() {
        assertRootAnnotation("{\"writeOnly\":true}", WRITE_ONLY, Boolean.TRUE);
    }

    @Test
    void readOnlyFalseIsCollectedAsAnAnnotation() {
        assertRootAnnotation("{\"readOnly\":false}", READ_ONLY, Boolean.FALSE);
    }

    @Test
    void writeOnlyFalseIsCollectedAsAnAnnotation() {
        assertRootAnnotation("{\"writeOnly\":false}", WRITE_ONLY, Boolean.FALSE);
    }

    /**
     * The values used by the cases in the annotation test suite. The two keywords
     * are collected alongside the other meta-data keywords of the same schema.
     */
    @Test
    void allMetaDataKeywordValuesAreCollected() {
        ExecutionContext executionContext = collect("42",
                "{\"deprecated\":true,\"default\":false,\"examples\":[1],\"title\":\"t\","
                        + "\"readOnly\":true,\"writeOnly\":false}",
                SpecificationVersion.DRAFT_2020_12);
        assertEquals(Boolean.TRUE, annotationOf(executionContext, "deprecated").getValue());
        assertEquals(Boolean.FALSE, annotationOf(executionContext, "default").getValue());
        assertEquals("t", annotationOf(executionContext, "title").getValue());
        assertInstanceOf(JsonNode.class, annotationOf(executionContext, "examples").getValue());
        assertEquals(Boolean.TRUE, annotationOf(executionContext, READ_ONLY).getValue());
        assertEquals(Boolean.FALSE, annotationOf(executionContext, WRITE_ONLY).getValue());
    }

    @Test
    void readOnlyIsAnAnnotationInDraft7() {
        assertRootAnnotation("{\"readOnly\":true}", READ_ONLY, Boolean.TRUE, SpecificationVersion.DRAFT_7);
        assertRootAnnotation("{\"readOnly\":false}", READ_ONLY, Boolean.FALSE, SpecificationVersion.DRAFT_7);
    }

    @Test
    void writeOnlyIsAnAnnotationInDraft7() {
        assertRootAnnotation("{\"writeOnly\":true}", WRITE_ONLY, Boolean.TRUE, SpecificationVersion.DRAFT_7);
        assertRootAnnotation("{\"writeOnly\":false}", WRITE_ONLY, Boolean.FALSE, SpecificationVersion.DRAFT_7);
    }

    /**
     * The annotation is attached to the instance location the keyword applies to,
     * which is the property and not the document root.
     */
    @Test
    void readOnlyIsAnAnnotationOfANestedProperty() {
        assertNestedAnnotation(READ_ONLY, NESTED_READ_ONLY_SCHEMA);
    }

    @Test
    void writeOnlyIsAnAnnotationOfANestedProperty() {
        assertNestedAnnotation(WRITE_ONLY, NESTED_WRITE_ONLY_SCHEMA);
    }

    /**
     * Annotation collection is opt-in, so nothing is reported unless it is
     * enabled, even when the filter would collect everything.
     */
    @Test
    void readOnlyIsNotCollectedWhenCollectionIsDisabled() {
        assertTrue(rootAnnotations("{\"readOnly\":true}", false, SpecificationVersion.DRAFT_2020_12).isEmpty());
    }

    @Test
    void writeOnlyIsNotCollectedWhenCollectionIsDisabled() {
        assertTrue(rootAnnotations("{\"writeOnly\":true}", false, SpecificationVersion.DRAFT_2020_12).isEmpty());
    }

    /**
     * Enabling collection is not enough, the keyword still has to be accepted by
     * the filter. This covers both a root level keyword and a nested one, to show
     * that the filter is what stops it and not the instance location.
     */
    @Test
    void readOnlyIsNotCollectedWhenItIsFilteredOut() {
        assertTrue(filteredOutAnnotations("{\"readOnly\":true}", "42", ROOT).isEmpty());
        assertTrue(filteredOutAnnotations(NESTED_READ_ONLY_SCHEMA, "{\"a\":1}", NESTED).isEmpty());
    }

    @Test
    void writeOnlyIsNotCollectedWhenItIsFilteredOut() {
        assertTrue(filteredOutAnnotations("{\"writeOnly\":true}", "42", ROOT).isEmpty());
        assertTrue(filteredOutAnnotations(NESTED_WRITE_ONLY_SCHEMA, "{\"a\":1}", NESTED).isEmpty());
    }

    /**
     * Annotating does not change validation, so a schema using these keywords
     * still accepts everything when the readOnly and writeOnly execution
     * policies are not in force.
     */
    @Test
    void readOnlyAndWriteOnlyStillDoNotAssert() {
        assertTrue(validate("{\"readOnly\":true}", "42", e -> e.annotationCollectionEnabled(true)).isEmpty());
        assertTrue(validate("{\"writeOnly\":true}", "42", e -> e.annotationCollectionEnabled(true)).isEmpty());
        assertTrue(validate("{\"readOnly\":true}", null).isEmpty());
        assertTrue(validate("{\"readOnly\":false}", null).isEmpty());
    }

    /**
     * The execution policies that make readOnly and writeOnly assert still work
     * alongside the annotation.
     */
    @Test
    void readOnlyPolicyStillReportsAnError() {
        assertEquals(1, validate("{\"readOnly\":true}", e -> e.readOnly(true)).size());
        assertEquals(1, validate("{\"writeOnly\":true}", e -> e.writeOnly(true)).size());
        assertTrue(validate("{\"readOnly\":true}", e -> e.readOnly(false)).isEmpty());
    }

    /**
     * A boolean false does not assert under the policy. The annotation that is
     * collected in the same run is not dropped, because nothing failed.
     */
    @Test
    void readOnlyFalseDoesNotAssertUnderTheReadOnlyPolicy() {
        assertFalseValueDoesNotAssert(READ_ONLY,
                "{\"properties\":{\"a\":{\"readOnly\":false}}}", e -> e.readOnly(true));
    }

    @Test
    void writeOnlyFalseDoesNotAssertUnderTheWriteOnlyPolicy() {
        assertFalseValueDoesNotAssert(WRITE_ONLY,
                "{\"properties\":{\"a\":{\"writeOnly\":false}}}", e -> e.writeOnly(true));
    }

    /**
     * A value that is not a boolean is not a valid readOnly or writeOnly value.
     * It asserted before the annotation was collected, so it still asserts.
     */
    @Test
    void nonBooleanKeywordValueStillAssertsUnderTheReadOnlyPolicy() {
        assertNonBooleanValueAsserts("{\"properties\":{\"a\":{\"readOnly\":\"true\"}}}",
                READ_ONLY_MESSAGE, e -> e.readOnly(true));
    }

    @Test
    void nonBooleanKeywordValueStillAssertsUnderTheWriteOnlyPolicy() {
        assertNonBooleanValueAsserts("{\"properties\":{\"a\":{\"writeOnly\":\"true\"}}}",
                WRITE_ONLY_MESSAGE, e -> e.writeOnly(true));
    }

    /**
     * A null value is not a boolean either, so it asserts as well.
     */
    @Test
    void nullKeywordValueStillAssertsUnderThePolicies() {
        assertNonBooleanValueAsserts("{\"properties\":{\"a\":{\"readOnly\":null}}}",
                READ_ONLY_MESSAGE, e -> e.readOnly(true));
        assertNonBooleanValueAsserts("{\"properties\":{\"a\":{\"writeOnly\":null}}}",
                WRITE_ONLY_MESSAGE, e -> e.writeOnly(true));
    }

    /**
     * A value that is not a boolean is still collected. It is reported as whatever
     * JSON value it is, without inventing a boolean for it.
     */
    @Test
    void nonBooleanKeywordValueIsCollectedAsAnAnnotation() {
        ExecutionContext readOnly = collect("{\"a\":1}",
                "{\"properties\":{\"a\":{\"readOnly\":\"yes\"}}}", SpecificationVersion.DRAFT_2020_12);
        assertEquals("yes", annotationOf(readOnly, NESTED, READ_ONLY).getValue());

        ExecutionContext nullValue = collect("{\"a\":1}",
                "{\"properties\":{\"a\":{\"readOnly\":null}}}", SpecificationVersion.DRAFT_2020_12);
        Object value = annotationOf(nullValue, NESTED, READ_ONLY).getValue();
        assertInstanceOf(JsonNode.class, value);
        assertTrue(((JsonNode) value).isNull());

        ExecutionContext arrayValue = collect("{\"a\":1}",
                "{\"properties\":{\"a\":{\"readOnly\":[true]}}}", SpecificationVersion.DRAFT_2020_12);
        assertInstanceOf(JsonNode.class, annotationOf(arrayValue, NESTED, READ_ONLY).getValue());
    }

    /**
     * The policy of both keywords applies to the OpenAPI 3.0 dialect as well, where
     * those keywords are part of the vocabulary rather than the meta-data ones.
     */
    @Test
    void readOnlyPolicyAppliesToTheOpenApi30Dialect() {
        assertOpenApi30Policy(READ_ONLY, "{\"readOnly\":true}", 1);
        assertOpenApi30Policy(READ_ONLY, "{\"readOnly\":false}", 0);
        assertOpenApi30Policy(WRITE_ONLY, "{\"writeOnly\":true}", 1);
        assertOpenApi30Policy(WRITE_ONLY, "{\"writeOnly\":false}", 0);
    }

    /**
     * The interaction with required, which is where the readOnly and writeOnly
     * policies were introduced for: a required property that is readOnly is not
     * reported as missing under the readOnly policy, and the boolean false exemption
     * applies to the annotation only.
     */
    @Test
    void requiredInteractsWithTheReadOnlyPolicy() {
        // The ReadOnlyValidator exemption does not reach required: the required path
        // treats only the boolean true as read only, so a false value is required.
        assertEquals(0, validateRequired("{\"readOnly\":true}", e -> e.readOnly(true)).size());
        assertEquals(1, validateRequired("{\"readOnly\":false}", e -> e.readOnly(true)).size());
        assertEquals(0, validateRequired("{\"writeOnly\":true}", e -> e.writeOnly(true)).size());
        assertEquals(1, validateRequired("{\"writeOnly\":false}", e -> e.writeOnly(true)).size());

        // Without the policy both are required, whatever the keyword value is.
        assertEquals(1, validateRequired("{\"readOnly\":true}", null).size());
        assertEquals(1, validateRequired("{\"readOnly\":false}", null).size());

        // A value that is not a boolean is not read only, so the property is
        // required. It must be reported as a missing property rather than throwing,
        // even though the properties path asserts on the same keyword.
        assertEquals(1, validateRequired("{\"readOnly\":\"true\"}", e -> e.readOnly(true)).size());
        assertEquals(1, validateRequired("{\"writeOnly\":\"true\"}", e -> e.writeOnly(true)).size());
        assertEquals(1, validateRequired("{\"readOnly\":null}", e -> e.readOnly(true)).size());
        assertEquals(1, validateRequired("{\"readOnly\":[true]}", e -> e.readOnly(true)).size());
        assertEquals(1, validateRequired("{\"readOnly\":1}", e -> e.readOnly(true)).size());
    }

    private List<Error> validateRequired(String propertySchema, Consumer<ExecutionConfig.Builder> config) {
        Schema schema = SchemaRegistry.withDefaultDialect(SpecificationVersion.DRAFT_2020_12).getSchema(
                "{\"type\":\"object\",\"required\":[\"a\"],\"properties\":{\"a\":" + propertySchema + "}}",
                InputFormat.JSON);
        return schema.validate("{}", InputFormat.JSON, context -> {
            if (config != null) {
                context.executionConfig(config);
            }
        });
    }

    private void assertOpenApi30Policy(String keyword, String schemaText, int expectedErrors) {
        Schema schema = SchemaRegistry.withDialect(Dialects.getOpenApi30())
                .getSchema(schemaText, InputFormat.JSON);
        List<Error> errors = schema.validate("42", InputFormat.JSON, context -> context
                .executionConfig(config -> config.readOnly(true).writeOnly(true)));
        assertEquals(expectedErrors, errors.size(), keyword + " " + errors);
    }

    /**
     * The policy error and the annotation are reported by the same run. The
     * annotation is collected first and then marked as dropped, because the schema
     * that reported it failed.
     */
    @Test
    void readOnlyPolicyErrorAndAnnotationAreBothReportedInTheSameRun() {
        assertPolicyErrorDropsAnnotation(READ_ONLY, NESTED_READ_ONLY_SCHEMA, READ_ONLY_MESSAGE,
                e -> e.readOnly(true));
    }

    @Test
    void writeOnlyPolicyErrorAndAnnotationAreBothReportedInTheSameRun() {
        assertPolicyErrorDropsAnnotation(WRITE_ONLY, NESTED_WRITE_ONLY_SCHEMA, WRITE_ONLY_MESSAGE,
                e -> e.writeOnly(true));
    }

    private void assertFalseValueDoesNotAssert(String keyword, String schemaText,
            Consumer<ExecutionConfig.Builder> policy) {
        ExecutionContext[] holder = new ExecutionContext[1];
        List<Error> errors = validate(schema(SpecificationVersion.DRAFT_2020_12, schemaText), "{\"a\":1}",
                e -> {
                    policy.accept(e);
                    AnnotationCapture.collectEverything(e);
                }, holder);
        assertTrue(errors.isEmpty(), errors.toString());

        Annotation annotation = annotationOf(holder[0], NESTED, keyword);
        assertEquals(Boolean.FALSE, annotation.getValue());
        assertEquals(NESTED, annotation.getInstanceLocation());
        assertEquals("#/properties/a/" + keyword, annotation.getSchemaLocation().toString());
        assertTrue(annotation.isValid());
    }

    private void assertNonBooleanValueAsserts(String schemaText, String expectedMessage,
            Consumer<ExecutionConfig.Builder> policy) {
        List<Error> errors = validate(schemaText, "{\"a\":1}", policy);
        assertEquals(1, errors.size(), errors.toString());
        assertEquals(expectedMessage, errors.get(0).getMessage());
        assertEquals(NESTED, errors.get(0).getInstanceLocation());
    }

    private void assertPolicyErrorDropsAnnotation(String keyword, String schemaText, String expectedMessage,
            Consumer<ExecutionConfig.Builder> policy) {
        ExecutionContext[] holder = new ExecutionContext[1];
        List<Error> errors = validate(schema(SpecificationVersion.DRAFT_2020_12, schemaText), "{\"a\":1}",
                e -> {
                    policy.accept(e);
                    AnnotationCapture.collectEverything(e);
                }, holder);

        // The policy error is reported.
        assertEquals(1, errors.size(), errors.toString());
        assertEquals(expectedMessage, errors.get(0).getMessage());
        assertEquals(NESTED, errors.get(0).getInstanceLocation());

        // The annotation of the failed schema is collected as a dropped annotation.
        Annotation annotation = annotationOf(holder[0], NESTED, keyword);
        assertEquals(Boolean.TRUE, annotation.getValue());
        assertEquals(NESTED, annotation.getInstanceLocation());
        assertEquals("#/properties/a/" + keyword, annotation.getSchemaLocation().toString());
        assertFalse(annotation.isValid());
    }

    private void assertNestedAnnotation(String keyword, String schemaText) {
        Annotation annotation = annotationOf(
                collect("{\"a\":1}", schemaText, SpecificationVersion.DRAFT_2020_12), NESTED, keyword);
        assertEquals(keyword, annotation.getKeyword());
        assertEquals(Boolean.TRUE, annotation.getValue());
        assertEquals("#/properties/a/" + keyword, annotation.getSchemaLocation().toString());
        assertEquals(NESTED, annotation.getInstanceLocation());
        assertTrue(annotation.isValid());
    }

    private void assertRootAnnotation(String schemaText, String keyword, Boolean expected) {
        assertRootAnnotation(schemaText, keyword, expected, SpecificationVersion.DRAFT_2020_12);
    }

    private void assertRootAnnotation(String schemaText, String keyword, Boolean expected,
            SpecificationVersion specificationVersion) {
        Annotation annotation = annotationOf(
                collect("42", schemaText, specificationVersion), ROOT, keyword);
        assertEquals(keyword, annotation.getKeyword());
        assertEquals(expected, annotation.getValue());
        assertEquals("#/" + keyword, annotation.getSchemaLocation().toString());
        assertEquals(ROOT, annotation.getInstanceLocation());
        assertTrue(annotation.isValid());
    }

    /**
     * Validates the instance and returns the annotations collected at the instance
     * location, with collection enabled but the filter rejecting every keyword.
     */
    private List<Annotation> filteredOutAnnotations(String schemaText, String instanceText,
            NodePath instanceLocation) {
        ExecutionContext executionContext = collect(instanceText, schemaText, SpecificationVersion.DRAFT_2020_12,
                e -> e.annotationCollectionFilter(keyword -> false));
        return AnnotationCapture.at(executionContext, instanceLocation);
    }

    /**
     * Validates the instance and returns the annotations collected at the root, with
     * the filter accepting every keyword and collection controlled by the argument.
     */
    private List<Annotation> rootAnnotations(String schemaText, boolean annotationCollectionEnabled,
            SpecificationVersion specificationVersion) {
        ExecutionContext executionContext = collect("42", schemaText, specificationVersion,
                e -> e.annotationCollectionEnabled(annotationCollectionEnabled));
        return AnnotationCapture.at(executionContext, ROOT);
    }

    /**
     * Validates the instance and returns the execution context, with annotation
     * collection enabled for every keyword and the given configuration applied on
     * top of that.
     */
    private ExecutionContext collect(String instanceText, String schemaText,
            SpecificationVersion specificationVersion) {
        return collect(instanceText, schemaText, specificationVersion, null);
    }

    private ExecutionContext collect(String instanceText, String schemaText,
            SpecificationVersion specificationVersion, Consumer<ExecutionConfig.Builder> config) {
        return AnnotationCapture.validate(schema(specificationVersion, schemaText), instanceText, config);
    }

    private Annotation annotationOf(ExecutionContext executionContext, String keyword) {
        return annotationOf(executionContext, ROOT, keyword);
    }

    private Annotation annotationOf(ExecutionContext executionContext, NodePath instanceLocation, String keyword) {
        return AnnotationCapture.of(executionContext, instanceLocation, keyword);
    }

    private List<Error> validate(String schemaText, Consumer<ExecutionConfig.Builder> config) {
        return validate(schemaText, "42", config);
    }

    private List<Error> validate(String schemaText, String instanceText, Consumer<ExecutionConfig.Builder> config) {
        return validate(schema(SpecificationVersion.DRAFT_2020_12, schemaText), instanceText, config,
                new ExecutionContext[1]);
    }

    private List<Error> validate(Schema schema, String instanceText, Consumer<ExecutionConfig.Builder> config) {
        return validate(schema, instanceText, config, new ExecutionContext[1]);
    }

    private List<Error> validate(Schema schema, String instanceText, Consumer<ExecutionConfig.Builder> config,
            ExecutionContext[] holder) {
        return schema.validate(instanceText, InputFormat.JSON, context -> {
            if (config != null) {
                context.executionConfig(config);
            }
            holder[0] = context;
        });
    }

    private Schema schema(SpecificationVersion specificationVersion, String schemaText) {
        return SchemaRegistry.withDefaultDialect(specificationVersion).getSchema(schemaText, InputFormat.JSON);
    }
}
