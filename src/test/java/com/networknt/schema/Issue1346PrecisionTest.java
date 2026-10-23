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
import static org.junit.jupiter.api.Assertions.assertThrows;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import com.networknt.schema.serialization.BasicNodeReader;
import com.networknt.schema.serialization.NodeReader;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.node.JsonNodeFactory;
import tools.jackson.databind.node.ObjectNode;

class Issue1346PrecisionTest {
    static Stream<NodeReader> readers() {
        return Stream.of(BasicNodeReader.getInstance(), NodeReader.builder().build(),
                NodeReader.builder().locationAware().build(), NodeReader.builder().jsonMapper(
                        JsonMapper.builder().enable(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS).build()).build());
    }

    private static SchemaRegistry registry(NodeReader reader) {
        return SchemaRegistry.withDefaultDialect(SpecificationVersion.DRAFT_2020_12,
                builder -> builder.nodeReader(reader));
    }

    private static Schema[] schemas(SchemaRegistry registry, String key, String value, InputFormat format) {
        String prefix = key.endsWith("Contains") ? (format == InputFormat.JSON ? "\"contains\":true," : "contains: true\n") : "";
        String text = format == InputFormat.JSON ? "{" + prefix + "\"" + key + "\":" + value + "}"
                : prefix + key + ": " + value + "\n";
        return new Schema[] { registry.getSchema(text, format), registry.getSchema(
                new ByteArrayInputStream(text.getBytes(StandardCharsets.UTF_8)), format) };
    }

    @ParameterizedTest
    @MethodSource("readers")
    void fractionalTokensKeepTheirFallbackEvenWhenDoublesBecomeWhole(NodeReader reader) {
        for (String value : new String[] { "1e-400", "-1e-400", "0.99999999999999999999",
                "1.0000000000000000001", "2147483647.9999999999" }) {
            for (InputFormat format : new InputFormat[] { InputFormat.JSON, InputFormat.YAML }) {
                for (String kind : new String[] { "Items", "Properties", "Length" }) {
                    String instance = kind.equals("Items") ? "[1,2]" : kind.equals("Properties") ? "{\"a\":1,\"b\":2}" : "\"ab\"";
                    for (String prefix : new String[] { "min", "max" }) {
                        for (Schema schema : schemas(registry(reader), prefix + kind, value, format)) {
                            assertTrue(schema.validate(instance, InputFormat.JSON).isEmpty(), prefix + kind + ": " + value);
                        }
                    }
                }
                for (String key : new String[] { "minContains", "maxContains" }) {
                    for (Schema schema : schemas(registry(reader), key, value, format)) {
                        List<Error> errors = schema.validate("[1,2]", InputFormat.JSON);
                        assertTrue(errors.stream().anyMatch(error -> key.equals(error.getKeyword())
                                && error.getArguments()[0].toString().startsWith("{")), key + ": " + value);
                    }
                }
            }
        }
    }

    @ParameterizedTest
    @MethodSource("readers")
    void messagesUseExactValuesAndCanonicalEquivalentSpellings(NodeReader reader) {
        for (String[] pair : new String[][] { { "9007199254740993.0", "9007199254740993" },
                { "3000000000", "3000000000" }, { "3000000000.0", "3000000000" }, { "3e9", "3E+9" },
                { "1.0e400", "1E+400" } }) {
            for (InputFormat format : new InputFormat[] { InputFormat.JSON, InputFormat.YAML }) {
                for (String key : new String[] { "minItems", "minProperties", "minLength", "minContains" }) {
                    for (Schema schema : schemas(registry(reader), key, pair[0], format)) {
                        String instance = key.equals("minProperties") ? "{}" : key.equals("minLength") ? "\"\"" : "[]";
                        assertEquals(pair[1], schema.validate(instance, InputFormat.JSON).get(0).getArguments()[0]);
                    }
                }
            }
        }
    }

    private static Object annotation(Schema schema) {
        Result result = schema.validate("[1]", InputFormat.JSON, OutputFormat.RESULT,
                context -> context.executionConfig(config -> config.annotationCollectionEnabled(true)
                        .annotationCollectionFilter(keyword -> true)));
        return result.getExecutionContext().getAnnotations().asMap().values().stream().flatMap(List::stream)
                .filter(a -> "maxContains".equals(a.getKeyword())).findFirst().get().getValue();
    }

    @ParameterizedTest
    @MethodSource("readers")
    void tokenAnnotationsMatchParsedValuesWithoutRangeDependentCoercion(NodeReader reader) {
        for (String value : new String[] { "1.0", "3000000000.0", "9007199254740993.0", "1.0e400" }) {
            for (Schema schema : schemas(registry(reader), "maxContains", value, InputFormat.JSON)) {
                Object actual = annotation(schema);
                assertEquals(schema.getSchemaNode().get("maxContains").numberValue(), actual);
            }
        }
    }

    @Test
    void programmaticDoubleAnnotationsKeepTheInputTypeAtEveryMagnitude() {
        for (double value : new double[] { 1, 3e9, Double.POSITIVE_INFINITY }) {
            ObjectNode node = JsonNodeFactory.instance.objectNode();
            node.put("contains", true);
            node.put("maxContains", value);
            assertEquals(Double.valueOf(value), annotation(SchemaRegistry.withDefaultDialect(
                    SpecificationVersion.DRAFT_2020_12).getSchema(node)));
        }
    }
    @ParameterizedTest
    @MethodSource("readers")
    void escapedReferencesAndDuplicateFieldsUseTheCorrectToken(NodeReader reader) {
        SchemaRegistry registry = registry(reader);
        Schema referenced = registry.getSchema("{\"$ref\":\"#/arbitrary/a~1b~0\","
                + "\"arbitrary\":{\"a/b~\":{\"maxItems\":1e-400}}}");
        assertTrue(referenced.validate("[1,2]", InputFormat.JSON).isEmpty());
        Schema lastIntegral = registry.getSchema("{\"maxItems\":1e-400,\"maxItems\":2.0}");
        assertTrue(lastIntegral.validate("[1,2]", InputFormat.JSON).isEmpty());
        assertEquals(1, lastIntegral.validate("[1,2,3]", InputFormat.JSON).size());
        Schema lastFractional = registry.getSchema("{\"maxItems\":2.0,\"maxItems\":1e-400}");
        assertTrue(lastFractional.validate("[1,2,3]", InputFormat.JSON).isEmpty());
        String literal = "{\"const\":{\"maxItems\":[1.0,{\"minItems\":0.99999999999999999999}]}}";
        assertEquals(reader.readTree(literal, InputFormat.JSON).toString(), registry.getSchema(literal).getSchemaNode().toString());
    }

    @Test
    void countTokenMetadataPreservesLocations() throws Exception {
        NodeReader reader = NodeReader.builder().locationAware().build();
        String text = "{\"minItems\": 1.0}";
        assertEquals(com.networknt.schema.utils.JsonNodes.tokenStreamLocationOf(
                        reader.readTree(text, InputFormat.JSON).get("minItems")).toString(),
                com.networknt.schema.utils.JsonNodes.tokenStreamLocationOf(
                        registry(reader).getSchema(text).getSchemaNode().get("minItems")).toString());
    }

    @Test
    void unsupportedFiniteScaleDoesNotSilentlyBecomeAnIntegralZero() {
        assertThrows(RuntimeException.class, () -> registry(BasicNodeReader.getInstance())
                .getSchema("{\"maxItems\":1e-2147483648}").validate("[1]", InputFormat.JSON));
    }
}
