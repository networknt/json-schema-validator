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

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Field;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import com.networknt.schema.ValidationLimitExceededException.LimitKind;
import com.networknt.schema.dialect.Dialect;
import com.networknt.schema.dialect.Dialects;
import com.networknt.schema.keyword.AbstractKeyword;
import com.networknt.schema.keyword.AbstractKeywordValidator;
import com.networknt.schema.keyword.KeywordValidator;
import com.networknt.schema.path.NodePath;
import com.networknt.schema.path.PathType;
import com.networknt.schema.serialization.JsonMapperFactory;
import com.networknt.schema.walk.KeywordWalkHandler;
import com.networknt.schema.walk.WalkEvent;
import com.networknt.schema.walk.WalkFlow;
import com.networknt.schema.walk.WalkListener;

import tools.jackson.databind.JsonNode;

class ExecutionLimitsTest {
    private static final NodePath ROOT = new NodePath(PathType.JSON_POINTER);

    private static JsonNode node(String text) {
        return JsonMapperFactory.getInstance().readTree(text);
    }

    private static Schema schema(String text) {
        return SchemaRegistry.withDefaultDialect(SpecificationVersion.DRAFT_2020_12).getSchema(text);
    }

    private static ExecutionContext context(long steps, int depth) {
        return new ExecutionContext(ExecutionConfig.builder().maxEvaluationSteps(steps)
                .maxEvaluationDepth(depth).build());
    }

    @Test
    void unlimitedValidationKeepsDeepNestingWithinOneMegabyteStack() throws Exception {
        Path output = Files.createTempFile("schema-limits-stack-", ".log");
        try {
            Process process = new ProcessBuilder(Path.of(System.getProperty("java.home"), "bin", "java").toString(),
                    "-Xss1m", "-cp", System.getProperty("surefire.test.class.path", System.getProperty("java.class.path")),
                    ExecutionLimitsStackProbe.class.getName(), "490")
                    .redirectErrorStream(true).redirectOutput(output.toFile()).start();
            assertEquals(0, process.waitFor(), () -> {
                try { return Files.readString(output); }
                catch (java.io.IOException failure) { throw new java.io.UncheckedIOException(failure); }
            });
        } finally {
            Files.deleteIfExists(output);
        }
    }

    private static void assertUnwound(ExecutionContext context, List<Error> parentErrors) {
        assertTrue(context.getEvaluationSchema().isEmpty());
        assertTrue(context.getEvaluationSchemaPath().isEmpty());
        assertEquals(ROOT, context.getEvaluationPath());
        if (context.isEvaluationAborted()) {
            assertSame(context.getEvaluationAbort(), assertThrows(ValidationLimitExceededException.class, context::getErrors));
            assertSame(context.getEvaluationAbort(), assertThrows(ValidationLimitExceededException.class, context::getAnnotations));
            if (parentErrors != null) {
                try {
                    Field errors = ExecutionContext.class.getDeclaredField("errors");
                    errors.setAccessible(true);
                    assertSame(parentErrors, errors.get(context));
                } catch (ReflectiveOperationException failure) {
                    throw new AssertionError(failure);
                }
            }
        } else {
            assertSame(parentErrors, context.getErrors());
        }
        assertFalse(context.isUnevaluatedItemsPresent());
        assertFalse(context.isUnevaluatedPropertiesPresent());
    }

    @Test
    void configurationAndLegacyConstructor() {
        assertEquals(0, ExecutionConfig.getInstance().getMaxEvaluationSteps());
        assertEquals(0, ExecutionConfig.getInstance().getMaxEvaluationDepth());
        ExecutionConfig config = ExecutionConfig.builder().maxEvaluationSteps(Long.MAX_VALUE)
                .maxEvaluationDepth(Integer.MAX_VALUE).build();
        ExecutionConfig copy = ExecutionConfig.builder(config).failFast(true).build();
        assertEquals(Long.MAX_VALUE, copy.getMaxEvaluationSteps());
        assertEquals(Integer.MAX_VALUE, copy.getMaxEvaluationDepth());
        assertThrows(IllegalArgumentException.class, () -> ExecutionConfig.builder().maxEvaluationSteps(-1).build());
        assertThrows(IllegalArgumentException.class, () -> ExecutionConfig.builder().maxEvaluationDepth(-1).build());
        ExecutionConfig legacy = new ExecutionConfig(Locale.ROOT, false, keyword -> false, null, false, null, null);
        assertEquals(0, legacy.getMaxEvaluationSteps());
        assertEquals(0, legacy.getMaxEvaluationDepth());
    }

    private static class CustomConfig extends ExecutionConfig {
        CustomConfig(CustomBuilder builder) {
            super(builder);
        }
    }

