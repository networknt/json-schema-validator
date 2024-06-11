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
import java.util.Collections;
import java.util.Set;

/**
 * {@link JsonValidator} for multipleOf.
 */
public class MultipleOfValidator extends BaseJsonValidator implements JsonValidator {
    private static final Logger logger = LoggerFactory.getLogger(MultipleOfValidator.class);

    private final BigDecimal divisor;

    public MultipleOfValidator(SchemaLocation schemaLocation, JsonNodePath evaluationPath, JsonNode schemaNode,
            JsonSchema parentSchema, ValidationContext validationContext) {
        super(schemaLocation, evaluationPath, schemaNode, parentSchema, ValidatorTypeCode.MULTIPLE_OF, validationContext);
        this.divisor = getDivisor(schemaNode);
    }

    public Set<ValidationMessage> validate(ExecutionContext executionContext, JsonNode node, JsonNode rootNode,
            JsonNodePath instanceLocation) {
        debug(logger, executionContext, node, rootNode, instanceLocation);
        if (this.divisor != null) {
            BigDecimal dividend = getDividend(node);
            if (dividend != null) {
                if (dividend.divideAndRemainder(this.divisor)[1].abs().compareTo(BigDecimal.ZERO) > 0) {
                    return Collections.singleton(message().instanceNode(node).instanceLocation(instanceLocation)
                            .locale(executionContext.getExecutionConfig().getLocale())
                            .failFast(executionContext.isFailFast()).arguments(this.divisor)
                            .build());
                }
            }
        }
        return Collections.emptySet();
    }

    /**
     * Gets the divisor to use.
     * 
     * @param schemaNode the schema node
     * @return the divisor or null if the input is not correct
     */
    protected BigDecimal getDivisor(JsonNode schemaNode) {
        if (schemaNode.isNumber()) {
            double divisor = schemaNode.doubleValue();
            if (divisor != 0) {
                // convert to BigDecimal since double type is not accurate enough to do the
                // division and multiple
                return schemaNode.isBigDecimal() ? schemaNode.decimalValue() : BigDecimal.valueOf(divisor);
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
            // convert to BigDecimal since double type is not accurate enough to do the
            // division and multiple
            return node.isBigDecimal() ? node.decimalValue() : BigDecimal.valueOf(node.doubleValue());
        } else if (this.validationContext.getConfig().isTypeLoose()
                && JsonNodeUtil.isNumber(node, this.validationContext.getConfig())) {
            // handling for type loose
            return new BigDecimal(node.textValue());
        }
        return null;
    }

}
