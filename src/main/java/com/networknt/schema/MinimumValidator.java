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

public class MinimumValidator extends BaseJsonValidator {
    private static final Logger logger = LoggerFactory.getLogger(MinimumValidator.class);
    private static final String PROPERTY_EXCLUSIVE_MINIMUM = "exclusiveMinimum";

    private boolean excludeEqual = false;

    /**
     * In order to limit number of `if` statements in `validate` method, all the
     * logic of picking the right comparison is abstracted into a mixin.
     */
    private final ThresholdMixin typedMinimum;

    public MinimumValidator(String schemaPath, final JsonNode schemaNode, JsonSchema parentSchema, ValidationContext validationContext) {
        super(schemaPath, schemaNode, parentSchema, ValidatorTypeCode.MINIMUM, validationContext);

        if (!schemaNode.isNumber()) {
            throw new JsonSchemaException("minimum value is not a number");
        }

        JsonNode exclusiveMinimumNode = getParentSchema().getSchemaNode().get(PROPERTY_EXCLUSIVE_MINIMUM);
        if (exclusiveMinimumNode != null && exclusiveMinimumNode.isBoolean()) {
            excludeEqual = exclusiveMinimumNode.booleanValue();
        }

        parseErrorCode(getValidatorType().getErrorCodeKey());

        final String minimumText = schemaNode.asText();
        if ((schemaNode.isLong() || schemaNode.isInt()) && JsonType.INTEGER.toString().equals(getNodeFieldType())) {
            // "integer", and within long range
            final long lmin = schemaNode.asLong();
            typedMinimum = new ThresholdMixin() {
                @Override
                public boolean crossesThreshold(JsonNode node) {
                    if (node.isBigInteger()) {
                        //node.isBigInteger is not trustable, the type BigInteger doesn't mean it is a big number.
                        int compare = node.bigIntegerValue().compareTo(new BigInteger(minimumText));
                        return compare < 0 || (excludeEqual && compare == 0);

                    } else if (node.isTextual()) {
                        BigDecimal min = new BigDecimal(minimumText);
                        BigDecimal value = new BigDecimal(node.asText());
                        int compare = value.compareTo(min);
                        return compare < 0 || (excludeEqual && compare == 0);

                    }
                    long val = node.asLong();
                    return lmin > val || (excludeEqual && lmin == val);
                }

                @Override
                public String thresholdValue() {
                    return String.valueOf(lmin);
                }
            };

        } else {
            typedMinimum = new ThresholdMixin() {
                @Override
                public boolean crossesThreshold(JsonNode node) {
                    // jackson's BIG_DECIMAL parsing is limited. see https://github.com/FasterXML/jackson-databind/issues/1770
                    if (schemaNode.isDouble() && schemaNode.doubleValue() == Double.NEGATIVE_INFINITY) {
                        return false;
                    }
                    if (schemaNode.isDouble() && schemaNode.doubleValue() == Double.POSITIVE_INFINITY) {
                        return true;
                    }
                    if (node.isDouble() && node.doubleValue() == Double.NEGATIVE_INFINITY) {
                        return true;
                    }
                    if (node.isDouble() && node.doubleValue() == Double.POSITIVE_INFINITY) {
                        return false;
                    }
                    final BigDecimal min = new BigDecimal(minimumText);
                    BigDecimal value = new BigDecimal(node.asText());
                    int compare = value.compareTo(min);
                    return compare < 0 || (excludeEqual && compare == 0);
                }

                @Override
                public String thresholdValue() {
                    return minimumText;
                }
            };
        }
        this.validationContext = validationContext;
    }

    public Set<ValidationMessage> validate(JsonNode node, JsonNode rootNode, String at) {
        debug(logger, node, rootNode, at);

        if (!JsonNodeUtil.isNumber(node, this.validationContext.getConfig())) {
            // minimum only applies to numbers
            return Collections.emptySet();
        }

        if (typedMinimum.crossesThreshold(node)) {
            return Collections.singleton(buildValidationMessage(at, typedMinimum.thresholdValue()));
        }
        return Collections.emptySet();
    }

}
