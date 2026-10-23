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
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.util.JsonParserDelegate;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.networknt.schema.serialization.node.JsonNodeFactoryFactory;

/** Shared tree parsing: preserve lossy finite decimals and existing count precision. */
final class SchemaNodeReader {
    static JsonNode read(ObjectMapper mapper, String content, JsonNodeFactoryFactory factory) throws java.io.IOException {
        return read(mapper, mapper.createParser(content), factory);
    }

    static JsonNode read(ObjectMapper mapper, InputStream content, JsonNodeFactoryFactory factory) throws java.io.IOException {
        return read(mapper, mapper.createParser(content), factory);
    }

    private static JsonNode read(ObjectMapper mapper, JsonParser source, JsonNodeFactoryFactory factory) throws java.io.IOException {
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
        public JsonToken nextToken() throws java.io.IOException {
            resetPrecision();
            return super.nextToken();
        }

        @Override
        public JsonToken nextValue() throws java.io.IOException {
            resetPrecision();
            return super.nextValue();
        }

        @Override
        public String nextFieldName() throws java.io.IOException {
            resetPrecision();
            return super.nextFieldName();
        }

        @Override
        public boolean nextFieldName(com.fasterxml.jackson.core.SerializableString name) throws java.io.IOException {
            resetPrecision();
            return super.nextFieldName(name);
        }

        @Override
        public String nextTextValue() throws java.io.IOException {
            resetPrecision();
            return super.nextTextValue();
        }

        @Override
        public int nextIntValue(int defaultValue) throws java.io.IOException {
            resetPrecision();
            return super.nextIntValue(defaultValue);
        }

        @Override
        public long nextLongValue(long defaultValue) throws java.io.IOException {
            resetPrecision();
            return super.nextLongValue(defaultValue);
        }

        @Override
        public Boolean nextBooleanValue() throws java.io.IOException {
            resetPrecision();
            return super.nextBooleanValue();
        }

        @Override
        public JsonParser skipChildren() throws java.io.IOException {
            resetPrecision();
            super.skipChildren();
            return this;
        }

        private boolean promote() throws java.io.IOException {
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
            if (isShortDecimal()) {
                super.getDoubleValue();
                return false;
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
            if (matchesShortest(shortest)) {
                return false; // Lossless machine-generated doubles need no token String or decimal.
            }
            String token = getText();
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
        private boolean isShortDecimal() throws java.io.IOException {
            int length = getTextLength();
            if (length > 16) {
                return false;
            }
            char[] characters = getTextCharacters();
            int offset = getTextOffset();
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

        private boolean matchesShortest(String shortest) throws java.io.IOException {
            int length = getTextLength();
            if (length != shortest.length()) {
                return false;
            }
            char[] characters = getTextCharacters();
            int offset = getTextOffset();
            for (int i = 0; i < length; i++) {
                if (characters[offset + i] != shortest.charAt(i)) {
                    return false;
                }
            }
            return true;
        }

        @Override
        public NumberType getNumberType() throws java.io.IOException {
            return promote() ? NumberType.BIG_DECIMAL : super.getNumberType();
        }

        @Override
        public NumberTypeFP getNumberTypeFP() throws java.io.IOException {
            return promote() ? NumberTypeFP.BIG_DECIMAL : super.getNumberTypeFP();
        }

        @Override
        public BigDecimal getDecimalValue() throws java.io.IOException {
            return promote() ? exact : super.getDecimalValue();
        }
    }
}
