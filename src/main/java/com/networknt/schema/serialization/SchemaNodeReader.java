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
        private long lastOffset = Long.MIN_VALUE;
        private BigDecimal exact;

        PrecisionParser(JsonParser source) {
            super(source);
        }

        private boolean promote() throws java.io.IOException {
            if (currentToken() != JsonToken.VALUE_NUMBER_FLOAT) {
                return false;
            }
            // Preserve exact/native values already supplied by token buffers or custom parsers.
            NumberTypeFP nativeType = super.getNumberTypeFP();
            if (nativeType == NumberTypeFP.BIG_DECIMAL || nativeType == NumberTypeFP.FLOAT32) {
                return false;
            }
            long offset = getTokenLocation().getCharOffset();
            if (offset < 0) {
                offset = getTokenLocation().getByteOffset();
            }
            if (offset >= 0 && offset == lastOffset) {
                return exact != null;
            }
            lastOffset = offset;
            exact = null;
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
            String token = getText();
            if (token.indexOf('_') >= 0) {
                token = token.replace("_", ""); // Accepted YAML numeric separators only.
            }
            String shortest = Double.toString(number);
            if (token.equals(shortest)) {
                return false; // Canonical lossless machine-generated doubles need no decimal allocation.
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
