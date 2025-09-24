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
import com.networknt.schema.Schema;
import com.networknt.schema.SchemaLocation;
import com.networknt.schema.path.NodePath;
import com.networknt.schema.SchemaContext;
import com.networknt.schema.utils.JsonNodeUtil;

import java.math.BigDecimal;

/**
 * {@link KeywordValidator} for multipleOf.
 */
public class MultipleOfValidator extends BaseKeywordValidator implements KeywordValidator {
    private final BigDecimal divisor;

    public MultipleOfValidator(SchemaLocation schemaLocation, NodePath evaluationPath, JsonNode schemaNode,
            Schema parentSchema, SchemaContext schemaContext) {
        super(KeywordType.MULTIPLE_OF, schemaNode, schemaLocation, parentSchema, schemaContext, evaluationPath);
        this.divisor = getDivisor(schemaNode);
    }

    public void validate(ExecutionContext executionContext, JsonNode node, JsonNode rootNode,
            NodePath instanceLocation) {
        
        if (this.divisor != null) {
            BigDecimal dividend = getDividend(node);
            if (dividend != null) {
                if (dividend.divideAndRemainder(this.divisor)[1].abs().compareTo(BigDecimal.ZERO) > 0) {
                    executionContext.addError(error().instanceNode(node).instanceLocation(instanceLocation)
                            .locale(executionContext.getExecutionConfig().getLocale())
                            .arguments(this.divisor)
                            .build());
                }
            }
        }
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
        } else if (this.schemaContext.getSchemaRegistryConfig().isTypeLoose()
                && JsonNodeUtil.isNumber(node, this.schemaContext.getSchemaRegistryConfig())) {
            // handling for type loose
            return new BigDecimal(node.textValue());
        }
        return null;
    }

}
