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
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Stream;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import com.networknt.schema.annotation.Annotation;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

class Issue1334Test {
    static Stream<Arguments> largeLimits() {
        return Stream.of(SpecificationVersion.DRAFT_4, SpecificationVersion.DRAFT_6, SpecificationVersion.DRAFT_7,
                        SpecificationVersion.DRAFT_2019_09, SpecificationVersion.DRAFT_2020_12)
                .flatMap(version -> Stream.of("2147483647", "2147483648", "4294967296",
                        "9223372036854775808", "2147483648.0", "1e30", "1e400", "1e1000000000")
                        .flatMap(limit -> Stream.of("Items", "Properties", "Length", "Contains")
                                .filter(kind -> !"Contains".equals(kind)
                                        || version == SpecificationVersion.DRAFT_2019_09
                                        || version == SpecificationVersion.DRAFT_2020_12)
                                .map(kind -> Arguments.of(version, limit, kind))));
    }

    @ParameterizedTest
    @MethodSource("largeLimits")
    void largeLimitsKeepTheirValue(SpecificationVersion version, String limit, String kind) {
        String instance = "Properties".equals(kind) ? "{}" : "Length".equals(kind) ? "\"\"" : "[]";
        String contains = "Contains".equals(kind) ? "\"contains\":true," : "";
        String minimum = "min" + kind;
        List<Error> errors = SchemaRegistry.withDefaultDialect(version)
                .getSchema("{" + contains + "\"" + minimum + "\":" + limit + "}")
                .validate(instance, InputFormat.JSON);
        assertEquals(1, errors.size());
        assertEquals(minimum, errors.get(0).getKeyword());
        if (Double.isInfinite(Double.parseDouble(limit))) {
            assertEquals("Infinity", errors.get(0).getArguments()[0]);
        } else {
            assertEquals(0, new BigDecimal(limit).compareTo(
                    new BigDecimal(errors.get(0).getArguments()[0].toString())), errors.get(0).toString());
        }
        String nonEmpty = "Properties".equals(kind) ? "{\"a\":1}" : "Length".equals(kind) ? "\"a\"" : "[1]";
        assertTrue(SchemaRegistry.withDefaultDialect(version)
                .getSchema("{" + contains + "\"max" + kind + "\":" + limit + "}")
                .validate(nonEmpty, InputFormat.JSON).isEmpty());
    }

    @ParameterizedTest
    @MethodSource("largeLimits")
    void applicabilityIsUnchanged(SpecificationVersion version, String limit, String kind) {
        String instance = "Length".equals(kind) ? "[]" : "\"text\"";
        String bounds = "\"min" + kind + "\":" + limit + ",\"max" + kind + "\":" + limit;
        SchemaRegistry registry = SchemaRegistry.withDefaultDialect(version);
        assertTrue(registry.getSchema("{" + bounds + "}").validate(instance, InputFormat.JSON).isEmpty());
        if ("Contains".equals(kind)) {
            assertTrue(registry.getSchema("{" + bounds + "}").validate("[]", InputFormat.JSON).isEmpty());
            assertTrue(registry.getSchema("{\"contains\":true," + bounds + "}")
                    .validate(instance, InputFormat.JSON).isEmpty());
        }
    }

    @Test
    void ordinaryBoundariesAndCodePoints() {
        SchemaRegistry registry = SchemaRegistry.withDefaultDialect(SpecificationVersion.DRAFT_2020_12);
        for (String kind : new String[] { "Items", "Properties", "Length", "Contains" }) {
            String instance = "Items".equals(kind) || "Contains".equals(kind) ? "[1]"
                    : "Properties".equals(kind) ? "{\"a\":1}" : "\"\uD83D\uDE00\"";
            String contains = "Contains".equals(kind) ? "\"contains\":true," : "";
            assertTrue(registry.getSchema("{" + contains + "\"min" + kind + "\":1,\"max" + kind + "\":1}")
                    .validate(instance, InputFormat.JSON).isEmpty());
            assertEquals(1, registry.getSchema("{" + contains + "\"min" + kind + "\":2}")
                    .validate(instance, InputFormat.JSON).size());
            assertEquals(1, registry.getSchema("{" + contains + "\"max" + kind + "\":0}")
                    .validate(instance, InputFormat.JSON).size());
        }
    }