    private static class CustomBuilder extends ExecutionConfig.BuilderSupport<CustomBuilder> {
        protected CustomBuilder self() { return this; }
        public CustomConfig build() { return new CustomConfig(this); }
    }

    @Test
    void subclassBuilderConstructorPreservesInheritedPolicy() {
        CustomConfig config = new CustomBuilder().maxEvaluationSteps(7).maxEvaluationDepth(3)
                .failFast(true).locale(null).build();
        assertEquals(7, config.getMaxEvaluationSteps());
        assertEquals(3, config.getMaxEvaluationDepth());
        assertEquals(Locale.getDefault(), config.getLocale());
        assertTrue(config.isFailFast());
        assertEquals(7, ExecutionConfig.builder(config).build().getMaxEvaluationSteps());
        assertThrows(IllegalArgumentException.class, () -> new CustomBuilder().maxEvaluationSteps(-1).build());
        assertThrows(IllegalArgumentException.class, () -> new CustomBuilder().maxEvaluationDepth(-1).build());
    }

    @Test
    void exhaustedContextCannotExposePartialErrorsOrAnnotations() {
        ExecutionContext context = context(6, 0);
        context.executionConfig(config -> config.annotationCollectionEnabled(true).annotationCollectionFilter(k -> true));
        List<Error> partialErrors = context.getErrors();
        Schema schema = schema("{\"description\":\"partial\",\"allOf\":[{\"type\":\"integer\"},{\"$ref\":\"#\"}]}");
        ValidationLimitExceededException failure = assertThrows(ValidationLimitExceededException.class,
                () -> schema.validate(context, node("\"x\"")));
        assertFalse(partialErrors.isEmpty());
        assertTrue(context.isEvaluationAborted());
        assertSame(failure, context.getEvaluationAbort());
        assertSame(failure, assertThrows(ValidationLimitExceededException.class, context::getErrors));
        assertSame(failure, assertThrows(ValidationLimitExceededException.class, context::getAnnotations));
        assertSame(failure, assertThrows(ValidationLimitExceededException.class, new Result(context)::getErrors));
        assertUnwound(context, partialErrors);
    }

    @Test
    void postListenersObserveAbortRatherThanSuccessfulEmptyDiagnostics() {
        ExecutionContext context = context(2, 1);
        AtomicReference<ValidationLimitExceededException> reported = new AtomicReference<>();
        AtomicInteger completions = new AtomicInteger();
        context.walkConfig(config -> config.keywordWalkHandler(KeywordWalkHandler.builder()
                .keywordWalkListener(new WalkListener() {
                    public WalkFlow onWalkStart(WalkEvent event) { return WalkFlow.CONTINUE; }
                    public void onWalkEnd(WalkEvent event, List<Error> errors) {
                        assertTrue(event.getExecutionContext().isEvaluationAborted());
                        reported.set(event.getExecutionContext().getEvaluationAbort());
                        assertTrue(errors.isEmpty());
                        assertThrows(ValidationLimitExceededException.class, event.getExecutionContext()::getErrors);
                        completions.incrementAndGet();
                    }
                }).build()));
        ValidationLimitExceededException failure = assertThrows(ValidationLimitExceededException.class,
                () -> schema("{\"allOf\":[{}]}").walk(context, node("1"), node("1"), ROOT, false));
        assertSame(failure, reported.get());
        assertEquals(1, completions.get());
    }

    @Test
    void completedWalkKeepsItsDiagnosticListAndSliceSemantics() {
        List<List<Error>> slices = new ArrayList<>();
        ExecutionContext context = context(0, 0);
        context.walkConfig(config -> config.keywordWalkHandler(KeywordWalkHandler.builder()
                .keywordWalkListener(new WalkListener() {
                    public WalkFlow onWalkStart(WalkEvent event) { return WalkFlow.CONTINUE; }
                    public void onWalkEnd(WalkEvent event, List<Error> errors) {
                        assertFalse(event.getExecutionContext().isEvaluationAborted());
                        slices.add(new ArrayList<>(errors));
                    }
                }).build()));
        schema("{\"type\":\"integer\",\"minLength\":5}").walk(context, node("\"x\""), node("\"x\""), ROOT, true);
        assertEquals(1, slices.get(0).size());
        assertEquals(2, slices.get(1).size());
        assertEquals(context.getErrors(), slices.get(1));
    }

