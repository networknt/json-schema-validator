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
import java.math.BigInteger;
import java.util.LinkedHashMap;
import java.util.Map;
import tools.jackson.core.JsonParser;
import tools.jackson.core.JsonToken;
import tools.jackson.core.util.JsonParserDelegate;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.JsonNodeFactory;
import com.networknt.schema.serialization.node.JsonNodeFactoryFactory;
import com.networknt.schema.internal.CountTokens;

/** Schema-only token capture. Ordinary node creation remains entirely with the configured factory. */
final class SchemaNodeReader {
    static JsonNode read(ObjectMapper mapper, String content, JsonNodeFactoryFactory factory) {
        return read(mapper, mapper.createParser(content), factory);
    }

    static JsonNode read(ObjectMapper mapper, InputStream content, JsonNodeFactoryFactory factory) {
        return read(mapper, mapper.createParser(content), factory);
    }

    private static JsonNode read(ObjectMapper mapper, JsonParser source, JsonNodeFactoryFactory factory) {
        try (TokenParser parser = new TokenParser(source)) {
            JsonNodeFactory nodes = factory == null ? mapper.getNodeFactory() : factory.getJsonNodeFactory(parser);
            JsonNode result = mapper.reader(nodes).readTree(parser);
            if (result == null) {
                return nodes.missingNode();
            }
            for (Map.Entry<String, String> token : parser.values.entrySet()) {
                int slash = token.getKey().lastIndexOf('/');
                JsonNode owner = result.at(token.getKey().substring(0, slash));
                String keyword = token.getKey().substring(slash + 1);
                JsonNode value = owner.get(keyword);
                if (value != null && value.isNumber()) {
                    CountTokens.put(owner, keyword, value, token.getValue());
                }
            }
            return result;
        }
    }

    private static final class TokenParser extends JsonParserDelegate {
        private final Map<String, String> values = new LinkedHashMap<>();
        private long lastOffset = Long.MIN_VALUE;
        private JsonToken lastToken;

        TokenParser(JsonParser source) {
            super(source);
        }

        private void remember() {
            JsonToken token = currentToken();
            if (token != JsonToken.VALUE_NUMBER_FLOAT && token != JsonToken.VALUE_NUMBER_INT) {
                return;
            }
            // Check token identity before constructing a pointer or extracting text.
            long offset = currentTokenLocation().getCharOffset();
            if (offset < 0) {
                offset = currentTokenLocation().getByteOffset();
            }
            if (offset >= 0 && offset == lastOffset && token == lastToken) {
                return;
            }
            lastOffset = offset;
            lastToken = token;
            String name = currentName();
            if ("minItems".equals(name) || "maxItems".equals(name)
                    || "minProperties".equals(name) || "maxProperties".equals(name)
                    || "minLength".equals(name) || "maxLength".equals(name)
                    || "minContains".equals(name) || "maxContains".equals(name)) {
                String path = streamReadContext().pathAsPointer().toString();
                if (token == JsonToken.VALUE_NUMBER_INT) {
                    values.remove(path); // A duplicate integer field replaces a previous float.
                    return;
                }
                String text = getString();
                int start = text.startsWith("+") || text.startsWith("-") ? 1 : 0;
                char first = text.charAt(start);
                boolean finite = first >= '0' && first <= '9'
                        || first == '.' && text.length() > start + 1 && Character.isDigit(text.charAt(start + 1));
                if (finite) {
                    // Do not parse here: opaque literal data may exceed BigDecimal's scale.
                    values.put(path, text);
                } else {
                    values.remove(path); // Explicit NaN/Infinity tokens use the node's value.
                }
            }
        }

        @Override
        public NumberType getNumberType() {
            remember();
            return super.getNumberType();
        }

        @Override
        public NumberTypeFP getNumberTypeFP() {
            remember();
            return super.getNumberTypeFP();
        }

        @Override
        public double getDoubleValue() {
            remember();
            return super.getDoubleValue();
        }

        @Override
        public float getFloatValue() {
            remember();
            return super.getFloatValue();
        }

        @Override
        public int getIntValue() {
            remember();
            return super.getIntValue();
        }

        @Override
        public long getLongValue() {
            remember();
            return super.getLongValue();
        }

        @Override
        public BigInteger getBigIntegerValue() {
            remember();
            return super.getBigIntegerValue();
        }
    }
}
