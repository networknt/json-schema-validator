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
import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.ObjectNode;
import com.networknt.schema.serialization.NodeReader;

class Issue1350Test {
    private static final String[] KEYWORDS = { "minItems", "maxItems", "minProperties", "maxProperties",
            "minLength", "maxLength", "minContains", "maxContains" };

    @Test
    void lossyCountValuesSurviveCopyMoveAndRebuild() {
        SchemaRegistry registry = SchemaRegistry.withDefaultDialect(SpecificationVersion.DRAFT_2020_12);
        String[] instances = { "[]", "[1,2]", "{}", "{\"a\":1,\"b\":2}", "\"\"", "\"ab\"" };
        for (String keyword : KEYWORDS) {
            for (String value : new String[] { "0.99999999999999999999", "1e-400", "9007199254740993.0", "1e400" }) {
                Schema original = registry.getSchema("{\"contains\":true,\"" + keyword + "\":" + value + "}");
                JsonNode tree = original.getSchemaNode();
                assertTrue(tree.get(keyword).isBigDecimal(), keyword + ":" + value);
                ObjectNode moved = (ObjectNode) registry.getSchema("{\"contains\":true}").getSchemaNode();
                moved.set(keyword, tree.get(keyword));
                for (JsonNode variant : new JsonNode[] { tree.deepCopy(), moved }) {
                    Schema rebuilt = registry.getSchema(variant);
                    for (String instance : instances) {
                        assertEquals(original.validate(instance, InputFormat.JSON).size(),
                                rebuilt.validate(instance, InputFormat.JSON).size(), keyword + ":" + value + ":" + instance);
                    }
                }
            }
        }
    }

    @Test
    void lossyFiniteFloatsArePromotedRegardlessOfFieldName() throws Exception {
        NodeReader reader = NodeReader.builder().build();
        for (InputFormat format : new InputFormat[] { InputFormat.JSON, InputFormat.YAML }) {
            String content = "{\"minItems\":0.99999999999999999999,\"maxItems\":3000000000.0,"
                    + "\"other\":0.99999999999999999999,\"default\":{\"minItems\":1e-400},"
                    + "\"minLength\":0.1,\"maxLength\":1e30}";
            for (JsonNode tree : new JsonNode[] { reader.readSchema(content, format), reader.readSchema(
                    new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8)), format) }) {
                assertTrue(tree.get("minItems").isBigDecimal());
                assertTrue(tree.get("maxItems").isDouble());
                assertTrue(tree.get("other").isBigDecimal());
                assertTrue(tree.get("default").get("minItems").isBigDecimal());
                assertTrue(tree.get("minLength").isDouble());
                assertTrue(tree.get("maxLength").isDouble());
            }
        }
    }

    @Test
    void unrepresentableExponentsUseOrdinaryInfinity() {
        SchemaRegistry registry = SchemaRegistry.withDefaultDialect(SpecificationVersion.DRAFT_2020_12);
        for (String keyword : KEYWORDS) {
            Schema schema = registry.getSchema("{\"contains\":true,\"" + keyword + "\":1e999999999999}");
            assertTrue(Double.isInfinite(schema.getSchemaNode().get(keyword).doubleValue()));
            assertTrue(registry.getSchema("{\"const\":{\"" + keyword + "\":1e999999999999}}") != null);
        }
    }
}