    @Test
    void walkRejectsUnrestoredErrorListInsteadOfClampingItsOffset() {
        AbstractKeyword keyword = new AbstractKeyword("replaceErrors") {
            public KeywordValidator newValidator(SchemaLocation location, JsonNode value,
                    Schema parent, SchemaContext schemaContext) {
                return new AbstractKeywordValidator(this, value, location) {
                    public void validate(ExecutionContext context, JsonNode node, JsonNode root, NodePath path) {
                        context.setErrors(new ArrayList<>());
                    }
                };
            }
        };
        Schema schema = SchemaRegistry.withDialect(Dialect.builder("urn:limits:replace", Dialects.getDraft202012())
                .keyword(keyword).build()).getSchema("{\"replaceErrors\":true}");
        ExecutionContext context = context(0, 0);
        context.addError(Error.builder().keyword("existing").message("existing").build());
        AtomicInteger completions = new AtomicInteger();
        context.walkConfig(config -> config.keywordWalkHandler(KeywordWalkHandler.builder()
                .keywordWalkListener(new WalkListener() {
                    public WalkFlow onWalkStart(WalkEvent event) { return WalkFlow.CONTINUE; }
                    public void onWalkEnd(WalkEvent event, List<Error> errors) { completions.incrementAndGet(); }
                }).build()));
        assertThrows(IllegalStateException.class, () -> schema.walk(context, node("1"), node("1"), ROOT, true));
        assertEquals(0, completions.get());
        assertFalse(context.isEvaluationLimited());
    }

    @Test
    void unlimitedExecutionAllocatesNoAccountingAndCannotBeEnabledMidRun() {
        AbstractKeyword keyword = new AbstractKeyword("configureLimits") {
            public KeywordValidator newValidator(SchemaLocation location, JsonNode value,
                    Schema parent, SchemaContext schemaContext) {
                return new AbstractKeywordValidator(this, value, location) {
                    public void validate(ExecutionContext context, JsonNode node, JsonNode root, NodePath path) {
                        assertFalse(context.isEvaluationLimited());
                        context.executionConfig(config -> config.maxEvaluationSteps(1).maxEvaluationDepth(1));
                    }
                };
            }
        };
        Schema schema = SchemaRegistry.withDialect(Dialect.builder("urn:limits:configure", Dialects.getDraft202012())
                .keyword(keyword).build()).getSchema("{\"configureLimits\":true,\"allOf\":[true,true]}");
        ExecutionContext context = context(0, 0);
        assertDoesNotThrow(() -> schema.validate(context, node("1")));
        assertFalse(context.isEvaluationLimited());
        ValidationLimitExceededException failure = assertThrows(ValidationLimitExceededException.class,
                () -> schema.validate(context, node("1")));
        assertEquals(1, failure.getAdmittedSteps());
    }

    @ParameterizedTest
    @EnumSource(PathType.class)
    void limitExceptionSerializationPreservesDiagnostics(PathType type) throws Exception {
        NodePath path = new NodePath(type).append("quote'/~\n").append(3);
        ValidationLimitExceededException original = new ValidationLimitExceededException(LimitKind.EVALUATION_DEPTH,
                8, 17, 8, SchemaLocation.of("https://example.test/schema#/$defs/n").append(2), path, path.append("$ref"));
        original.initCause(new IllegalArgumentException("cause"));
        original.addSuppressed(new IllegalStateException("callback"));
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (ObjectOutputStream stream = new ObjectOutputStream(bytes)) { stream.writeObject(original); }
        ValidationLimitExceededException restored;
        try (ObjectInputStream stream = new ObjectInputStream(new ByteArrayInputStream(bytes.toByteArray()))) {
            restored = (ValidationLimitExceededException) stream.readObject();
        }
        assertEquals(original.getMessage(), restored.getMessage());
        assertEquals(original.getLimitKind(), restored.getLimitKind());
        assertEquals(original.getLimit(), restored.getLimit());
        assertEquals(original.getAdmittedSteps(), restored.getAdmittedSteps());
        assertEquals(original.getActiveDepth(), restored.getActiveDepth());
        assertEquals(original.getSchemaLocation(), restored.getSchemaLocation());
        assertEquals(original.getInstanceLocation(), restored.getInstanceLocation());
        assertEquals(original.getEvaluationPath(), restored.getEvaluationPath());
        assertEquals(type, restored.getInstanceLocation().getPathType());
        assertEquals(3, restored.getInstanceLocation().getElement(-1));
        assertArrayEquals(original.getStackTrace(), restored.getStackTrace());
        assertEquals("cause", restored.getCause().getMessage());
        assertEquals("callback", restored.getSuppressed()[0].getMessage());
    }

    @Test
    void emptySchemaChargesOnlyEntryAndCompletedCallsResetAccounting() {
        Schema schema = schema("{}");
        ExecutionContext context = context(1, 1);
        for (int i = 0; i < 3; i++) {
            schema.validate(context, node("1"), node("1"), ROOT);
            schema.walk(context, node("1"), node("1"), ROOT, false);
        }
        assertTrue(context.getErrors().isEmpty());
        assertUnwound(context, context.isEvaluationAborted() ? null : context.getErrors());
    }

