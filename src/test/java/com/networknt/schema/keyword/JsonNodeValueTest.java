package com.networknt.schema.keyword;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.JsonParser.NumberType;
import com.fasterxml.jackson.databind.node.BigIntegerNode;
import com.fasterxml.jackson.databind.node.DecimalNode;

class JsonNodeValueTest {
    @Test
    void cachesFractionalDecimalClassificationAcrossSetLookups() {
        CountingDecimal a = new CountingDecimal("2.500");
        CountingDecimal b = new CountingDecimal("2.50");
        JsonNodeValue left = JsonNodeValue.of(a);
        JsonNodeValue right = JsonNodeValue.of(b);
        Set<JsonNodeValue> values = new HashSet<>();
        values.add(left);
        for (int i = 0; i < 100; i++) {
            assertTrue(values.contains(right));
        }
        assertEquals(1, a.kindReads);
        assertEquals(1, b.kindReads);
    }

    @Test
    void cachesBigIntegerDecimalConversionAcrossSetLookups() {
        CountingInteger a = new CountingInteger(BigInteger.ONE.shiftLeft(70));
        CountingInteger b = new CountingInteger(BigInteger.ONE.shiftLeft(70));
        Set<JsonNodeValue> values = new HashSet<>();
        values.add(JsonNodeValue.of(a));
        JsonNodeValue key = JsonNodeValue.of(b);
        for (int i = 0; i < 100; i++) {
            assertTrue(values.contains(key));
        }
        assertEquals(1, a.decimalReads);
        assertEquals(1, b.decimalReads);
    }

    private static class CountingDecimal extends DecimalNode {
        int kindReads;
        int decimalReads;

        CountingDecimal(String value) {
            super(new BigDecimal(value));
        }

        @Override
        public NumberType numberType() {
            kindReads++;
            return super.numberType();
        }

        @Override
        public BigDecimal decimalValue() {
            decimalReads++;
            return super.decimalValue();
        }
    }

    private static class CountingInteger extends BigIntegerNode {
        int decimalReads;

        CountingInteger(BigInteger value) {
            super(value);
        }

        @Override
        public BigDecimal decimalValue() {
            decimalReads++;
            return super.decimalValue();
        }
    }
}
