/*
 * Copyright (c) 2016 Network New Technologies Inc.
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

import java.util.HashSet;
import java.util.Set;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Bounded characterization of the recursive applicator cost in issue #1276.
 *
 * These counts describe the current limitation, not a required output contract.
 * Update them when validation avoids repeated work or bounds diagnostics. Keep
 * this fixture shallow: it must not depend on timing thresholds or heap exhaustion.
 */
class Issue1276Test {
    private static Stream<Arguments> recursiveAlternatives() {
        return Stream.of("anyOf", "oneOf").flatMap(keyword ->
                Stream.of("DEFAULT", "BOOLEAN", "FLAG").flatMap(format ->
                        Stream.of(false, true).flatMap(failFast ->
                                IntStream.of(0, 4, 8).mapToObj(depth ->
                                        Arguments.of(keyword, format, failFast, depth)))));
    }

    @ParameterizedTest(name = "{0}, {1}, failFast={2}, depth={3}")
    @MethodSource("recursiveAlternatives")
    void failingRecursiveAlternativesAccumulateErrors(String keyword, String format,
            boolean failFast, int depth) {
        String schemaText = "{\"$ref\":\"#/$defs/n\",\"$defs\":{\"n\":{\"" + keyword + "\":["
                + "{\"type\":\"array\",\"items\":{\"$ref\":\"#/$defs/n\"},\"minItems\":0},"
                + "{\"type\":\"array\",\"items\":{\"$ref\":\"#/$defs/n\"},\"maxItems\":99}]}}}";
        SchemaRegistryConfig config = SchemaRegistryConfig.builder().failFast(failFast).build();
        Schema schema = SchemaRegistry.withDefaultDialect(SpecificationVersion.DRAFT_2020_12,
                builder -> builder.schemaRegistryConfig(config)).getSchema(schemaText);
        StringBuilder input = new StringBuilder();
        for (int i = 0; i < depth; i++) {
            input.append('[');
        }
        input.append("\"x\"");
        for (int i = 0; i < depth; i++) {
            input.append(']');
        }

        CountingExecutionContext context = new CountingExecutionContext(
                schema.createExecutionContext().getExecutionConfig());
        if ("BOOLEAN".equals(format)) {
            assertFalse(schema.validate(context, input.toString(), InputFormat.JSON, OutputFormat.BOOLEAN));
        } else if ("FLAG".equals(format)) {
            assertFalse(schema.validate(context, input.toString(), InputFormat.JSON, OutputFormat.FLAG).isValid());
        } else {
            assertFalse(schema.validate(context, input.toString(), InputFormat.JSON).isEmpty());
        }

        long leafErrors = 1L << (depth + 1);
        assertEquals(leafErrors, context.typeErrors);
        assertEquals(2, context.typeErrorLocations.size());
        // oneOf can discard child diagnostics at the root when fail-fast is on,
        // but the counter above still observes all repeated leaf failures.
        long expectedErrors = "anyOf".equals(keyword) ? leafErrors
                : failFast || !"DEFAULT".equals(format) ? 1 : 2 * leafErrors - 1;
        assertEquals(expectedErrors, context.getErrors().size());
    }

    private static class CountingExecutionContext extends ExecutionContext {
        private long typeErrors;
        private final Set<String> typeErrorLocations = new HashSet<>();

        CountingExecutionContext(ExecutionConfig config) {
            super(config);
        }

        @Override
        public void addError(Error error) {
            if ("type".equals(error.getKeyword())) {
                typeErrors++;
                typeErrorLocations.add(error.getSchemaLocation().toString() + "|" + error.getInstanceLocation());
            }
            super.addError(error);
        }
    }
}
