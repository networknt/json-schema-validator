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

public class MaximumValidator extends BaseJsonValidator implements JsonValidator {
    private static final Logger logger = LoggerFactory.getLogger(MaximumValidator.class);
    private static final String PROPERTY_EXCLUSIVE_MAXIMUM = "exclusiveMaximum";

    private boolean excludeEqual = false;

    private final ThresholdMixin typedMaximum;


    public MaximumValidator(String schemaPath, JsonNode schemaNode, JsonSchema parentSchema, ValidationContext validationContext) {
        super(schemaPath, schemaNode, parentSchema, ValidatorTypeCode.MAXIMUM, validationContext);

        if (!schemaNode.isNumber()) {
            throw new JsonSchemaException("maximum value is not a number");
        }

        JsonNode exclusiveMaximumNode = getParentSchema().getSchemaNode().get(PROPERTY_EXCLUSIVE_MAXIMUM);
        if (exclusiveMaximumNode != null && exclusiveMaximumNode.isBoolean()) {
            excludeEqual = exclusiveMaximumNode.booleanValue();
        }

        parseErrorCode(getValidatorType().getErrorCodeKey());

        if (!JsonType.INTEGER.toString().equals(getNodeFieldType())) {
            // "number" or no type
            // by default treat value as double: compatible with previous behavior
            final double dm = schemaNode.doubleValue();
            typedMaximum = new ThresholdMixin() {
                @Override
                public boolean crossesThreshold(JsonNode node) {
                    double value = node.asDouble();
                    return greaterThan(value, dm) || (excludeEqual && MaximumValidator.this.equals(value, dm));
                }

                @Override
                public String thresholdValue() {
                    return String.valueOf(dm);
                }
            };

        } else if ( schemaNode.isLong() || schemaNode.isInt() ) {
            // "integer", and within long range
            final long lm = schemaNode.asLong();
            typedMaximum = new ThresholdMixin() {
                @Override
                public boolean crossesThreshold(JsonNode node) {
                    long val = node.asLong();
                    return node.isBigInteger() || lm < val || (excludeEqual && lm <= val);
                }

                @Override
                public String thresholdValue() {
                    return String.valueOf(lm);
                }
            };

        } else {
            // "integer" outside long range
            final BigInteger bim = new BigInteger(schemaNode.asText());
            typedMaximum = new ThresholdMixin() {
                @Override
                public boolean crossesThreshold(JsonNode node) {
                    int cmp = bim.compareTo(node.bigIntegerValue());
                    return cmp < 0 || (excludeEqual && cmp <= 0);
                }

                @Override
                public String thresholdValue() {
                    return String.valueOf(bim);
                }
            };
        }
    }

    public Set<ValidationMessage> validate(JsonNode node, JsonNode rootNode, String at) {
        debug(logger, node, rootNode, at);

        if (!TypeValidator.isNumber(node, config.isTypeLoose())) {
            // maximum only applies to numbers
            return Collections.emptySet();
        }

        if (typedMaximum.crossesThreshold(node)) {
            return Collections.singleton(buildValidationMessage(at, typedMaximum.thresholdValue()));
        }
        return Collections.emptySet();
    }

}