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

package com.networknt.schema;

import com.fasterxml.jackson.databind.JsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;
import java.util.Collections;
import java.util.Set;

public class MinimumValidator extends BaseJsonValidator implements JsonValidator {
    private static final Logger logger = LoggerFactory.getLogger(MinimumValidator.class);
    private static final String PROPERTY_EXCLUSIVE_MINIMUM = "exclusiveMinimum";

    private boolean excluded = false;

    /**
     *  In order to limit number of `if` statements in `validate` method, all the
     *  logic of picking the right comparison is abstracted into a mixin.
     */
    private final ThresholdMixin typedMinimum;

    public MinimumValidator(String schemaPath, JsonNode schemaNode, JsonSchema parentSchema, ValidationContext validationContext) {
        super(schemaPath, schemaNode, parentSchema, ValidatorTypeCode.MINIMUM, validationContext);

        if (!schemaNode.isNumber()) {
            throw new JsonSchemaException("minimum value is not a number");
        }

        JsonNode exclusiveMinimumNode = getParentSchema().getSchemaNode().get(PROPERTY_EXCLUSIVE_MINIMUM);
        if (exclusiveMinimumNode != null && exclusiveMinimumNode.isBoolean()) {
            excluded = exclusiveMinimumNode.booleanValue();
        }

        parseErrorCode(getValidatorType().getErrorCodeKey());

        if (!JsonType.INTEGER.toString().equals(getNodeFieldType())) {
            // "number" or no type
            // by default treat value as double: compatible with previous behavior
            final double dmin = schemaNode.doubleValue();
            typedMinimum = new ThresholdMixin() {
                @Override
                public boolean crossesThreshold(JsonNode node) {
                    double value = node.asDouble();
                    return lessThan(value, dmin) || (excluded && MinimumValidator.this.equals(value, dmin));
                }

                @Override
                public String thresholdValue() {
                    return String.valueOf(dmin);
                }
            };

        } else if ( schemaNode.isLong() || schemaNode.isInt() ) {
            // "integer", and within long range
            final long lmin = schemaNode.asLong();
            typedMinimum = new ThresholdMixin() {
                @Override
                public boolean crossesThreshold(JsonNode node) {
                    long val = node.asLong();
                    return node.isBigInteger() || lmin > val || (excluded && lmin >= val);
                }

                @Override
                public String thresholdValue() {
                    return String.valueOf(lmin);
                }
            };

        } else {
            // "integer" outside long range
            final BigInteger bimin = new BigInteger(schemaNode.asText());
            typedMinimum = new ThresholdMixin() {
                @Override
                public boolean crossesThreshold(JsonNode node) {
                    int cmp = bimin.compareTo(node.bigIntegerValue());
                    return cmp > 0 || (excluded && cmp >= 0);
                }

                @Override
                public String thresholdValue() {
                    return String.valueOf(bimin);
                }
            };
        }
    }

    public Set<ValidationMessage> validate(JsonNode node, JsonNode rootNode, String at) {
        debug(logger, node, rootNode, at);

        if (!TypeValidator.isNumber(node, config.isTypeLoose())) {
            // minimum only applies to numbers
            return Collections.emptySet();
        }

        if (typedMinimum.crossesThreshold(node)) {
            return Collections.singleton(buildValidationMessage(at, typedMinimum.thresholdValue()));
        }
        return Collections.emptySet();
    }

}