    @ParameterizedTest
    @ValueSource(strings = {"true", "false", "{\"type\":\"integer\"}"})
    void exactlyTwoStepsAllowOneKeyword(String text) {
        Schema schema = schema(text);
        assertDoesNotThrow(() -> schema.validate(context(2, 1), node("1")));
        ExecutionContext context = context(1, 1);
        List<Error> errors = context.getErrors();
        ValidationLimitExceededException failure = assertThrows(ValidationLimitExceededException.class,
                () -> schema.validate(context, node("1")));
        assertEquals(LimitKind.EVALUATION_STEPS, failure.getLimitKind());
        assertEquals(1, failure.getLimit());
        assertEquals(1, failure.getAdmittedSteps());
        assertEquals(1, failure.getActiveDepth());
        assertEquals(ROOT, failure.getInstanceLocation());
        assertEquals(schema.getSchemaLocation().append(schema.getValidators().get(0).getKeyword()),
                failure.getSchemaLocation());
        assertEquals(ROOT.append(schema.getValidators().get(0).getKeyword()), failure.getEvaluationPath());
        assertUnwound(context, errors);
        // The terminal marker survives attempted policy changes and independent calls.
        context.executionConfig(builder -> builder.maxEvaluationSteps(0).maxEvaluationDepth(0));
        assertSame(failure, assertThrows(ValidationLimitExceededException.class,
                () -> schema("{}").walk(context, node("1"), node("1"), ROOT, false)));
        assertDoesNotThrow(() -> schema.validate(context(2, 1), node("1")));
    }

    @Test
    void depthPrecedesStepsAndRejectedEntryDoesNotPushAFrame() {
        Schema schema = schema("{\"allOf\":[{}]}");
        ExecutionContext context = context(2, 1);
        List<Error> errors = context.getErrors();
        ValidationLimitExceededException failure = assertThrows(ValidationLimitExceededException.class,
                () -> schema.validate(context, node("1")));
        assertEquals(LimitKind.EVALUATION_DEPTH, failure.getLimitKind());
        assertEquals(1, failure.getLimit());
        assertEquals(2, failure.getAdmittedSteps());
        assertEquals(1, failure.getActiveDepth());
        assertEquals("/allOf/0", failure.getEvaluationPath().toString());
        assertEquals(schema.getSchemaLocation().append("allOf").append(0), failure.getSchemaLocation());
        assertUnwound(context, errors);
        assertDoesNotThrow(() -> schema.validate(context(3, 2), node("1")));
        ValidationLimitExceededException stepFailure = assertThrows(ValidationLimitExceededException.class,
                () -> schema.validate(context(2, 2), node("1")));
        assertEquals(LimitKind.EVALUATION_STEPS, stepFailure.getLimitKind());
        assertEquals(2, stepFailure.getAdmittedSteps());
    }

    @Test
    void refusedSchemaEntryDoesNotExecuteItsValidators() {
        AtomicInteger constructed = new AtomicInteger();
        AtomicInteger executed = new AtomicInteger();
        AbstractKeyword keyword = new AbstractKeyword("probe") {
            public KeywordValidator newValidator(SchemaLocation location, JsonNode value,
                    Schema parent, SchemaContext schemaContext) {
                constructed.incrementAndGet();
                return new AbstractKeywordValidator(this, value, location) {
                    public void validate(ExecutionContext context, JsonNode node, JsonNode root, NodePath path) {
                        executed.incrementAndGet();
                    }
                };
            }
        };
        Schema schema = SchemaRegistry.withDialect(Dialect.builder("urn:limits:lazy", Dialects.getDraft202012())
                .keyword(keyword).build(), builder -> builder.schemaRegistryConfig(
                        SchemaRegistryConfig.builder().preloadSchema(false).build()))
                .getSchema("{\"allOf\":[{\"probe\":true}]}");
        // Schema construction eagerly creates keyword validators even when reference
        // preloading is disabled. Compilation is outside the evaluation budget.
        assertEquals(1, constructed.get());
        assertThrows(ValidationLimitExceededException.class, () -> schema.validate(context(2, 0), node("1")));
        assertEquals(1, constructed.get());
        assertEquals(0, executed.get());
        assertDoesNotThrow(() -> schema.validate(context(4, 0), node("1")));
        assertEquals(1, constructed.get());
        assertEquals(1, executed.get());
    }

    private static Stream<Arguments> compositions() {
        return Stream.of(
                Arguments.of("{\"allOf\":[{}]}", "1"),
                Arguments.of("{\"anyOf\":[false,true]}", "1"),
                Arguments.of("{\"oneOf\":[false,true]}", "1"),
                Arguments.of("{\"not\":{}}", "1"),
                Arguments.of("{\"if\":{},\"then\":{},\"else\":{}}", "1"),
                Arguments.of("{\"contains\":{}}", "[1]"),
                Arguments.of("{\"propertyNames\":{}}", "{\"x\":1}"),
                Arguments.of("{\"$ref\":\"#/$defs/n\",\"$defs\":{\"n\":{}}}", "1"));
    }

