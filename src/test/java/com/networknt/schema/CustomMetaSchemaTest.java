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
import com.networknt.schema.dialect.Dialect;
import com.networknt.schema.dialect.Dialects;
import com.networknt.schema.keyword.AbstractKeyword;
import com.networknt.schema.keyword.AbstractKeywordValidator;
import com.networknt.schema.keyword.KeywordValidator;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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

        private static final class Validator extends AbstractKeywordValidator {
            private final List<String> enumValues;
            private final List<String> enumNames;
            private final String keyword;

            private Validator(SchemaLocation schemaLocation, JsonNodePath evaluationPath, String keyword,
                    List<String> enumValues, List<String> enumNames, JsonNode schemaNode) {
                super(new EnumNamesKeyword(), schemaNode, schemaLocation, evaluationPath);
                if (enumNames.size() != enumValues.size()) {
                    throw new IllegalArgumentException("enum and enumNames need to be of same length");
                }
                this.enumNames = enumNames;
                this.enumValues = enumValues;
                this.keyword = keyword;
            }

            @Override
            public void validate(ExecutionContext executionContext, JsonNode node, JsonNode rootNode, JsonNodePath instanceLocation) {
                String value = node.asText();
                int idx = enumValues.indexOf(value);
                if (idx < 0) {
                    throw new IllegalArgumentException("value not found in enum. value: " + value + " enum: " + enumValues);
                }
                String valueName = enumNames.get(idx);
                Error error = Error.builder().keyword(keyword)
                        .schemaNode(node)
                        .instanceNode(node)
                        .messageKey("tests.example.enumNames").message("enumName is {0}").instanceLocation(instanceLocation)
                        .arguments(valueName).build();
                executionContext.addError(error);
            }
        }


        EnumNamesKeyword() {
            super("enumNames");
        }

        @Override
        public KeywordValidator newValidator(SchemaLocation schemaLocation, JsonNodePath evaluationPath, JsonNode schemaNode,
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
        final Dialect dialect = Dialect
                .builder("https://github.com/networknt/json-schema-validator/tests/schemas/example01", Dialects.getDraft4())
                // Generated UI uses enumNames to render Labels for enum values
                .keyword(new EnumNamesKeyword())
                .build();

        final JsonSchemaFactory validatorFactory = JsonSchemaFactory.builder(JsonSchemaFactory.getInstance(Specification.Version.DRAFT_4)).metaSchema(dialect).build();
        final JsonSchema schema = validatorFactory.getSchema("{\n" +
                "  \"$schema\":\n" +
                "    \"https://github.com/networknt/json-schema-validator/tests/schemas/example01\",\n" +
                "  \"enum\": [\"foo\", \"bar\"],\n" +
                "  \"enumNames\": [\"Foo !\", \"Bar !\"]\n" +
                "}");

        List<Error> messages = schema.validate(objectMapper.readTree("\"foo\""));
        assertEquals(1, messages.size());

        Error message = messages.iterator().next();
        assertEquals("$: enumName is Foo !", message.toString());
    }
}