    @Test
    void containsAnnotationsKeepOrdinaryIntegersAndLargeValues() {
        SchemaRegistry registry = SchemaRegistry.withDefaultDialect(SpecificationVersion.DRAFT_2020_12);
        Result result = registry.getSchema("{\"contains\":{\"type\":\"integer\"},\"minContains\":0,"
                + "\"maxContains\":9223372036854775808}")
                .validate("[1,\"a\"]", InputFormat.JSON, OutputFormat.RESULT,
                        context -> context.executionConfig(config -> config.annotationCollectionEnabled(true)
                                .annotationCollectionFilter(keyword -> true)));
        assertTrue(result.getErrors().isEmpty());
        Map<String, Annotation> annotations = result.getExecutionContext().getAnnotations().asMap().values().stream()
                .flatMap(List::stream).collect(Collectors.toMap(Annotation::getKeyword, Function.identity()));
        assertEquals(Integer.valueOf(0), annotations.get("minContains").getValue());
        assertEquals(0, new BigDecimal("9223372036854775808").compareTo(
                new BigDecimal(annotations.get("maxContains").getValue().toString())));
        assertEquals(java.util.Arrays.asList(0), annotations.get("contains").getValue());
    }

    @Test
    void exponentSizedLimitsDoNotExpandToIntegersOrMessages() {
        SchemaRegistry registry = SchemaRegistry.withDefaultDialect(SpecificationVersion.DRAFT_2020_12);
        for (String kind : new String[] { "Items", "Properties", "Length", "Contains" }) {
            ObjectNode schemaNode = JsonNodeFactory.instance.objectNode();
            schemaNode.put("min" + kind, new BigDecimal("1e1000000000"));
            if ("Contains".equals(kind)) {
                schemaNode.put("contains", true);
            }
            String instance = "Properties".equals(kind) ? "{}" : "Length".equals(kind) ? "\"\"" : "[]";
            List<Error> errors = registry.getSchema(schemaNode).validate(instance, InputFormat.JSON);
            assertEquals(1, errors.size());
            assertTrue(errors.get(0).getMessage().contains("1E+1000000000"));
            assertTrue(errors.get(0).getMessage().length() < 200);
        }
    }

    static Stream<SpecificationVersion> allDrafts() {
        return Stream.of(SpecificationVersion.DRAFT_4, SpecificationVersion.DRAFT_6,
                SpecificationVersion.DRAFT_7, SpecificationVersion.DRAFT_2019_09,
                SpecificationVersion.DRAFT_2020_12);
    }

    @ParameterizedTest
    @MethodSource("allDrafts")
    void negativeOutOfRangeLimits(SpecificationVersion version) {
        SchemaRegistry registry = SchemaRegistry.withDefaultDialect(version);
        for (String limit : new String[] { "-2147483649", "-1e400" }) {
            for (String kind : new String[] { "Items", "Properties", "Length" }) {
                String instance = "Properties".equals(kind) ? "{}" : "Length".equals(kind) ? "\"\"" : "[]";
                assertTrue(registry.getSchema("{\"min" + kind + "\":" + limit + "}")
                        .validate(instance, InputFormat.JSON).isEmpty());
                List<Error> errors = registry.getSchema("{\"max" + kind + "\":" + limit + "}")
                        .validate(instance, InputFormat.JSON);
                assertEquals(1, errors.size());
                if (Double.isInfinite(Double.parseDouble(limit))) {
                    assertEquals("-Infinity", errors.get(0).getArguments()[0]);
                } else {
                    assertEquals(0, new BigDecimal(limit).compareTo(
                            new BigDecimal(errors.get(0).getArguments()[0].toString())));
                }
            }
        }
    }