    @ParameterizedTest
    @MethodSource("compositions")
    void applicatorsPropagateExhaustionAndRestoreTemporaryState(String text, String input) {
        Schema schema = schema(text);
        for (boolean walk : new boolean[] {false, true}) {
            ExecutionContext context = context(0, 1);
            context.setFailFast(true);
            List<Error> errors = context.getErrors();
            ValidationLimitExceededException failure = assertThrows(ValidationLimitExceededException.class,
                    () -> {
                        if (walk) schema.walk(context, node(input), node(input), ROOT, true);
                        else schema.validate(context, node(input));
                    });
            assertEquals(LimitKind.EVALUATION_DEPTH, failure.getLimitKind());
            assertTrue(context.isFailFast());
            assertUnwound(context, errors);
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"anyOf", "oneOf"})
    void refusalAtNestedBoundariesUnwindsPathsFlagsAndBranchLists(String keyword) {
        Schema schema = schema("{\"" + keyword + "\":[{\"allOf\":[{\"$ref\":\"#\"}]}],"
                + "\"unevaluatedItems\":false,\"unevaluatedProperties\":false}");
        for (boolean walk : new boolean[] {false, true}) {
            for (int budget = 1; budget <= 24; budget++) {
                ExecutionContext context = context(budget, 0);
                context.setFailFast(true);
                List<Error> parent = context.getErrors();
                parent.add(Error.builder().keyword("existing").message("existing").build());
                ValidationLimitExceededException failure = assertThrows(ValidationLimitExceededException.class,
                        () -> {
                            if (walk) schema.walk(context, node("1"), node("1"), ROOT, true);
                            else schema.validate(context, node("1"));
                        });
                assertEquals(budget, failure.getAdmittedSteps());
                assertTrue(context.isFailFast());
                assertEquals(1, parent.size());
                assertUnwound(context, parent);
            }
        }
    }

    private static Stream<Arguments> recursiveReferences() {
        return Stream.of(
                Arguments.of(SpecificationVersion.DRAFT_2020_12, "{\"$ref\":\"#\"}"),
                Arguments.of(SpecificationVersion.DRAFT_2020_12,
                        "{\"$dynamicAnchor\":\"n\",\"$dynamicRef\":\"#n\"}"),
                Arguments.of(SpecificationVersion.DRAFT_2019_09,
                        "{\"$recursiveAnchor\":true,\"$recursiveRef\":\"#\"}"));
    }

    @ParameterizedTest
    @MethodSource("recursiveReferences")
    void narrowReferencesStopAtDepthInValidationAndWalk(SpecificationVersion version, String text) {
        Schema schema = SchemaRegistry.withDefaultDialect(version).getSchema(text);
        for (int mode = 0; mode < 3; mode++) {
            ExecutionContext context = context(Long.MAX_VALUE, 8);
            List<Error> errors = context.getErrors();
            int selectedMode = mode;
            ValidationLimitExceededException failure = assertThrows(ValidationLimitExceededException.class, () -> {
                if (selectedMode == 0) schema.validate(context, node("1"));
                else schema.walk(context, node("1"), node("1"), ROOT, selectedMode == 1);
            });
            assertEquals(LimitKind.EVALUATION_DEPTH, failure.getLimitKind());
            assertEquals(8, failure.getActiveDepth());
            assertNotNull(failure.getSchemaLocation());
            assertUnwound(context, errors);
        }
    }

    @Test
    void ordinaryFailFastDoesNotMakeContextTerminal() {
        Schema schema = schema("{\"type\":\"integer\"}");
        ExecutionContext context = context(2, 1);
        context.setFailFast(true);
        assertThrows(FailFastAssertionException.class, () -> schema.validate(context, node("\"x\"")));
        context.getErrors().clear();
        assertDoesNotThrow(() -> schema.validate(context, node("1")));
        assertUnwound(context, context.isEvaluationAborted() ? null : context.getErrors());
    }

    private static Stream<Arguments> annotationCases() {
        return Stream.of(
                Arguments.of("{\"anyOf\":[{\"properties\":{\"a\":true}},{\"properties\":{\"b\":true}}],"
                        + "\"unevaluatedProperties\":false}", "{\"a\":1,\"b\":2}"),
                Arguments.of("{\"oneOf\":[{\"required\":[\"a\"],\"properties\":{\"a\":true}},"
                        + "{\"required\":[\"b\"],\"properties\":{\"b\":true}}],\"unevaluatedProperties\":false}",
                        "{\"a\":1}"),
                Arguments.of("{\"anyOf\":[{\"prefixItems\":[true]},{\"contains\":{\"type\":\"integer\"}}],"
                        + "\"unevaluatedItems\":false}", "[\"x\",1]"),
                Arguments.of("{\"anyOf\":[{\"properties\":{\"a\":true}}],\"unevaluatedProperties\":false}",
                        "{\"extra\":1}"));
    }

