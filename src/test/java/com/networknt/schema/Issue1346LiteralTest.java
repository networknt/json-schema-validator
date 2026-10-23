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
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import com.networknt.schema.serialization.BasicNodeReader;
import com.networknt.schema.serialization.NodeReader;
import tools.jackson.databind.node.JsonNodeFactory;
import tools.jackson.databind.node.NumericNode;
import tools.jackson.databind.node.FloatNode;

class Issue1346LiteralTest {
    static Stream<NodeReader> readers() {
        return Stream.of(BasicNodeReader.getInstance(), NodeReader.builder().build(), NodeReader.builder().locationAware().build());
    }

    private static SchemaRegistry registry(NodeReader reader) {
        return SchemaRegistry.withDefaultDialect(SpecificationVersion.DRAFT_2020_12, b -> b.nodeReader(reader));
    }

    @ParameterizedTest
    @MethodSource("readers")
    void unrepresentableCountNamedLiteralDataKeepsOrdinaryParsing(NodeReader reader) throws Exception {
        for (String value : new String[] { "1e999999999999", "1e-999999999999", "-1e999999999999" }) {
            for (String count : new String[] { "minItems", "maxItems", "minProperties", "maxProperties",
                    "minLength", "maxLength", "minContains", "maxContains" }) {
                String data = "{\"" + count + "\":" + value + "}";
                for (String keyword : new String[] { "const", "enum", "default", "examples", "x-unknown" }) {
                    String payload = keyword.equals("enum") || keyword.equals("examples") ? "[" + data + "]" : data;
                    String text = "{\"" + keyword + "\":" + payload + "}";
                    Schema[] schemas = { registry(reader).getSchema(text), registry(reader).getSchema(
                            new ByteArrayInputStream(text.getBytes(StandardCharsets.UTF_8)), InputFormat.JSON) };
                    for (Schema schema : schemas) {
                        assertEquals(reader.readTree(text, InputFormat.JSON).toString(), schema.getSchemaNode().toString());
                        if (keyword.equals("const") || keyword.equals("enum")) {
                            assertTrue(schema.validate(reader.readTree(data, InputFormat.JSON)).isEmpty());
                        }
                    }
                    String yaml = keyword + ": " + payload + "\n";
                    assertEquals(reader.readTree(yaml, InputFormat.YAML).toString(),
                            registry(reader).getSchema(yaml, InputFormat.YAML).getSchemaNode().toString());
                }
            }
        }
    }

    @ParameterizedTest
    @MethodSource("readers")
    void plainIntegerMessageContractUsesFixedWrittenExpectations(NodeReader reader) {
        for (String limit : new String[] { "3000000000", "2147483650", "2147483648", "123456789012345678901234567890" }) {
            for (String kind : new String[] { "Items", "Properties", "Length", "Contains" }) {
                String text = "{" + (kind.equals("Contains") ? "\"contains\":true," : "") + "\"min" + kind + "\":" + limit + "}";
                String instance = kind.equals("Properties") ? "{}" : kind.equals("Length") ? "\"\"" : "[]";
                assertEquals(limit, registry(reader).getSchema(text).validate(instance, InputFormat.JSON).get(0).getArguments()[0]);
            }
        }
    }

    @ParameterizedTest
    @MethodSource("readers")
    void annotationsMatchTheParsedNodesOwnNumber(NodeReader reader) {
        for (String limit : new String[] { "2", "2.0", "3000000000.0", "1e400" }) {
            Schema schema = registry(reader).getSchema("{\"contains\":true,\"maxContains\":" + limit + "}");
            Result result = schema.validate("[1]", InputFormat.JSON, OutputFormat.RESULT,
                    c -> c.executionConfig(e -> e.annotationCollectionEnabled(true).annotationCollectionFilter(k -> true)));
            Object actual = result.getExecutionContext().getAnnotations().asMap().values().stream().flatMap(List::stream)
                    .filter(a -> a.getKeyword().equals("maxContains")).findFirst().get().getValue();
            assertEquals(schema.getSchemaNode().get("maxContains").numberValue(), actual);
        }
    }

    @ParameterizedTest
    @MethodSource("readers")
    void differentCountsHaveDifferentTreeValues(NodeReader reader) {
        Schema fraction = registry(reader).getSchema("{\"maxItems\":0.99999999999999999999}");
        Schema integral = registry(reader).getSchema("{\"maxItems\":1.0}");
        assertNotEquals(fraction.getSchemaNode(), integral.getSchemaNode());
        assertTrue(fraction.validate("[1,2]", InputFormat.JSON).isEmpty());
        assertEquals(1, integral.validate("[1,2]", InputFormat.JSON).size());
        Schema duplicate = registry(reader).getSchema("{\"maxItems\":1e-400,\"maxItems\":2}");
        assertEquals(1, duplicate.validate("[1,2,3]", InputFormat.JSON).size());
    }

    @org.junit.jupiter.api.Test
    void nonDoubleNumericFactoryStillUsesExactCounts() {
        NodeReader reader = NodeReader.builder().jsonNodeFactoryFactory(parser -> new JsonNodeFactory() {
            private static final long serialVersionUID = 1L;
            @Override
            public NumericNode numberNode(double value) {
                return FloatNode.valueOf((float) value);
            }
        }).build();
        assertTrue(registry(reader).getSchema("{\"maxItems\":0.99999999999999999999}")
                .validate("[1,2]", InputFormat.JSON).isEmpty());
    }
}
