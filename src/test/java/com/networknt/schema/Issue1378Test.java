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
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.params.provider.CsvSource;

import com.networknt.schema.ExecutionContext.DiscriminatorScope;
import com.networknt.schema.dialect.Dialects;
import com.networknt.schema.keyword.DiscriminatorState;
import com.networknt.schema.path.NodePath;
import com.networknt.schema.path.PathType;

class Issue1378Test {
    @ParameterizedTest
    @CsvSource({ "oneOf,3.0", "anyOf,3.0", "oneOf,3.1", "anyOf,3.1" })
    void nestedDiscriminatorsStayWithinAlternativeBranches(String keyword, String version) throws Exception {
        String doc;
        try (var input = getClass().getResourceAsStream("/schema/issue1378.json")) {
            doc = new String(input.readAllBytes(), StandardCharsets.UTF_8);
        }
        // Change only the outer alternative; keep both nested oneOf schemas intact.
        doc = doc.replaceFirst("oneOf", keyword);
        String document = doc;
        SchemaRegistry registry = SchemaRegistry.withDialect("3.0".equals(version) ? Dialects.getOpenApi30() : Dialects.getOpenApi31(),
                builder -> builder.schemas(Map.of("https://example.com/openapi.json", document)));
        Schema schema = registry.getSchema(
                SchemaLocation.of("https://example.com/openapi.json#/components/schemas/Container"));
        assertEquals(0, schema.validate("{\"kind\":\"B\",\"items\":[{\"type\":\"x\",\"value\":\"hello\"}]}", InputFormat.JSON).size());
        List<Error> errors = schema.validate("{\"kind\":\"B\",\"items\":[{\"type\":\"x\",\"value\":42}]}", InputFormat.JSON);
        assertEquals("oneOf".equals(keyword) ? 7 : 6, errors.size());
        assertEquals(1, errors.stream().filter(error -> "type".equals(error.getKeyword())
                && error.getInstanceLocation().toString().endsWith("value")).count());
        assertEquals(0, schema.validate("{\"kind\":\"B\",\"items\":[{\"type\":\"y\",\"count\":1}]}", InputFormat.JSON).size());
        assertEquals(0, schema.validate("{\"kind\":\"A\",\"items\":[{\"type\":\"x\",\"values\":[\"a\"]}]}", InputFormat.JSON).size());
        String valid = "{\"kind\":\"B\",\"items\":[{\"type\":\"x\",\"value\":\"hello\"}]}";
        assertTrue(schema.walk(valid, InputFormat.JSON, true).getErrors().isEmpty());
        assertTrue(schema.walk(valid, InputFormat.JSON, false).getErrors().isEmpty());
        String differentProperty = document.replaceFirst("\"propertyName\": \"type\"", "\"propertyName\": \"other\"");
        Schema changed = SchemaRegistry.withDialect("3.0".equals(version) ? Dialects.getOpenApi30() : Dialects.getOpenApi31(),
                builder -> builder.schemas(java.util.Collections.singletonMap("https://example.com/openapi.json", differentProperty)))
                .getSchema(SchemaLocation.of("https://example.com/openapi.json#/components/schemas/Container"));
        assertTrue(changed.validate(valid, InputFormat.JSON).isEmpty());
        assertTrue(changed.walk(valid, InputFormat.JSON, true).getErrors().isEmpty());
    }

    @ParameterizedTest
    @CsvSource({ "oneOf,3.0", "anyOf,3.0", "oneOf,3.1", "anyOf,3.1" })
    void sameLocationRedefinitionsStillThrow(String keyword, String version) {
        Schema schema = SchemaRegistry.withDialect("3.0".equals(version) ? Dialects.getOpenApi30() : Dialects.getOpenApi31()).getSchema(
                "{\"" + keyword + "\":[{\"discriminator\":{\"propertyName\":\"a\"}},"
                + "{\"discriminator\":{\"propertyName\":\"b\"}}]}", InputFormat.JSON);
        assertTrue(assertThrows(SchemaException.class,
                () -> schema.validate("{}", InputFormat.JSON)).getMessage().contains("redefining"));
        assertThrows(SchemaException.class, () -> schema.walk("{}", InputFormat.JSON, true));
        ExecutionContext context = new ExecutionContext();
        schema.walk(context, "{}", InputFormat.JSON, false);
        assertTrue(context.getDiscriminatorMapping().isEmpty());
    }

