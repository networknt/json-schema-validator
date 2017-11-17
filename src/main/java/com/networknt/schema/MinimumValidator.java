/*
 * Copyright (c) 2016 Network New Technologies Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * You may not use this file except in compliance with the License.
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

import java.util.Collections;
import java.util.Set;

public class MinimumValidator extends BaseJsonValidator implements JsonValidator {
    private static final Logger logger = LoggerFactory.getLogger(MinimumValidator.class);
    private static final String PROPERTY_EXCLUSIVE_MINIMUM = "exclusiveMinimum";

    private double minimum;
    private boolean excluded = false;

    public MinimumValidator(String schemaPath, JsonNode schemaNode, JsonSchema parentSchema, ValidationContext validationContext) {
        super(schemaPath, schemaNode, parentSchema, ValidatorTypeCode.MINIMUM, validationContext);
        if (schemaNode.isNumber()) {
            minimum = schemaNode.doubleValue();
        } else {
            throw new JsonSchemaException("minimum value is not a number");
        }

        JsonNode exclusiveMinimumNode = getParentSchema().getSchemaNode().get(PROPERTY_EXCLUSIVE_MINIMUM);
        if (exclusiveMinimumNode != null && exclusiveMinimumNode.isBoolean()) {
            excluded = exclusiveMinimumNode.booleanValue();
        }

        parseErrorCode(getValidatorType().getErrorCodeKey());
    }

    public Set<ValidationMessage> validate(JsonNode node, JsonNode rootNode, String at) {
        debug(logger, node, rootNode, at);

        if (!node.isNumber()) {
            // minimum only applies to numbers
            return Collections.emptySet();
        }

        double value = node.doubleValue();
        if (lessThan(value, minimum) || (excluded && equals(value, minimum))) {
            return Collections.singleton(buildValidationMessage(at, "" + minimum));
        }
        return Collections.emptySet();
    }

}
