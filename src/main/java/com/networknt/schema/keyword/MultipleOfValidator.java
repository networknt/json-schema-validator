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
import com.networknt.schema.SchemaException;
import com.networknt.schema.SchemaLocation;
import com.networknt.schema.path.NodePath;
import com.networknt.schema.SchemaContext;
import com.networknt.schema.utils.JsonNodeTypes;
import com.networknt.schema.utils.DecimalUtils;
import com.networknt.schema.utils.DecimalUtils.DecimalNumber;

import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * {@link KeywordValidator} for multipleOf.
 */
public class MultipleOfValidator extends BaseKeywordValidator implements KeywordValidator {
    private final BigDecimal divisor;
    private final long longDivisor;
    private final BigInteger denominator;
    private final String divisorText;

    public MultipleOfValidator(SchemaLocation schemaLocation, JsonNode schemaNode,
            Schema parentSchema, SchemaContext schemaContext) {
        super(KeywordType.MULTIPLE_OF, schemaNode, schemaLocation, parentSchema, schemaContext);
        this.divisor = getDivisor(schemaNode);
        this.denominator = this.divisor == null ? null : this.divisor.unscaledValue().abs();
        // Subclasses may override either conversion hook. Do not bypass them.
        this.longDivisor = getClass() == MultipleOfValidator.class ? integralDivisor(this.divisor) : 0;
        this.divisorText = this.divisor == null ? null
                : schemaNode.isIntegralNumber() && this.divisor.compareTo(schemaNode.decimalValue()) == 0
                        ? schemaNode.bigIntegerValue().toString()
                        : this.divisor.scale() <= 0 && (long) this.divisor.precision() - this.divisor.scale() <= 19
                                ? this.divisor.toPlainString() : this.divisor.toString();
    }

    private static long integralDivisor(BigDecimal value) {
        if (value == null || value.signum() <= 0 || value.scale() > 0
                || (long) value.precision() - value.scale() > 19) {
            return 0;
        }
        try {
            return value.longValueExact();
        } catch (ArithmeticException exception) {
            return 0;
        }
    }

    public void validate(ExecutionContext executionContext, JsonNode node, JsonNode rootNode,
            NodePath instanceLocation) {
        
        if (this.divisor != null) {
            boolean invalid;
            if (this.longDivisor != 0 && (node.isInt() || node.isLong())) {
                invalid = node.longValue() % this.longDivisor != 0;
            } else {
                try {
                    BigDecimal dividend = getDividend(node);
                    invalid = dividend != null && !isMultipleOf(dividend);
                } catch (DecimalScaleException exception) {
                    DecimalNumber dividend = exception.number;
                    BigInteger scaleDifference = dividend.getExponent().add(BigInteger.valueOf(
                            (long) this.divisor.scale() - dividend.getSignificand().scale()));
                    invalid = !isMultipleOf(dividend.getSignificand().unscaledValue(), scaleDifference);
                }
            }
            if (invalid) {
                executionContext.addError(error().instanceNode(node).instanceLocation(instanceLocation)
                        .evaluationPath(executionContext.getEvaluationPath()).locale(executionContext.getExecutionConfig().getLocale())
                        .arguments(this.divisorText) // Preserve precision without MessageFormat NumberFormat rounding
                        .build());
            }
        }
    }

    /**
     * Checks divisibility without expanding exponent-sized powers or quotients.
     */
    private boolean isMultipleOf(BigDecimal dividend) {
        return isMultipleOf(dividend.unscaledValue(),
                BigInteger.valueOf((long) this.divisor.scale() - dividend.scale()));
    }

    private boolean isMultipleOf(BigInteger numerator, BigInteger scaleDifference) {
        if (numerator.signum() == 0) {
            return true;
        }
        BigInteger denominator = this.denominator;
        if (scaleDifference.signum() < 0) {
            BigInteger zeros = scaleDifference.negate();
            // Bound all constructed powers by the input coefficient size, never the exponent alone.
            // 30103/100000 is an upper bound for log10(2).
            long digitUpperBound = (numerator.abs().bitLength() * 30103L) / 100000 + 1;
            if (zeros.compareTo(BigInteger.valueOf(digitUpperBound)) >= 0
                    || numerator.abs().getLowestSetBit() < zeros.intValue()) {
                return false;
            }
            BigInteger[] division = numerator.divideAndRemainder(BigInteger.TEN.pow(zeros.intValue()));
            return division[1].signum() == 0 && division[0].remainder(denominator).signum() == 0;
        }

        BigInteger remainder = numerator.remainder(denominator);
        if (remainder.signum() == 0) {
            return true;
        }
        if (scaleDifference.signum() == 0) {
            return false;
        }
        BigInteger powerOfTen = BigInteger.TEN.modPow(scaleDifference, denominator);
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
            if (!schemaNode.isIntegralNumber() && !schemaNode.isBigDecimal()) {
                double number = schemaNode.doubleValue();
                // A double mapper cannot distinguish zero from a positive value
                // that underflowed to zero. Preserve its legacy ignored divisor.
                if (number == 0) {
                    return null;
                }
                if (!Double.isFinite(number)) {
                    return null;
                }
            }
            BigDecimal value = DecimalUtils.decimalValue(schemaNode);
            if (value.signum() <= 0) {
                throw new SchemaException("multipleOf must be greater than zero");
            }
            return DecimalUtils.normalize(value);
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
            return DecimalUtils.decimalValue(node);
        } else if (this.schemaContext.getSchemaRegistryConfig().isTypeLoose()
                && JsonNodeTypes.isNumber(node, this.schemaContext.getSchemaRegistryConfig())) {
            // handling for type loose
            try {
                return new BigDecimal(node.asString());
            } catch (NumberFormatException exception) {
                throw new DecimalScaleException(DecimalNumber.parse(node.asString()), exception);
            }
        }
        return null;
    }

    /** Carries the original loose number without swallowing unrelated conversion-hook exceptions. */
    private static final class DecimalScaleException extends IllegalArgumentException {
        private static final long serialVersionUID = 1L;
        private final DecimalNumber number;

        private DecimalScaleException(DecimalNumber number, NumberFormatException cause) {
            super(cause);
            this.number = number;
        }
    }
}
