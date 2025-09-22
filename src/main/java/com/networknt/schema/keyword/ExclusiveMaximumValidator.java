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

import com.fasterxml.jackson.databind.JsonNode;
import com.networknt.schema.ExecutionContext;
import com.networknt.schema.JsonNodePath;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaException;
import com.networknt.schema.JsonType;
import com.networknt.schema.SchemaLocation;
import com.networknt.schema.ThresholdMixin;
import com.networknt.schema.ValidationContext;
import com.networknt.schema.utils.JsonNodeUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * {@link KeywordValidator} for exclusiveMaximum.
 */
public class ExclusiveMaximumValidator extends BaseKeywordValidator {
    private static final Logger logger = LoggerFactory.getLogger(ExclusiveMaximumValidator.class);

    private final ThresholdMixin typedMaximum;

    public ExclusiveMaximumValidator(SchemaLocation schemaLocation, JsonNodePath evaluationPath, final JsonNode schemaNode, JsonSchema parentSchema, ValidationContext validationContext) {
        super(ValidatorTypeCode.EXCLUSIVE_MAXIMUM, schemaNode, schemaLocation, parentSchema, validationContext, evaluationPath);
        if (!schemaNode.isNumber()) {
            throw new JsonSchemaException("exclusiveMaximum value is not a number");
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
                        return compare > 0 || compare == 0;

                    } else if (node.isTextual()) {
                        BigDecimal max = new BigDecimal(maximumText);
                        BigDecimal value = new BigDecimal(node.asText());
                        int compare = value.compareTo(max);
                        return compare > 0 || compare == 0;
                    }
                    long val = node.asLong();
                    return lm < val || lm == val;
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
                    return compare > 0 || compare == 0;
                }

                @Override
                public String thresholdValue() {
                    return maximumText;
                }
            };
        }
    }

    public void validate(ExecutionContext executionContext, JsonNode node, JsonNode rootNode, JsonNodePath instanceLocation) {
        debug(logger, executionContext, node, rootNode, instanceLocation);

        if (!JsonNodeUtil.isNumber(node, validationContext.getConfig())) {
            // maximum only applies to numbers
            return;
        }

        if (typedMaximum.crossesThreshold(node)) {
            executionContext.addError(error().instanceNode(node).instanceLocation(instanceLocation)
                    .locale(executionContext.getExecutionConfig().getLocale())
                    .arguments(typedMaximum.thresholdValue()).build());
        }
    }
}