    @ParameterizedTest
    @MethodSource("annotationCases")
    void completedAnnotationsAndValidityRemainUnchanged(String text, String input) {
        Schema schema = schema(text);
        for (boolean collect : new boolean[] {false, true}) {
            ExecutionContext unlimited = context(0, 0);
            ExecutionContext bounded = context(1_000, 32);
            unlimited.executionConfig(builder -> builder.annotationCollectionEnabled(collect).annotationCollectionFilter(k -> true));
            bounded.executionConfig(builder -> builder.annotationCollectionEnabled(collect).annotationCollectionFilter(k -> true));
            schema.validate(unlimited, node(input));
            schema.validate(bounded, node(input));
            assertEquals(unlimited.getErrors(), bounded.getErrors());
            assertEquals(unlimited.getAnnotations().asMap(), bounded.getAnnotations().asMap());
            List<Boolean> originalValidity = new ArrayList<>();
            List<Boolean> boundedValidity = new ArrayList<>();
            unlimited.getAnnotations().asMap().values().forEach(list -> list.forEach(a -> originalValidity.add(a.isValid())));
            bounded.getAnnotations().asMap().values().forEach(list -> list.forEach(a -> boundedValidity.add(a.isValid())));
            assertEquals(originalValidity, boundedValidity);
        }
    }

    @Test
    void registryDefaultsSurviveOutputCustomizationAndPerCallOverridesWin() {
        Schema schema = SchemaRegistry.withDefaultDialect(SpecificationVersion.DRAFT_2020_12,
                builder -> builder.schemaRegistryConfig(SchemaRegistryConfig.builder()
                        .executionContextCustomizer((context, schemaContext) -> context.executionConfig(
                                config -> config.maxEvaluationSteps(1).maxEvaluationDepth(1)))
                        .build())).getSchema("{\"type\":\"integer\"}");
        for (OutputFormat<?> format : List.of(OutputFormat.DEFAULT, OutputFormat.BOOLEAN, OutputFormat.FLAG,
                OutputFormat.LIST, OutputFormat.HIERARCHICAL)) {
            assertThrows(ValidationLimitExceededException.class, () -> schema.validate(node("1"), format));
            assertDoesNotThrow(() -> schema.validate(node("1"), format,
                    context -> context.executionConfig(config -> config.maxEvaluationSteps(2))));
        }
    }

    @Test
    void allWalkConvenienceEntryPointsEnforceLimitsBeforeFormatting() {
        Schema schema = schema("{\"allOf\":[{}]}");
        AtomicInteger formatted = new AtomicInteger();
        OutputFormat<Boolean> format = (s, context, schemaContext) -> { formatted.incrementAndGet(); return true; };
        assertThrows(ValidationLimitExceededException.class,
                () -> schema.walk(context(2, 0), node("1"), format, false, (ExecutionContextCustomizer) null));
        assertThrows(ValidationLimitExceededException.class,
                () -> schema.walk(context(2, 0), "1", InputFormat.JSON, format, true));
        assertThrows(ValidationLimitExceededException.class,
                () -> schema.walkAtNode(context(2, 0), node("1"), node("1"), ROOT, false));
        assertThrows(ValidationLimitExceededException.class,
                () -> schema.walk("1", InputFormat.JSON, false,
                        context -> context.executionConfig(config -> config.maxEvaluationSteps(2))));
        assertEquals(0, formatted.get());
    }

    @Test
    void differentContextInListenerStartsIndependentExecution() {
        Schema nested = schema("{\"type\":\"integer\"}");
        ExecutionContext context = context(2, 1);
        AtomicInteger visits = new AtomicInteger();
        context.walkConfig(builder -> builder.keywordWalkHandler(KeywordWalkHandler.builder()
                .keywordWalkListener(new WalkListener() {
                    public WalkFlow onWalkStart(WalkEvent event) {
                        nested.validate(context(2, 1), node("1"));
                        visits.incrementAndGet();
                        return WalkFlow.CONTINUE;
                    }
                    public void onWalkEnd(WalkEvent event, List<Error> errors) { }
                }).build()));
        assertDoesNotThrow(() -> nested.walk(context, node("1"), node("1"), ROOT, false));
        assertEquals(1, visits.get());
    }

