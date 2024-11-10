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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CustomMetaSchemaTest {

    /**
     * Introduces the keyword "enumNames".
     * <p>
     * The keyword is used together with "enum" and must have the same length.
     * <p>
     * This keyword always produces a warning during validation -
     * so it makes no sense in reality but should be useful for demonstration / testing purposes.
     *
     * @author klaskalass
     */
    static class EnumNamesKeyword extends AbstractKeyword {

        private static final class Validator extends AbstractJsonValidator {
            private final List<String> enumValues;
            private final List<String> enumNames;
            private final String keyword;

            private Validator(SchemaLocation schemaLocation, JsonNodePath evaluationPath, String keyword,
                    List<String> enumValues, List<String> enumNames, JsonNode schemaNode) {
                super(schemaLocation, evaluationPath, new EnumNamesKeyword(), schemaNode);
                if (enumNames.size() != enumValues.size()) {
                    throw new IllegalArgumentException("enum and enumNames need to be of same length");
                }
                this.enumNames = enumNames;
                this.enumValues = enumValues;
                this.keyword = keyword;
            }

            @Override
            public Set<ValidationMessage> validate(ExecutionContext executionContext, JsonNode node, JsonNode rootNode, JsonNodePath instanceLocation) {
                String value = node.asText();
                int idx = enumValues.indexOf(value);
                if (idx < 0) {
                    throw new IllegalArgumentException("value not found in enum. value: " + value + " enum: " + enumValues);
                }
                String valueName = enumNames.get(idx);
                Set<ValidationMessage> messages = new HashSet<>();
                ValidationMessage validationMessage = ValidationMessage.builder().type(keyword)
                        .schemaNode(node)
                        .instanceNode(node)
                        .code("tests.example.enumNames").message("{0}: enumName is {1}").instanceLocation(instanceLocation)
                        .arguments(valueName).build();
                messages.add(validationMessage);
                return messages;
            }
        }


        EnumNamesKeyword() {
            super("enumNames");
        }

        @Override
        public JsonValidator newValidator(SchemaLocation schemaLocation, JsonNodePath evaluationPath, JsonNode schemaNode,
                                          JsonSchema parentSchema, ValidationContext validationContext) throws JsonSchemaException, Exception {
            /*
             * You can access the schema node here to read data from your keyword
             */
            if (!schemaNode.isArray()) {
                throw new JsonSchemaException("Keyword enumNames needs to receive an array");
            }
            JsonNode parentSchemaNode = parentSchema.getSchemaNode();
            if (!parentSchemaNode.has("enum")) {
                throw new JsonSchemaException("Keyword enumNames needs to have a sibling enum keyword");
            }
            JsonNode enumSchemaNode = parentSchemaNode.get("enum");

            return new Validator(schemaLocation, evaluationPath, getValue(), readStringList(enumSchemaNode),
                    readStringList(schemaNode), schemaNode);
        }

        private List<String> readStringList(JsonNode node) {
            if (!node.isArray()) {
                throw new JsonSchemaException("Keyword enum needs to receive an array");
            }
            ArrayList<String> result = new ArrayList<String>(node.size());
            for (JsonNode child : node) {
                result.add(child.asText());
            }
            return result;
        }
    }

    @Test
    void customMetaSchemaWithIgnoredKeyword() throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        final JsonMetaSchema metaSchema = JsonMetaSchema
                .builder("https://github.com/networknt/json-schema-validator/tests/schemas/example01", JsonMetaSchema.getV4())
                // Generated UI uses enumNames to render Labels for enum values
                .keyword(new EnumNamesKeyword())
                .build();

        final JsonSchemaFactory validatorFactory = JsonSchemaFactory.builder(JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V4)).metaSchema(metaSchema).build();
        final JsonSchema schema = validatorFactory.getSchema("{\n" +
                "  \"$schema\":\n" +
                "    \"https://github.com/networknt/json-schema-validator/tests/schemas/example01\",\n" +
                "  \"enum\": [\"foo\", \"bar\"],\n" +
                "  \"enumNames\": [\"Foo !\", \"Bar !\"]\n" +
                "}");

        Set<ValidationMessage> messages = schema.validate(objectMapper.readTree("\"foo\""));
        assertEquals(1, messages.size());

        ValidationMessage message = messages.iterator().next();
        assertEquals("$: enumName is Foo !", message.getMessage());
    }
}
