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
import com.networknt.schema.utils.JsonNodeUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Collections;
import java.util.Set;

public class MaximumValidator extends BaseJsonValidator {
    private static final Logger logger = LoggerFactory.getLogger(MaximumValidator.class);
    private static final String PROPERTY_EXCLUSIVE_MAXIMUM = "exclusiveMaximum";

    private boolean excludeEqual = false;

    private final ThresholdMixin typedMaximum;


    public MaximumValidator(SchemaLocation schemaLocation, JsonNodePath evaluationPath, final JsonNode schemaNode, JsonSchema parentSchema, ValidationContext validationContext) {
        super(schemaLocation, evaluationPath, schemaNode, parentSchema, ValidatorTypeCode.MAXIMUM, validationContext);
        this.validationContext = validationContext;
        if (!schemaNode.isNumber()) {
            throw new JsonSchemaException("maximum value is not a number");
        }

        JsonNode exclusiveMaximumNode = getParentSchema().getSchemaNode().get(PROPERTY_EXCLUSIVE_MAXIMUM);
        if (exclusiveMaximumNode != null && exclusiveMaximumNode.isBoolean()) {
            excludeEqual = exclusiveMaximumNode.booleanValue();
        }

        final String maximumText = schemaNode.asText();
        if ((schemaNode.isLong() || schemaNode.isInt()) && (JsonType.INTEGER.toString().equals(getNodeFieldType()))) {
            // "integer", and within long range
            final long lm = schemaNode.asLong();
            typedMaximum = new ThresholdMixin() {
                @Override
                public boolean crossesThreshold(JsonNode node) {
                    if (node.isBigInteger()) {
                        //node.isBigInteger is not trustable, the type BigInteger doesn't mean it is a big number.
                        int compare = node.bigIntegerValue().compareTo(new BigInteger(schemaNode.asText()));
                        return compare > 0 || (excludeEqual && compare == 0);

                    } else if (node.isTextual()) {
                        BigDecimal max = new BigDecimal(maximumText);
                        BigDecimal value = new BigDecimal(node.asText());
                        int compare = value.compareTo(max);
                        return compare > 0 || (excludeEqual && compare == 0);
                    }
                    long val = node.asLong();
                    return lm < val || (excludeEqual && lm == val);
                }

                @Override
                public String thresholdValue() {
                    return String.valueOf(lm);
                }
            };
        } else {
            typedMaximum = new ThresholdMixin() {
                @Override
                public boolean crossesThreshold(JsonNode node) {
                    if (schemaNode.isDouble() && schemaNode.doubleValue() == Double.POSITIVE_INFINITY) {
                        return false;
                    }
                    if (schemaNode.isDouble() && schemaNode.doubleValue() == Double.NEGATIVE_INFINITY) {
                        return true;
                    }
                    if (node.isDouble() && node.doubleValue() == Double.NEGATIVE_INFINITY) {
                        return false;
                    }
                    if (node.isDouble() && node.doubleValue() == Double.POSITIVE_INFINITY) {
                        return true;
                    }
                    final BigDecimal max = new BigDecimal(maximumText);
                    BigDecimal value = new BigDecimal(node.asText());
                    int compare = value.compareTo(max);
                    return compare > 0 || (excludeEqual && compare == 0);
                }

                @Override
                public String thresholdValue() {
                    return maximumText;
                }
            };
        }
    }

    public Set<ValidationMessage> validate(ExecutionContext executionContext, JsonNode node, JsonNode rootNode, JsonNodePath instanceLocation) {
        debug(logger, node, rootNode, instanceLocation);

        if (!JsonNodeUtil.isNumber(node, this.validationContext.getConfig())) {
            // maximum only applies to numbers
            return Collections.emptySet();
        }

        if (typedMaximum.crossesThreshold(node)) {
            return Collections.singleton(message().instanceLocation(instanceLocation)
                    .locale(executionContext.getExecutionConfig().getLocale()).arguments(typedMaximum.thresholdValue())
                    .build());
        }
        return Collections.emptySet();
    }
}