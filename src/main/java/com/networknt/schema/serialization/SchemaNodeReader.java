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

package com.networknt.schema.serialization;

import java.io.InputStream;
import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.util.JsonParserDelegate;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.DoubleNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.networknt.schema.serialization.node.JsonNodeFactoryFactory;

/** Schema-only token capture. Ordinary node creation remains entirely with the configured factory. */
final class SchemaNodeReader {
    static JsonNode read(ObjectMapper mapper, String content, JsonNodeFactoryFactory factory) throws java.io.IOException {
        return read(mapper, mapper.createParser(content), factory);
    }

    static JsonNode read(ObjectMapper mapper, InputStream content, JsonNodeFactoryFactory factory) throws java.io.IOException {
        return read(mapper, mapper.createParser(content), factory);
    }

    private static JsonNode read(ObjectMapper mapper, JsonParser source, JsonNodeFactoryFactory factory) throws java.io.IOException {
        try (CountTokens parser = new CountTokens(source)) {
            JsonNodeFactory nodes = factory == null ? mapper.getNodeFactory() : factory.getJsonNodeFactory(parser);
            JsonNode result = mapper.reader(nodes).readTree(parser);
            if (result == null) {
                return nodes.missingNode();
            }
            for (Map.Entry<String, BigDecimal> token : parser.values.entrySet()) {
                JsonNode value = result.at(token.getKey());
                if (value instanceof DoubleNode) {
                    int slash = token.getKey().lastIndexOf('/');
                    JsonNode parent = result.at(token.getKey().substring(0, slash));
                    if (parent.isObject()) {
                        ((ObjectNode) parent).set(token.getKey().substring(slash + 1),
                                CountTokenNode.of((DoubleNode) value, token.getValue()));
                    }
                }
            }
            return result;
        }
    }

    private static final class CountTokens extends JsonParserDelegate {
        private final Map<String, BigDecimal> values = new LinkedHashMap<>();
        private String lastPath;
        private String lastText;

        CountTokens(JsonParser source) {
            super(source);
        }

        private void remember() throws java.io.IOException {
            if (currentToken() != JsonToken.VALUE_NUMBER_FLOAT) {
                return;
            }
            String name = currentName();
            if ("minItems".equals(name) || "maxItems".equals(name)
                    || "minProperties".equals(name) || "maxProperties".equals(name)
                    || "minLength".equals(name) || "maxLength".equals(name)
                    || "minContains".equals(name) || "maxContains".equals(name)) {
                String path = getParsingContext().pathAsPointer().toString();
                String text = getText();
                if (path.equals(lastPath) && text.equals(lastText)) {
                    return; // Jackson can query the same token through several numeric accessors.
                }
                lastPath = path;
                lastText = text;
                try {
                    values.put(path, new BigDecimal(text));
                } catch (NumberFormatException e) {
                    String special = text.toLowerCase(java.util.Locale.ROOT);
                    if (!special.equals("nan") && !special.equals("infinity") && !special.equals("+infinity")
                            && !special.equals("-infinity") && !special.equals("inf") && !special.equals("+inf")
                            && !special.equals("-inf") && !special.equals(".nan") && !special.equals(".inf")
                            && !special.equals("+.inf") && !special.equals("-.inf")) {
                        throw e; // Do not silently round an unrepresentable finite token to zero.
                    }
                    values.remove(path);
                }
            }
        }

        @Override
        public NumberType getNumberType() throws java.io.IOException {
            remember();
            return super.getNumberType();
        }

        @Override
        public NumberTypeFP getNumberTypeFP() throws java.io.IOException {
            remember();
            return super.getNumberTypeFP();
        }

        @Override
        public double getDoubleValue() throws java.io.IOException {
            remember();
            return super.getDoubleValue();
        }
    }
}