    @Test
    void scopePreservesExistingAndSameLocationStateAndUnwindsNestedFailures() {
        ExecutionContext context = new ExecutionContext();
        NodePath root = new NodePath(PathType.JSON_POINTER);
        NodePath child = root.append("items").append(0);
        DiscriminatorState existing = new DiscriminatorState();
        context.getDiscriminatorMapping().put(child, existing);
        RuntimeException failure = new RuntimeException("branch failure");
        assertSame(failure, assertThrows(RuntimeException.class, () -> {
            try (DiscriminatorScope outer = context.openDiscriminatorScope(root, true)) {
                context.getDiscriminatorMapping().put(root, new DiscriminatorState());
                try (DiscriminatorScope inner = context.openDiscriminatorScope(child, true)) {
                    context.getDiscriminatorMapping().put(child.append("nested"), new DiscriminatorState());
                    throw failure;
                }
            }
        }));
        assertEquals(2, context.getDiscriminatorMapping().size());
        assertSame(existing, context.getDiscriminatorMapping().get(child));
        assertTrue(context.getDiscriminatorMapping().containsKey(root));
        try (DiscriminatorScope outer = context.openDiscriminatorScope(root, true);
                DiscriminatorScope inner = context.openDiscriminatorScope(child, true)) {
            context.getDiscriminatorMapping().put(root.append("new"), new DiscriminatorState());
        }
        assertEquals(2, context.getDiscriminatorMapping().size());
    }

    @ParameterizedTest
    @CsvSource({ "oneOf,3.0", "anyOf,3.0", "oneOf,3.1", "anyOf,3.1" })
    void branchWorkDoesNotTraversePreviouslyVisitedElements(String keyword, String version) throws Exception {
        Schema schema = SchemaRegistry.withDialect("3.0".equals(version) ? Dialects.getOpenApi30() : Dialects.getOpenApi31()).getSchema(
                "{\"oneOf\":[{\"type\":\"object\",\"properties\":{\"items\":{\"type\":\"array\","
                + "\"items\":{\"discriminator\":{\"propertyName\":\"type\"},\"" + keyword
                + "\":[{\"$ref\":\"#/definitions/object\"},{\"$ref\":\"#/definitions/string\"}]}}}},{\"type\":\"null\"}],"
                + "\"definitions\":{\"object\":{\"type\":\"object\"},\"string\":{\"type\":\"string\"}}}", InputFormat.JSON);
        int smallCount = 1000;
        int largeCount = 16000;
        long smallValidationWork = 0;
        long smallWalkWork = 0;
        for (int count : new int[] { smallCount, largeCount }) {
            ExecutionContext context = new ExecutionContext();
            CountingEntries entries = countBackingEntries(context);
            StringBuilder input = new StringBuilder("{\"items\":[");
            for (int i = 0; i < count; i++) {
                if (i > 0) {
                    input.append(',');
                }
                input.append("{\"type\":\"object\"}");
            }
            input.append("]}");
            List<Error> errors = schema.validate(context, input.toString(), InputFormat.JSON);
            assertTrue(errors.isEmpty(), () -> errors.subList(0, Math.min(5, errors.size())).toString());
            assertTrue(context.getDiscriminatorMapping().isEmpty(), "Outer branch must discard all item discriminators");
            if (count == smallCount) {
                smallValidationWork = entries.operations;
            } else {
                assertLinearGrowth(smallValidationWork, entries.operations, smallCount, largeCount);
            }
            ExecutionContext walking = new ExecutionContext();
            CountingEntries walkEntries = countBackingEntries(walking);
            assertTrue(schema.walk(walking, input.toString(), InputFormat.JSON, true).getErrors().isEmpty());
            assertTrue(walking.getDiscriminatorMapping().isEmpty(), "Outer branch must discard all item discriminators");
            if (count == smallCount) {
                smallWalkWork = walkEntries.operations;
            } else {
                assertLinearGrowth(smallWalkWork, walkEntries.operations, smallCount, largeCount);
            }
        }
    }

    @ParameterizedTest
    @ValueSource(strings = { "put", "putIfAbsent", "computeIfAbsent", "compute", "putAll", "merge" })
    void allMapInsertionMethodsParticipateInBranchCleanup(String method) {
        ExecutionContext context = new ExecutionContext();
        Map<NodePath, DiscriminatorState> mapping = context.getDiscriminatorMapping();
        NodePath root = new NodePath(PathType.JSON_POINTER);
        NodePath child = root.append("child");
        NodePath existing = root.append("existing");
        DiscriminatorState state = new DiscriminatorState();
        mapping.put(existing, state);
        try (DiscriminatorScope outer = context.openDiscriminatorScope(root, true)) {
            insert(method, mapping, root, state);
            try (DiscriminatorScope inner = context.openDiscriminatorScope(child, true)) {
                insert(method, mapping, child, state);
                insert(method, mapping, child.append("nested"), state);
            }
            assertSame(state, mapping.get(child));
            assertTrue(!mapping.containsKey(child.append("nested")));
        }
        assertEquals(2, mapping.size());
        assertSame(state, mapping.get(root));
        assertSame(state, mapping.get(existing));
        assertTrue(!mapping.containsKey(child));
    }

