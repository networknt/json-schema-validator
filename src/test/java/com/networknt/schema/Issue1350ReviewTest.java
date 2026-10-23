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

import static org.junit.jupiter.api.Assertions.*;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import com.networknt.schema.serialization.BasicNodeReader;
import com.networknt.schema.serialization.NodeReader;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.json.JsonMapper;

class Issue1350ReviewTest {
    static Stream<NodeReader> readers() {
        return Stream.of(BasicNodeReader.getInstance(), NodeReader.builder().build(),
                NodeReader.builder().locationAware().build(), NodeReader.builder().jsonMapper(
                        JsonMapper.builder().enable(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS).build()).build(),
                NodeReader.builder().jsonNodeFactoryFactory(parser -> new tools.jackson.databind.node.JsonNodeFactory() {
                    private static final long serialVersionUID = 1L;
                    @Override
                    public tools.jackson.databind.node.NumericNode numberNode(double value) {
                        return tools.jackson.databind.node.DoubleNode.valueOf(value);
                    }
                    @Override
                    public tools.jackson.databind.node.NumericNode numberNode(java.math.BigDecimal value) {
                        return tools.jackson.databind.node.DecimalNode.valueOf(value);
                    }
                }).build());
    }

    @ParameterizedTest
    @MethodSource("readers")
    void schemaAndInstanceLiteralsHaveConsistentNumericValues(NodeReader reader) throws Exception {
        SchemaRegistry registry = SchemaRegistry.withDefaultDialect(SpecificationVersion.DRAFT_2020_12,
                builder -> builder.nodeReader(reader));
        for (String keyword : new String[] { "minItems", "maxItems", "minProperties", "maxProperties",
                "minLength", "maxLength", "minContains", "maxContains", "foo" }) {
            for (String value : new String[] { "0.99999999999999999999", "1e-400", "9007199254740993.0" }) {
                String literal = "{\"" + keyword + "\":" + value + "}";
                for (String schemaText : new String[] { "{\"const\":" + literal + "}",
                        "{\"enum\":[" + literal + "]}" }) {
                    for (InputFormat format : new InputFormat[] { InputFormat.JSON, InputFormat.YAML }) {
                        Schema schema = registry.getSchema(schemaText, format);
                        assertTrue(schema.validate(literal, format).isEmpty(), schemaText);
                        assertTrue(schema.validate(reader.readTree(new ByteArrayInputStream(
                                literal.getBytes(StandardCharsets.UTF_8)), format)).isEmpty(), schemaText);
                    }
                    assertTrue(registry.getSchema(registry.getSchema(schemaText).getSchemaNode().deepCopy())
                            .validate(literal, InputFormat.JSON).isEmpty(), schemaText);
                }
            }
        }
        assertEquals(1, registry.getSchema("{\"const\":{\"minItems\":0.99999999999999999999}}")
                .validate("{\"minItems\":1.0}", InputFormat.JSON).size());
    }

    @ParameterizedTest
    @MethodSource("readers")
    void integralLimitMessagesUseFixedPlainExpectations(NodeReader reader) {
        SchemaRegistry registry = SchemaRegistry.withDefaultDialect(SpecificationVersion.DRAFT_2020_12,
                builder -> builder.nodeReader(reader));
        for (String[] pair : new String[][] { { "3000000000.0", "3000000000" },
                { "3e9", "3000000000" }, { "2147483650.0", "2147483650" },
                { "1.23456789012345678901234567890e29", "123456789012345678901234567890" },
                { "1e308", "1E+308" } }) {
            for (InputFormat format : new InputFormat[] { InputFormat.JSON, InputFormat.YAML }) {
                for (String keyword : new String[] { "minItems", "minProperties", "minLength", "minContains" }) {
                    String schemaText = "{\"contains\":true,\"" + keyword + "\":" + pair[0] + "}";
                    String instance = keyword.equals("minProperties") ? "{}" : keyword.equals("minLength") ? "\"\"" : "[]";
                    Schema schema = registry.getSchema(schemaText, format);
                    assertEquals(pair[1], schema.validate(instance, InputFormat.JSON).stream().filter(e -> keyword.equals(e.getKeyword()))
                            .findFirst().get().getArguments()[0], schemaText);
                    tools.jackson.databind.node.ObjectNode moved = tools.jackson.databind.node.JsonNodeFactory.instance.objectNode();
                    moved.put("contains", true);
                    moved.set(keyword, schema.getSchemaNode().get(keyword));
                    assertEquals(pair[1], registry.getSchema(moved).validate(instance, InputFormat.JSON).stream()
                            .filter(e -> keyword.equals(e.getKeyword())).findFirst().get().getArguments()[0], schemaText);
                    assertEquals(pair[1], registry.getSchema(schema.getSchemaNode().deepCopy())
                            .validate(instance, InputFormat.JSON).stream().filter(e -> keyword.equals(e.getKeyword()))
                            .findFirst().get().getArguments()[0], schemaText);
                }
            }
        }
    }
    @Test
    void externallyRoundedInstancesCannotRecoverSourcePrecision() throws Exception {
        SchemaRegistry registry = SchemaRegistry.withDefaultDialect(SpecificationVersion.DRAFT_2020_12);
        Schema schema = registry.getSchema("{\"const\":{\"minItems\":0.99999999999999999999}}");
        assertEquals(1, schema.validate(new tools.jackson.databind.ObjectMapper().readTree(
                "{\"minItems\":0.99999999999999999999}")).size());
        assertTrue(schema.validate("{\"minItems\":0.99999999999999999999}", InputFormat.JSON).isEmpty());
    }