    @Test
    void skippedKeywordStillConsumesDispatchBeforeListeners() {
        AtomicInteger starts = new AtomicInteger();
        AtomicInteger ends = new AtomicInteger();
        KeywordWalkHandler handler = KeywordWalkHandler.builder().keywordWalkListener(new WalkListener() {
            public WalkFlow onWalkStart(WalkEvent event) { starts.incrementAndGet(); return WalkFlow.SKIP; }
            public void onWalkEnd(WalkEvent event, List<Error> errors) { ends.incrementAndGet(); }
        }).build();
        ExecutionContext context = context(2, 1);
        context.walkConfig(builder -> builder.keywordWalkHandler(handler));
        Schema schema = schema("{\"type\":\"integer\",\"minimum\":0}");
        ValidationLimitExceededException failure = assertThrows(ValidationLimitExceededException.class,
                () -> schema.walk(context, node("1"), node("1"), ROOT, false));
        assertEquals(2, failure.getAdmittedSteps());
        assertEquals("/minimum", failure.getEvaluationPath().toString());
        assertEquals(1, starts.get());
        assertEquals(1, ends.get());
    }

    @Test
    void listenerReentrantConvenienceCallSharesSnapshotAndPath() {
        Schema nested = schema("{\"allOf\":[{}]}");
        AtomicInteger visits = new AtomicInteger();
        ExecutionContext context = context(5, 3);
        context.walkConfig(builder -> builder.keywordWalkHandler(KeywordWalkHandler.builder()
                .keywordWalkListener("type", new WalkListener() {
                    public WalkFlow onWalkStart(WalkEvent event) {
                        visits.incrementAndGet();
                        // Even trusted policy changes cannot replenish an active execution.
                        event.getExecutionContext().executionConfig(config -> config.maxEvaluationSteps(0).maxEvaluationDepth(0));
                        nested.validate(event.getExecutionContext(), node("1"));
                        return WalkFlow.CONTINUE;
                    }
                    public void onWalkEnd(WalkEvent event, List<Error> errors) { }
                }).build()));
        ValidationLimitExceededException failure = assertThrows(ValidationLimitExceededException.class,
                () -> schema("{\"allOf\":[{\"type\":\"integer\"}]}")
                        .walk(context, node("1"), node("1"), ROOT, false));
        assertEquals(5, failure.getAdmittedSteps());
        assertEquals(3, failure.getActiveDepth());
        assertEquals("/allOf/0/allOf", failure.getEvaluationPath().toString());
        assertEquals(1, visits.get());
        assertUnwound(context, null);
    }

    @Test
    void listenerCannotDisableSnapshottedDepthLimit() {
        ExecutionContext context = context(0, 1);
        context.walkConfig(builder -> builder.keywordWalkHandler(KeywordWalkHandler.builder()
                .keywordWalkListener(new WalkListener() {
                    public WalkFlow onWalkStart(WalkEvent event) {
                        event.getExecutionContext().executionConfig(config -> config.maxEvaluationDepth(0));
                        schema("{}").validate(event.getExecutionContext(), node("1"));
                        return WalkFlow.CONTINUE;
                    }
                    public void onWalkEnd(WalkEvent event, List<Error> errors) { }
                }).build()));
        ValidationLimitExceededException failure = assertThrows(ValidationLimitExceededException.class,
                () -> schema("{\"type\":\"integer\"}").walk(context, node("1"), node("1"), ROOT, false));
        assertEquals(LimitKind.EVALUATION_DEPTH, failure.getLimitKind());
        assertEquals(1, failure.getLimit());
        assertEquals(2, failure.getAdmittedSteps());
        assertUnwound(context, null);
    }

    @Test
    void postListenerFailureCannotMaskLimitAndBranchErrorsAreRestored() {
        RuntimeException callbackFailure = new IllegalStateException("post listener failed");
        ExecutionContext context = context(0, 2);
        List<Error> parent = context.getErrors();
        parent.add(Error.builder().keyword("existing").message("existing").build());
        AtomicInteger postCalls = new AtomicInteger();
        context.walkConfig(builder -> builder.keywordWalkHandler(KeywordWalkHandler.builder()
                .keywordWalkListener(new WalkListener() {
                    public WalkFlow onWalkStart(WalkEvent event) { return WalkFlow.CONTINUE; }
                    public void onWalkEnd(WalkEvent event, List<Error> errors) {
                        assertNotNull(errors);
                        postCalls.incrementAndGet();
                        throw callbackFailure;
                    }
                }).build()));
        ValidationLimitExceededException failure = assertThrows(ValidationLimitExceededException.class,
                () -> schema("{\"anyOf\":[{\"allOf\":[{}]}]}")
                        .walk(context, node("1"), node("1"), ROOT, true));
        assertEquals(LimitKind.EVALUATION_DEPTH, failure.getLimitKind());
        assertTrue(List.of(failure.getSuppressed()).contains(callbackFailure));
        assertEquals(2, postCalls.get());
        assertEquals(1, parent.size());
        assertUnwound(context, parent);
    }

