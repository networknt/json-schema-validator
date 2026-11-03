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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonParser;
import tools.jackson.core.JsonToken;
import tools.jackson.core.exc.InputCoercionException;
import tools.jackson.core.util.JsonParserDelegate;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.cfg.JsonNodeFeature;
import tools.jackson.dataformat.yaml.YAMLMapper;

@Tag("jackson-upgrade")
class YamlNonFiniteParserTest {
    @ParameterizedTest
    @CsvSource({".nan, NaN", ".inf, Infinity", "-.inf, -Infinity",
            "!!float .NaN, NaN", "!!float .NAN, NaN", "!!float .Inf, Infinity", "!!float .INF, Infinity",
            "!!float -.inf, -Infinity", "!!float -.Inf, -Infinity", "!!float -.INF, -Infinity",
            "!!float +.inf, Infinity", "!!float +.Inf, Infinity", "!!float +.INF, Infinity"})
    void decimalModeAndNumericAccessors(String literal, double expected) {
        var mapper = YamlMapperFactory.getInstance().rebuild()
                .enable(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS)
                .disable(JsonNodeFeature.FAIL_ON_NAN_TO_BIG_DECIMAL_COERCION).build();
        assertEquals(expected, mapper.readTree(literal).doubleValue());
        for (boolean decimal : new boolean[] {false, true}) {
            try (JsonParser p = SchemaNodeReader.precisionParser(YAMLMapper.builder().build().createParser(literal), decimal)) {
                assertEquals(JsonToken.VALUE_NUMBER_FLOAT, p.nextToken());
                assertTrue(p.isNaN());
                assertEquals(p.version().getMinorVersion() == 1 ? JsonParser.NumberTypeFP.DOUBLE64
                        : JsonParser.NumberTypeFP.UNKNOWN, p.getNumberTypeFP());
                assertEquals(JsonParser.NumberType.DOUBLE, p.getNumberType());
                assertEquals(expected, p.getDoubleValue());
                assertEquals(expected, p.getValueAsDouble());
                assertEquals(expected, p.getValueAsDouble(42));
                assertEquals((float) expected, p.getFloatValue());
                assertEquals(expected, p.getNumberValue().doubleValue());
                assertEquals(expected, p.getNumberValueExact().doubleValue());
                assertEquals(expected, ((Number) p.getNumberValueDeferred()).doubleValue());
                // BigDecimal has no representation for NaN or infinity.
                // Native 3.2 throws NumberFormatException here; the 3.1 adapter
                // reports the unsupported conversion as InputCoercionException.
                Class<? extends RuntimeException> coercion = p.version().getMinorVersion() == 1
                        ? InputCoercionException.class : NumberFormatException.class;
                assertThrows(coercion, p::getDecimalValue);
                assertThrows(coercion, p::getBigIntegerValue);
                if (Double.isNaN(expected)) {
                    assertEquals(0, p.getIntValue());
                    assertEquals(0L, p.getLongValue());
                    assertEquals((byte) 0, p.getByteValue());
                    assertEquals((short) 0, p.getShortValue());
                } else {
                    assertThrows(InputCoercionException.class, p::getIntValue);
                    assertThrows(InputCoercionException.class, p::getLongValue);
                }
            }
        }
    }

    @Test
    void decimalCoercionFeatureAndFiniteValuesRemainEffective() {
        var mapper = YamlMapperFactory.getInstance().rebuild()
                .enable(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS)
                .enable(JsonNodeFeature.FAIL_ON_NAN_TO_BIG_DECIMAL_COERCION).build();
        assertThrows(JacksonException.class, () -> mapper.readTree(".nan"));
        assertEquals(new BigDecimal("1.25"), mapper.readTree("1.25").decimalValue());
        assertEquals(".nan", mapper.readTree("'.nan'").stringValue());
    }

    @Test
    void tokenAdvancesDoNotReuseNonFiniteState() {
        try (JsonParser p = SchemaNodeReader.precisionParser(YAMLMapper.builder().build()
                .createParser("{a: .nan, b: 1.25, c: -.inf, d: '.nan', e: [.inf, 2.5, .nan]}"), true)) {
            assertEquals(JsonToken.START_OBJECT, p.nextToken());
            assertEquals("a", p.nextName());
            p.nextToken();
            assertTrue(p.isNaN());
            assertTrue(Double.isNaN(p.getDoubleValue()));
            assertEquals("b", p.nextName());
            p.nextToken();
            assertEquals(1.25, p.getDoubleValue());
            assertEquals("c", p.nextName());
            p.nextValue();
            assertEquals(Double.NEGATIVE_INFINITY, p.getDoubleValue());
            assertEquals("d", p.nextName());
            assertEquals(".nan", p.nextStringValue());
            assertFalse(p.isNaN());
            assertEquals("e", p.nextName());
            assertEquals(JsonToken.START_ARRAY, p.nextToken());
            p.nextToken();
            assertEquals(Double.POSITIVE_INFINITY, p.getDoubleValue());
            p.nextValue();
            assertEquals(2.5, p.getDoubleValue());
            p.nextToken();
            assertTrue(Double.isNaN(p.getDoubleValue()));
            assertEquals(JsonToken.END_ARRAY, p.nextToken());
            assertEquals(JsonToken.END_OBJECT, p.nextToken());
            assertNull(p.nextToken());
        }
    }

    @Test
    void recoveryDoesNotCallTheBrokenNumericDecoder() {
        try (JsonParser raw = YAMLMapper.builder().build().createParser("[.nan, .inf, -.inf]")) {
            // Only 3.1 needs the adapter; newer versions use their native decoder.
            if (raw.version().getMinorVersion() != 1) {
                assertSame(raw, YamlNonFiniteParser.wrap(raw));
                return;
            }
            JsonParser source = new JsonParserDelegate(raw) {
                @Override
                public double getDoubleValue() {
                    throw new AssertionError("The broken numeric decoder must not run");
                }
                @Override
                public Number getNumberValue() {
                    throw new AssertionError("The broken numeric decoder must not run");
                }
            };
            JsonParser p = SchemaNodeReader.precisionParser(source, false);
            assertEquals(JsonToken.START_ARRAY, p.nextToken());
            for (int i = 0; i < 3; i++) {
                assertEquals(JsonToken.VALUE_NUMBER_FLOAT, p.nextToken());
                for (int repeat = 0; repeat < 100; repeat++) {
                    assertTrue(p.isNaN());
                    assertNotNull(p.getNumberValue());
                    assertFalse(Double.isFinite(p.getDoubleValue()));
                }
            }
        }
    }

    @Test
    void malformedTaggedNumbersAreStillRejected() {
        var mapper = YamlMapperFactory.getInstance();
        assertThrows(JacksonException.class, () -> mapper.readTree("!!float .notanumber"));
        assertThrows(JacksonException.class, () -> mapper.readTree("!!float .infinite"));
        assertThrows(JacksonException.class, () -> mapper.readTree("!!float .iNF"));
        assertThrows(JacksonException.class, () -> mapper.readTree("!!float .ınf"));
        assertEquals(".inf", mapper.readTree("!!str .inf").stringValue());
    }
}
