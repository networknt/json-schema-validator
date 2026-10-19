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
    private static final int MAX_PLAIN_DIVISOR_LENGTH = 32;

    private final BigDecimal divisor;
    private final BigInteger denominator;
    private final long longDivisor;
    private final String divisorMessage;

    public MultipleOfValidator(SchemaLocation schemaLocation, JsonNode schemaNode,
            Schema parentSchema, SchemaContext schemaContext) {
        super(KeywordType.MULTIPLE_OF, schemaNode, schemaLocation, parentSchema, schemaContext);
        BigDecimal divisor = getDivisor(schemaNode);
        this.divisor = divisor == null || divisor.signum() == 0 ? null : divisor;
        this.denominator = this.divisor == null ? null : this.divisor.unscaledValue().abs();
        this.longDivisor = integralDivisor(this.divisor);
        this.divisorMessage = formatDivisorMessage(this.divisor);
    }

    public void validate(ExecutionContext executionContext, JsonNode node, JsonNode rootNode,
            NodePath instanceLocation) {
        if (this.divisor == null) {
            return;
        }
        BigDecimal dividend = getDividend(node);
        if (dividend == null) {
            return;
        }
        boolean invalid;
        if (this.longDivisor != 0 && dividend.scale() >= 0 && dividend.precision() <= 19) {
            // Check the hook result itself, including scaled integers, before using long arithmetic.
            long value = dividend.longValue();
            invalid = dividend.compareTo(BigDecimal.valueOf(value)) == 0
                    ? value % this.longDivisor != 0
                    : !isMultipleOf(dividend, this.divisor, this.denominator);
        } else {
            invalid = !isMultipleOf(dividend, this.divisor, this.denominator);
        }
        if (invalid) {
            executionContext.addError(error().instanceNode(node).instanceLocation(instanceLocation)
                    .evaluationPath(executionContext.getEvaluationPath()).locale(executionContext.getExecutionConfig().getLocale())
                    .arguments(this.divisorMessage) // Avoid MessageFormat NumberFormat rounding
                    .build());
        }
    }

    private static String formatDivisorMessage(BigDecimal value) {
        if (value == null) {
            return null;
        }
        long precision = value.precision();
        long scale = value.scale();
        long length = scale <= 0 ? precision - scale : Math.max(precision + 1, scale + 2);
        if (value.signum() < 0) {
            length++;
        }
        if (length <= MAX_PLAIN_DIVISOR_LENGTH) {
            return value.toPlainString();
        }
        String text = value.toString();
        if (length > text.length()) {
            return text;
        }
        return text.indexOf('E') < 0 ? text : value.toPlainString();
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

    private static BigDecimal normalizeDivisor(BigDecimal value) {
        try {
            return value.stripTrailingZeros();
        } catch (ArithmeticException exception) {
            // Removing zeros at the minimum scale can overflow the scale range.
            return value;
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
     * Gets the divisor to use. The default implementation preserves exact values
     * for integral and BigDecimal nodes. Finite Double and Float nodes are converted
     * with {@link BigDecimal#valueOf(double)} using their {@code doubleValue()}.
     * Non-finite floating-point nodes return null, so this constraint is ignored
     * for those inputs.
     * Validation uses the returned value without further rounding, including when
     * this method is overridden.
     * A null or zero return value causes this constraint to be ignored, including
     * when this method is overridden.
     * The default conversion removes trailing zeros unless doing so would overflow the scale.
     * Error messages use the returned value's plain notation when it fits within
     * 32 characters or is no longer than its {@link BigDecimal#toString()}
     * representation. Otherwise, that representation is used.
     * This also applies to values returned by overrides.
     * 
     * @param schemaNode the schema node
     * @return the divisor or null if the input is not correct
     */
    protected BigDecimal getDivisor(JsonNode schemaNode) {
        if (schemaNode.isNumber()) {
            if (schemaNode.isIntegralNumber() || schemaNode.isBigDecimal()) {
                BigDecimal value = schemaNode.decimalValue();
                return value.signum() == 0 ? null : normalizeDivisor(value);
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
     * Gets the dividend to use. The default implementation preserves exact values
     * for integral and BigDecimal nodes. Finite Double and Float nodes are converted
     * with {@link BigDecimal#valueOf(double)} using their {@code doubleValue()}.
     * Non-finite floating-point nodes return null, so this constraint is ignored
     * for those inputs.
     * Validation uses the returned value without further rounding, including when
     * this method is overridden.
     * Integral inputs can have a different scale than with the former
     * double-based conversion, for example {@code 2} instead of {@code 2.0}.
     * 
     * @param node the node
     * @return the dividend or null if the type is incorrect or the floating-point value is non-finite
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
            if (node.isIntegralNumber() || node.isBigDecimal()) {
                return node.decimalValue();
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