    private static Schema swallowingSchema(AtomicReference<ValidationLimitExceededException> swallowed) {
        AbstractKeyword keyword = new AbstractKeyword("swallow") {
            public KeywordValidator newValidator(SchemaLocation location, JsonNode value,
                    Schema parent, SchemaContext schemaContext) {
                return new AbstractKeywordValidator(this, value, location) {
                    public void validate(ExecutionContext context, JsonNode node, JsonNode root, NodePath path) {
                        try {
                            parent.validate(context, node, root, path);
                        } catch (ValidationLimitExceededException failure) {
                            swallowed.set(failure);
                        }
                    }
                };
            }
        };
        return SchemaRegistry.withDialect(Dialect.builder("urn:limits:swallow", Dialects.getDraft202012())
                .keyword(keyword).build()).getSchema("{\"swallow\":true}");
    }

    @Test
    void swallowedExhaustionCannotReturnLowLevelOrFormattedVerdict() {
        AtomicReference<ValidationLimitExceededException> swallowed = new AtomicReference<>();
        Schema schema = swallowingSchema(swallowed);
        AtomicInteger formatted = new AtomicInteger();
        OutputFormat<Boolean> format = (s, context, schemaContext) -> { formatted.incrementAndGet(); return true; };
        ValidationLimitExceededException failure = assertThrows(ValidationLimitExceededException.class,
                () -> schema.validate(context(2, 1), node("1"), format));
        assertSame(swallowed.get(), failure);
        assertEquals(0, formatted.get());
        ExecutionContext lowLevel = context(2, 1);
        ValidationLimitExceededException lowFailure = assertThrows(ValidationLimitExceededException.class,
                () -> schema.validate(lowLevel, node("1"), node("1"), ROOT));
        assertSame(swallowed.get(), lowFailure);
        assertUnwound(lowLevel, null);
        ValidationLimitExceededException walkFailure = assertThrows(ValidationLimitExceededException.class,
                () -> schema.walk(context(2, 1), node("1"), format, true, (ExecutionContextCustomizer) null));
        assertSame(swallowed.get(), walkFailure);
        assertEquals(0, formatted.get());
    }

    @Test
    void counterSaturatesAndLongMaxCapacityIsCheckedWithoutOverflow() throws Exception {
        for (long capacity : new long[] {0, Long.MAX_VALUE}) {
            ExecutionContext context = context(capacity, 1);
            Schema schema = schema("{}");
            context.enterEvaluation(schema.getSchemaLocation(), ROOT, ROOT);
            Field stateField = ExecutionContext.class.getDeclaredField("evaluationState");
            stateField.setAccessible(true);
            Object state = stateField.get(context);
            Field stepsField = state.getClass().getDeclaredField("admittedSteps");
            stepsField.setAccessible(true);
            stepsField.setLong(state, Long.MAX_VALUE - 1);
            context.admitKeyword(schema.getSchemaLocation(), ROOT, "test");
            assertEquals(Long.MAX_VALUE, stepsField.getLong(state));
            if (capacity == 0) {
                context.admitKeyword(schema.getSchemaLocation(), ROOT, "test");
                assertEquals(Long.MAX_VALUE, stepsField.getLong(state));
                context.exitEvaluation();
            } else {
                ValidationLimitExceededException failure = assertThrows(ValidationLimitExceededException.class,
                        () -> context.admitKeyword(schema.getSchemaLocation(), ROOT, "test"));
                assertEquals(Long.MAX_VALUE, failure.getAdmittedSteps());
                assertSame(failure, assertThrows(ValidationLimitExceededException.class, context::exitEvaluation));
            }
        }
    }

    @Test
    void sharedSchemaAndIndependentContextsDoNotShareAccounting() throws Exception {
        Schema schema = schema("{\"allOf\":[{},{}]}"); // entry + dispatch + two entries = four steps
        ExecutorService executor = Executors.newFixedThreadPool(4);
        try {
            List<Callable<Boolean>> calls = new ArrayList<>();
            for (int i = 0; i < 24; i++) {
                boolean limited = i % 2 == 0;
                calls.add(() -> {
                    if (limited) {
                        assertThrows(ValidationLimitExceededException.class,
                                () -> schema.validate(context(3, 2), node("1")));
                    } else {
                        assertTrue(schema.validate(context(4, 2), node("1"), OutputFormat.DEFAULT).isEmpty());
                    }
                    return true;
                });
            }
            for (java.util.concurrent.Future<Boolean> result : executor.invokeAll(calls)) assertTrue(result.get());
        } finally {
            executor.shutdownNow();
        }
    }
}
