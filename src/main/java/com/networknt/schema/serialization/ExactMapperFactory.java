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

import java.math.BigDecimal;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.JsonStreamContext;
import com.fasterxml.jackson.core.JsonLocation;
import com.fasterxml.jackson.core.util.JsonParserDelegate;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.deser.std.JsonNodeDeserializer;
import com.fasterxml.jackson.databind.module.SimpleModule;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.cfg.JsonNodeFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import com.fasterxml.jackson.databind.ObjectMapper;

/** Exact default tree readers, separate from the public mapper factories. */
final class ExactMapperFactory {
    private ExactMapperFactory() {
    }

    static ObjectMapper json() {
        return DefaultJsonMapper.INSTANCE;
    }

    static ObjectMapper yaml() {
        return DefaultYamlMapper.INSTANCE;
    }

    /** Private readers must not change the public general-purpose mapper singletons. */
    private static class DefaultJsonMapper {
        private static final ObjectMapper INSTANCE = JsonMapper.builder()
                .disable(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS)
                .addModule(new SimpleModule().addDeserializer(JsonNode.class, new ExactNodeDeserializer()))
                .disable(JsonNodeFeature.STRIP_TRAILING_BIGDECIMAL_ZEROES).build();
    }

    private static class DefaultYamlMapper {
        private static final ObjectMapper INSTANCE = YAMLMapper.builder()
                .disable(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS)
                .addModule(new SimpleModule().addDeserializer(JsonNode.class, new ExactNodeDeserializer()))
                .disable(JsonNodeFeature.STRIP_TRAILING_BIGDECIMAL_ZEROES).build();
    }

    /** Keep Jackson's tree construction, choosing precision before allocating a decimal. */
    private static final class ExactNodeDeserializer extends StdDeserializer<JsonNode> {
        private static final long serialVersionUID = 1L;

        ExactNodeDeserializer() {
            super(JsonNode.class);
        }

        @Override
        public JsonNode getNullValue(DeserializationContext context) {
            return context.getNodeFactory().nullNode();
        }

        @Override
        public JsonNode deserialize(JsonParser parser, DeserializationContext context) throws IOException {
            return JsonNodeDeserializer.getDeserializer(JsonNode.class)
                    .deserialize(new ExactNumberParser(parser), context);
        }
    }

    /**
     * Coefficients of at most 15 digits with scales from 0 through 15 round-trip
     * through binary64. Negative scales qualify only when the expanded integer
     * also has at most 15 digits. Read these without an intermediate BigDecimal.
     * Other literals use double only when its decimal representation is exact.
     * Representation is chosen here so custom node factories, including the
     * location-aware factory, receive the same numeric type.
     */
    private static final class ExactNumberParser extends JsonParserDelegate {
        private static final double[] POWERS_OF_TEN = {
                1d, 10d, 100d, 1000d, 10000d, 100000d, 1000000d, 10000000d,
                100000000d, 1000000000d, 10000000000d, 100000000000d,
                1000000000000d, 10000000000000d, 100000000000000d, 1000000000000000d
        };

        private long compactOffset = -1;
        private double compactValue;
        private JsonStreamContext compactContext;

        ExactNumberParser(JsonParser parser) {
            super(parser);
        }

        @Override
        public String nextFieldName() throws IOException {
            return delegate.nextFieldName();
        }

        @Override
        public NumberTypeFP getNumberTypeFP() throws IOException {
            compactValue = compactDoubleValue();
            if (compactValue != 0d) {
                compactContext = delegate.getParsingContext();
                compactOffset = compactContext == null ? -1 : tokenOffset();
                return NumberTypeFP.DOUBLE64;
            }
            compactOffset = -1;
            return exactNumberType();
        }

        /** Zero means this token needs the exact path; exact zero is never compact. */
        private double compactDoubleValue() throws IOException {
            int length = delegate.getTextLength();
            if (length <= 17) {
                char[] text = delegate.getTextCharacters();
                int start = delegate.getTextOffset();
                int end = start + length;
                boolean negative = start < end && text[start] == '-';
                int offset = negative ? start + 1 : start;
                long coefficient = 0;
                int point = -1;
                int coefficientEnd = end;
                int exponent = 0;
                for (; offset < end; offset++) {
                    char c = text[offset];
                    if (c >= '0' && c <= '9') {
                        int nextDigit = offset + 1 < end ? text[offset + 1] - '0' : -1;
                        if (nextDigit >= 0 && nextDigit <= 9) {
                            coefficient = coefficient * 100 + (c - '0') * 10 + nextDigit;
                            offset++;
                        } else {
                            coefficient = coefficient * 10 + c - '0';
                        }
                    } else if (c == '.' && point < 0) {
                        point = offset;
                    } else if (c == 'e' || c == 'E') {
                        coefficientEnd = offset++;
                        boolean negativeExponent = offset < end && text[offset] == '-';
                        if (offset < end && (negativeExponent || text[offset] == '+')) {
                            offset++;
                        }
                        for (; offset < end; offset++) {
                            int digit = text[offset] - '0';
                            // Larger exponents cannot qualify; stop before the int can overflow.
                            if (digit < 0 || digit > 9 || exponent > 30) {
                                return 0d;
                            }
                            exponent = exponent * 10 + digit;
                        }
                        if (negativeExponent) {
                            exponent = -exponent;
                        }
                        break;
                    } else {
                        return delegate.isNaN() ? delegate.getDoubleValue() : 0d;
                    }
                }
                int digits = coefficientEnd - start - (negative ? 1 : 0) - (point < 0 ? 0 : 1);
                int scale = (point < 0 ? 0 : coefficientEnd - point - 1) - exponent;
                if (coefficient != 0 && digits <= 15 && scale <= 15 && scale >= digits - 15) {
                    long signedCoefficient = negative ? -coefficient : coefficient;
                    return scale >= 0 ? signedCoefficient / POWERS_OF_TEN[scale]
                            : signedCoefficient * POWERS_OF_TEN[-scale];
                }
            }
            return 0d;
        }

        @Override
        public double getDoubleValue() throws IOException {
            // Context and a known offset identify the token even across parser sequences.
            // Parsers without offsets use the stateless compact calculation.
            if (delegate.hasToken(JsonToken.VALUE_NUMBER_FLOAT)) {
                if (compactOffset >= 0 && compactContext == delegate.getParsingContext()
                        && compactOffset == tokenOffset()) {
                    return compactValue;
                }
                double compact = compactDoubleValue();
                if (compact != 0d) {
                    return compact;
                }
            }
            return delegate.getDoubleValue();
        }

        private long tokenOffset() {
            JsonLocation location = delegate.currentTokenLocation();
            long offset = location.getCharOffset();
            return offset >= 0 ? offset : location.getByteOffset();
        }

        private NumberTypeFP exactNumberType() throws IOException {
            double number = delegate.getDoubleValue();
            // Keep exact zero distinct from underflow in a caller's double mapper.
            if (number != 0d && Double.isFinite(number)) {
                String roundTrip = Double.toString(number);
                // Canonical Double.toString output matches directly, avoiding both
                // BigDecimal parses and BigDecimal.doubleValue's JDK 17 round trip.
                if (roundTrip.equals(delegate.getText())
                        || new BigDecimal(roundTrip).compareTo(delegate.getDecimalValue()) == 0) {
                    return NumberTypeFP.DOUBLE64;
                }
            }
            return NumberTypeFP.BIG_DECIMAL;
        }

    }

}
