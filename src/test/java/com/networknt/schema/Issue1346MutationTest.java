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
import java.util.List;
import org.junit.jupiter.api.Test;
import com.networknt.schema.serialization.NodeReader;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

class Issue1346MutationTest {
    private static SchemaRegistry registry(NodeReader reader) {
        return SchemaRegistry.withDefaultDialect(SpecificationVersion.DRAFT_2020_12, b -> b.nodeReader(reader));
    }

    @Test
    void replacedBoundDoesNotReuseTheOldLexeme() {
        SchemaRegistry registry = SchemaRegistry.withDefaultDialect(SpecificationVersion.DRAFT_2020_12);
        ObjectNode tree = (ObjectNode) registry.getSchema("{\"minItems\":3000000000.0}").getSchemaNode();
        tree.put("minItems", 2);
        assertTrue(registry.getSchema(tree).validate("[1,2]", InputFormat.JSON).isEmpty());
        tree.put("minItems", 3.0);
        List<Error> errors = registry.getSchema(tree).validate("[1,2]", InputFormat.JSON);
        assertEquals(1, errors.size());
        assertEquals("3", errors.get(0).getArguments()[0]);
        ObjectNode fraction = (ObjectNode) registry.getSchema("{\"maxItems\":0.99999999999999999999}").getSchemaNode();
        fraction.put("maxItems", 1.0); // Same rounded value, different node and intended bound.
        assertEquals(1, registry.getSchema(fraction).validate("[1,2]", InputFormat.JSON).size());
    }

    @Test
    void copiedTreesUseTheirSuppliedNumericPrecision() {
        SchemaRegistry ordinary = SchemaRegistry.withDefaultDialect(SpecificationVersion.DRAFT_2020_12);
        Schema original = ordinary.getSchema("{\"maxItems\":0.99999999999999999999}");
        assertTrue(original.validate("[1,2]", InputFormat.JSON).isEmpty());
        JsonNode copied = original.getSchemaNode().deepCopy();
        assertTrue(ordinary.getSchema(copied).validate("[1,2]", InputFormat.JSON).isEmpty());
        NodeReader exactReader = NodeReader.builder().jsonMapper(JsonMapper.builder()
                .enable(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS).build()).build();
        SchemaRegistry exact = registry(exactReader);
        Schema exactOriginal = exact.getSchema("{\"maxItems\":0.99999999999999999999}");
        assertTrue(exact.getSchema(exactOriginal.getSchemaNode().deepCopy()).validate("[1,2]", InputFormat.JSON).isEmpty());
    }

    @Test
    void literalObjectCanAlsoBeAReferencedSchema() {
        Schema schema = SchemaRegistry.withDefaultDialect(SpecificationVersion.DRAFT_2020_12)
                .getSchema("{\"$ref\":\"#/const\",\"const\":{\"maxItems\":1e-400}}");
        List<Error> errors = schema.validate("[1,2]", InputFormat.JSON);
        assertTrue(errors.stream().anyMatch(e -> e.getKeyword().equals("const")));
        assertTrue(errors.stream().noneMatch(e -> e.getKeyword().equals("maxItems")));
    }

    @Test
    void activeCountsBeyondBigDecimalRangeSaturate() {
        assertEquals(1, SchemaRegistry.withDefaultDialect(SpecificationVersion.DRAFT_2020_12)
                .getSchema("{\"minItems\":1e999999999999}").validate("[]", InputFormat.JSON).size());
    }

    @Test
    void yamlNumericSpellingsAcceptedByTheReaderKeepTheirMeaning() throws Exception {
        NodeReader reader = NodeReader.builder().build();
        SchemaRegistry registry = registry(reader);
        String[][] cases = { { "1_0.0", "10" }, { "1_000.5", null }, { "10_000_000_000.0", "10000000000" } };
        for (String[] c : cases) {
            String text = "minItems: " + c[0] + "\n";
            JsonNode parsed = reader.readTree(text, InputFormat.YAML).get("minItems");
            List<Error> errors = registry.getSchema(text, InputFormat.YAML).validate("[]", InputFormat.JSON);
            if (parsed.isNumber() && c[1] != null) {
                assertEquals(1, errors.size(), c[0]);
                assertEquals(c[1], errors.get(0).getArguments()[0]);
            } else {
                assertTrue(errors.isEmpty(), c[0]);
            }
        }
    }
}
