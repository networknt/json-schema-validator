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
import tools.jackson.core.JsonParser;
import tools.jackson.core.JsonToken;
import tools.jackson.core.util.JsonParserDelegate;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.JsonNodeFactory;
import com.networknt.schema.serialization.node.JsonNodeFactoryFactory;

/** Shared tree parsing: preserve lossy finite decimals and existing count precision. */
final class SchemaNodeReader {
    static JsonNode read(ObjectMapper mapper, String content, JsonNodeFactoryFactory factory) {
        return read(mapper, mapper.createParser(content), factory);
    }

    static JsonNode read(ObjectMapper mapper, InputStream content, JsonNodeFactoryFactory factory) {
        return read(mapper, mapper.createParser(content), factory);
    }

    private static JsonNode read(ObjectMapper mapper, JsonParser source, JsonNodeFactoryFactory factory) {
        // Exact-decimal readers must not decode through double first.
        try (JsonParser parser = precisionParser(source,
                mapper.isEnabled(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS))) {
            JsonNodeFactory nodes = factory == null ? mapper.getNodeFactory() : factory.getJsonNodeFactory(parser);
            JsonNode result = mapper.reader(nodes).readTree(parser);
            return result == null ? nodes.missingNode() : result;
        }
    }

    static JsonParser precisionParser(JsonParser source, boolean useBigDecimals) {
        return useBigDecimals || source instanceof PrecisionParser ? source : new PrecisionParser(source);
    }

    private static final class PrecisionParser extends JsonParserDelegate {
        private boolean checked;
        private BigDecimal exact;

        PrecisionParser(JsonParser source) {
            super(source);
        }

        // Delegates may advance through optimized methods without calling nextToken().
        private void resetPrecision() {
            checked = false;
            exact = null;
        }

        @Override
        public JsonToken nextToken() {
            resetPrecision();
            return super.nextToken();
        }

        @Override
        public JsonToken nextValue() {
            resetPrecision();
            return super.nextValue();
        }

        @Override
        public String nextName() {
            resetPrecision();
            return super.nextName();
        }

        @Override
        public boolean nextName(tools.jackson.core.SerializableString name) {
            resetPrecision();
            return super.nextName(name);
        }

        @Override
        public String nextStringValue() {
            resetPrecision();
            return super.nextStringValue();
        }

        @Override
        public int nextIntValue(int defaultValue) {
            resetPrecision();
            return super.nextIntValue(defaultValue);
        }

        @Override
        public long nextLongValue(long defaultValue) {
            resetPrecision();
            return super.nextLongValue(defaultValue);
        }

        @Override
        public Boolean nextBooleanValue() {
            resetPrecision();
            return super.nextBooleanValue();
        }

        @Override
        public int nextNameMatch(tools.jackson.core.sym.PropertyNameMatcher matcher) {
            resetPrecision();
            return super.nextNameMatch(matcher);
        }

        @Override
        public JsonParser skipChildren() {
            resetPrecision();
            super.skipChildren();
            return this;
        }

        private boolean promote() {
            if (currentToken() != JsonToken.VALUE_NUMBER_FLOAT) {
                return false;
            }
            if (checked) {
                return exact != null;
            }
            checked = true;
            exact = null;
            // Preserve exact/native values already supplied by token buffers or custom parsers.
            NumberTypeFP nativeType = super.getNumberTypeFP();
            if (nativeType == NumberTypeFP.BIG_DECIMAL || nativeType == NumberTypeFP.FLOAT32) {
                return false;
            }
            int length = getStringLength();
            char[] characters = null;
            int offset = 0;
            // Keep the buffer local to this decision. YAML builds a new array on
            // each request; reuse it if the shortest-spelling comparison needs it.
            if (length <= 16) {
                characters = getStringCharacters();
                offset = getStringOffset();
                if (isShortDecimal(characters, offset, length)) {
                    super.getDoubleValue();
                    return false;
                }
            }
            double number = super.getDoubleValue();
            if (!Double.isFinite(number)) {
                String name = currentName();
                if (!("minItems".equals(name) || "maxItems".equals(name)
                        || "minProperties".equals(name) || "maxProperties".equals(name)
                        || "minLength".equals(name) || "maxLength".equals(name)
                        || "minContains".equals(name) || "maxContains".equals(name))) {
                    return false; // Retain the existing nonfinite divisor/bound policy.
                }
            }
            String shortest = Double.toString(number);
            if (length == shortest.length()) {
                if (characters == null) {
                    characters = getStringCharacters();
                    offset = getStringOffset();
                }
                if (matchesShortest(shortest, characters, offset, length)) {
                    return false; // Lossless doubles need no token String or decimal.
                }
            }
            String token = getString();
            if (token.indexOf('_') >= 0) {
                token = token.replace("_", ""); // Accepted YAML numeric separators only.
            }
            BigDecimal decimal;
            try {
                decimal = new BigDecimal(token);
            } catch (NumberFormatException e) {
                // Explicit nonfinite values and exponents beyond BigDecimal's range
                // retain ordinary parsing, including Infinity saturation.
                return false;
            }
            // Match the library's shortest-decimal interpretation of doubles.
            if (!Double.isFinite(number) || decimal.compareTo(new BigDecimal(shortest)) != 0) {
                exact = decimal;
            }
            return exact != null;
        }

        // Binary64 preserves at least 15 significant decimal digits. Restrict this
        // shortcut to short fixed-point tokens so underflow/overflow cannot apply.
        private boolean isShortDecimal(char[] characters, int offset, int length) {
            int digits = 0;
            for (int i = 0; i < length; i++) {
                char c = characters[offset + i];
                if (c >= '0' && c <= '9') {
                    digits++;
                } else if (c != '.' && !(i == 0 && c == '-')) {
                    return false;
                }
            }
            return digits <= 15;
        }

        private boolean matchesShortest(String shortest, char[] characters, int offset, int length) {
            for (int i = 0; i < length; i++) {
                if (characters[offset + i] != shortest.charAt(i)) {
                    return false;
                }
            }
            return true;
        }

        @Override
        public NumberType getNumberType() {
            return promote() ? NumberType.BIG_DECIMAL : super.getNumberType();
        }

        @Override
        public NumberTypeFP getNumberTypeFP() {
            return promote() ? NumberTypeFP.BIG_DECIMAL : super.getNumberTypeFP();
        }

        @Override
        public BigDecimal getDecimalValue() {
            return promote() ? exact : super.getDecimalValue();
        }
    }
}
