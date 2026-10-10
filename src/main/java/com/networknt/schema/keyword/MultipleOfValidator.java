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

package com.networknt.schema.keyword;

import tools.jackson.databind.JsonNode;
import com.networknt.schema.ExecutionContext;
import com.networknt.schema.Schema;
import com.networknt.schema.SchemaLocation;
import com.networknt.schema.path.NodePath;
import com.networknt.schema.SchemaContext;
import com.networknt.schema.utils.JsonNodeTypes;

import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * {@link KeywordValidator} for multipleOf.
 */
public class MultipleOfValidator extends BaseKeywordValidator implements KeywordValidator {
    private final BigDecimal divisor;
    private final BigInteger denominator;
    private final long longDivisor;
    private final String divisorMessage;
    private final boolean floatingPointSchema;
    private final BigDecimal floatingPointDivisor;
    private final BigInteger floatingPointDenominator;

    public MultipleOfValidator(SchemaLocation schemaLocation, JsonNode schemaNode,
            Schema parentSchema, SchemaContext schemaContext) {
        super(KeywordType.MULTIPLE_OF, schemaNode, schemaLocation, parentSchema, schemaContext);
        this.divisor = getDivisor(schemaNode);
        this.denominator = this.divisor == null ? null : this.divisor.unscaledValue().abs();
        this.longDivisor = integralDivisor(this.divisor);
        this.divisorMessage = formatDivisor(this.divisor);
        this.floatingPointSchema = isFloatingPoint(schemaNode) && !JsonNodeTypes.isNonFiniteNumber(schemaNode)
                && this.divisor != null && this.divisor.compareTo(BigDecimal.valueOf(schemaNode.doubleValue())) == 0;
        if (schemaNode.isIntegralNumber() && this.divisor != null
                && this.divisor.equals(schemaNode.decimalValue())) {
            // Floating-point inputs retain the base conversion on both sides. Do not
            // combine a newly exact integer divisor with an already rounded dividend.
            double value = this.divisor.doubleValue();
            this.floatingPointDivisor = Double.isFinite(value) ? BigDecimal.valueOf(value) : this.divisor;
        } else {
            // Preserve a divisor supplied by an overridden conversion hook.
            this.floatingPointDivisor = this.divisor;
        }
        this.floatingPointDenominator = this.floatingPointDivisor == null ? null
                : this.floatingPointDivisor.unscaledValue().abs();
    }

    public void validate(ExecutionContext executionContext, JsonNode node, JsonNode rootNode,
            NodePath instanceLocation) {
        
        if (this.divisor != null) {
            // Call the conversion hook before selecting the fast path, including for subclasses.
            BigDecimal dividend = getDividend(node);
            if (dividend == null) {
                return;
            }
            BigDecimal effectiveDivisor = this.divisor;
            BigInteger effectiveDenominator = this.denominator;
            if (isFloatingPoint(node) && !JsonNodeTypes.isNonFiniteNumber(node)
                    && dividend.compareTo(BigDecimal.valueOf(node.doubleValue())) == 0) {
                effectiveDivisor = this.floatingPointDivisor;
                effectiveDenominator = this.floatingPointDenominator;
            }
            boolean invalid;
            if (effectiveDivisor == this.divisor && this.longDivisor != 0
                    && (node.isInt() || node.isLong()) && dividend.compareTo(node.decimalValue()) == 0) {
                invalid = node.longValue() % this.longDivisor != 0;
            } else {
                invalid = !isMultipleOf(dividend, effectiveDivisor, effectiveDenominator);
            }
            if (invalid) {
                executionContext.addError(error().instanceNode(node).instanceLocation(instanceLocation)
                        .evaluationPath(executionContext.getEvaluationPath()).locale(executionContext.getExecutionConfig().getLocale())
                        .arguments(this.divisorMessage) // Avoid MessageFormat NumberFormat rounding
                        .build());
            }
        }
    }

    private static boolean isFloatingPoint(JsonNode node) {
        return node.isFloatingPointNumber() && !node.isBigDecimal();
    }

    private static long integralDivisor(BigDecimal value) {
        if (value == null) {
            return 0;
        }
        try {
            return value.longValueExact();
        } catch (ArithmeticException exception) {
            return 0;
        }
    }

