/*
 * Copyright (c) 2026 Network New Technologies Inc.
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
import java.math.BigInteger;

import tools.jackson.core.JsonParser;
import tools.jackson.core.JsonToken;
import tools.jackson.core.Version;
import tools.jackson.core.exc.InputCoercionException;
import tools.jackson.core.util.JsonParserDelegate;

/**
 * Tree-read compatibility for Jackson YAML 3.1 nonfinite numeric tokens.
 * Remove when the minimum Jackson version includes the native YAML fix.
 */
final class YamlNonFiniteParser extends JsonParserDelegate {
    private String lastText;
    private Double lastValue;

    private YamlNonFiniteParser(JsonParser source) {
        super(source);
    }

    static JsonParser wrap(JsonParser source) {
        if (source instanceof YamlNonFiniteParser) {
            return source;
        }
        Version version = source.version();
        return version.getMajorVersion() == 3 && version.getMinorVersion() == 1
                && "jackson-dataformat-yaml".equals(version.getArtifactId())
                ? new YamlNonFiniteParser(source) : source;
    }

    private Double nonFinite() {
        if (currentToken() != JsonToken.VALUE_NUMBER_FLOAT) {
            return null;
        }
        int length = super.getStringLength();
        if (length < 4 || length > 5) {
            return null;
        }
        String text = super.getString();
        // Classification depends only on text, so repeated spellings may safely
        // reuse the result across token advances, including optimized advances.
        if (!text.equals(lastText)) {
            lastText = text;
            // Match the native 3.2 YAML spellings exactly; arbitrary mixed
            // case and Unicode case-folding lookalikes remain malformed.
            switch (text) {
            case ".nan":
            case ".NaN":
            case ".NAN":
                lastValue = Double.NaN;
                break;
            case ".inf":
            case ".Inf":
            case ".INF":
            case "+.inf":
            case "+.Inf":
            case "+.INF":
                lastValue = Double.POSITIVE_INFINITY;
                break;
            case "-.inf":
            case "-.Inf":
            case "-.INF":
                lastValue = Double.NEGATIVE_INFINITY;
                break;
            default:
                lastValue = null;
            }
        }
        return lastValue;
    }

    @Override
    public boolean isNaN() {
        return nonFinite() != null || super.isNaN();
    }

    @Override
    public NumberType getNumberType() {
        return nonFinite() != null ? NumberType.DOUBLE : super.getNumberType();
    }

    @Override
    public NumberTypeFP getNumberTypeFP() {
        return nonFinite() != null ? NumberTypeFP.DOUBLE64 : super.getNumberTypeFP();
    }

    @Override
    public double getDoubleValue() {
        Double value = nonFinite();
        return value != null ? value : super.getDoubleValue();
    }

    @Override
    public float getFloatValue() {
        Double value = nonFinite();
        return value != null ? value.floatValue() : super.getFloatValue();
    }

    @Override
    public Number getNumberValue() {
        Double value = nonFinite();
        return value != null ? value : super.getNumberValue();
    }

    @Override
    public Number getNumberValueExact() {
        Double value = nonFinite();
        return value != null ? value : super.getNumberValueExact();
    }

    @Override
    public Object getNumberValueDeferred() {
        Double value = nonFinite();
        return value != null ? value : super.getNumberValueDeferred();
    }

    private InputCoercionException cannotCoerce(Class<?> target) {
        return new InputCoercionException(this, "Cannot convert non-finite YAML value '"
                + getString() + "' to " + target.getSimpleName(), currentToken(), target);
    }

    @Override
    public BigDecimal getDecimalValue() {
        if (nonFinite() != null) {
            throw cannotCoerce(BigDecimal.class);
        }
        return super.getDecimalValue();
    }

    @Override
    public BigInteger getBigIntegerValue() {
        if (nonFinite() != null) {
            throw cannotCoerce(BigInteger.class);
        }
        return super.getBigIntegerValue();
    }

    @Override
    public int getIntValue() {
        Double value = nonFinite();
        if (value == null) {
            return super.getIntValue();
        }
        if (value.isNaN()) {
            return 0;
        }
        throw cannotCoerce(Integer.TYPE);
    }

    @Override
    public long getLongValue() {
        Double value = nonFinite();
        if (value == null) {
            return super.getLongValue();
        }
        if (value.isNaN()) {
            return 0L;
        }
        throw cannotCoerce(Long.TYPE);
    }

    @Override
    public byte getByteValue() {
        return nonFinite() != null ? (byte) getIntValue() : super.getByteValue();
    }

    @Override
    public short getShortValue() {
        return nonFinite() != null ? (short) getIntValue() : super.getShortValue();
    }

    @Override
    public double getValueAsDouble() {
        return getValueAsDouble(0.0);
    }

    @Override
    public int getValueAsInt() {
        return getValueAsInt(0);
    }

    @Override
    public long getValueAsLong() {
        return getValueAsLong(0L);
    }

    @Override
    public double getValueAsDouble(double defaultValue) {
        Double value = nonFinite();
        return value != null ? value : super.getValueAsDouble(defaultValue);
    }

    @Override
    public int getValueAsInt(int defaultValue) {
        return nonFinite() != null ? getIntValue() : super.getValueAsInt(defaultValue);
    }

    @Override
    public long getValueAsLong(long defaultValue) {
        return nonFinite() != null ? getLongValue() : super.getValueAsLong(defaultValue);
    }
}