    @ParameterizedTest
    @MethodSource("allDrafts")
    void looseArrayCountsUseOneForScalars(SpecificationVersion version) {
        SchemaRegistryConfig config = SchemaRegistryConfig.builder().typeLoose(true).build();
        SchemaRegistry registry = SchemaRegistry.withDefaultDialect(version,
                builder -> builder.schemaRegistryConfig(config));
        for (String limit : new String[] { "2147483648", "1e400", "1e1000000000" }) {
            assertEquals(1, registry.getSchema("{\"minItems\":" + limit + "}")
                    .validate("1", InputFormat.JSON).size());
            assertTrue(registry.getSchema("{\"maxItems\":" + limit + "}")
                    .validate("1", InputFormat.JSON).isEmpty());
        }
        assertTrue(registry.getSchema("{\"minItems\":-2147483649}")
                .validate("1", InputFormat.JSON).isEmpty());
        assertEquals(1, registry.getSchema("{\"maxItems\":-2147483649}")
                .validate("1", InputFormat.JSON).size());
    }

    @Test
    void countMessagesUseConsistentArgumentsAcrossIntBoundaryAndLocales() {
        for (java.util.Locale locale : new java.util.Locale[] { java.util.Locale.US, java.util.Locale.GERMANY }) {
            SchemaRegistryConfig config = SchemaRegistryConfig.builder().locale(locale).build();
            SchemaRegistry registry = SchemaRegistry.withDefaultDialect(SpecificationVersion.DRAFT_2020_12,
                    builder -> builder.schemaRegistryConfig(config));
            for (String limit : new String[] { "2147483647", "2147483648" }) {
                for (String kind : new String[] { "Items", "Properties", "Length", "Contains" }) {
                    String contains = "Contains".equals(kind) ? "\"contains\":true," : "";
                    String instance = "Properties".equals(kind) ? "{}" : "Length".equals(kind) ? "\"\"" : "[]";
                    Error error = registry.getSchema("{" + contains + "\"min" + kind + "\":" + limit + "}")
                            .validate(instance, InputFormat.JSON).get(0);
                    assertEquals(limit, error.getArguments()[0]);
                    assertTrue(error.getMessage().contains(limit));
                }
            }
        }
    }

    @Test
    void fractionalMaxItemsDoesNotInventAZeroMaximum() {
        assertTrue(SchemaRegistry.withDefaultDialect(SpecificationVersion.DRAFT_2020_12)
                .getSchema("{\"maxItems\":1.5}").validate("[1]", InputFormat.JSON).isEmpty());
    }

    @Test
    void countPrecisionWorksForSchemaStreamsAndConfiguredNodeFactories() throws Exception {
        for (com.networknt.schema.serialization.NodeReader reader : new com.networknt.schema.serialization.NodeReader[] {
                com.networknt.schema.serialization.BasicNodeReader.getInstance(),
                com.networknt.schema.serialization.NodeReader.builder().build(),
                com.networknt.schema.serialization.NodeReader.builder().locationAware().build() }) {
            SchemaRegistry registry = SchemaRegistry.withDefaultDialect(SpecificationVersion.DRAFT_2020_12,
                    builder -> builder.nodeReader(reader));
            String text = "{\"$defs\":{\"large\":{\"minItems\":1e400}},\"$ref\":\"#/$defs/large\"}";
            Schema schema = registry.getSchema(new java.io.ByteArrayInputStream(
                    text.getBytes(java.nio.charset.StandardCharsets.UTF_8)), InputFormat.JSON);
            List<Error> errors = schema.validate("[]", InputFormat.JSON);
            assertEquals(1, errors.size());
            assertEquals("Infinity", errors.get(0).getArguments()[0]);
            assertTrue(registry.getSchema("{\"minItems\":1,\"multipleOf\":1.1}")
                    .getSchemaNode().get("minItems").isInt());
            assertTrue(registry.getSchema("{\"multipleOf\":1.1}")
                    .getSchemaNode().get("multipleOf").isDouble());
            assertTrue(Double.isInfinite(reader.readTree("{\"minItems\":1e400}", InputFormat.JSON)
                    .get("minItems").doubleValue()));
        }
    }
}