    @Test
    void defaultsAndExamplesKeepObjectShapeAndExactNumbers() throws Exception {
        NodeReader reader = NodeReader.builder().build();
        SchemaRegistry registry = SchemaRegistry.withDefaultDialect(SpecificationVersion.DRAFT_2020_12,
                builder -> builder.nodeReader(reader));
        String literal = "{\"minItems\":0.99999999999999999999}";
        Schema schema = registry.getSchema("{\"properties\":{\"data\":{\"default\":" + literal
                + ",\"examples\":[" + literal + "]}}}");
        com.networknt.schema.walk.WalkConfig config = com.networknt.schema.walk.WalkConfig.builder()
                .applyDefaultsStrategy(new com.networknt.schema.walk.ApplyDefaultsStrategy(true, true, true)).build();
        tools.jackson.databind.JsonNode instance = reader.readTree("{}", InputFormat.JSON);
        assertTrue(schema.walk(instance, true, context -> context.setWalkConfig(config)).getErrors().isEmpty());
        assertTrue(instance.get("data").isObject());
        assertTrue(instance.get("data").get("minItems").isBigDecimal());
        assertEquals(literal, instance.get("data").toString());
        assertEquals(instance, reader.readTree(instance.toString(), InputFormat.JSON));
        assertEquals(literal, schema.getSchemaNode().get("properties").get("data").get("examples").get(0).toString());
    }

    @Test
    void boundedPlainFormattingUsesIntegerDigitCount() {
        SchemaRegistry registry = SchemaRegistry.withDefaultDialect(SpecificationVersion.DRAFT_2020_12);
        String hundredDigits = "1" + new String(new char[99]).replace('\0', '0');
        assertEquals(hundredDigits, registry.getSchema("{\"minItems\":1e99}")
                .validate("[]", InputFormat.JSON).get(0).getArguments()[0]);
        assertEquals("-" + hundredDigits, registry.getSchema("{\"maxItems\":-1e99}")
                .validate("[1]", InputFormat.JSON).get(0).getArguments()[0]);
        assertEquals("1E+100", registry.getSchema("{\"minItems\":1e100}")
                .validate("[]", InputFormat.JSON).get(0).getArguments()[0]);
        assertEquals("-1E+100", registry.getSchema("{\"maxItems\":-1e100}")
                .validate("[1]", InputFormat.JSON).get(0).getArguments()[0]);
    }

    @Test
    void promotedLiteralsRemainObjectsInAnnotationJsonOutput() throws Exception {
        SchemaRegistry registry = SchemaRegistry.withDefaultDialect(SpecificationVersion.DRAFT_2020_12);
        String literal = "{\"minItems\":0.99999999999999999999}";
        Schema schema = registry.getSchema("{\"default\":" + literal + ",\"examples\":[" + literal + "]}");
        Object output = schema.validate("{}", InputFormat.JSON, OutputFormat.LIST,
                context -> context.executionConfig(config -> config.annotationCollectionEnabled(true)
                        .annotationCollectionFilter(keyword -> true)));
        String json = new tools.jackson.databind.ObjectMapper().writeValueAsString(output);
        assertTrue(json.contains(literal), json);
    }

    @Test
    void uniqueItemsRetainsExactDecimalEquality() {
        Schema schema = SchemaRegistry.withDefaultDialect(SpecificationVersion.DRAFT_2020_12)
                .getSchema("{\"uniqueItems\":true}");
        assertTrue(schema.validate("[{\"minItems\":0.99999999999999999999},{\"minItems\":1.0}]",
                InputFormat.JSON).isEmpty());
        assertEquals(1, schema.validate("[{\"minItems\":0.99999999999999999999},"
                + "{\"minItems\":0.99999999999999999999}]", InputFormat.JSON).size());
    }

}