    private static void insert(String method, Map<NodePath, DiscriminatorState> mapping,
            NodePath path, DiscriminatorState state) {
        switch (method) {
        case "put":
            mapping.put(path, state);
            break;
        case "putIfAbsent":
            mapping.putIfAbsent(path, state);
            break;
        case "computeIfAbsent":
            mapping.computeIfAbsent(path, key -> state);
            break;
        case "compute":
            mapping.compute(path, (key, old) -> state);
            break;
        case "putAll":
            mapping.putAll(java.util.Collections.singletonMap(path, state));
            break;
        case "merge":
            mapping.merge(path, state, (old, value) -> value);
            break;
        default:
            throw new AssertionError(method);
        }
    }

    @Test
    void disabledScopesAreSharedAndLeaveStateUntouched() {
        ExecutionContext context = new ExecutionContext();
        ExecutionContext other = new ExecutionContext();
        NodePath root = new NodePath(PathType.JSON_POINTER);
        DiscriminatorScope disabled = context.openDiscriminatorScope(root, false);
        assertSame(disabled, other.openDiscriminatorScope(root, false));
        try (DiscriminatorScope scope = disabled) {
            context.getDiscriminatorMapping().put(root.append("child"), new DiscriminatorState());
        }
        disabled.close();
        assertEquals(1, context.getDiscriminatorMapping().size());
        try (DiscriminatorScope scope = context.openDiscriminatorScope(root, true)) {
            context.getDiscriminatorMapping().put(root.append("new"), new DiscriminatorState());
        }
        assertEquals(1, context.getDiscriminatorMapping().size());
    }

    private static void assertLinearGrowth(long smallWork, long largeWork, int smallCount, int largeCount) {
        assertTrue(smallWork > 0, "The backing map must actually be instrumented");
        // The same per-item workload grows 16-fold. Linear work may grow at most
        // by that input ratio; fixed startup work only reduces the observed ratio.
        assertTrue(largeWork * smallCount <= smallWork * largeCount,
                () -> "Discriminator map work grew " + ((double) largeWork / smallWork)
                        + "x for " + ((double) largeCount / smallCount) + "x as many items");
    }

    @SuppressWarnings("unchecked")
    private static CountingEntries countBackingEntries(ExecutionContext context) throws Exception {
        // Keep instrumentation test-local: wrap the actual backing entries of an
        // ordinarily constructed context, without adding a production test hook.
        Field field = DiscriminatorMapping.class.getDeclaredField("entries");
        field.setAccessible(true);
        Map<NodePath, DiscriminatorState> mapping = context.getDiscriminatorMapping();
        CountingEntries counting = new CountingEntries((Map<NodePath, DiscriminatorState>) field.get(mapping));
        field.set(mapping, counting);
        return counting;
    }

    /**
     * Instruments the actual private map's backing entries, rather than overriding
     * the public getter. A scan in ExecutionContext or DiscriminatorMapping must
     * visit these iterators too. Counting is passive; assertions run only after
     * validation or walking has returned.
     */
    private static final class CountingEntries extends AbstractMap<NodePath, DiscriminatorState> {
        private final Map<NodePath, DiscriminatorState> entries;
        private long operations;

        private CountingEntries(Map<NodePath, DiscriminatorState> entries) {
            this.entries = entries;
        }

        private void visit() {
            operations++;
        }

        @Override
        public DiscriminatorState get(Object path) {
            visit();
            return entries.get(path);
        }

        @Override
        public boolean containsKey(Object path) {
            visit();
            return entries.containsKey(path);
        }

        @Override
        public DiscriminatorState put(NodePath path, DiscriminatorState value) {
            visit();
            return entries.put(path, value);
        }

        @Override
        public DiscriminatorState remove(Object path) {
            visit();
            return entries.remove(path);
        }

        @Override
        public int size() {
            return entries.size();
        }

        @Override
        public Set<Entry<NodePath, DiscriminatorState>> entrySet() {
            return new AbstractSet<Entry<NodePath, DiscriminatorState>>() {
                @Override
                public int size() {
                    return entries.size();
                }

                @Override
                public Iterator<Entry<NodePath, DiscriminatorState>> iterator() {
                    Iterator<Entry<NodePath, DiscriminatorState>> delegate = entries.entrySet().iterator();
                    return new Iterator<Entry<NodePath, DiscriminatorState>>() {
                        @Override
                        public boolean hasNext() {
                            return delegate.hasNext();
                        }

                        @Override
                        public Entry<NodePath, DiscriminatorState> next() {
                            visit();
                            return delegate.next();
                        }

                        @Override
                        public void remove() {
                            delegate.remove();
                        }
                    };
                }
            };
        }
    }
}