    private static String formatDivisor(BigDecimal value) {
        if (value == null) {
            return null;
        }
        try {
            return value.stripTrailingZeros().toString();
        } catch (ArithmeticException exception) {
            // Removing zeros at the minimum scale can overflow the scale range.
            return value.toString();
        }
    }

    /**
     * Checks divisibility without constructing exponent-sized powers or quotients.
     */
    private static boolean isMultipleOf(BigDecimal dividend, BigDecimal divisor, BigInteger denominator) {
        BigInteger numerator = dividend.unscaledValue();
        if (numerator.signum() == 0) {
            return true;
        }
        long scaleDifference = (long) divisor.scale() - dividend.scale();
        if (scaleDifference < 0) {
            long zeros = -scaleDifference;
            // Bound constructed powers by the input coefficient size.
            // 30103/100000 is an upper bound for log10(2).
            BigInteger absoluteNumerator = numerator.abs();
            long digitUpperBound = (absoluteNumerator.bitLength() * 30103L) / 100000 + 1;
            if (zeros >= digitUpperBound || absoluteNumerator.getLowestSetBit() < zeros) {
                return false;
            }
            BigInteger[] division = numerator.divideAndRemainder(BigInteger.TEN.pow((int) zeros));
            return division[1].signum() == 0 && division[0].remainder(denominator).signum() == 0;
        }
        BigInteger remainder = numerator.remainder(denominator);
        if (remainder.signum() == 0) {
            return true;
        }
        if (scaleDifference == 0) {
            return false;
        }
        // Small powers fit in a long; reserve modular exponentiation for larger scales.
        BigInteger powerOfTen = scaleDifference <= 18 ? BigInteger.TEN.pow((int) scaleDifference)
                : BigInteger.TEN.modPow(BigInteger.valueOf(scaleDifference), denominator);
        return remainder.multiply(powerOfTen).remainder(denominator).signum() == 0;
    }

    /**
     * Gets the divisor to use.
     * 
     * @param schemaNode the schema node
     * @return the divisor or null if the input is not correct
     */
    protected BigDecimal getDivisor(JsonNode schemaNode) {
        if (schemaNode.isNumber()) {
            if (schemaNode.isIntegralNumber() || schemaNode.isBigDecimal()) {
                BigDecimal value = schemaNode.decimalValue();
                // Keep the exact coefficient and scale; normalization can overflow at scale limits.
                return value.signum() == 0 ? null : value;
            }
            double divisor = schemaNode.doubleValue();
            if (divisor != 0 && Double.isFinite(divisor)) {
                // convert to BigDecimal since double type is not accurate enough to do the
                // division and multiple
                return BigDecimal.valueOf(divisor).stripTrailingZeros();
            }
        }
        return null;
    }

    /**
     * Gets the dividend to use.
     * 
     * @param node the node
     * @return the dividend or null if the type is incorrect
     */
    protected BigDecimal getDividend(JsonNode node) {
        if (node.isNumber()) {
            // Handle NaN, Infinity and -Infinity
            if (JsonNodeTypes.isNonFiniteNumber(node)) {
                // Incorrect type as NaN, Infinity and -Infinity are not valid JSON numbers so return null
                return null;
            }
            // convert to BigDecimal since double type is not accurate enough to do the
            // division and multiple
            if (node.isBigDecimal()) {
                return node.decimalValue();
            }
            if (node.isIntegralNumber()) {
                BigDecimal value = node.decimalValue();
                if (this.floatingPointSchema) {
                    double floatingPointValue = value.doubleValue();
                    if (Double.isFinite(floatingPointValue)) {
                        return BigDecimal.valueOf(floatingPointValue);
                    }
                }
                return value;
            }
            return BigDecimal.valueOf(node.doubleValue());
        } else if (this.schemaContext.getSchemaRegistryConfig().isTypeLoose()
                && JsonNodeTypes.isNumber(node, this.schemaContext.getSchemaRegistryConfig())) {
            // handling for type loose
            return new BigDecimal(node.asString());
        }
        return null;
    }

}
